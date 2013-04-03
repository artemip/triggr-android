package co.fether.triggr

import android.util.Log
import java.net._
import java.io.BufferedReader
import java.io.InputStreamReader
import android.os.AsyncTask
import scala.concurrent.ops._
import scala.collection.mutable.ListBuffer
import scala.actors.Actor
import android.widget.Toast
import java.io.DataOutputStream
import java.util.concurrent._
import android.telephony.PhoneNumberUtils

object HeartbeatGenerator {
  private val scheduler = Executors.newSingleThreadScheduledExecutor()

  private val callback = new Runnable {
    def run = ServerActor ! ServerActor.Heartbeat
  }

  def start() = scheduler.scheduleAtFixedRate( callback, 0, 3, TimeUnit.MINUTES )
}

object ServerActor extends Actor {
  val tag = "co.fether.triggr.ServerActor"

  case object Heartbeat
  case object Disconnect
  case class Pair( pairingKey : String )
  case class IncomingCall( deviceId : String, number : String, name : String )
  case class OutgoingCall( deviceId : String, number : String, name : String  )
  case class EndCall( deviceId : String )

  private val requestActor = new HTTPRequestActor()
  private var selectedServer : Option[String] = Some( "http://api.triggrapp.com:8000" )

  def act() {
    requestActor.start()

    loop {
      receive {
        case Heartbeat => {
          Log.d( tag, "Sending heartbeat" )

          requestActor ! HTTPRequestActor.PostRequest(
            getFullURL( "heartbeat" ),
            Map( "device_id" -> Preferences.getDeviceId().toString(), "paired_device_id" -> Preferences.getPairedDeviceId().getOrElse( "" ) ) )
        }
        case Disconnect => {
        Log.d( tag, "Disconnecting" )

          requestActor ! HTTPRequestActor.PostRequest(
            getFullURL( "disconnect" ),
            Map( "device_id" -> Preferences.getDeviceId().toString(), "paired_device_id" -> Preferences.getPairedDeviceId().getOrElse( "" ) ) )  
        }
        case Pair( k ) => {
          Log.d( tag, "Pairing with new device using key " + k )

          def handler( response : Option[String] ) {
            response match {
              case Some( s : String ) => {
                Log.d( tag, "Pairing request returned OK" )
                Preferences.getService() match {
                  case Some( s ) => s.showToast( "Pairing successful.", Toast.LENGTH_SHORT )
                  case None =>
                }
                Preferences.setPairedDeviceId( Some(s) )
              }
              case None => {
                Preferences.getService() match {
                  case Some( s ) => s.showToast( "Invalid pairing key.", Toast.LENGTH_LONG )
                  case None =>
                }
              }
            }
          }

          requestActor ! HTTPRequestActor.PostRequest(
            getFullURL( "pair" ),
            Map( "device_id" -> Preferences.getDeviceId().toString(), "pairing_key" -> k ),
            handler )
        }
        case IncomingCall( deviceId, number, name) => {
          def handler( response : Option[String] ) {
            response match {
              case Some( s : String ) => {
                Log.d( tag, "incoming_call method called successfully" )
              }
              case None => {
                Log.e( tag, "incoming_call method failed" )
                Preferences.getService() match {
                  case Some( s ) => s.showToast( "Connection failed.", Toast.LENGTH_LONG )
                  case None =>
                }
              }
            }
          }

          requestActor ! HTTPRequestActor.PostRequest(
            getFullURL( "events" ),
            Map( "device_id" -> deviceId, "event" -> ("incoming_call:" concat PhoneNumberUtils.formatNumber( number ) concat "," concat name) ),
            handler )
        }
        case OutgoingCall( deviceId, number, name ) => {
          def handler( response : Option[String] ) {
            response match {
              case Some( s : String ) => {
                Log.d( tag, "outgoing_call method called successfully" )
              }
              case None => {
                Log.e( tag, "outgoing_call method failed" )
                Preferences.getService() match {
                  case Some( s ) => s.showToast( "Connection failed.", Toast.LENGTH_LONG )
                  case None =>
                }
              }
            }
          }

          requestActor ! HTTPRequestActor.PostRequest(
            getFullURL( "events" ),
            Map( "device_id" -> deviceId, "event" -> ("outgoing_call:" concat PhoneNumberUtils.formatNumber( number ) concat "," concat name) ),
            handler )
        }
        case EndCall( deviceId ) => {
          def handler( response : Option[String] ) {
            response match {
              case Some( s : String ) => {
                Log.d( tag, "end_call method called successfully" )
              }
              case None => {
                Log.e( tag, "end_call method failed" )
                Preferences.getService() match {
                  case Some( s ) => s.showToast( "Connection failed.", Toast.LENGTH_LONG )
                  case None =>
                }
              }
            }
          }

          requestActor ! HTTPRequestActor.PostRequest(
            getFullURL( "events" ),
            Map( "device_id" -> deviceId, "event" -> "end_call" ),
            handler )
        }
      }
    }
  }

  private def getFullURL( tail : String ) = {
    Log.d( tag, "Creating URL with " + selectedServer.getOrElse( "Nothing!" ) )

    selectedServer match {
      case Some( server ) => {
        new URL( server + "/" + tail )
      }
      case None => {
        new URL( "/" + tail )
      }
    }
  }
}

private object HTTPRequestActor {
  case class GetRequest( url : URL, handler : Option[String] => Unit = _ => () )
  case class PostRequest( url : URL, params : Map[String, String], handler : Option[String] => Unit = _ => () )

  private def mapToString( map : Map[String, String] ) : String = {
    map.foldRight( "" )( ( kv, acc ) => kv._1 + "=" + kv._2 + "&" + acc )
  }
}

private class HTTPRequestActor extends Actor {
  def act() {
    loop {
      receive {
        case HTTPRequestActor.GetRequest( url, handler ) => {
          Log.d( "co.fether.triggr.RequestActor", "GET Request to " + url.toString() )

          try {
            val connection = url.openConnection
            val response = io.Source
              .fromInputStream( connection.getInputStream() )
              .getLines
              .mkString( "\n" )

            handler( Some( response ) )
          } catch {
            case e : Exception => {
              Log.e( "Server communication error: ", e.toString() )
              handler( None )
            }
          }
        }
        case HTTPRequestActor.PostRequest( url, params, handler ) => {
          Log.d( "co.fether.triggr.RequestActor", "POST Request to " + url.toString() )

          try {
            val connection = url.openConnection.asInstanceOf[HttpURLConnection]
            val stringParams : String = HTTPRequestActor.mapToString( params )

            connection.setDoInput( true )
            connection.setDoOutput( true )
            connection.setInstanceFollowRedirects( false )
            connection.setRequestMethod( "POST" )
            connection.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded" )
            connection.setRequestProperty( "charset", "utf-8" )
            connection.setUseCaches( false )

            connection.setRequestProperty( "Content-Length", Integer.toString( stringParams.getBytes.length ) )

            val wr = new DataOutputStream( connection.getOutputStream() )
            wr.writeBytes( stringParams )
            wr.flush()
            wr.close()

            var response : Option[String] = None

            try {
              val responseStream = connection.getInputStream()

              response = Some( io.Source
                .fromInputStream( responseStream )
                .getLines
                .mkString( "\n" ) )
            } finally {
              connection.disconnect()
            }

            handler( response )
          } catch {
            case e : Exception => {
              Log.e( "Server communication error: ", e.toString() )
              handler( None )
            }
          }
        }
      }
    }
  }
}