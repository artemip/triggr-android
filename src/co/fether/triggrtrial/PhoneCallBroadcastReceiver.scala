package co.fether.triggrtrial
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.telephony.PhoneStateListener


/**
 * Activates a listener that listens for phone call events
 */
class PhoneCallBroadcastReceiver extends BroadcastReceiver {
  override def onReceive( context : Context, intent : Intent ) {
    //If listener has not been activated, activate it
    if ( !PhoneCallStateListener.isListening ) {
      val phoneManager = context.getSystemService( Context.TELEPHONY_SERVICE ).asInstanceOf[TelephonyManager]
      val listener = PhoneCallStateListener()

      phoneManager.listen( listener, PhoneStateListener.LISTEN_CALL_STATE )
    }
  }
}