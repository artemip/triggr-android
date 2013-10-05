package co.fether.triggr

import android.util.Log
import scala.actors.Actor
import android.widget.Toast
import android.telephony.PhoneNumberUtils
import net.liftweb.json._
import net.liftweb.json.Serialization.{read, write}

object EventActor extends Actor {
  val tag = EventActor.getClass.getName

  // For JSON serialization
  implicit val formats = Serialization.formats(NoTypeHints)

  // Event case classes
  case object Disconnect
  case class Connect( pairingKey : String )
  case class IncomingCall( number : String, name : String )
  case class OutgoingCall( number : String, name : String )
  case class SMSMessage( number : String, name : String, message : String )
  case object EndCall

  // Actor that performs HTTP requests
  private val requestActor = new HTTPRequestActor()

  /* JSON Request and Response case classes */

  /*
   * 'event' : {
   *   'sender_id' : sender_id,
   *   'type' : type,
   *   'notification' : {
   *     'icon_uri' : icon_uri,
   *     'title' : title,
   *     'subtitle' : subtitle,
   *     'description' : description
   *   },
   *   'handlers' : ['notify', 'lower_volume', 'restore_volume', 'alert_noise']
   * }
   */
  private case class Event( sender_id : String = Preferences.getDeviceId().toString, `type` : String, notification : Notification, handlers : List[String] = List("none") )
  private case class Notification( icon_uri : String, title : String, subtitle : String = "", description : String = "" )

  /*
   * 'server_response' : {
   *   'status' : {"ok", "error"},
   *   'message' : message,
   *   'paired_device_id' : paired_device_id
   * }
   */
  private case class ServerResponse( status : String, message : String, paired_device_id : String )


  /**
   * Default handler for server responses. Shows an error message for erronous responses
   * @param response response received from the server
   */
  private def defaultResponseHandler(response : Option[String]) {
    response match {
      case Some( json : String ) => {
        val serverResponse = read[ServerResponse](json)

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
                val serverResponse = read[ServerResponse](json)

                serverResponse.status match {
                  case "ok" => {
                    Log.d( tag, "Connection successful." )
                    Preferences.setConnectedDeviceId( Some(serverResponse.paired_device_id) )
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

          val notification = Notification(
            icon_uri = "",
            title = "Connected"
          )

          val eventDefinition = Event(
            `type` = "connect",
            notification = notification,
            handlers = List("notify")
          )

          requestActor ! HTTPRequestActor.POSTRequest(
            path = "/connect",
            params =
              Map(
                "device_id" -> Preferences.getDeviceId().toString,
                "pairing_key" -> k,
                "event" -> write(eventDefinition)
              ),
            responseHandler = responseHandler
          )
        }

        case Disconnect => {
          Log.d( tag, "Disconnecting" )

          Preferences.getConnectedDeviceId() match {
            case Some( id ) => {

              val notification = Notification(
                icon_uri = "",
                title = "Disconnected"
              )

              val eventDefinition = Event(
                `type` = "disconnect",
                notification = notification,
                handlers = List("notify")
              )

              requestActor ! HTTPRequestActor.POSTRequest(
                path = "/disconnect",
                params = Map(
                    "device_id" -> Preferences.getDeviceId().toString,
                    "paired_device_id" -> id,
                    "event" -> write(eventDefinition)
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
              val notification = Notification(
                icon_uri = "",
                title = "Incoming Call",
                subtitle = name.trim,
                description = PhoneNumberUtils.formatNumber( number ).trim
              )

              val eventDefinition = Event(
                `type` = "incoming_call",
                notification = notification,
                handlers = List("notify", "lower_volume")
              )

              requestActor ! HTTPRequestActor.POSTRequest(
                path = "/events",
                params =
                  Map(
                  "device_id" -> deviceID,
                  "event" -> write(eventDefinition)
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
              val notification = Notification(
                icon_uri = "",
                title = "Outgoing Call",
                subtitle = name.trim,
                description = PhoneNumberUtils.formatNumber( number ).trim
              )

              val eventDefinition = Event(
                `type` = "outgoing_call",
                notification = notification,
                handlers = List("notify", "lower_volume")
              )

              requestActor ! HTTPRequestActor.POSTRequest(
                path = "/events",
                params = Map(
                    "device_id" -> deviceID,
                    "event" -> write(eventDefinition)
                  ),
                responseHandler = defaultResponseHandler
              )
            }
            case None => {
              Log.d( tag, "No connected devices detected. Ignoring OutgoingCall event..." )
            }
          }
        }
        case EndCall => {
          Preferences.getConnectedDeviceId() match {
            case Some( deviceID ) => {
              val notification = Notification(
                icon_uri = "",
                title = "Call Ended"
              )

              val eventDefinition = Event(
                `type` = "end_call",
                notification = notification,
                handlers = List("notify", "restore_volume")
              )

              requestActor ! HTTPRequestActor.POSTRequest(
                path = "/events",
                params = Map(
                    "device_id" -> deviceID,
                    "event" -> write(eventDefinition)
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