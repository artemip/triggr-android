package apasyech.cbridge

import android.net.wifi.WifiManager
import android.net.wifi.WifiManager._
import android.util.Log
import android.content.Context
import java.net.InetAddress
import javax.jmdns.JmDNS
import javax.jmdns.ServiceEvent
import javax.jmdns.ServiceInfo
import javax.jmdns.ServiceListener
import scala.actors.Actor

object NetworkDiscoveryActor {
  val tag = classOf[NetworkDiscoveryActor].getName
  val cbridgeServiceType = "_http._tcp.local."
  val cbridgeServiceName = "cbridgedesktop"

  case object StartDiscovery
  case object StopDiscovery
}

class NetworkDiscoveryActor( appContext : Context ) extends Actor {
  val wifi : WifiManager = appContext.getSystemService( android.content.Context.WIFI_SERVICE ).asInstanceOf[WifiManager]
  val handler = new android.os.Handler()
  val lock = wifi.createMulticastLock( "cbridgeNetworkLock" )
  var jmdns : JmDNS = null
  lock.setReferenceCounted( true )

  def act() {
    jmdns = JmDNS.create()
    
    loop {
      receive {
        case NetworkDiscoveryActor.StartDiscovery => {
          lock.acquire

          Log.d( NetworkDiscoveryActor.tag, "Started" )

          jmdns.addServiceListener( NetworkDiscoveryActor.cbridgeServiceType, new ServiceListener() {
            override def serviceResolved( ev : ServiceEvent ) {
              Log.d( NetworkDiscoveryActor.tag, "Service resolved: " + ev.getInfo().getQualifiedName() + " port:" + ev.getInfo().getPort() )
            }

            override def serviceRemoved( ev : ServiceEvent ) {
              Log.d( NetworkDiscoveryActor.tag, "Service removed: " + ev.getName() )
            }

            override def serviceAdded( event : ServiceEvent ) {
              // Required to force serviceResolved to be called again (after the first search)
              jmdns.requestServiceInfo( event.getType(), event.getName(), 1 )
            }
          });
        }
        case NetworkDiscoveryActor.StopDiscovery => {
          jmdns.unregisterAllServices()
          jmdns.close()
          lock.release()
        }
      }
    }
  }
}