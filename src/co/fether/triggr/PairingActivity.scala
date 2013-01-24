package co.fether.triggr;

import android.os.Bundle
import android.app.Activity
import android.content.Intent
import android.content.IntentFilter
import android.view.Menu
import android.view.MenuItem
import android.util.Log
import android.os.Handler
import android.view.View
import android.widget.EditText
import android.content.ServiceConnection
import android.content.ComponentName
import android.os.IBinder
import co.fether.triggr.R;

class PairingActivity extends Activity {
  var pairKeyTextBox : EditText = null

  override def onCreate( savedInstanceState : Bundle ) {
    super.onCreate( savedInstanceState )
    setContentView( R.layout.activity_phone_call_listener )

    Preferences.setMainActivity( this )

    var serviceIntent = new Intent( "co.fether.triggr.TriggrService" );
    getApplicationContext().startService( serviceIntent );

    pairKeyTextBox = findViewById( R.id.pairKeyTextBox ).asInstanceOf[EditText]
  }

  override def onCreateOptionsMenu( menu : Menu ) : Boolean = {
    getMenuInflater().inflate( R.menu.activity_phone_call_listener, menu )
    return true
  }

  def pairWithDevice( view : View ) {
    val pairingKey = pairKeyTextBox.getText().toString()
    ServerActor ! ServerActor.Pair( pairingKey )

    pairKeyTextBox.setText( "" )
  }
}