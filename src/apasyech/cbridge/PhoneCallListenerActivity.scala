package apasyech.cbridge;

import android.os.Bundle
import android.app.Activity
import android.content.Intent
import android.content.IntentFilter
import android.view.Menu
import android.view.MenuItem
import sun.nio.ch.Net
import android.util.Log
import android.os.Handler
import scala.concurrent.ops._

class PhoneCallListenerActivity extends Activity {
  var nsd : NetworkDiscoveryActor = null;
  
  override def onCreate( savedInstanceState : Bundle ) {
    super.onCreate( savedInstanceState )
    setContentView( R.layout.activity_phone_call_listener )

    Log.d( "PhoneCallListenerActivity", "Starting discovery..." )

    nsd = new NetworkDiscoveryActor( this.getApplicationContext() )
    nsd.start
    
    nsd ! NetworkDiscoveryActor.StartDiscovery
  }
  
  override def onStop() {
    nsd ! NetworkDiscoveryActor.StopDiscovery
  }

  override def onCreateOptionsMenu( menu : Menu ) : Boolean = {
    getMenuInflater().inflate( R.menu.activity_phone_call_listener, menu )
    return true
  }
}