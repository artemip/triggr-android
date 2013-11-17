package co.fether.triggrtrial
import android.os._
import android.content.{Context, Intent}
import android.util.Log
import android.widget.Toast
import android.app.{NotificationManager, PendingIntent, Service}
import android.support.v4.app.NotificationCompat

object TriggrService {
  val tag = classOf[TriggrService].getName

  val PROD_ID_WHATSAPP_NOTIFICATIONS = "notifications_whatsapp"
  val PROD_ID_SNAPCHAT_NOTIFICATIONS = "notifications_snapchat"

  //Return  an instance of the service (not doing IPC)
  class TriggrServiceBinder( service : TriggrService ) extends Binder {
    def getService : TriggrService = service
  }
}

class TriggrService extends Service {
  val binder = new TriggrService.TriggrServiceBinder( this )
  var handler : Handler = null


  override def onCreate() {
    Preferences.setService( this )

    EventActor.start()
  }

  override def onStartCommand( intent : Intent, flags : Int, startId : Int ) : Int = {
    Log.i( TriggrService.tag, "Started service." )
    handler = new Handler( Looper.getMainLooper )

    Service.START_STICKY
  }

  override def onBind( intent : Intent ) : IBinder = {
    binder
  }

  /**
   * Show toast on UI thread
   * @param text message to show
   * @param duration duration for which toast will remain on the screen
   */
  def showToast( text : String, duration : Int ) {
    val ctx = getApplicationContext

    handler.post(
      new Runnable() {
        override def run() {
          Toast.makeText( ctx, "Triggr: " + text, duration ).show()
        }
      }
    )
  }

  def createNotification( title : String, subtext : String, intent: Intent ) {
    val ctx = getApplicationContext
    val pIntent = PendingIntent.getActivity(ctx, 0, intent, 0)

    val notificationManager = ctx.getSystemService(Context.NOTIFICATION_SERVICE).asInstanceOf[NotificationManager]
    val notification = new NotificationCompat.Builder(ctx)
                            .setSmallIcon(R.drawable.notification_icon)
                            .setContentTitle(title)
                            .setContentText(subtext)
                            .setContentIntent(pIntent)
                            .setAutoCancel(true)
                            .build()

    notificationManager.notify(0, notification)
  }
}