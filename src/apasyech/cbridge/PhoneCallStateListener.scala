package apasyech.cbridge
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.os.AsyncTask
import android.os.StrictMode
import android.util.Log

object PhoneCallStateListener {
  private var tag = classOf[PhoneCallStateListener].getName
  private var listener: Option[PhoneCallStateListener] = None

  def apply(): PhoneCallStateListener =
    listener match {
      case Some(x) => x
      case None => {
        val newListener = new PhoneCallStateListener
        listener = Some(newListener)

        newListener
      }
    }

  def isListening = {
    listener.isDefined
  }
}

class PhoneCallStateListener extends PhoneStateListener {
  override def onCallStateChanged(state: Int, incomingNumber: String) {
    state match {
      case TelephonyManager.CALL_STATE_RINGING => {
        //Phone is ringing
        Log.d(PhoneCallStateListener.tag, "Receiving phone call: " + incomingNumber)

        cBridgeApp.getServerAddress() match {
          case Some(s) => ServerActor ! ServerActor.StartCall(s)
          case None => Log.w(PhoneCallStateListener.tag, "ServerID not specified")
        }
        
      }
      case TelephonyManager.CALL_STATE_IDLE => {
        //Phone call ended
        Log.d(PhoneCallStateListener.tag, "Ended phone call: " + incomingNumber)

        cBridgeApp.getServerAddress() match {
          case Some(s) => ServerActor ! ServerActor.EndCall(s)
          case None => Log.w(PhoneCallStateListener.tag, "ServerID not specified")
        }
      }
      case _ => {
        Log.d(PhoneCallStateListener.tag, "callStateChanged: " + state)
      }
    }
  }
}