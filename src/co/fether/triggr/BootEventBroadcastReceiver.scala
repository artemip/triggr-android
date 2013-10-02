package co.fether.triggr
import android.content.BroadcastReceiver
import android.content.Intent
import android.content.Context

/**
 * Class that listens for boot events and starts the Triggr service automatically.
 *
 * Registered in the manifest.
 */
class BootEventBroadcastReceiver extends BroadcastReceiver {
  override def onReceive( context : Context, intent : Intent ) {
    if ( Intent.ACTION_BOOT_COMPLETED.equals( intent.getAction ) ) {
      val serviceIntent = new Intent(TriggrService.getClass.getName)
      context.startService( serviceIntent )
    }
  }
}