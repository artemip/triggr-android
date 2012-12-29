package apasyech.cbridge
import android.os.Binder
import android.content.Intent
import android.util.Log
import android.os.IBinder
import android.widget.Toast
import android.os.Handler
import android.app.Service
import android.os.Looper

object cBridgeService {
  val tag = classOf[cBridgeService].getName()
  
  //Return  an instance of the service (not doing IPC)
  class cBridgeServiceBinder(service : cBridgeService) extends Binder {  
	def getService() : cBridgeService = service
  }
}

class cBridgeService extends Service {
  val binder = new cBridgeService.cBridgeServiceBinder(this)
  var handler : Handler = null;
  
  override def onCreate() {
    Preferences.setService(this)
    
    ServerActor.start()
    HeartbeatGenerator.start()
  }
  
  override def onStartCommand(intent : Intent, flags : Int, startId : Int) : Int = {
    Log.i(cBridgeService.tag, "Started service.")
    handler = new Handler(Looper.getMainLooper())
    return Service.START_STICKY
  }
  
  override def onBind(intent : Intent) : IBinder = {
    binder
  }
  
  //Show toast on UI thread
  def showToast(text : String, duration : Int) {
    val ctx = getApplicationContext()
    
    handler.post(new Runnable() {
      override def run() {
        Toast.makeText(ctx, text, duration).show()
      }
    })
  }
}