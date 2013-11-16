package co.fether.triggr
import android.telephony.{PhoneNumberUtils, PhoneStateListener, TelephonyManager}
import android.util.Log
import android.net.Uri
import android.provider.ContactsContract.PhoneLookup

object PhoneCallStateListener {
  private val tag = PhoneCallStateListener.getClass.getCanonicalName
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

/**
 * Listen for call events and react appropriately
 */
class PhoneCallStateListener extends PhoneStateListener {
  private var lastState = -1

  override def onCallStateChanged( state : Int, incomingNumber : String ) {
    state match {
      /**
       * Incoming Call
       */
      case TelephonyManager.CALL_STATE_RINGING => {
        lastState = state
        Log.d( PhoneCallStateListener.tag, "Incoming phone call: " + incomingNumber )

        val callerName = Util.getCallerName(incomingNumber.trim)

        EventActor ! EventActor.IncomingCall( PhoneNumberUtils.formatNumber( incomingNumber ).trim, callerName.trim )
      }

      /**
       * Outgoing call, or pick-up event
       */
      case TelephonyManager.CALL_STATE_OFFHOOK => {
        if ( lastState == TelephonyManager.CALL_STATE_RINGING ) {
          lastState = state
          return
        }
        
        val outgoingNumber = OutgoingCallReceiver.outgoingNumber.getOrElse("").trim
        val callerName = Util.getCallerName(outgoingNumber)

        Log.d( PhoneCallStateListener.tag, "Outgoing phone call: " + outgoingNumber )
        lastState = TelephonyManager.CALL_STATE_OFFHOOK

        EventActor ! EventActor.OutgoingCall( PhoneNumberUtils.formatNumber( outgoingNumber ).trim, callerName.trim )

        OutgoingCallReceiver.outgoingNumber = None
      }

      /**
       * Ended call, or missed call
       */
      case TelephonyManager.CALL_STATE_IDLE => {
        if(lastState == TelephonyManager.CALL_STATE_RINGING) {
          Log.d( PhoneCallStateListener.tag, "Missed phone call: " + incomingNumber )

          val callerName = Util.getCallerName(incomingNumber)

          EventActor ! EventActor.MissedCall(PhoneNumberUtils.formatNumber( incomingNumber ).trim, callerName.trim)
        } else {
          Log.d( PhoneCallStateListener.tag, "Ended phone call: " + incomingNumber )
          EventActor ! EventActor.EndCall
        }

        lastState = state
      }
      case _ => {
        Log.d( PhoneCallStateListener.tag, "callStateChanged: " + state )
      }
    }
  }
}