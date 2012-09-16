package apasyech.cbridge
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import java.net._
import java.io.BufferedReader
import java.io.InputStreamReader
import android.os.AsyncTask
import android.os.StrictMode

object PhoneCallStateListener {
  private val tag = classOf[PhoneCallStateListener].getName
  private var listener : Option[PhoneCallStateListener] = None 
  
  def apply() : PhoneCallStateListener =
    listener match {
      case Some(x) => x
      case None => {
        listener = Some(new PhoneCallStateListener)
        listener.get
      }
    }
  
  def isListening = {
    listener.isDefined
  }
  
}

class PhoneCallStateListener extends PhoneStateListener {
	val policy = new StrictMode.ThreadPolicy.Builder().permitAll().build()
	StrictMode.setThreadPolicy(policy)
  
	val uuid = java.util.UUID.randomUUID
	override def onCallStateChanged(state : Int, incomingNumber : String) {
	  state match {
	    case TelephonyManager.CALL_STATE_RINGING => {
	    	//Phone is ringing, so do stuff
	    	Log.d(PhoneCallStateListener.tag + " - " + uuid, "Receiving phone call: " + incomingNumber)

	      	val url = new URL("http://ec2-75-101-183-71.compute-1.amazonaws.com:8000/incoming_call")
	      
			try {
				val connection = url.openConnection
				connection.getInputStream()
			} catch {
				case e : Exception => Log.e("Shit", e.toString())
			}

	    }
	    case TelephonyManager.CALL_STATE_IDLE => {
	      //Phone call ended
	      Log.d(PhoneCallStateListener.tag + " - " + uuid, "Ended phone call: " + incomingNumber)
	      
	      val url = new URL("http://ec2-75-101-183-71.compute-1.amazonaws.com:8000/call_ended")
	      
			try {
				val connection = url.openConnection
				connection.getInputStream()
			} catch {
				case e : Exception => Log.e("Shit", e.toString())
			}
	    }
	    case _ => {
	      Log.d(PhoneCallStateListener.tag + " - " + uuid, "callStateChanged: " + state)
	    }
	  }
	  
	}
}