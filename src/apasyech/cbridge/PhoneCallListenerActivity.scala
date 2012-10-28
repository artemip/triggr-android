package apasyech.cbridge;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.Menu;
import android.view.MenuItem;

class PhoneCallListenerActivity extends Activity {
	override def onCreate(savedInstanceState : Bundle) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_phone_call_listener)
		
		Server.incomingCall()
	}

	override def onCreateOptionsMenu(menu : Menu) : Boolean = {
		getMenuInflater().inflate(R.menu.activity_phone_call_listener, menu)
		return true
	}
}