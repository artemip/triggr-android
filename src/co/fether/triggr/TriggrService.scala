package co.fether.triggr
import android.os._
import android.content.{ComponentName, ServiceConnection, Context, Intent}
import android.util.Log
import android.widget.Toast
import android.app.{NotificationManager, PendingIntent, Service}
import android.support.v4.app.NotificationCompat
import com.android.vending.billing.IInAppBillingService
import co.fether.triggr.json.InAppProductInfo
import java.util
import scala.collection.JavaConversions._

object TriggrService {
  val tag = classOf[TriggrService].getName

  val PROD_ID_WHATSAPP_NOTIFICATIONS = "notifications_whatsapp"
  val PROD_ID_SNAPCHAT_NOTIFICATIONS = "notifications_snapchat"

  //Return  an instance of the service (not doing IPC)
  class TriggrServiceBinder( service : TriggrService ) extends Binder {
    def getService : TriggrService = service
  }

  private var billingService : IInAppBillingService = null
  private var billingServiceConnection : ServiceConnection = null

  def getProductInfo(productIds : List[String]) : List[InAppProductInfo] = {
    val querySkus = new Bundle()
    val productIdArrayList = new util.ArrayList[String]()
    productIdArrayList.addAll(productIds)

    querySkus.putStringArrayList("ITEM_ID_LIST", productIdArrayList)

    if (billingService != null) {
      val skuDetails = billingService.getSkuDetails(3, "co.fether.triggr", "inapp", querySkus)

      if(skuDetails.getInt("RESPONSE_CODE") == 0) {
        val details = skuDetails.getStringArrayList("DETAILS_LIST")
        var detList = List[InAppProductInfo]()

        // There are better ways to do this, but this works.
        for(i <- 0 until details.length) {
          val d = details.get(i)
          val inf = new InAppProductInfo().deserialize(d)
          detList = inf :: detList
        }

        return detList
      }
    }

    List()
  }

  def getPurchasedProducts : List[String] = {
    if(billingService != null) {
      val purchasedItems = billingService.getPurchases(3, "co.fether.triggr", "inapp", null)

      if(purchasedItems.getInt("RESPONSE_CODE") == 0) {
        purchasedItems.getStringArrayList("INAPP_PURCHASE_ITEM_LIST").toList
      }
    }

    List()
  }

  def getPurchaseIntent(productId : String) : Option[PendingIntent] = {
    val buyIntentBundle = billingService.getBuyIntent(3, "co.fether.triggr", productId, "inapp", null)

    if(buyIntentBundle.getInt("RESPONSE_CODE") == 0) {
      Some(buyIntentBundle.getParcelable("BUY_INTENT"))
    } else {
      None
    }
  }
}

class TriggrService extends Service {
  val binder = new TriggrService.TriggrServiceBinder( this )
  var handler : Handler = null


  override def onCreate() {
    Preferences.setService( this )

    TriggrService.billingServiceConnection = new ServiceConnection() {
      def onServiceDisconnected(name: ComponentName) {
        TriggrService.billingService = null
      }

      def onServiceConnected(name: ComponentName, service: IBinder) {
        TriggrService.billingService = IInAppBillingService.Stub.asInterface(service)
      }
    }

    bindService(
      new Intent("com.android.vending.billing.InAppBillingService.BIND"),
      TriggrService.billingServiceConnection,
      Context.BIND_AUTO_CREATE
    )

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

  override def onDestroy() {
    super.onDestroy()
    if (TriggrService.billingServiceConnection != null) {
      unbindService(TriggrService.billingServiceConnection)
    }
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

  def createNotification( title : String, subtext : String ) {
    val ctx = getApplicationContext
    val intent = new Intent(ctx, classOf[PairingActivity])
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