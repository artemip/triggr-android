package co.fether.triggr
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.os.AsyncTask
import android.os.StrictMode
import android.util.Log
import android.content.ServiceConnection
import android.content.ComponentName
import android.os.IBinder
import android.net.Uri
import android.provider.ContactsContract.PhoneLookup
import android.content.ContentResolver
import android.provider.ContactsContract

object PhoneCallStateListener {
  private var tag = classOf[PhoneCallStateListener].getName
  private var listener : Option[PhoneCallStateListener] = None

  def apply() : PhoneCallStateListener =
    listener match {
      case Some( x ) => x
      case None => {
        val newListener = new PhoneCallStateListener
        listener = Some( newListener )

        newListener
      }
    }

  def isListening = {
    listener.isDefined
  }
}

class PhoneCallStateListener extends PhoneStateListener {
  private var lastState = -1

  override def onCallStateChanged( state : Int, incomingNumber : String ) {
    state match {
      case TelephonyManager.CALL_STATE_RINGING => {
        //Phone is ringing (Incoming call)
        Log.d( PhoneCallStateListener.tag, "Receiving phone call: " + incomingNumber )
        lastState = TelephonyManager.CALL_STATE_RINGING

        val uri = Uri.withAppendedPath( PhoneLookup.CONTENT_FILTER_URI, Uri.encode( incomingNumber ) )
        var callerName = {
          Preferences.getService() match {
            case Some( s ) => {
              val resolver = s.getContentResolver()
              val cursor = resolver.query( uri, Array( "display_name" ), null, null, null )

              if ( cursor.moveToFirst() ) {
                cursor.getString( cursor.getColumnIndex( "display_name" ) );
              } else {
                "Unknown Caller"
              }
            }
            case None => {
              "Unknown Caller"
            }
          }
        }
        
        Preferences.getPairedDeviceId() match {
          case Some( s ) => ServerActor ! ServerActor.IncomingCall( s, incomingNumber, callerName )
          case None => Log.w( PhoneCallStateListener.tag, "No paired device" )
        }
      }
      case TelephonyManager.CALL_STATE_OFFHOOK => {
        //Picking up an incoming call?
        if ( lastState == TelephonyManager.CALL_STATE_RINGING ) {
          lastState = TelephonyManager.CALL_STATE_OFFHOOK
          return
        }

        //Outgoing call
        Log.d( PhoneCallStateListener.tag, "Outgoing phone call: " + incomingNumber )
        lastState = TelephonyManager.CALL_STATE_OFFHOOK

        Preferences.getPairedDeviceId() match {
          case Some( s ) => ServerActor ! ServerActor.OutgoingCall( s )
          case None => Log.w( PhoneCallStateListener.tag, "No paired device" )
        }
      }
      case TelephonyManager.CALL_STATE_IDLE => {
        //Phone call ended
        Log.d( PhoneCallStateListener.tag, "Ended phone call: " + incomingNumber )
        lastState = TelephonyManager.CALL_STATE_IDLE

        Preferences.getPairedDeviceId() match {
          case Some( s ) => ServerActor ! ServerActor.EndCall( s )
          case None => Log.w( PhoneCallStateListener.tag, "No paired device" )
        }
      }
      case _ => {
        Log.d( PhoneCallStateListener.tag, "callStateChanged: " + state )
      }
    }
  }
}