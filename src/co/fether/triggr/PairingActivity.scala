package co.fether.triggr

import android.os.Bundle
import android.app.Activity
import android.content.{Context, Intent}
import android.view.View
import android.widget._
import android.view.inputmethod.InputMethodManager
import android.text.Html

class PairingActivity extends Activity {
  var pairKeyTextBox : EditText = null
  var viewFlipper : ViewFlipper = null

  override def onCreate( savedInstanceState : Bundle ) {
    super.onCreate( savedInstanceState )
    setContentView( R.layout.pairing_activity )

    Preferences.setMainActivity( this )

    val serviceIntent = new Intent( TriggrService.getClass.getName )
    getApplicationContext().startService( serviceIntent )

    pairKeyTextBox = findViewById( R.id.pairKeyTextBox ).asInstanceOf[EditText]
    viewFlipper = findViewById(R.id.viewFlipper).asInstanceOf[ViewFlipper]

    pairKeyTextBox.setHint(Html.fromHtml("<span style=\"text-color: gray; text-align: center; font-size: 10px\">Pair Key</span>"))

    if (!Preferences.getConnectedDeviceId().isEmpty) { // The phone is paired
    	viewFlipper.setDisplayedChild(2)
    } else if (Preferences.getWasPreviouslyPaired()){ // The phone was paired at some point. Skip instructions
      viewFlipper.setDisplayedChild(1)
    }
  }

  def showPairingView( view : View) {
    viewFlipper.setInAnimation(this, R.anim.in_from_right)
    viewFlipper.setOutAnimation(this, R.anim.out_to_left)
    viewFlipper.showNext()
  }
  
  def pairWithDevice( view : View ) {
    val pairingKey = pairKeyTextBox.getText().toString()
    EventActor ! EventActor.Connect( pairingKey )

    pairKeyTextBox.setText( "" )
  }

  def showDisconnectView() {
    pairKeyTextBox.clearFocus()
    viewFlipper.setInAnimation(this, R.anim.in_from_right)
    viewFlipper.setOutAnimation(this, R.anim.out_to_left)
    viewFlipper.showNext()

    // Hide keyboard
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE).asInstanceOf[InputMethodManager]
    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
  }
  
  def disconnectDevice( view : View ) {
    EventActor ! EventActor.Disconnect

    viewFlipper.setInAnimation(this, R.anim.in_from_left)
    viewFlipper.setOutAnimation(this, R.anim.out_to_right)
    viewFlipper.showPrevious()
  }
}