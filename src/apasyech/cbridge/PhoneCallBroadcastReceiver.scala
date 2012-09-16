package apasyech.cbridge
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.telephony.PhoneStateListener
import android.util.Log

class PhoneCallBroadcastReceiver extends BroadcastReceiver {
  val uuid = java.util.UUID.randomUUID
  
  override def onReceive(context : Context, intent : Intent) {
	  //Handle shit if listener is inactive
	  if(!PhoneCallStateListener.isListening) {
	    val phoneManager = context.getSystemService(Context.TELEPHONY_SERVICE).asInstanceOf[TelephonyManager]
	    val listener = PhoneCallStateListener()
	    
	    phoneManager.listen(listener, PhoneStateListener.LISTEN_CALL_STATE)
	  }
  }
}