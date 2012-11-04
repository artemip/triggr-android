package apasyech.cbridge

import android.net.nsd._
import android.util.Log
import android.content.Context
import java.net.InetAddress

object NetworkDiscoveryListener {
	val tag = classOf[NetworkDiscoveryListener].getName
	val cbridgeServiceType = "_http._tcp."
	var cbridgeServiceName = "cbridgeService"
	var cbridgePort = 1234
	var cbridgeHost : InetAddress = null
}

class NetworkDiscoveryListener(appContext : Context) extends NsdManager.DiscoveryListener {
	val registrationListener = new NsdManager.RegistrationListener() {
        override def onServiceRegistered(NsdServiceInfo : NsdServiceInfo) {
            // Save the service name.  Android may have changed it in order to
            // resolve a conflict, so update the name you initially requested
            // with the name Android actually used.
            NetworkDiscoveryListener.cbridgeServiceName = NsdServiceInfo.getServiceName()
        }

        override def onRegistrationFailed(serviceInfo : NsdServiceInfo, errorCode : Int) {
            // Registration failed!  Put debugging code here to determine why.
        }

        override def onServiceUnregistered(arg0 : NsdServiceInfo) {
            // Service has been unregistered.  This only happens when you call
            // NsdManager.unregisterService() and pass in this listener.
        }

        override def onUnregistrationFailed(serviceInfo : NsdServiceInfo, errorCode : Int) {
            // Unregistration failed.  Put debugging code here to determine why.
        }
    }

	val resolveListener = new NsdManager.ResolveListener() {
        override def onResolveFailed(serviceInfo : NsdServiceInfo, errorCode : Int) {
            // Called when the resolve fails.  Use the error code to debug.
            Log.e(NetworkDiscoveryListener.tag, "Resolve failed" + errorCode);
        }

        override def onServiceResolved(serviceInfo : NsdServiceInfo) {
            Log.e(NetworkDiscoveryListener.tag, "Resolve Succeeded. " + serviceInfo);

            if (serviceInfo.getServiceName().equals(NetworkDiscoveryListener.cbridgeServiceName)) {
                Log.d(NetworkDiscoveryListener.tag, "Same IP.");
                return;
            }
            mServiceInfo = serviceInfo
            NetworkDiscoveryListener.cbridgePort = mServiceInfo.getPort()
            NetworkDiscoveryListener.cbridgeHost = mServiceInfo.getHost()
        }
    }
	
	var mServiceInfo = new NsdServiceInfo()
	mServiceInfo.setServiceName(NetworkDiscoveryListener.cbridgeServiceName);
	mServiceInfo.setServiceType(NetworkDiscoveryListener.cbridgeServiceType);
	mServiceInfo.setPort(NetworkDiscoveryListener.cbridgePort);

	val nsdManager = appContext.getSystemService(Context.NSD_SERVICE).asInstanceOf[NsdManager];

	nsdManager.registerService(mServiceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener);

	//Called as soon as service discovery begins.
	override def onDiscoveryStarted(regType : String) {
		Log.d(NetworkDiscoveryListener.tag, "Service discovery started")
	}

	override def onServiceFound(service : NsdServiceInfo) {
		// A service was found
		Log.d(NetworkDiscoveryListener.tag, "Service discovery success" + service);

		if (!service.getServiceType.equals(NetworkDiscoveryListener.cbridgeServiceType)) {
			Log.d(NetworkDiscoveryListener.tag, "Unknown Service Type: " + service.getServiceType);
		} else if (service.getServiceName.equals(NetworkDiscoveryListener.cbridgeServiceName)) {
			// The name of the service tells the user what they'd be
			// connecting to. It could be "Bob's Chat App".
			Log.d(NetworkDiscoveryListener.tag, "Same machine: " + NetworkDiscoveryListener.cbridgeServiceName);
		} else if (service.getServiceName().contains("NsdChat")){
			nsdManager.resolveService(service, resolveListener);
		}
	}
	
	override def onServiceLost(service : NsdServiceInfo) {
		// When the network service is no longer available.
		// Internal bookkeeping code goes here.
		Log.e(NetworkDiscoveryListener.tag, "service lost" + service);
	}
	
	override def onDiscoveryStopped(serviceType : String) {
		Log.i(NetworkDiscoveryListener.tag, "Discovery stopped: " + serviceType);
	}
	
	override def onStartDiscoveryFailed(serviceType : String, errorCode : Int) {
		Log.e(NetworkDiscoveryListener.tag, "Discovery failed: Error code:" + errorCode);
		nsdManager.stopServiceDiscovery(this);
	}
	
	override def onStopDiscoveryFailed(serviceType : String, errorCode : Int) {
		Log.e(NetworkDiscoveryListener.tag, "Discovery failed: Error code:" + errorCode);
		nsdManager.stopServiceDiscovery(this);
	}
}