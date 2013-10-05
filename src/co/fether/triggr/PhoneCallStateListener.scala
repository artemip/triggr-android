package co.fether.triggr
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import android.net.Uri
import android.provider.ContactsContract.PhoneLookup

object PhoneCallStateListener {
  private val tag = PhoneCallStateListener.getClass.getName
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
      case TelephonyManager.CALL_STATE_RINGING => {
        //Phone is ringing (Incoming call)
        Log.d( PhoneCallStateListener.tag, "Receiving phone call: " + incomingNumber )
        lastState = TelephonyManager.CALL_STATE_RINGING

        val callerName = Util.getCallerName(incomingNumber.trim).trim

        EventActor ! EventActor.IncomingCall( incomingNumber.trim, callerName.trim )
      }
      case TelephonyManager.CALL_STATE_OFFHOOK => {
        //Picking up an incoming call?
        if ( lastState == TelephonyManager.CALL_STATE_RINGING ) {
          lastState = TelephonyManager.CALL_STATE_OFFHOOK
          return
        }
        
        val outgoingNumber = OutgoingCallReceiver.outgoingNumber.getOrElse("").trim
        val callerName = Util.getCallerName(outgoingNumber)

        //Outgoing call
        Log.d( PhoneCallStateListener.tag, "Outgoing phone call: " + outgoingNumber )
        lastState = TelephonyManager.CALL_STATE_OFFHOOK

        EventActor ! EventActor.OutgoingCall( outgoingNumber.trim, callerName.trim )

        OutgoingCallReceiver.outgoingNumber = None
      }
      case TelephonyManager.CALL_STATE_IDLE => {
        //Phone call ended
        Log.d( PhoneCallStateListener.tag, "Ended phone call: " + incomingNumber )
        lastState = TelephonyManager.CALL_STATE_IDLE

        EventActor ! EventActor.EndCall
      }
      case _ => {
        Log.d( PhoneCallStateListener.tag, "callStateChanged: " + state )
      }
    }
  }
}