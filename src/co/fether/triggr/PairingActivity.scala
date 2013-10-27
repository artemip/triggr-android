package co.fether.triggr

import android.os.Bundle
import android.app.Activity
import android.content.{Context, Intent}
import android.view.View
import android.widget._
import android.view.inputmethod.InputMethodManager
import android.text.Html
import android.graphics.Typeface

class PairingActivity extends Activity {
  var pairKeyEditText : EditText = null
  var viewFlipper : ViewFlipper = null

  var regularTypeFace : Typeface = null
  var lightTypeFace : Typeface = null
  var ultralightTypeFace : Typeface = null

  override def onCreate( savedInstanceState : Bundle ) {
    super.onCreate( savedInstanceState )
    setContentView( R.layout.pairing_activity )

    Preferences.setMainActivity( this )

    regularTypeFace = Typeface.createFromAsset(this.getAssets(), "fonts/HelveticaNeue.ttf")
    lightTypeFace = Typeface.createFromAsset(this.getAssets(), "fonts/HelveticaNeueLight.ttf")
    ultralightTypeFace = Typeface.createFromAsset(this.getAssets(), "fonts/HelveticaNeueUltraLight.ttf")

    // Set fonts
    Util.overrideFonts(this, findViewById(android.R.id.content), lightTypeFace)

    //findViewById(R.id.connectedTextView).asInstanceOf[TextView].setTypeface(ultralightTypeFace)

    pairKeyEditText = findViewById(R.id.pairKeyTextBox).asInstanceOf[EditText]
    viewFlipper = findViewById(R.id.pairingViewFlipper).asInstanceOf[ViewFlipper]

    pairKeyEditText.setHint(Html.fromHtml("<span style=\"text-color: gray; text-align: center; font-size: 10px\">Pair Key</span>"))
  }

  def testSms(view : View) {
    EventActor ! EventActor.SMSMessage("555-555-5555", "Test User", "My ovaries have been feeling really sore. Do you have any idea if it was the chicken?")
  }

  def testIncomingCall(view : View) {
    EventActor ! EventActor.IncomingCall("555-555-5555", "Test User")
  }

  def testOutgoingCall(view : View) {
    EventActor ! EventActor.OutgoingCall("555-555-5555", "Test User")
  }

  def testEndCall(view : View) {
    EventActor ! EventActor.EndCall
  }

  def testMissedCall(view : View) {
    EventActor ! EventActor.MissedCall("1-10-555-555-5555", "Artem Pasyechnyk Ishlamabadarisha")
  }

  def showPairingView( view : View) {
    viewFlipper.setInAnimation(this, R.anim.in_from_right)
    viewFlipper.setOutAnimation(this, R.anim.out_to_left)
    viewFlipper.showNext()
  }

  def pairWithDevice( view : View ) {
    val pairingKey = pairKeyEditText.getText().toString()
    EventActor ! EventActor.Connect( pairingKey )

    pairKeyEditText.setText( "" )
  }

  def showDisconnectView() {
    pairKeyEditText.clearFocus()
    viewFlipper.setInAnimation(this, R.anim.in_from_right)
    viewFlipper.setOutAnimation(this, R.anim.out_to_left)
    viewFlipper.showNext()

    Util.hideKeyboard(this)
  }

  def disconnectDevice( view : View ) {
    EventActor ! EventActor.Disconnect

    viewFlipper.setInAnimation(this, R.anim.in_from_left)
    viewFlipper.setOutAnimation(this, R.anim.out_to_right)
    viewFlipper.showPrevious()
  }

  def goToStore( view : View ) {

  }

  def goToShare( view : View ) {

  }

  def goToSettings( view : View ) {
    PairingActivity.this.startActivity(
      new Intent(PairingActivity.this, classOf[SettingsActivity])
    )
  }
}