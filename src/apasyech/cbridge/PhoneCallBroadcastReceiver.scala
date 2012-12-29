package apasyech.cbridge
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.telephony.PhoneStateListener
import android.util.Log

class PhoneCallBroadcastReceiver extends BroadcastReceiver {
  override def onReceive(context: Context, intent: Intent) {
    //If listener has not been activated, activate it
    if (!PhoneCallStateListener.isListening) {
      val phoneManager = context.getSystemService(Context.TELEPHONY_SERVICE).asInstanceOf[TelephonyManager]
      val listener = PhoneCallStateListener()
      
      phoneManager.listen(listener, PhoneStateListener.LISTEN_CALL_STATE)
    }
  }
}