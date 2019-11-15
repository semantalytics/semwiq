/**
 * Copyright 2007-2008 Institute for Applied Knowledge Processing, Johannes Kepler University Linz
 *  
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package at.jku.semwiq.endpoint.daemon;


import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.jku.semwiq.endpoint.JosekiInstance;
import at.jku.semwiq.rmi.CommonConstants;
import at.jku.semwiq.rmi.EndpointMetadata;
import at.jku.semwiq.rmi.RemoteEndpointDaemon;
import at.jku.semwiq.rmi.SemWIQInterfaceException;
import at.jku.semwiq.rmi.SpawnedEndpointMetadata;

import com.healthmarketscience.rmiio.RemoteInputStream;
import com.healthmarketscience.rmiio.RemoteInputStreamClient;

/**
 * @author dorgon
 *
 */
public class EndpointDaemonImpl extends UnicastRemoteObject implements RemoteEndpointDaemon {
	private static final long serialVersionUID = 4216750361260799546L;
	private static final Logger log = LoggerFactory.getLogger(EndpointDaemonImpl.class);

	/** static instance for self-configuration using static createInstance() */
	private static RemoteEndpointDaemon daemon;

	private final ConcurrentHashMap<SpawnedEndpointMetadata, JosekiInstance> endpoints;
	private final String name;
	private final PortManager portManager;

	/** create singleton instance and bind to local RMI registry
	 * 
	 * @param startPort
	 * @return
	 * @throws SemWIQInterfaceException
	 */
	public static RemoteEndpointDaemon createDaemon(int startPort) throws SemWIQInterfaceException {
		if (daemon != null)
			return daemon;
		
		try {
			// create local RMI registry and register static instance
			LocateRegistry.createRegistry(CommonConstants.RMI_REGISTRY_PORT);			
			daemon = new EndpointDaemonImpl(startPort);
			Naming.rebind(CommonConstants.RMI_SERVICE_NAME, daemon);
			
			return daemon;
		} catch (Exception e) {
			throw new SemWIQInterfaceException("Failed to initialize remote endpoint daemon.", e); 
		}
	}
	
	/**
	 * constructs a new endpoint
	 * @param name
	 * @param startPort
	 */
	public EndpointDaemonImpl(int startPort) throws RemoteException {
		name =  CommonConstants.HOSTNAME + ":" + CommonConstants.RMI_REGISTRY_PORT;
		endpoints = new ConcurrentHashMap<SpawnedEndpointMetadata, JosekiInstance>();
		portManager = new PortManager(startPort);

		log.info("Registered endpoint daemon " + getName() + ", waiting for requests from controller...");
	}
	
	@Override
	public synchronized SpawnedEndpointMetadata spawnEndpoint(EndpointMetadata meta, RemoteInputStream dataset, String baseUri, String dataFormat) throws RemoteException {
		int port = portManager.getFreePort();

		try {
			SpawnedEndpointMetadata spec = new SpawnedEndpointMetadata(CommonConstants.HOSTNAME, port, meta);
			JosekiInstance instance = JosekiInstance.createInstance(spec, RemoteInputStreamClient.wrap(dataset), baseUri, dataFormat);			
			endpoints.put(spec, instance);
			
			return spec;
		} catch (Throwable e) {
			throw new RemoteException("Failed to spawn endpoint.", e);
		}
	}

	@Override
	public synchronized void killEndpoint(SpawnedEndpointMetadata spec) throws RemoteException {
		try {
			JosekiInstance instance = endpoints.get(spec);
			instance.shutdown();
			
			endpoints.remove(spec);
			portManager.freePort(instance.getPort());
		} catch (SemWIQInterfaceException e) {
			throw new RemoteException("Failed to get Joseki instance.", e);
		}
	}

	@Override
	public synchronized void killAllEndpoints() throws RemoteException {
		for (SpawnedEndpointMetadata spec : endpoints.keySet())
			killEndpoint(spec);
	}
	
	@Override
	public synchronized SpawnedEndpointMetadata[] listRunningEndpoints() throws RemoteException {
		SpawnedEndpointMetadata[] specs = new SpawnedEndpointMetadata[endpoints.size()];
		int i = 0;
		for (SpawnedEndpointMetadata s : endpoints.keySet())
			specs[i++] = s;
		return specs;
	}
	
	@Override
	public String getName() throws RemoteException {
		return name;
	}
	
	@Override
	public synchronized void shutdown() throws RemoteException {
		log.info("Shutdown triggered...");
		Map<JosekiInstance, Exception> failed = new Hashtable<JosekiInstance, Exception>();
		
		try {
			for (JosekiInstance instance : endpoints.values()) {
				try {
					instance.shutdown();
				} catch (SemWIQInterfaceException e) {
					failed.put(instance, e);
				}
			}
			
			if (failed.size() > 0) {
				StringBuilder sb = new StringBuilder();
				for (JosekiInstance i : failed.keySet())
					sb.append("port ").append(i.getPort()).append(": ").append(failed.get(i).getMessage()).append("; ");
				throw new SemWIQInterfaceException("Some instances failed to shutdown: " + sb.toString());
			}
		} catch (SemWIQInterfaceException e) {
			throw new RemoteException(e.getMessage(), e);
		}

		// delay shutdown registry in forked thread
		final Remote ref = this;
		new Thread() {
			@Override
			public void run() {
				try {
					Thread.sleep(1000);
//					Naming.unbind(Utils.getRMIServiceName(Constants.HOSTNAME, Constants.RMI_REGISTRY_PORT));
					UnicastRemoteObject.unexportObject(ref, true);
					log.info("Shutdown complete.");
				} catch (Throwable e) {
					log.error("Failed to unbind daemon from local RMI registry after remotely triggered shutdown.", e);
				}
			}
		}.start();
	}
	
}
