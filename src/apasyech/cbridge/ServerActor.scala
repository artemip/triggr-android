package apasyech.cbridge

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

object ServerActor extends Actor {
  val tag = "apasyech.cbridge.ServerActor"

  case class SelectServer(server : String)
  case class StartCall(deviceId : String)
  case class EndCall(deviceId : String)
  
  private val requestActor = new HTTPRequestActor()
  private var selectedServer : Option[String] = Some("http://api.cbridgeapp.com:8000") //None
  
  def act() {
    requestActor.start()
    
    loop {
      receive {
        case SelectServer(s) => {
        	Log.d(tag, "Selecting server " + s)
        	selectedServer = Some(s)
        	val response = (requestActor !? (10000, HTTPRequestActor.GetRequest(getFullURL("register"))) )
        	response match {
        	  case Some(s : String) => {
        		  Log.d(tag, "Verification returned OK")
        		  cBridgeApp.showToast("Connection successful.", Toast.LENGTH_SHORT)
        	  }
        	  case None => {
        	    cBridgeApp.showToast("Connection failed.", Toast.LENGTH_LONG)
        	  }
        	}
        }
        case StartCall(deviceId) => {
			val response = (requestActor !? (10000, HTTPRequestActor.PostRequest(getFullURL("events"), Map("device_id" -> deviceId, "event" -> "start_call"))) )
			response match {	
			  case Some(s : String) => {
        		  Log.d(tag, "start_call method called successfully")
        	  }
        	  case None => {
        	    Log.e(tag, "start_call method failed")
        	    cBridgeApp.showToast("Connection failed.", Toast.LENGTH_LONG)
        	  }
			}
        }
        case EndCall(deviceId) => {
          val response = (requestActor !? (10000, HTTPRequestActor.PostRequest(getFullURL("events"),  Map("device_id" -> deviceId, "event" -> "end_call"))) )
          response match {
			  case Some(s : String) => {
        		  Log.d(tag, "end_call method called successfully")
        	  }
        	  case None => {
        	    Log.e(tag, "end_call method failed")
        	    cBridgeApp.showToast("Connection failed.", Toast.LENGTH_LONG)
        	  }
			}
        }
      }
    }    
  }
  
  private def getFullURL( tail : String ) = {
    Log.d(tag, "Creating URL with " + selectedServer.getOrElse("Nothing!"))
    
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
  case class GetRequest(url : URL)  
  case class PostRequest(url : URL, params : Map[String, String])  
  private def mapToString(map : Map[String, String]) : String = {    
    map.foldRight("")( (kv, acc) => kv._1 + "=" + kv._2 + "&" + acc)
  }
}

private class HTTPRequestActor extends Actor {  
	def act() {
		loop {
			receive {
				case HTTPRequestActor.GetRequest(url) => {
					Log.d("apasyech.cbridge.RequestActor", "GET Request to " + url.toString())
	
					try {
						val connection = url.openConnection
						val response = io.Source
							.fromInputStream(connection.getInputStream())
							.getLines
							.mkString("\n")
	
						reply(response)
					} catch {
						case e: Exception => Log.e("Server communication error: ", e.toString())
					}  
				}
				case HTTPRequestActor.PostRequest(url, params) => {
					Log.d("apasyech.cbridge.RequestActor", "POST Request to " + url.toString())
	
					try {
						val connection = url.openConnection.asInstanceOf[HttpURLConnection]
						val stringParams = HTTPRequestActor.mapToString(params)
						
						connection.setDoInput(true)
						connection.setDoOutput(true)
						connection.setInstanceFollowRedirects(false) 
						connection.setRequestMethod("POST")
						connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded") 
						connection.setRequestProperty("charset", "utf-8")
						connection.setUseCaches(false)												
						
						connection.setRequestProperty("Content-Length", Integer.toString(stringParams.getBytes.length))
						
						val wr = new DataOutputStream(connection.getOutputStream())
						wr.writeBytes(stringParams)
						wr.flush()
						wr.close()
						
						val response = io.Source
							.fromInputStream(connection.getInputStream())
							.getLines
							.mkString("\n")
							
						connection.disconnect()
						
						reply(response)
					} catch {
						case e: Exception => Log.e("Server communication error: ", e.toString())
					}
				}
			}
		}
	}
}