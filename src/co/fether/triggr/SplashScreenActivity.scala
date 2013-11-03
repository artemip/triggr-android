package co.fether.triggr

import android.app.Activity
import android.os.{Handler, Bundle}
import android.content.Intent

class SplashScreenActivity extends Activity{
  val SPLASH_SCREEN_TIME = 2500

  override def onCreate( savedInstanceState : Bundle ) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.splash_screen_activity)

    SplashScreenActivity.this.startService(
      new Intent( SplashScreenActivity.this, classOf[TriggrService] )
    )

    new Handler().postDelayed(new Runnable() {
      override def run() {
        // Skip instructions if user has paired before
        SplashScreenActivity.this.startActivity(
          new Intent(SplashScreenActivity.this,
            if (!Preferences.getWasPreviouslyPaired)
              classOf[PairingActivity]
            else
              classOf[InstructionsActivity]
          )
        )
        SplashScreenActivity.this.finish()

        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
      }
    }, SPLASH_SCREEN_TIME)

  }
}
