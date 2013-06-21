package co.fether.triggr
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

object OutgoingCallReceiver {
  var outgoingNumber : Option[String] = None
}

class OutgoingCallReceiver extends BroadcastReceiver {
  override def onReceive(ctx : Context, intent : Intent) {
	  OutgoingCallReceiver.outgoingNumber = Some(intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER))
  }
}