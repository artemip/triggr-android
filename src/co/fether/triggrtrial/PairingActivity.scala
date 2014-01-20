package co.fether.triggrtrial

import android.os.Bundle
import android.app.Activity
import android.content.{Context, Intent}
import android.view.View
import android.widget._
import android.view.inputmethod.InputMethodManager
import android.text.Html
import android.graphics.Typeface
import android.net.Uri

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

    findViewById(R.id.connectedTextView).asInstanceOf[TextView].setTypeface(ultralightTypeFace)

    pairKeyEditText = findViewById(R.id.pairKeyTextBox).asInstanceOf[EditText]
    viewFlipper = findViewById(R.id.pairingViewFlipper).asInstanceOf[ViewFlipper]

    if( Preferences.getConnectedDeviceId.isDefined ) viewFlipper.setDisplayedChild(1)

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
    val appName = "co.fether.triggr"

    try {
      startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id="+appName)))
    } catch {
      case _ : android.content.ActivityNotFoundException => startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id="+appName)))
      case _ : Exception =>
    }
  }

  def goToShare( view : View ) {
    Preferences.NUM_TRIAL_DAYS = 30

    val intent = new Intent(Intent.ACTION_SEND)
    intent.setType("text/plain")
    intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.full_website_url))
    intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.sharing_desc))
    startActivity(Intent.createChooser(intent, "Share Triggr"))
  }

  def goToSettings( view : View ) {
    PairingActivity.this.startActivity(
      new Intent(PairingActivity.this, classOf[SettingsActivity])
    )
  }
}
