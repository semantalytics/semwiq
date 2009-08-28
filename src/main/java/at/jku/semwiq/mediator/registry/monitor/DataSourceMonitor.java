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

import at.jku.semwiq.mediator.registry.RegistryException;
import at.jku.semwiq.mediator.registry.model.DataSource;

/**
 * @author dorgon
 *
 */
public interface DataSourceMonitor {
	
	/** start the monitor 
	 * @throws RegistryException */
	public void start() throws RegistryException;
	
	/** stop the monitor */
	public void shutdown();

	/** schedule a worker thread to monitor ds */
	public boolean scheduleWorker(DataSource ds, boolean updateImmediately);

	/** stop monitoring ds */
	public void stopMonitoring(DataSource ds);

	/** explicitly schedules update of datasource ds */
	public void triggerUpdate(DataSource ds);

	/** schedule complete update 
	 * @throws RegistryException */
	public void triggerCompleteUpdate() throws RegistryException;

	/** returns true if monitor is currently updating */
	public boolean isUpdating();
	
	/** returns true if data source ds is currently updating */
	public boolean isUpdating(DataSource ds);

	/** block thread execution until monitor has finished updating */
	public void waitUntilFinished();
}
