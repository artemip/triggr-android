package co.fether.triggr

import android.util.Log
import java.net._
import scala.actors.Actor
import java.io.DataOutputStream

private object HTTPRequestActor {
  private val routingServer = "http://ec2-107-21-171-184.compute-1.amazonaws.com:8000"

  case class GETRequest( path : String, responseHandler : Option[String] => Unit = _ => () )
  case class POSTRequest( path : String, params : Map[String, String], responseHandler : Option[String] => Unit = _ => () )

  private def mapToString( map : Map[String, String] ) : String = {
    map.foldRight( "" )( ( kv, acc ) => kv._1 + "=" + kv._2 + "&" + acc )
  }

  private def getFullURL( path : String ) = {
    new URL( routingServer + "/" + path )
  }
}

private class HTTPRequestActor extends Actor {
  val tag = HTTPRequestActor.getClass.getName

  def act() {
    loop {
      receive {
        case HTTPRequestActor.GETRequest( path, responseHandler ) => {
          val url = HTTPRequestActor.getFullURL(path)
          Log.d( tag, "GET Request to " + url.toString )

          try {
            val connection = url.openConnection
            val response = io.Source
              .fromInputStream( connection.getInputStream )
              .getLines()
              .mkString( "\n" )

            responseHandler( Some( response ) )
          } catch {
            case e : Exception => {
              Log.e( "Server communication error: ", e.toString )
              responseHandler( None )
            }
          }
        }

        case HTTPRequestActor.POSTRequest( path, params, responseHandler ) => {
          val url = HTTPRequestActor.getFullURL(path)

          Log.d( tag, "POST Request to " + url.toString )

          val connection = url.openConnection.asInstanceOf[HttpURLConnection]

          try {
            val stringParams : String = HTTPRequestActor.mapToString( params )
            Log.i(tag, stringParams)
            connection.setDoInput( true )
            connection.setDoOutput( true )
            connection.setInstanceFollowRedirects( false )
            connection.setRequestMethod( "POST" )
            connection.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded; charset=utf-8" )
            connection.setUseCaches( false )

            connection.getOutputStream.write(stringParams.getBytes("UTF-8"))
            connection.getOutputStream.flush()
            connection.getOutputStream.close()

            val responseStream = connection.getInputStream

            val response = Some(
              io.Source
                .fromInputStream( responseStream )
                .getLines()
                .mkString( "\n" )
            )

            responseHandler( response )
          } catch {
            case e : Exception => {
              Log.e(tag, "Server communication error: " + e.toString)
              responseHandler( None )
            }
          } finally {
            connection.disconnect()
          }
        }
      }
    }
  }
}
