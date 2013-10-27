package co.fether.triggr

import android.content.{BroadcastReceiver, Context, Intent}
import android.telephony.SmsMessage

//TODO: Does this get started if we never open the activity?
class SMSBroadcastReceiver extends BroadcastReceiver {

  override def onReceive(context : Context, intent : Intent) {
    val extras = intent.getExtras

    if( intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED") && extras != null) {
      val smsExtras = extras.get( "pdus" ).asInstanceOf[Array[Object]]

      for(e <- smsExtras) {
        val msg = SmsMessage.createFromPdu(e.asInstanceOf[Array[Byte]])
        val number = msg.getOriginatingAddress.trim
        val name = Util.getCallerName(number.trim)
        val message = msg.getMessageBody.trim

        EventActor ! EventActor.SMSMessage(number, name, message)
      }
    }
  }

}