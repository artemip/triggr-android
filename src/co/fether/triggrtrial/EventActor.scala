package co.fether.triggrtrial

import android.util.Log
import scala.actors.Actor
import android.widget.Toast
import co.fether.triggrtrial.json._
import android.content.Intent
import android.net.Uri

object EventActor extends Actor {
  private val tag = EventActor.getClass.getCanonicalName

  // Event case classes
  case object Disconnect
  case class Connect( pairingKey : String )
  case class IncomingCall( number : String, name : String )
  case class OutgoingCall( number : String, name : String )
  case class MissedCall( number : String, name : String )
  case class SMSMessage( number : String, name : String, message : String )
  case class WhatsAppMessage( name : String, message : String )
  case class SnapchatMessage( name : String, message : String )
  case object EndCall

  // Actor that performs HTTP requests
  private val requestActor = new HTTPRequestActor()

  // Event Handlers
  object EventHandlers {
    val Notify = "notify"
    val AlertNoise = "alert_noise"
    val LowerVolume = "lower_volume"
    val RestoreVolume = "restore_volume"
    val EndPairMode = "end_pair_mode"
  }

  object EventIcons {
    val iconURI = "http://www.triggrapp.com/desktop_resources/"

    val Connect = iconURI + "icon_connect_10.26.13.png"
    val Disconnect = iconURI + "icon_disconnect_10.26.13.png"
    val IncomingCall = iconURI + "icon_callstart_10.26.13.png"
    val OutgoingCall = iconURI + "icon_callstart_10.26.13.png"
    val MissedCall = iconURI + "icon_missedcall_10.26.13.png"
    val EndCall = iconURI + "icon_callend_10.26.13.png"
    val SmsMessage = iconURI + "icon_message_10.26.13.png"
    val WhatsAppMessage = iconURI + "icon_whatsapp_10.26.13.png"
    val SnapChatMessage = iconURI + "icon_snapchat_10.26.13.png"
  }

  /**
   * Default handler for server responses. Shows an error message for erronous responses
   * @param response response received from the server
   */
  private def defaultResponseHandler(response : Option[String]) {
    response match {
      case Some( json : String ) => {
        val serverResponse = new ServerResponse().deserialize(json)

        serverResponse.status match {
          case "error" => {
            Util.showToast( serverResponse.message, Toast.LENGTH_LONG )
          }
          case _ =>
        }

      }
      case None => {
        Util.showToast( "Server connection failed.", Toast.LENGTH_LONG )
      }
    }
  }

