package co.fether.triggr
import android.content.BroadcastReceiver
import android.content.Intent
import android.content.Context

class BootEventBroadcastReceiver extends BroadcastReceiver {
  override def onReceive( context : Context, intent : Intent ) {
    if ( Intent.ACTION_BOOT_COMPLETED.equals( intent.getAction() ) ) {
      var serviceIntent = new Intent( "co.fether.triggr.TriggrService" );
      context.startService( serviceIntent );
    }
  }
}