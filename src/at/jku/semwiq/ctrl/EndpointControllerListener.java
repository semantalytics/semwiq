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

import at.jku.semwiq.rmi.DaemonRegistry;
import at.jku.semwiq.rmi.SpawnedEndpointMetadata;



/**
 * @author dorgon
 *
 */
public interface EndpointControllerListener {
	
	public void endpointDaemonAdded(DaemonRegistry registry);
	public void endpointDaemonRemoved(DaemonRegistry registry);

	public void endpointSpawned(SpawnedEndpointMetadata spec);
	public void endpointKilled(SpawnedEndpointMetadata spec);

}
