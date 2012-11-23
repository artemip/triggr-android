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
  var jmdns : Option[JmDNS] = None
  lock.setReferenceCounted( true )

  def act() {
    loop {
      receive {
        case NetworkDiscoveryActor.StartDiscovery => {
          jmdns match {
            case None => {
              val dns = JmDNS.create()
              jmdns = Some( dns )

              lock.acquire

              Log.d( NetworkDiscoveryActor.tag, "Started" )

              dns.addServiceListener( NetworkDiscoveryActor.cbridgeServiceType, new ServiceListener() {
                override def serviceResolved( ev : ServiceEvent ) {
                  val serviceInfo = ev.getInfo 
                  val serviceName = serviceInfo.getQualifiedName()
                  val serviceAddress = serviceInfo.getHostAddress()
                  val servicePort = serviceInfo.getPort()
                  
                  Log.d( NetworkDiscoveryActor.tag, "Service resolved: " + serviceName + " port:" + servicePort + " address:" + serviceAddress )
                  
                  if(serviceName.startsWith(NetworkDiscoveryActor.cbridgeServiceName) == 0) {
                	//Add the stuff
                    //ServerTools.addServer(serviceInfo)
                  }	                                
                }

                override def serviceRemoved( ev : ServiceEvent ) {
                  Log.d( NetworkDiscoveryActor.tag, "Service removed: " + ev.getName() )
                }

                override def serviceAdded( event : ServiceEvent ) {
                  // Required to force serviceResolved to be called again (after the first search)
                  dns.requestServiceInfo( event.getType(), event.getName(), 1 )
                }
              })
            }
            case Some( j ) => {
            	Log.w(NetworkDiscoveryActor.tag, "StartDiscovery message sent called multiple times before StopDiscovery was called")
            }
          }
        }
        case NetworkDiscoveryActor.StopDiscovery => {
          jmdns match {
            case Some(j) => {
            	j.unregisterAllServices()
            	j.close()
            	lock.release()
            }
            case None => {
              Log.w(NetworkDiscoveryActor.tag, "StopDiscovery was called before StartDiscovery ")
            }
          }  
         }
      }
    }
  }
}