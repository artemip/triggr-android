package co.fether.triggr
import android.os.Binder
import android.content.Intent
import android.util.Log
import android.os.IBinder
import android.widget.Toast
import android.os.Handler
import android.app.Service
import android.os.Looper

object TriggrService {
  val tag = classOf[TriggrService].getName()

  //Return  an instance of the service (not doing IPC)
  class TriggrServiceBinder( service : TriggrService ) extends Binder {
    def getService() : TriggrService = service
  }
}

class TriggrService extends Service {
  val binder = new TriggrService.TriggrServiceBinder( this )
  var handler : Handler = null;

  override def onCreate() {
    Preferences.setService( this )

    ServerActor.start()
    HeartbeatGenerator.start()
  }

  override def onStartCommand( intent : Intent, flags : Int, startId : Int ) : Int = {
    Log.i( TriggrService.tag, "Started service." )
    handler = new Handler( Looper.getMainLooper() )
    return Service.START_STICKY
  }

  override def onBind( intent : Intent ) : IBinder = {
    binder
  }

  //Show toast on UI thread
  def showToast( text : String, duration : Int ) {
    val ctx = getApplicationContext()

    handler.post( new Runnable() {
      override def run() {
        Toast.makeText( ctx, text, duration ).show()
      }
    } )
  }
}