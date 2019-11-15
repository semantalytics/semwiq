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

import java.util.List;

import at.jku.rdfstats.RDFStatsModel;
import at.jku.rdfstats.RDFStatsUpdatableModel;
import at.jku.semwiq.mediator.conf.DataSourceRegistryConfig;
import at.jku.semwiq.mediator.registry.model.FOAFAgent;
import at.jku.semwiq.mediator.registry.model.DataSource;
import at.jku.semwiq.mediator.registry.model.MonitoringProfile;
import at.jku.semwiq.mediator.registry.model.RDFStatsUpdatableModelExt;
import at.jku.semwiq.mediator.registry.monitor.DataSourceMonitor;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.TripleMatch;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.expr.ExprList;

/**
 * @author dorgon, Andreas Langegger, al@jku.at
 *
 */
public interface DataSourceRegistry {

	/** get the registry configuration */
	public DataSourceRegistryConfig getConfig();
	
	/** get the datasource monitor (responsible for updates of meta data and statistics )*/
	public DataSourceMonitor getMonitor();
	
	/** get the commonly used RDFStats statistics model */
	public RDFStatsModel getRDFStatsModel();
	
	/** get the commonly used RDFStatsUpdatableModelExt (with extensions for getting/setting swq:lastDownload) */
	public RDFStatsUpdatableModelExt getRDFStatsUpdatableModel();
	
	/** returns a single data source by the endpoint URI 
	 * @throws RegistryException */ // TODO rename
	public DataSource getDataSourceByEndpointUri(String endpointUri) throws RegistryException;
	
	/** returns all registered (but possibly disabled) data sources */
	public List<DataSource> getRegisteredDataSources() throws RegistryException;

	/** returns all registered but not disabled (and possibly currently unavailable) data sources */
	public List<DataSource> getEnabledDataSources() throws RegistryException;
	
	/** returns all not disabled and currently available data sources */
	public List<DataSource> getAvailableDataSources() throws RegistryException;

	/** returns all available data sources that store descriptions about the given resource URI 
	 * @throws RegistryException */
	public List<DataSource> getAvailableDescribingDataSources(String uri) throws RegistryException;

	/** returns all available data sources that are relevant for a given triple pattern
	 * @throws RegistryException */
	public List<DataSource> getAvailableRelevantDataSources(Node subject, Node predicate, Node object) throws RegistryException;

	/** returns all available data sources that are relevant for a given filtered triple pattern
	 * @throws RegistryException */
	public List<DataSource> getAvailableRelevantDataSources(Node subject, Node predicate, Node object, ExprList filter) throws RegistryException;

	/** get the list of available monitoring profiles
	 * 
	 * @return
	 * @throws RegistryException
	 */
	public List<MonitoringProfile> getAvailableMonitoringProfiles() throws RegistryException;
		
	/** returns a monitoring profile by known URI
	 * 
	 * @param uri
	 * @return
	 * @throws RegistryException
	 */
	public MonitoringProfile getMonitoringProfile(String uri) throws RegistryException;

	/** shutdown registry (called by the Mediator when shutting down) */
	public void shutdown();
	
	
	/** get the manager */
	public DataSourceRegistryManager getManager();
}
