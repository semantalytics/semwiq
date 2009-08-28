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
package at.jku.semwiq.mediator.registry;

import at.jku.semwiq.mediator.registry.model.DataSource;
import at.jku.semwiq.mediator.registry.model.MonitoringProfile;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * @author dorgon, Andreas Langegger, al@jku.at
 *
 */
public interface DataSourceRegistryManager extends DataSourceRegistry {
	
	public boolean register(String sparqlEndpoint) throws RegistryException;
	public boolean register(String sparqlEndpoint, MonitoringProfile monitProfile) throws RegistryException;
	public boolean register(Model voidModel, Resource voidDataset) throws RegistryException;
	public int register(Model voidModel) throws RegistryException;
	public boolean unregister(String sparqlEndpoint) throws RegistryException;
	public boolean unregister(DataSource ds) throws RegistryException;
	public boolean update(DataSource ds, Model voidModel) throws RegistryException;
	
}
