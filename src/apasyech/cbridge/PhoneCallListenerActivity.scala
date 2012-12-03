package apasyech.cbridge;

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

class PhoneCallListenerActivity extends Activity {
  var serverIdTextBox : EditText = null

  override def onCreate( savedInstanceState : Bundle ) {
    super.onCreate( savedInstanceState )
    setContentView( R.layout.activity_phone_call_listener )
    cBridgeApp.setActivity( this )

    serverIdTextBox = findViewById( R.id.ipTextBox ).asInstanceOf[EditText]

    serverIdTextBox.setText( cBridgeApp.getServerAddress().getOrElse( "" ) )
  }

  override def onCreateOptionsMenu( menu : Menu ) : Boolean = {
    getMenuInflater().inflate( R.menu.activity_phone_call_listener, menu )
    return true
  }

  def connectToPC( view : View ) {
    val serverId = serverIdTextBox.getText().toString()
    cBridgeApp.setServerAddress( serverId )

    ServerActor.start()  
  }
  
  def lowerVolume( view : View ) {
    cBridgeApp.getServerAddress() match {
      case Some(s) => ServerActor ! ServerActor.StartCall(s)
      case None => 
    }
  }
  
  def raiseVolume( view : View ) {
    cBridgeApp.getServerAddress() match {
      case Some(s) => ServerActor ! ServerActor.EndCall(s)
      case None => 
    }
  }
  
}