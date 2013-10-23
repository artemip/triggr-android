package co.fether.triggr

import android.util.Log
import scala.actors.Actor
import android.widget.Toast
import co.fether.triggr.json._

object EventActor extends Actor {
  val tag = EventActor.getClass.getName

  // Event case classes
  case object Disconnect
  case class Connect( pairingKey : String )
  case class IncomingCall( number : String, name : String )
  case class OutgoingCall( number : String, name : String )
  case class MissedCall( number : String, name : String )
  case class SMSMessage( number : String, name : String, message : String )
  case object EndCall

  // Actor that performs HTTP requests
  private val requestActor = new HTTPRequestActor()

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

                    Preferences.getMainActivity() match {
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
            icon_uri = "",
            title = "Connected"
          )

          val eventDefinition = new Event(
            `type` = "connect",
            notification = notification,
            handlers = List("notify")
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

          Preferences.getConnectedDeviceId() match {
            case Some( id ) => {

              val notification = new Notification(
                icon_uri = "",
                title = "Disconnected"
              )

              val eventDefinition = new Event(
                `type` = "disconnect",
                notification = notification,
                handlers = List("notify")
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

        case IncomingCall( number, name) => {
          Preferences.getConnectedDeviceId() match {
            case Some( deviceID ) => {
              val notification = new Notification(
                icon_uri = "",
                title = "Incoming Call",
                subtitle = name,
                description = number
              )

              val eventDefinition = new Event(
                `type` = "incoming_call",
                notification = notification,
                handlers = List("notify", "lower_volume")
              )

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
        case OutgoingCall( number, name ) => {
          Preferences.getConnectedDeviceId() match {
            case Some( deviceID ) => {
              val notification = new Notification(
                icon_uri = "",
                title = "Outgoing Call",
                subtitle = name,
                description = number
              )

              val eventDefinition = new Event(
                `type` = "outgoing_call",
                notification = notification,
                handlers = List("notify", "lower_volume")
              )

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
        case MissedCall( number, name ) => {
          Preferences.getConnectedDeviceId() match {
            case Some( deviceID ) => {
              val notification = new Notification(
                icon_uri = "",
                title = "Missed Call",
                subtitle = name,
                description = number
              )

              val eventDefinition = new Event(
                `type` = "missed_call",
                notification = notification,
                handlers = List("notify", "restore_volume")
              )

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
        case EndCall => {
          Preferences.getConnectedDeviceId() match {
            case Some( deviceID ) => {
              val notification = new Notification(
                icon_uri = "",
                title = "Call Ended"
              )

              val eventDefinition = new Event(
                `type` = "end_call",
                notification = notification,
                handlers = List("notify", "restore_volume")
              )

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
      }
    }
  }
}