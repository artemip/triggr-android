package co.fether.triggrtrial
import android.content.BroadcastReceiver
import android.content.Intent
import android.content.Context
import android.util.Log

/**
 * Class that listens for boot events and starts the Triggr service automatically.
 *
 * Registered in the manifest.
 */
class BootEventBroadcastReceiver extends BroadcastReceiver {
  private val tag = getClass.getCanonicalName

  override def onReceive( context : Context, intent : Intent ) {
    if ( Intent.ACTION_BOOT_COMPLETED.equals( intent.getAction ) ) {
      Log.d(tag, "Starting Triggr service after boot event")
      val serviceIntent = new Intent(TriggrService.getClass.getName)
      context.startService( serviceIntent )
    }
  }
}