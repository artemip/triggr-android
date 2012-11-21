package apasyech.cbridge

import android.util.Log
import java.net._
import java.io.BufferedReader
import java.io.InputStreamReader
import android.os.AsyncTask
import scala.concurrent.ops._
import scala.collection.mutable.ListBuffer
import javax.jmdns.ServiceInfo

object ServerTools {
  private var selectedServer : Option[ServiceInfo] = None
  private val baseUrl = "http://ec2-75-101-183-71.compute-1.amazonaws.com:8000"
  private var servers = new ListBuffer[ServiceInfo]
    
  def getServers() : List[ServiceInfo] = servers.toList
  
  def selectServer(server : ServiceInfo) {
    selectedServer = Some(server)
  }
  
  def addServer(server : ServiceInfo) {
    servers prepend server
    for(s <- servers) Log.d("ServerTools", "Server: " + s)
  }
  
  def removeServer(server : ServiceInfo) {
   	servers  = servers.filterNot(x => x.getAddress == server.getAddress && x.getPort == server.getPort)  
  }
  
  private def getFullURL(tail: String) = {
    new URL(baseUrl + "/" + tail)
  }
  
  private def get(url : URL) = {
    Log.d("apasyech.cbridge.ServerTools", "Connecting to " + url.getPath)
    
    spawn {
	    try {
	      val connection = url.openConnection
	      
	      io.Source
	      	.fromInputStream(connection.getInputStream())
	  		.getLines
	  		.mkString("\n")
	    } catch {
	      case e: Exception => Log.e("Server communication error: ", e.toString())
	      ""
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