  def act() {
    requestActor.start()

    loop {
      receive {
        case _ if !Preferences.isTrialActive => {
          Preferences.getService match {
            case Some(service) => {
              val intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=co.fether.triggr"))
              service.createNotification(Util.getString(R.string.alerts_unavailable), Util.getString(R.string.buy_full_version), intent)
            }
            case _ =>
          }
        }
        case Connect( k ) => {
          Log.d( tag, "Connecting to new device using key " + k )

          def responseHandler( response : Option[String] ) {
            response match {
              case Some( json : String ) => {
                val serverResponse = new ServerResponse().deserialize(json)

                serverResponse.status match {
                  case "ok" => {
                    Log.d( tag, "Connection successful." )
                    Preferences.setConnectedDeviceId( Some(serverResponse.paired_device_id) )

                    Preferences.getMainActivity match {
                      case Some(a : PairingActivity) => a.runOnUiThread(new Runnable() {
                        override def run() {
                          a.showDisconnectView()
                        }
                      })
                      case _ =>
                    }

                  }
                  case "error" => {
                    Util.showToast( serverResponse.message, Toast.LENGTH_LONG )
                  }
                }

              }
              case None => {
                Util.showToast("Server connection failed.", Toast.LENGTH_LONG)
              }
            }
          }

          val notification = new Notification(
            icon_uri = EventIcons.Connect,
            title = Util.getString(R.string.connected_notification)
          )

          val eventDefinition = new Event(
            `type` = "connect",
            notification = notification,
            handlers = List(
              EventHandlers.Notify,
              EventHandlers.EndPairMode
            )
          )

          requestActor ! HTTPRequestActor.POSTRequest(
            path = "connect",
            params =
              Map(
                "device_id" -> Preferences.getDeviceId().toString,
                "pairing_key" -> k,
                "event" -> eventDefinition.serialize()
              ),
            responseHandler = responseHandler
          )
        }
        case Disconnect => {
          Log.d( tag, "Disconnecting" )

          Preferences.getConnectedDeviceId match {
            case Some( id ) => {

              val notification = new Notification(
                icon_uri = EventIcons.Disconnect,
                title = Util.getString(R.string.disconnected_notification)
              )

              val eventDefinition = new Event(
                `type` = "disconnect",
                notification = notification,
                handlers = List(
                  EventHandlers.Notify
                )
              )

              requestActor ! HTTPRequestActor.POSTRequest(
                path = "disconnect",
                params = Map(
                    "device_id" -> Preferences.getDeviceId().toString,
                    "paired_device_id" -> id,
                    "event" -> eventDefinition.serialize()
                  ),
                responseHandler = defaultResponseHandler
              )
            }
            case None => {
              Log.d( tag, "No connected device found when trying to disconnect." )
            }
          }

          Preferences.setConnectedDeviceId(None)
        }
        case IncomingCall( number, name) if Preferences.getPhoneCallNotificationsEnabled => {
          Preferences.getConnectedDeviceId match {
            case Some( deviceID ) => {
              Log.d(tag, "Incoming call event triggered")

              val notification = new Notification(
                icon_uri = EventIcons.IncomingCall,
                title = name,
                subtitle = number
              )

              val eventDefinition = new Event(
                `type` = "incoming_call",
                notification = notification,
                handlers = List(
                  EventHandlers.Notify
                )
              )

              if (Preferences.getSmartVolumeEnabled) {
                eventDefinition.handlers = EventHandlers.LowerVolume :: eventDefinition.handlers
              }

              requestActor ! HTTPRequestActor.POSTRequest(
                path = "events",
                params =
                  Map(
                  "device_id" -> deviceID,
                  "event" -> eventDefinition.serialize()
                  ),
                responseHandler = defaultResponseHandler
              )
            }
            case None => {
              Log.d( tag, "No connected devices detected. Ignoring IncomingCall event..." )
            }
          }
        }
        case OutgoingCall( number, name ) if Preferences.getPhoneCallNotificationsEnabled => {
          Preferences.getConnectedDeviceId match {
            case Some( deviceID ) => {
              Log.d(tag, "Outgoing call event triggered")

              val notification = new Notification(
                icon_uri = EventIcons.OutgoingCall,
                title = name,
                subtitle = number
              )

              val eventDefinition = new Event(
                `type` = "outgoing_call",
                notification = notification,
                handlers = List(
                  EventHandlers.Notify
                )
              )

              if(Preferences.getSmartVolumeEnabled) {
                eventDefinition.handlers = EventHandlers.LowerVolume :: eventDefinition.handlers
              }

              requestActor ! HTTPRequestActor.POSTRequest(
                path = "events",
                params = Map(
                    "device_id" -> deviceID,
                    "event" -> eventDefinition.serialize()
                  ),
                responseHandler = defaultResponseHandler
              )
            }
            case None => {
              Log.d( tag, "No connected devices detected. Ignoring OutgoingCall event..." )
            }
          }
        }
        case MissedCall( number, name ) if Preferences.getPhoneCallNotificationsEnabled => {
          Preferences.getConnectedDeviceId match {
            case Some( deviceID ) => {
              Log.d(tag, "Missed call event triggered")

              val notification = new Notification(
                icon_uri = EventIcons.MissedCall,
                title = Util.getString(R.string.missed_call_notification),
                subtitle = name,
                description = number
              )

              val eventDefinition = new Event(
                `type` = "missed_call",
                notification = notification,
                handlers = List(
                  EventHandlers.Notify
                )
              )

              if(Preferences.getSmartVolumeEnabled) {
                eventDefinition.handlers = EventHandlers.RestoreVolume :: eventDefinition.handlers
              }

              requestActor ! HTTPRequestActor.POSTRequest(
                path = "events",
                params = Map(
                  "device_id" -> deviceID,
                  "event" -> eventDefinition.serialize()
                ),
                responseHandler = defaultResponseHandler
              )
            }
            case None => {
              Log.d( tag, "No connected devices detected. Ignoring MissedCall event..." )
            }
          }
        }
        case EndCall if Preferences.getPhoneCallNotificationsEnabled => {
          Preferences.getConnectedDeviceId match {
            case Some( deviceID ) => {
              Log.d(tag, "End call event triggered")

              val notification = new Notification(
                icon_uri = EventIcons.EndCall,
                title = Util.getString(R.string.end_call_notification)
              )

              val eventDefinition = new Event(
                `type` = "end_call",
                notification = notification,
                handlers = List(
                  EventHandlers.Notify
                )
              )

              if(Preferences.getSmartVolumeEnabled) {
                eventDefinition.handlers = EventHandlers.RestoreVolume :: eventDefinition.handlers
              }

              requestActor ! HTTPRequestActor.POSTRequest(
                path = "events",
                params = Map(
                    "device_id" -> deviceID,
                    "event" -> eventDefinition.serialize()
                  ),
                responseHandler = defaultResponseHandler
              )
            }
            case None => {
              Log.d( tag, "No connected devices detected. Ignoring EndCall event..." )
            }
          }
        }
        case SMSMessage(number, name, message) if Preferences.getSMSNotificationsEnabled => {
          Preferences.getConnectedDeviceId match {
            case Some( deviceID ) => {
              Log.d(tag, "SMS event triggered")

              val notification = new Notification(
                icon_uri = EventIcons.SmsMessage,
                title = name,
                subtitle = number,
                description = message
              )

              val eventDefinition = new Event(
                `type` = "sms_message",
                notification = notification,
                handlers = List(
                  EventHandlers.Notify
                )
              )

              if(Preferences.getNoiseAlertEnabled) {
                eventDefinition.handlers = EventHandlers.AlertNoise :: eventDefinition.handlers
              }

              requestActor ! HTTPRequestActor.POSTRequest(
                path = "events",
                params = Map(
                  "device_id" -> deviceID,
                  "event" -> eventDefinition.serialize()
                ),
                responseHandler = defaultResponseHandler
              )
            }
            case None => {
              Log.d( tag, "No connected devices detected. Ignoring SMSMessage event..." )
            }
          }
        }
        case WhatsAppMessage(name, message) if Preferences.getWhatsAppNotificationsEnabled => {
          Preferences.getConnectedDeviceId match {
            case Some( deviceID ) => {
              Log.d(tag, "WhatsApp event triggered")

              val notification = new Notification(
                icon_uri = EventIcons.WhatsAppMessage,
                title = name,
                subtitle = "",
                description = message
              )

              val eventDefinition = new Event(
                `type` = "whatsapp_message",
                notification = notification,
                handlers = List(
                  EventHandlers.Notify
                )
              )

              if(Preferences.getNoiseAlertEnabled) {
                eventDefinition.handlers = EventHandlers.AlertNoise :: eventDefinition.handlers
              }

              requestActor ! HTTPRequestActor.POSTRequest(
                path = "events",
                params = Map(
                  "device_id" -> deviceID,
                  "event" -> eventDefinition.serialize()
                ),
                responseHandler = defaultResponseHandler
              )
            }
            case None => {
              Log.d( tag, "No connected devices detected. Ignoring WhatsAppMessage event..." )
            }
          }
        }
        case SnapchatMessage(name, message) if Preferences.getSnapChatNotificationsEnabled => {
          Preferences.getConnectedDeviceId match {
            case Some( deviceID ) => {
              Log.d(tag, "SnapChat event triggered")

              val notification = new Notification(
                icon_uri = EventIcons.SnapChatMessage,
                title = name,
                subtitle = "",
                description = message
              )

              val eventDefinition = new Event(
                `type` = "snapchat_message",
                notification = notification,
                handlers = List(
                  EventHandlers.Notify
                )
              )

              if(Preferences.getNoiseAlertEnabled) {
                eventDefinition.handlers = EventHandlers.AlertNoise :: eventDefinition.handlers
              }

              requestActor ! HTTPRequestActor.POSTRequest(
                path = "events",
                params = Map(
                  "device_id" -> deviceID,
                  "event" -> eventDefinition.serialize()
                ),
                responseHandler = defaultResponseHandler
              )
            }
            case None => {
              Log.d( tag, "No connected devices detected. Ignoring SnapChatMessage event..." )
            }
          }
        }
        case _ => Log.d(tag, "Cannot react to message type. Ignoring...")
      }
    }
  }
}