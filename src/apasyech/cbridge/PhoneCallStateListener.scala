package apasyech.cbridge
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log

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
	val uuid = java.util.UUID.randomUUID
	override def onCallStateChanged(state : Int, incomingNumber : String) {
	  state match {
	    case TelephonyManager.CALL_STATE_RINGING => {
	      //Phone is ringing, so do stuff
	      Log.d(PhoneCallStateListener.tag + " - " + uuid, "Receiving phone call: " + incomingNumber)
	    }
	    case _ => {
	      Log.d(PhoneCallStateListener.tag + " - " + uuid, "callStateChanged: " + state)
	    }
	  }
	  
	}
}