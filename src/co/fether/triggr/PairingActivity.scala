package co.fether.triggr

import android.os.Bundle
import android.app.Activity
import android.content.Intent
import android.view.Menu
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ViewSwitcher

class PairingActivity extends Activity {
  var pairKeyTextBox : EditText = null
  var connectView : LinearLayout = null
  var disconnectView : LinearLayout = null
  var viewSwitcher : ViewSwitcher = null

  override def onCreate( savedInstanceState : Bundle ) {
    super.onCreate( savedInstanceState )
    setContentView( R.layout.activity_phone_call_listener )

    Preferences.setMainActivity( this )

    val serviceIntent = new Intent( "co.fether.triggr.TriggrService" )
    getApplicationContext().startService( serviceIntent )

    pairKeyTextBox = findViewById( R.id.pairKeyTextBox ).asInstanceOf[EditText]
    connectView = findViewById(R.id.ConnectView).asInstanceOf[LinearLayout]
    disconnectView = findViewById(R.id.DisconnectView).asInstanceOf[LinearLayout]
    viewSwitcher = findViewById(R.id.viewSwitcher).asInstanceOf[ViewSwitcher]
    
    if (!Preferences.getPairedDeviceId().isEmpty) {
    	viewSwitcher.showNext()
    }
  }

  override def onCreateOptionsMenu( menu : Menu ) : Boolean = {
    getMenuInflater().inflate( R.menu.activity_phone_call_listener, menu )
    true
  }
  
  def pairWithDevice( view : View ) {
    val pairingKey = pairKeyTextBox.getText().toString()
    ServerActor ! ServerActor.Pair( pairingKey )

    pairKeyTextBox.setText( "" )
  }
  
  def disconnectDevice( view : View ) {
    ServerActor ! ServerActor.Disconnect
    
    viewSwitcher.showPrevious()
  }
}