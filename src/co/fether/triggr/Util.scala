package co.fether.triggr

import android.util.Log

object Util {
  val tag = Util.getClass.getName

  def showToast(text : String, duration : Int) {
    Preferences.getService() match {
      case Some( s ) => s.showToast(text, duration)
      case None => {
        Log.d( tag, "No Triggr service found. Unable to display Toasts" )
      }
    }
  }
}
