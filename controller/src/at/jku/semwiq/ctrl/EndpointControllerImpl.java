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
package at.jku.semwiq.ctrl;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.jku.semwiq.rmi.EndpointDaemon;
import at.jku.semwiq.rmi.EndpointMetadata;
import at.jku.semwiq.rmi.InterfaceUtils;
import at.jku.semwiq.rmi.RemoteEndpointDaemon;
import at.jku.semwiq.rmi.DaemonRegistry;
import at.jku.semwiq.rmi.SpawnedEndpointMetadata;

import com.healthmarketscience.rmiio.RemoteInputStream;

/**
 * @author dorgon
 *
 */
public class EndpointControllerImpl implements EndpointController {
	private static final Logger log = LoggerFactory.getLogger(EndpointControllerImpl.class);

	/** config */
	private final Config cfg;
	
	/** remote endpoint registry references */
	private final Map<DaemonRegistry, RemoteEndpointDaemon> registries;
	
	/** listeners */
	private final Set<EndpointControllerListener> listeners;
	
	/**
	 */
	public EndpointControllerImpl() {
		this(new Config());
	}

	/**
	 * @param cfg
	 */
	public EndpointControllerImpl(Config cfg) {
		this.cfg = cfg;
		registries = new Hashtable<DaemonRegistry, RemoteEndpointDaemon>();
		listeners = new HashSet<EndpointControllerListener>();
	}
	
	/**
	 * init remote registries from config file
	 */
	public synchronized void init() throws SemWIQControllerException {
		for (DaemonRegistry r : registries.keySet()) {
			try {
				registries.get(r).getName();
			} catch (Throwable e) {
				log.error("Removing unavailable daemon " + r + ".");
				removeEndpointDaemon(r);
			}
		}
		
		log.info("Refreshing endpoint daemons...");
		Map<DaemonRegistry, Exception> failed = new Hashtable<DaemonRegistry, Exception>();
		
		// init registries based on configuration file
		for (DaemonRegistry r : cfg.getRegistries()) {
			try {
				getEndpointDaemon(r);
			} catch (SemWIQControllerException e) {
				failed.put(r, e);
			}
		}
		
		if (failed.size() > 0) {
			StringBuilder sb = new StringBuilder();
			for (DaemonRegistry reg : failed.keySet())
				sb.append(reg.toString()).append(": ").append(failed.get(reg).getMessage()).append("; ");
			throw new SemWIQControllerException("Failed to get daemons: " + sb.toString());
		}
	}
	
	@Override
	public SpawnedEndpointMetadata[] listEndpoints(DaemonRegistry daemonRegistry) throws SemWIQControllerException {
		try {
			SpawnedEndpointMetadata[] list = getEndpointDaemon(daemonRegistry).listRunningEndpoints();
			for (SpawnedEndpointMetadata s : list)
				s.setDaemonRegistry(daemonRegistry);
			return list;
		} catch (Throwable e) {
			throw new SemWIQControllerException("Failed to list endpoints for daemon: " + daemonRegistry + ".", e);
		}
	}
	
	@Override
	public SpawnedEndpointMetadata spawnEndpoint(DaemonRegistry daemonRegistry, EndpointMetadata meta, RemoteInputStream dataset, String baseUri, String dataFormat) throws SemWIQControllerException {
		try {
			SpawnedEndpointMetadata s = registries.get(daemonRegistry).spawnEndpoint(meta, dataset, dataFormat, baseUri);
			s.setDaemonRegistry(daemonRegistry);
			log.info("New endpoint spawned: " + s);

			// inform listeners
			for (EndpointControllerListener l : listeners)
				l.endpointSpawned(s);
			
			return s;
		} catch (Throwable e) {
			throw new SemWIQControllerException("Failed to spawn endpoint: " + e.getMessage(), e);
		}
	}

	@Override
	public void killEndpoint(SpawnedEndpointMetadata spec) throws SemWIQControllerException {
		try {
			registries.get(spec.getDaemonRegistry()).killEndpoint(spec);
			log.info("Endpoint killed: " + spec);

			// inform listeners
			for (EndpointControllerListener l : listeners)
				l.endpointKilled(spec);
		} catch (Throwable e) {
			throw new SemWIQControllerException("Failed to kill " + spec + ": " + e.getMessage(), e);
		}
	}

	/**
	 * @param daemonRegistry
	 * @return
	 * @throws SemWIQControllerException 
	 */
	public RemoteEndpointDaemon getEndpointDaemon(DaemonRegistry daemonRegistry) throws SemWIQControllerException {
		try {
			RemoteEndpointDaemon daemon = registries.get(daemonRegistry);
			if (daemon != null)
				return daemon;
			
			// fetch via remote registry
			else {
//				Registry reg = LocateRegistry.getRegistry(daemonRegistry.getHostname(), daemonRegistry.getPort());
				daemon = (RemoteEndpointDaemon) Naming.lookup(InterfaceUtils.getRMIServiceName(daemonRegistry.getHostname(), daemonRegistry.getPort()));
				registries.put(daemonRegistry, daemon);
				
				// inform listeners
				for (EndpointControllerListener l : listeners)
					l.endpointDaemonAdded(daemonRegistry);
			
				log.info("New endpoint daemon acquired from " + daemonRegistry);
				return daemon;
			}			
		} catch (Throwable e) {
			throw new SemWIQControllerException("Failed to acquire remote endpoint daemon at " + daemonRegistry + ".", e);
		}
	}
	
	@Override
	public void shutdownEndpointDaemon(DaemonRegistry daemonRegistry) throws SemWIQControllerException {
		try {
			getEndpointDaemon(daemonRegistry).shutdown();

			// inform listeners
			for (EndpointControllerListener l : listeners)
				l.endpointDaemonAdded(daemonRegistry);

			log.info("Endpoint daemon " + daemonRegistry + " successfully shut down.");
		
		} catch (RemoteException e) {
			throw new SemWIQControllerException("Failed to shutdown endpoint daemon " + daemonRegistry + ".", e);
		}
	}
	
	
	@Override
	public void removeEndpointDaemon(DaemonRegistry daemonRegistry) {
		if (registries.remove(daemonRegistry) != null) {
			// inform listeners
			for (EndpointControllerListener l : listeners)
				l.endpointDaemonRemoved(daemonRegistry);
		}
	}
	
	@Override
	public void registerListener(EndpointControllerListener l) {
		listeners.add(l);
	}
	
	@Override
	public void unregisterListener(EndpointControllerListener l) {
		listeners.remove(l);
	}

	@Override
	public Map<DaemonRegistry, RemoteEndpointDaemon> listEndpointDaemons() {
		return registries;
	}
}
