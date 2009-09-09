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

package at.jku.semwiq.mediator.registry.monitor;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.jku.rdfstats.RDFStatsConfiguration;
import at.jku.semwiq.mediator.registry.DataSourceRegistry;
import at.jku.semwiq.mediator.registry.RegistryException;
import at.jku.semwiq.mediator.registry.model.CentralizedMonitoringProfile;
import at.jku.semwiq.mediator.registry.model.DataSource;
import at.jku.semwiq.mediator.registry.model.MonitoringProfile;
import at.jku.semwiq.mediator.registry.model.RemoteMonitoringProfile;
import at.jku.semwiq.mediator.registry.model.VoidMonitoringProfile;

import com.hp.hpl.jena.rdf.model.Model;

/**
 * @author dorgon
 */
public class DataSourceMonitorImpl implements DataSourceMonitor {
	private static final Logger log = LoggerFactory.getLogger(DataSourceMonitorImpl.class);
	
	public static final int CONCURRENT_WORKERS = 10;
	
	private final DataSourceRegistry registry;
	private final RDFStatsConfiguration globalConf;
	private final Scheduler scheduler;

	private final ConcurrentMap<DataSource, UpdateWorkerBase> workerReferences;
	
	public DataSourceMonitorImpl(DataSourceRegistry registry) {
		this.registry = registry;
		this.globalConf = registry.getConfig().getRdfStatsConfig();
		this.workerReferences = new ConcurrentHashMap<DataSource, UpdateWorkerBase>();
		this.scheduler = new Scheduler(CONCURRENT_WORKERS);
	}

	public void start() throws RegistryException {
		for (DataSource ds : registry.getEnabledDataSources()) {
			if (startMonitoring(ds, false) && log.isDebugEnabled()) {
				MonitoringProfile profile = ds.getMonitoringProfile();
				log.debug("Monitoring " + ds + " every " + profile.getInterval() + " seconds (" + profile.getName() + ").");
			}
		}
	}
	
	public synchronized boolean startMonitoring(DataSource ds, boolean updateImmediately) {
		if (isMonitoring(ds)) {
			log.info("Already monitoring " + ds + ".");
			if (updateImmediately)
				triggerUpdate(ds);
			return true;
		}
		
		MonitoringProfile profile = ds.getMonitoringProfile();
		UpdateWorkerBase task;
		
		if (profile == null) {
			log.warn("Data source " + ds + " cannot be monitored since it has no monitoring profile.");
			return false;
			
		} else if (profile.getInterval() > 0) {
			if (profile instanceof RemoteMonitoringProfile) {
				task = new RemoteUpdateWorker(ds, registry, (RemoteMonitoringProfile) profile);
				workerReferences.put(ds, task);
				scheduler.scheduleWithFixedDelay(task, (updateImmediately || profile.updateOnStartup()) ? 0 : profile.getInterval(), profile.getInterval(), TimeUnit.SECONDS);
				return true;
			} else if (profile instanceof CentralizedMonitoringProfile) {
				task = new CentralizedUpdateWorker(ds, registry, (CentralizedMonitoringProfile) profile);
				workerReferences.put(ds, task);
				scheduler.scheduleWithFixedDelay(task, (updateImmediately || profile.updateOnStartup()) ? 0 : profile.getInterval(), profile.getInterval(), TimeUnit.SECONDS);
				return true;
			} else if (profile instanceof VoidMonitoringProfile) {
				task = new VoidUpdateWorker(ds, registry, (VoidMonitoringProfile) profile);
				workerReferences.put(ds, task);
				scheduler.scheduleWithFixedDelay(task, (updateImmediately || profile.updateOnStartup()) ? 0 : profile.getInterval(), profile.getInterval(), TimeUnit.SECONDS);
				return true;

			} else {
				log.warn("No monitoring profile for " + ds + ". The data source is not monitored.");
				return false;
			}
		} else {
			log.warn("Update interval of profile " + profile.getUri() + " is set to 0. The data source is not monitored.");
			return false;
		}
	}
	
	/**
	 * @param ds
	 * @return
	 */
	private synchronized boolean isMonitoring(DataSource ds) {
		return workerReferences.containsKey(ds);
	}
	
	/**
	 * @param ds
	 */
	public synchronized void stopMonitoring(DataSource ds) {
		UpdateWorkerBase worker = workerReferences.get(ds);
		scheduler.remove(worker);
		workerReferences.remove(ds);
	}

	/* (non-Javadoc)
	 * @see at.faw.semwiq.mediator.registry.monitor.DataSourceMonitor#isUpdating()
	 */
	public synchronized boolean isUpdating() {
		return scheduler.getActiveCount() > 0;
	}
	
	/* (non-Javadoc)
	 * @see at.faw.semwiq.mediator.registry.monitor.DataSourceMonitor#isUpdating(at.faw.semwiq.mediator.registry.model.DataSource)
	 */
	public boolean isUpdating(DataSource ds) {
		return UpdateWorkerBase.isUpdating(ds);
	}
	
	/* (non-Javadoc)
	 * @see at.faw.semwiq.mediator.registry.monitor.DataSourceMonitor#shutdown()
	 */
	public void shutdown() {
		log.info("Shutting down data source monitor...");
		scheduler.shutdown();
		
		// wait for update to complete
		if (isUpdating())
			log.info("Update of a datasource is still in progress. Waiting for update to complete...");
		while (isUpdating()) {
			try { Thread.sleep(100); } catch (InterruptedException ignore) {}
		}		
	}
	
	/* (non-Javadoc)
	 * @see at.faw.semwiq.mediator.registry.monitor.DataSourceMonitor#triggerCompleteUpdate()
	 */
	public void triggerCompleteUpdate() throws RegistryException {
		Set<DataSource> currentlyUpdating = new HashSet<DataSource>();
		
		for (DataSource ds : workerReferences.keySet()) {
			if (isUpdating(ds))
				currentlyUpdating.add(ds);
			else
				stopMonitoring(ds);
		}
		
		for (DataSource ds : registry.getEnabledDataSources()) {
			if (!currentlyUpdating.contains(ds))
				startMonitoring(ds, true);
		}
	}

	/* (non-Javadoc)
	 * @see at.faw.semwiq.mediator.registry.monitor.DataSourceMonitor#triggerUpdate(at.faw.semwiq.mediator.registry.model.DataSource)
	 */
	public void triggerUpdate(DataSource ds) {
		stopMonitoring(ds);
		startMonitoring(ds, true);
	}
	
	/* (non-Javadoc)
	 * @see at.jku.semwiq.mediator.registry.monitor.DataSourceMonitor#waitUntilFinished()
	 */
	public void waitUntilFinished() {
		while (isUpdating())
			try { Thread.sleep(500); } catch (InterruptedException e) {}
	}
	
}
