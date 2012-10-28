package apasyech.cbridge

import android.util.Log
import java.net._
import java.io.BufferedReader
import java.io.InputStreamReader
import android.os.AsyncTask
import scala.concurrent.ops._

//TODO: Rename
object Server {
  private val baseUrl = "http://ec2-75-101-183-71.compute-1.amazonaws.com:8000"

  private def getFullURL(tail: String) = {
    new URL(baseUrl + "/" + tail)
  }
  
  private def get(url : URL) = {
    Log.d("apasyech.cbridge.Server", "Connecting to " + url.getPath)
    
    spawn {
	    try {
	      val connection = url.openConnection
	      
	      io.Source
	      	.fromInputStream(connection.getInputStream())
	  		.getLines
	  		.mkString("\n")
	    } catch {
	      case e: Exception => Log.e("Server communication error: ", e.toString())
	      "Error"
	    }
    }
  }

  def incomingCall() {
    get(getFullURL("incoming_call"))
  }

  def callEnded() {
    get(getFullURL("call_ended"))
  }
}