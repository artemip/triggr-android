package co.fether.triggr

import android.content.Context
import android.provider.Settings.Secure
import android.telephony.TelephonyManager
import java.io.UnsupportedEncodingException
import java.util.UUID
import android.util.Log
import android.app.Activity
import android.view.inputmethod.InputMethodManager

object Preferences {
  private val PREFS_FILE = "device_id.xml"
  private val PREFS_DEVICE_ID = "device_id"
  private val PREFS_PAIRED_DEVICE_ID = "connected_device_id"

  private var service : Option[TriggrService] = None
  private var activity : Option[Activity] = None
  private var pairedDeviceId : Option[String] = None

  @volatile private var uuid : Option[UUID] = None

  private def getContext() : Option[Context] = {
    service match {
      case Some( s ) => Some(s)
      case None => {
        activity match {
          case Some( a ) => Some(a)
          case None => {
            Log.e( "TriggrApp", "Cannot retrieve app context from service or activity" )

            None
          }
        }
      }
    }
  }

  private def setPreference( key : String, value : String ) {
    getContext() match {
      case Some(c) => {
        val prefs = c.getSharedPreferences( PREFS_FILE, 0 )

        // Write the value out to the prefs file
        prefs.edit().putString( key, value ).commit()
      }
      case None =>
    }
  }

  private def removePreference (key : String ) {
    getContext() match {
      case Some( c ) => {
        val prefs = c.getSharedPreferences( PREFS_FILE, 0 )

        // Remove the value from the prefs file
        prefs.edit().remove( key ).commit()
      }
      case None =>
    }
  }

  private def getPreference( key : String ) : Option[String] = {
    getContext() match {
      case Some( c ) => {
        val prefs = c.getSharedPreferences( PREFS_FILE, 0 )
        val p = prefs.getString( key, null )

        if ( p == null ) None else Some( p )
      }
      case None => None
    }
  }

  def setService( c : TriggrService ) = {
    service = Some( c )
  }

  def getService() : Option[TriggrService] = {
    service
  }

  def setMainActivity( c : Activity ) = {
    activity = Some( c )
  }

  def getMainActivity() : Option[Activity] = {
    activity
  }

  def setPairedDeviceId( deviceId : Option[String] ) {
    pairedDeviceId = deviceId
    deviceId match {
      case Some(s) => {
    	  setPreference( PREFS_PAIRED_DEVICE_ID, s )
    	  activity match {
    	    case Some(a : PairingActivity) => a.runOnUiThread(new Runnable() {
		      override def run() {
		        a.pairKeyTextBox.clearFocus()
		        a.viewSwitcher.showNext()
		        val imm = a.getSystemService(Context.INPUT_METHOD_SERVICE).asInstanceOf[InputMethodManager]
		        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
		      }
		    })
    	    case _ =>
    	  }
      }
      case None => removePreference( PREFS_PAIRED_DEVICE_ID )
    }
  }

  def getPairedDeviceId() : Option[String] = {
    if ( pairedDeviceId.isEmpty ) {
      pairedDeviceId = getPreference( PREFS_PAIRED_DEVICE_ID )
    }

    pairedDeviceId
  }

  def getDeviceId() : UUID = {
    uuid match {
      case Some( u ) => {
        u
      }
      case None => {
        service match {
          case Some( c ) => {
            val prefs = c.getSharedPreferences( PREFS_FILE, 0 )
            val id = prefs.getString( PREFS_DEVICE_ID, null )
            if ( id != null ) {

              // Use the ids previously computed and stored in the prefs file
              uuid = Some( UUID.fromString( id ) )
            } else {
              val androidId = Secure.getString( c.getContentResolver(), Secure.ANDROID_ID )

              // Use the Android ID unless it's broken, in which case fallback on deviceId,
              // unless it's not available, then fallback on a random number which we store
              // to a prefs file
              try {
                if ( !"9774d56d682e549c".equals( androidId ) ) {
                  uuid = Some( UUID.nameUUIDFromBytes( androidId.getBytes( "utf8" ) ) )
                } else {
                  val deviceId = c.getSystemService( Context.TELEPHONY_SERVICE ).asInstanceOf[TelephonyManager].getDeviceId()

                  uuid = Some( if ( deviceId != null ) UUID.nameUUIDFromBytes( deviceId.getBytes( "utf8" ) ) else UUID.randomUUID() )
                }
              } catch {
                case ( e : UnsupportedEncodingException ) => throw new RuntimeException( e )
              }

              // Write the value out to the prefs file
              prefs.edit().putString( PREFS_DEVICE_ID, uuid.get.toString() ).commit()
            }
          }
          case None => {
            Log.e( "TriggrApp", "Attempt to retrieve UUID without providing AppContext" )
          }
        }

        uuid.getOrElse( UUID.randomUUID() )
      }
    }
  }
}