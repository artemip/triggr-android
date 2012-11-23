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

object ServerActor extends Actor {
  val tag = "apasyech.cbridge.ServerActor"

  case class SelectServer(server : String)
  case object StartCall
  case object EndCall
  
  private val requestActor = new RequestActor()
  private var selectedServer : Option[String] = None
  
  def act() {
    requestActor.start()
    
    loop {
      receive {
        case SelectServer(s) => {
        	Log.d(tag, "Selecting server " + s)
        	selectedServer = Some(s)
        	val response = (requestActor !? (10000, RequestActor.GetRequest(getFullURL("verify"))) )
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
        case StartCall => {
			val response = (requestActor !? (10000, RequestActor.GetRequest(getFullURL("start_call"))) )
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
        case EndCall => {
          val response = (requestActor !? (10000, RequestActor.GetRequest(getFullURL("end_call"))) )
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

private object RequestActor {
  case class GetRequest(url : URL)  
}

private class RequestActor extends Actor {  
  def act() {
    loop {
      receive {
        case RequestActor.GetRequest(url) => {
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
      }
    }
  }
}