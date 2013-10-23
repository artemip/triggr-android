package co.fether.triggr

import android.os.Bundle
import android.app.Activity
import android.content.Intent
import android.view.View
import android.widget._
import android.graphics.Typeface
import android.view.animation.{AnimationUtils, Animation}
import android.view.animation.Animation.AnimationListener

class InstructionsActivity extends Activity {
  var viewFlipper : ViewFlipper = null
  var welcomeTextView : TextView = null
  var leftNavButton : Button = null
  var rightNavButton : Button = null
  var leftNavArrowImageView : ImageView = null
  var rightNavArrowImageView : ImageView = null
  var finishInstructionsButton : Button = null

  var inFromLeftAnim : Animation = null
  var inFromRightAnim : Animation = null
  var outToLeftAnim : Animation = null
  var outToRightAnim : Animation = null

  var regularTypeFace : Typeface = null
  var lightTypeFace : Typeface = null
  var ultralightTypeFace : Typeface = null

  var activeAnimationCounter : Int = 0

  override def onCreate( savedInstanceState : Bundle ) {
    super.onCreate( savedInstanceState )
    setContentView( R.layout.instructions_activity )

    regularTypeFace = Typeface.createFromAsset(this.getAssets, "fonts/HelveticaNeue.ttf")
    lightTypeFace = Typeface.createFromAsset(this.getAssets, "fonts/HelveticaNeueLight.ttf")
    ultralightTypeFace = Typeface.createFromAsset(this.getAssets, "fonts/HelveticaNeueUltraLight.ttf")

    viewFlipper = findViewById(R.id.viewFlipper).asInstanceOf[ViewFlipper]
    welcomeTextView = findViewById(R.id.welcomeTextView).asInstanceOf[TextView]
    leftNavButton = findViewById(R.id.leftNavButton).asInstanceOf[Button]
    rightNavButton = findViewById(R.id.rightNavButton).asInstanceOf[Button]
    finishInstructionsButton = findViewById(R.id.finishInstructionsButton).asInstanceOf[Button]
    leftNavArrowImageView = findViewById(R.id.leftNavArrowImageView).asInstanceOf[ImageView]
    rightNavArrowImageView = findViewById(R.id.rightNavArrowImageView).asInstanceOf[ImageView]

    viewFlipper.setInAnimation(this, android.R.anim.fade_in)
    viewFlipper.setOutAnimation(this, android.R.anim.fade_out)

    setLeftNavVisibility(View.INVISIBLE)

    setAnimationListeners()

    // Set fonts
    Util.overrideFonts(this, findViewById(android.R.id.content), lightTypeFace)
    welcomeTextView.setTypeface(ultralightTypeFace)

    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
  }

  private def setAnimationListeners() {
    inFromLeftAnim = AnimationUtils.loadAnimation(getApplicationContext, R.anim.in_from_left)
    inFromRightAnim = AnimationUtils.loadAnimation(getApplicationContext, R.anim.in_from_right)
    outToLeftAnim = AnimationUtils.loadAnimation(getApplicationContext, R.anim.out_to_left)
    outToRightAnim = AnimationUtils.loadAnimation(getApplicationContext, R.anim.out_to_right)

    val animListener = new AnimationListener() {
      def onAnimationStart(p1: Animation) {
        activeAnimationCounter = activeAnimationCounter + 1
      }

      def onAnimationEnd(p1: Animation) {
        activeAnimationCounter = activeAnimationCounter - 1
      }

      def onAnimationRepeat(p1: Animation) {}
    }

    inFromLeftAnim.setAnimationListener(animListener)
    inFromRightAnim.setAnimationListener(animListener)
    outToLeftAnim.setAnimationListener(animListener)
    outToRightAnim.setAnimationListener(animListener)
  }

  private def setLeftNavVisibility( visibility : Int ) {
    leftNavButton.setVisibility(visibility)
    leftNavArrowImageView.setVisibility(visibility)
  }

  private def setRightNavVisibility( visibility : Int ) {
    rightNavButton.setVisibility(visibility)
    rightNavArrowImageView.setVisibility(visibility)
  }

    def startPairingActivity(view : View) {
    InstructionsActivity.this.startActivity(
      new Intent(InstructionsActivity.this, classOf[PairingActivity])
    )
  }

  private def setNavButtonVisiblity() {
    // Left nav handling
    if (viewFlipper.getDisplayedChild == 0)
      setLeftNavVisibility(View.INVISIBLE)
    else
      setLeftNavVisibility(View.VISIBLE)

    // Right nav handling
    if (viewFlipper.getDisplayedChild == viewFlipper.getChildCount - 1) {
      setRightNavVisibility(View.INVISIBLE)
      finishInstructionsButton.setVisibility(View.VISIBLE)
    } else {
      setRightNavVisibility(View.VISIBLE)
    }
  }

  def goBack(view : View) {
    if (activeAnimationCounter == 0 && viewFlipper.getDisplayedChild > 0) {
      viewFlipper.setInAnimation(inFromLeftAnim)
      viewFlipper.setOutAnimation(outToRightAnim)

      viewFlipper.showPrevious()

      setNavButtonVisiblity()
    }
  }

  def goForward(view : View) {
    if (activeAnimationCounter == 0 && viewFlipper.getDisplayedChild < viewFlipper.getChildCount - 1) {
      viewFlipper.setInAnimation(inFromRightAnim)
      viewFlipper.setOutAnimation(outToLeftAnim)

      viewFlipper.showNext()

      setNavButtonVisiblity()
    }
  }
}
