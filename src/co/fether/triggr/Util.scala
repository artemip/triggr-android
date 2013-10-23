package co.fether.triggr

import android.util.Log
import android.net.Uri
import android.provider.ContactsContract.PhoneLookup
import android.content.Context
import android.view.{ViewGroup, View}
import android.widget.TextView
import android.graphics.Typeface
import android.view.inputmethod.InputMethodManager

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

  /**
   * Attempt to retrieve the name of a contact, given the phone number.
   *
   * @param number the phone number of the contact
   * @return
   */
  def getCallerName( number : String ) = {
    val uri = Uri.withAppendedPath( PhoneLookup.CONTENT_FILTER_URI, Uri.encode( number ) )

    val defaultCallerName = "Unknown Contact"

    Preferences.getService() match {
      case Some( s ) => {
        val resolver = s.getContentResolver
        val cursor = resolver.query( uri, Array( "display_name" ), null, null, null )

        if ( cursor.moveToFirst() ) {
          cursor.getString( cursor.getColumnIndex( "display_name" ) )
        } else {
          defaultCallerName
        }
      }
      case None => {
        defaultCallerName
      }
    }
  }

  def overrideFonts(context : Context, view : View, typeface : Typeface) {
    if (view == null)
      return

    try {
      view match {
        case vg : ViewGroup => {
          for(i <- 0 until vg.getChildCount) {
            overrideFonts(context, vg.getChildAt(i), typeface)
          }
        }
        case tv : TextView => {
          tv.setTypeface(typeface)
        }
      }
    } catch {
      case e : Exception => {
        //No-op
      }
    }
  }

  def hideKeyboard(context : Context) {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE).asInstanceOf[InputMethodManager]
    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
  }
}
