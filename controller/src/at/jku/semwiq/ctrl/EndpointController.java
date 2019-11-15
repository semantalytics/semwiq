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

import java.util.Map;

import at.jku.semwiq.rmi.EndpointDaemon;
import at.jku.semwiq.rmi.EndpointMetadata;
import at.jku.semwiq.rmi.RemoteEndpointDaemon;
import at.jku.semwiq.rmi.DaemonRegistry;
import at.jku.semwiq.rmi.SpawnedEndpointMetadata;

import com.healthmarketscience.rmiio.RemoteInputStream;

/** 
 * @author dorgon
 *
 */
public interface EndpointController {
	
	// endpoint daemon-specific methods
	public void init() throws SemWIQControllerException;
	public RemoteEndpointDaemon getEndpointDaemon(DaemonRegistry DaemonRegistry) throws SemWIQControllerException;
	public Map<DaemonRegistry, RemoteEndpointDaemon> listEndpointDaemons();
	public void removeEndpointDaemon(DaemonRegistry DaemonRegistry);
	public void shutdownEndpointDaemon(DaemonRegistry DaemonRegistry) throws SemWIQControllerException;

	// endpoint controlling methods
	public SpawnedEndpointMetadata spawnEndpoint(DaemonRegistry DaemonRegistry, EndpointMetadata meta, RemoteInputStream dataset, String baseUri, String dataFormat) throws SemWIQControllerException;
	public void killEndpoint(SpawnedEndpointMetadata spec) throws SemWIQControllerException;
	public SpawnedEndpointMetadata[] listEndpoints(DaemonRegistry DaemonRegistry) throws SemWIQControllerException;
	
	// listeners
	public void registerListener(EndpointControllerListener l);
	public void unregisterListener(EndpointControllerListener l);
}
