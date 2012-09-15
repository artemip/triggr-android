package apasyech.cbridge
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.telephony.PhoneStateListener
import android.util.Log

class PhoneCallBroadcastReceiver extends BroadcastReceiver {
  val uuid = java.util.UUID.randomUUID
  
  override def onReceive(context : Context, intent : Intent) {
	  //Handle shit if listener is inactive
	  if(!PhoneCallStateListener.isListening) {
	    val phoneManager = context.getSystemService(Context.TELEPHONY_SERVICE).asInstanceOf[TelephonyManager]
	    val listener = PhoneCallStateListener()
	    
	    phoneManager.listen(listener, PhoneStateListener.LISTEN_CALL_STATE)
	  }
  }
  
  /*
	protected AudioManager audioManager;// = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
    protected Context context;
    private ITelephony telephonyService;
    @Override
     public void onReceive(Context context, Intent intent) {
      audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
      this.context = context;
      String action = intent.getAction();
             if(action.equalsIgnoreCase("android.intent.action.PHONE_STATE")){
              if (intent.getStringExtra(TelephonyManager.EXTRA_STATE).equals(
                                  TelephonyManager.EXTRA_STATE_RINGING)) {
                  //Incoming call 
               doSomething(intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER));
              }         
             }
             else {

             Bundle bundle = intent.getExtras();
             Object[] pdus = (Object[]) bundle.get("pdus");
             SmsMessage message = SmsMessage.createFromPdu((byte[])pdus[0]);
             if(!message.isEmail())
                 doSomething(message.getOriginatingAddress());

             }

     }
     */
}