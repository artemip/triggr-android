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
import android.view.View
import android.widget.EditText

class PhoneCallListenerActivity extends Activity {
  var ipAddressTextBox : EditText = null

  override def onCreate( savedInstanceState : Bundle ) {
    super.onCreate( savedInstanceState )
    setContentView( R.layout.activity_phone_call_listener )
    cBridgeApp.setActivity( this )

    ipAddressTextBox = findViewById( R.id.ipTextBox ).asInstanceOf[EditText]

    ipAddressTextBox.setText( cBridgeApp.getLastServerAddress().getOrElse( "" ) )
  }

  override def onCreateOptionsMenu( menu : Menu ) : Boolean = {
    getMenuInflater().inflate( R.menu.activity_phone_call_listener, menu )
    return true
  }

  def connectToPC( view : View ) {
    val ipAndHost = ipAddressTextBox.getText().toString()
    if ( cBridgeApp.getLastServerAddress().isEmpty ) cBridgeApp.setServerAddress( ipAndHost )

    val hostAddress = "http://" + ipAndHost

    ServerActor.start()
    ServerActor ! ServerActor.SelectServer( hostAddress )
    //ServerActor ! ServerActor.StartCall
  }
}