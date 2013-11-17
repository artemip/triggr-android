package co.fether.triggrtrial

import android.content.Context
import android.provider.Settings.Secure
import android.telephony.TelephonyManager
import java.io.UnsupportedEncodingException
import java.util.UUID
import android.util.Log
import android.app.Activity
import android.preference.PreferenceManager
import java.util.Date

object Preferences {
  val PREF_FILE = "preferences.xml"
  val PREF_DEVICE_ID = "device_id"
  val PREF_CONNECTED_DEVICE_ID = "connected_device_id"
  val PREF_WAS_PREVIOUSLY_PAIRED = "was_previously_paired"
  val PREF_INSTALLATION_DATE = "installation_date"
  var NUM_TRIAL_DAYS = -1

  val PREF_CALL_NOTIFICATIONS = "pref_call_notifications"
  val PREF_SMS_NOTIFICATIONS = "pref_sms_notifications"
  val PREF_WHATSAPP_NOTIFICATIONS = "pref_whatsapp_notifications"
  val PREF_SNAPCHAT_NOTIFICATIONS = "pref_snapchat_notifications"
  val PREF_SMART_VOLUME = "pref_smart_volume"
  val PREF_NOISE_ALERT = "pref_noise_alert"

  private var service : Option[TriggrService] = None
  private var activity : Option[Activity] = None
  private var connectedDeviceId : Option[String] = None
  private var wasPreviouslyPaired : Option[Boolean] = None

  @volatile private var uuid : Option[UUID] = None

  private def getContext : Option[Context] = {
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
    getContext match {
      case Some(c) => {
        val prefs = c.getSharedPreferences( PREF_FILE, 0 )

        // Write the value out to the prefs file
        prefs.edit().putString( key, value ).commit()
      }
      case None =>
    }
  }

  private def removePreference (key : String ) {
    getContext match {
      case Some( c ) => {
        val prefs = c.getSharedPreferences( PREF_FILE, 0 )

        // Remove the value from the prefs file
        prefs.edit().remove( key ).commit()
      }
      case None =>
    }
  }

  private def getPreference( key : String ) : Option[String] = {
    getContext match {
      case Some( c ) => {
        val prefs = c.getSharedPreferences( PREF_FILE, 0 )
        val pref = prefs.getString( key, null )

        if ( pref == null ) None else Some( pref )
      }
      case None => None
    }
  }

  private def getBooleanSharedPreference( key : String ) : Boolean = {
    getContext match {
      case Some(c) => {
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(c)
        sharedPrefs.getBoolean(key, false)
      }
      case None => false
    }
  }

  def setService( c : TriggrService ) = {
    service = Some( c )
  }

  def getService : Option[TriggrService] = {
    service
  }

  def setMainActivity( c : Activity ) = {
    activity = Some( c )
  }

  def getMainActivity : Option[Activity] = {
    activity
  }

  def setConnectedDeviceId( deviceId : Option[String] ) {
    connectedDeviceId = deviceId
    deviceId match {
      case Some(s) => {
    	  setPreference( PREF_CONNECTED_DEVICE_ID, s )
      }
      case None => removePreference( PREF_CONNECTED_DEVICE_ID )
    }
  }

  def getConnectedDeviceId : Option[String] = {
    if ( connectedDeviceId.isEmpty ) {
      connectedDeviceId = getPreference( PREF_CONNECTED_DEVICE_ID )
    }

    connectedDeviceId
  }

  def getWasPreviouslyPaired : Boolean = {
    if (wasPreviouslyPaired.isEmpty) {
      wasPreviouslyPaired = Some(getPreference(PREF_WAS_PREVIOUSLY_PAIRED).isDefined)
    }

    wasPreviouslyPaired.get
  }

  def setWasPreviouslyPaired( flag : Boolean ) = {
    if (flag) setPreference( PREF_WAS_PREVIOUSLY_PAIRED, "yes" )

    wasPreviouslyPaired = Some(flag)
  }

  def getPhoneCallNotificationsEnabled : Boolean = {
    getBooleanSharedPreference(PREF_CALL_NOTIFICATIONS)
  }

  def getSMSNotificationsEnabled : Boolean = {
    getBooleanSharedPreference(PREF_SMS_NOTIFICATIONS)
  }
  def getSmartVolumeEnabled : Boolean = {
    getBooleanSharedPreference(PREF_SMART_VOLUME)
  }
  def getNoiseAlertEnabled : Boolean = {
    getBooleanSharedPreference(PREF_NOISE_ALERT)
  }

  def getWhatsAppNotificationsEnabled : Boolean = {
    getBooleanSharedPreference(PREF_WHATSAPP_NOTIFICATIONS)
  }

  def getSnapChatNotificationsEnabled : Boolean = {
    getBooleanSharedPreference(PREF_SNAPCHAT_NOTIFICATIONS)
  }

  def getInstalledDate : Long = {
    getContext match {
      case Some( c ) => {
        c.getPackageManager()
          .getPackageInfo("co.fether.triggrtrial", 0)
          .firstInstallTime
      }
      case None => Long.MinValue
    }
  }

  def isTrialActive : Boolean = {
    val DAY_IN_MS = 1000 * 60 * 60 * 24

    val now  = new Date()
    val diffInDays = (now.getTime - getInstalledDate) / DAY_IN_MS
    val active = diffInDays < NUM_TRIAL_DAYS

    Log.i( "TriggrApp", "Application has been installed for " + diffInDays + " days. Status: " + ( if (active) "Active" else "Inactive" ))

    active
  }

  def getDeviceId() : UUID = {
    uuid match {
      case Some( u ) => {
        u
      }
      case None => {
        service match {
          case Some( c ) => {
            val prefs = c.getSharedPreferences( PREF_FILE, 0 )
            val id = prefs.getString( PREF_DEVICE_ID, null )
            if ( id != null ) {

              // Use the ids previously computed and stored in the prefs file
              uuid = Some( UUID.fromString( id ) )
            } else {
              val androidId = Secure.getString( c.getContentResolver, Secure.ANDROID_ID )

              // Use the Android ID unless it's broken, in which case fallback on deviceId,
              // unless it's not available, then fallback on a random number which we store
              // to a prefs file
              try {
                if ( !"9774d56d682e549c".equals( androidId ) ) {
                  uuid = Some( UUID.nameUUIDFromBytes( androidId.getBytes( "utf8" ) ) )
                } else {
                  val deviceId = c.getSystemService( Context.TELEPHONY_SERVICE ).asInstanceOf[TelephonyManager].getDeviceId

                  uuid = Some( if ( deviceId != null ) UUID.nameUUIDFromBytes( deviceId.getBytes( "utf8" ) ) else UUID.randomUUID() )
                }
              } catch {
                case ( e : UnsupportedEncodingException ) => throw new RuntimeException( e )
              }

              // Write the value out to the prefs file
              prefs.edit().putString( PREF_DEVICE_ID, uuid.get.toString ).commit()
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