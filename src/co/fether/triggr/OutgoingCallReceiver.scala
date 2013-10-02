package co.fether.triggr
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Object storing the last number the user called.
 */
object OutgoingCallReceiver {
  var outgoingNumber : Option[String] = None
}

/**
 * Listens for outgoing calls and sets the global outgoingNumber value in the above object
 */
class OutgoingCallReceiver extends BroadcastReceiver {
  override def onReceive(ctx : Context, intent : Intent) {
	  OutgoingCallReceiver.outgoingNumber = Some(intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER))
  }
}