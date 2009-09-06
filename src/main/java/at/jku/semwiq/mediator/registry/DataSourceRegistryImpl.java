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

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.jku.rdfstats.RDFStatsDataset;
import at.jku.rdfstats.RDFStatsModel;
import at.jku.rdfstats.RDFStatsModelException;
import at.jku.rdfstats.RDFStatsModelFactory;
import at.jku.rdfstats.RDFStatsModelImpl;
import at.jku.rdfstats.RDFStatsUpdatableModel;
import at.jku.rdfstats.RDFStatsUpdatableModelImpl;
import at.jku.rdfstats.vocabulary.Stats;
import at.jku.semwiq.mediator.Constants;
import at.jku.semwiq.mediator.conf.ConfigException;
import at.jku.semwiq.mediator.conf.DataSourceRegistryConfig;
import at.jku.semwiq.mediator.registry.model.FOAFAgent;
import at.jku.semwiq.mediator.registry.model.DataSource;
import at.jku.semwiq.mediator.registry.model.FOAFGroup;
import at.jku.semwiq.mediator.registry.model.MonitoringProfile;
import at.jku.semwiq.mediator.registry.model.FOAFOrganization;
import at.jku.semwiq.mediator.registry.model.FOAFPerson;
import at.jku.semwiq.mediator.registry.model.RDFStatsUpdatableModelExt;
import at.jku.semwiq.mediator.registry.monitor.DataSourceMonitor;
import at.jku.semwiq.mediator.registry.monitor.DataSourceMonitorImpl;
import at.jku.semwiq.mediator.util.StmtClosureIterator;
import at.jku.semwiq.mediator.vocabulary.SDV;
import at.jku.semwiq.mediator.vocabulary.voiD;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.TripleMatch;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.XSD;

/**
 * @author dorgon
 * 
 */
public class DataSourceRegistryImpl implements DataSourceRegistry {
	private static final Logger log = LoggerFactory.getLogger(DataSourceRegistryImpl.class);

	/** registry configuration, contains RDFStatsConfiguration */
	protected final DataSourceRegistryConfig config;
	
	/** global RDF store */
	protected final Model globalStore;
	
	/** view on statistics in storage - all access via the RDFStatsModel using locking */
	protected final RDFStatsUpdatableModelExt statsModel;
	
	/** a monitor for data sources */
	protected final DataSourceMonitor monitor;
	
	/**
	 * 
	 * @param config
	 * @param globalStore
	 * @throws RegistryException 
	 */
	public DataSourceRegistryImpl(DataSourceRegistryConfig config, Model globalStore) throws RegistryException {
		log.info("Initializing data source registry...");

		this.config = config;
		this.globalStore = globalStore;
		this.statsModel = new RDFStatsUpdatableModelExt(globalStore); // has extensions to get/set swq:lastDownload
		
		selfCheck();
		
		// initialize data source monitor
		this.monitor = new DataSourceMonitorImpl(this);
		this.monitor.start();
	}
	
	/**
	 * @throws RegistryException 
	 * 
	 */
	private void selfCheck() throws RegistryException {
		List<DataSource> sources = getEnabledDataSources();
		log.info(getRegisteredDataSources().size() + " data sources registered - " + sources.size() + " activated.");
		try {
			log.info(statsModel.getDatasets().size() + " RDFStats datasets in cache.");			
		} catch (Exception e) {
			log.error("Failed to access RDFStats statistics. You should delete or rebuild the global RDF store.");
		}
		
		for (DataSource ds : sources) {
			if (ds.getSPARQLEndpointURL() == null)
				log.error("Invalid data source: " + ds.getUri() + " - Missing " + voiD.sparqlEndpoint.getURI() + "!");
			try {
				if (statsModel.getDataset(ds.getSPARQLEndpointURL()) == null) {
					log.error("Missing statistics for " + ds + " - will trigger update.");
					monitor.triggerUpdate(ds);
				}
			} catch (Exception e) {
				log.error("Failed to get statistics for " + ds + " - will trigger update.");
				monitor.triggerUpdate(ds);
			}
		}
	}

	public DataSourceRegistryConfig getConfig() {
		return config;
	}
	
	public RDFStatsModel getRDFStatsModel() {
		return statsModel;
	}

	public RDFStatsUpdatableModelExt getRDFStatsUpdatableModel() {
		return statsModel;
	}
	
	public DataSourceMonitor getMonitor() {
		return monitor;
	}
	
//	/* (non-Javadoc)
//	 * @see at.faw.semwiq.mediator.registry.Registry#reloadVocabularies()
//	 */
//	public void reloadVocabularies() {
//		log.info("Reloading global vocabularies...");
//		for (Resource r : statsModel.getClasses())
//			vm.getOntClass(r.getURI());
//		for (Property p : statsModel.getProperties())
//			vm.getOntProperty(p.getURI());
//	}
	
//	/* (non-Javadoc)
//	 * @see at.faw.semwiq.mediator.registry.Registry#reloadVocabularies(DataSource ds)
//	 */
//	public void reloadVocabularies(DataSource ds) {
//		String endpoint = ds.getServiceEndpoint();
//		log.info("Reloading vocabularies of data source '" + ds.getLabel() + "' <" + endpoint + ">...");
//		for (Resource r : statsModel.getClasses(endpoint))
//			vm.getOntClass(r.getURI());
//		for (Property p : statsModel.getProperties(endpoint))
//			vm.getOntProperty(p.getURI());
//	}
	
	public void shutdown() {
		log.info("Shutting down data source registry...");
		
		if (monitor != null)
			monitor.shutdown(); // statsModel is committed and closed by monitor		
	}

	/* (non-Javadoc)
	 * @see at.jku.semwiq.mediator.registry.DataSourceRegistry#getManager()
	 */
	public DataSourceRegistryManager getManager() {
		if (this instanceof DataSourceRegistryManager)
			return (DataSourceRegistryManager) this;
		else
			return null;
	}

// information services

	public DataSource getDataSourceByEndpointUri(String sparqlEndpoint) throws RegistryException {
		DataSource ds = null;
		String q = Constants.QUERY_PREFIX +
			"SELECT ?ds WHERE {\n" +
			"	?ds a void:Dataset ;\n" +
			"		void:sparqlEndpoint <" + sparqlEndpoint + "> .\n" +
			"}";

		QueryExecution qe = null;
		globalStore.enterCriticalSection(Lock.READ);
		try {
			qe = QueryExecutionFactory.create(q, globalStore);
			ResultSet r = qe.execSelect();
			if (r.hasNext()) 
				ds = new DataSource(r.next().getResource("ds"));
			if (r.hasNext())
				log.warn("Warning! There are multiple data sources with the same SPARQL endpoint URI " + sparqlEndpoint + " in the store.");
		} catch (Exception e) {
			throw new RegistryException("Failed to get data source with SPARQL endpoint URI " + sparqlEndpoint + ".", e);
		} finally {
			if (qe != null) qe.close();
			globalStore.leaveCriticalSection();
		}
		return ds;
	}

	/* (non-Javadoc)
	 * @see at.faw.semwiq.mediator.registry.Registry#getRegisteredDataSources()
	 */
	public List<DataSource> getRegisteredDataSources() throws RegistryException {
		List<DataSource> list = new ArrayList<DataSource>();

		QueryExecution qe = null;
		globalStore.enterCriticalSection(Lock.READ);
		try {
			String qryStr = Constants.QUERY_PREFIX +
			"SELECT ?ds WHERE {\n" +
			"	?ds a void:Dataset ;\n" +
			"		void:sparqlEndpoint ?someUri .\n" +
			"}";
			
			qe = QueryExecutionFactory.create(qryStr, globalStore);
			ResultSet r = qe.execSelect();			
			Resource res;
			while (r.hasNext()) {
				res = r.nextSolution().getResource("ds");
				list.add(new DataSource(res));
			}
		} catch (Exception e) {
			throw new RegistryException("Failed to get registered data sources.", e);
		} finally {
			globalStore.leaveCriticalSection();
			if (qe != null) qe.close();
		}
		
		return list;
	}

	/* (non-Javadoc)
	 * @see at.faw.semwiq.mediator.registry.Registry#getEnabledDataSources()
	 */
	public List<DataSource> getEnabledDataSources() throws RegistryException {
		List<DataSource> list = new ArrayList<DataSource>();
		
		QueryExecution qe = null;
		globalStore.enterCriticalSection(Lock.READ);
		try {
			String qryStr = Constants.QUERY_PREFIX +
			"SELECT ?ds WHERE {\n" +
			"	{	?ds a void:Dataset ;\n" +
			"			void:sparqlEndpoint ?someUri .\n" +
			"	} OPTIONAL {\n" +
			"		?ds sdv:disabled ?disabled .\n" +
			"	} FILTER (!bound(?disabled) || ?disabled = \"false\"^^xsd:boolean) ." +
			"}";
			
			qe = QueryExecutionFactory.create(qryStr, globalStore);
			ResultSet r = qe.execSelect();			
			Resource res;
			while (r.hasNext()) {
				res = r.nextSolution().getResource("ds");
				list.add(new DataSource(res));
			}
		} catch (Exception e) {
			throw new RegistryException("Failed to get enabled data sources.", e);
		} finally {
			globalStore.leaveCriticalSection();
			if (qe != null) qe.close();
		}
		
		return list;
	}


	/* (non-Javadoc)
	 * @see at.faw.semwiq.mediator.registry.Registry#getAvailableDataSources()
	 */
	public List<DataSource> getAvailableDataSources() throws RegistryException {
		List<DataSource> list = new ArrayList<DataSource>();
		
		QueryExecution qe = null;
		globalStore.enterCriticalSection(Lock.READ);
		try {
			String qry = "SELECT ?ds WHERE {\n" +
			"	{	?ds a void:Dataset ;\n" +
			"			void:sparqlEndpoint ?someUri .\n" +
			"	} OPTIONAL {\n" +
			"		?ds sdv:disabled ?disabled .\n" +
			"	} OPTIONAL {\n" +
			"		?ds sdv:unavailable ?unavail .\n" +
			"	} FILTER ((!bound(?disabled) || ?disabled = \"false\"^^xsd:boolean) &&" +
			"			  (!bound(?unavail) || ?unavail = \"false\"^^xsd:boolean)) ." +
			"}";
			
			qe = QueryExecutionFactory.create(Constants.QUERY_PREFIX + qry, globalStore);
			ResultSet r = qe.execSelect();
			Resource res;
			while (r.hasNext()) {
				res = r.nextSolution().getResource("ds");
				list.add(new DataSource(res));
			}
		} catch (Exception e) {
			throw new RegistryException("Failed to get available data sources.", e);
		} finally { 
			globalStore.leaveCriticalSection();
			if (qe != null) qe.close();
		}
		
		return list;
	}

	/* (non-Javadoc)
	 * @see at.faw.semwiq.mediator.registry.Registry#getAvailableDescribingDataSources(java.lang.String)
	 */
	public List<DataSource> getAvailableDescribingDataSources(String uri) throws RegistryException {
		List<DataSource> result = new ArrayList<DataSource>();
		try {
			List<DataSource> dataSources = getAvailableDataSources();
			for (DataSource ds : dataSources)
				if (!statsModel.getDataset(ds.getSPARQLEndpointURL()).subjectNotExists(uri)) // check if possibly exists at ds
					result.add(ds);
			return result;
		} catch (RDFStatsModelException e) {
			throw new RegistryException("Failed to get data sources which possibly describe " + uri + ".", e);
		}
	}

	/* (non-Javadoc)
	 * @see at.jku.semwiq.mediator.registry.DataSourceRegistry#getAvailableRelevantDataSources(com.hp.hpl.jena.graph.Node, com.hp.hpl.jena.graph.Node, com.hp.hpl.jena.graph.Node)
	 */
	public List<DataSource> getAvailableRelevantDataSources(Node subject, Node predicate, Node object) throws RegistryException {
		return getAvailableRelevantDataSources(subject, predicate, object, null);
	}

	/* (non-Javadoc)
	 * @see at.jku.semwiq.mediator.registry.DataSourceRegistry#getAvailableRelevantDataSources(com.hp.hpl.jena.graph.Node, com.hp.hpl.jena.graph.Node, com.hp.hpl.jena.graph.Node, com.hp.hpl.jena.sparql.expr.ExprList)
	 */
	public List<DataSource> getAvailableRelevantDataSources(Node subject, Node predicate, Node object, ExprList filter) throws RegistryException {
		List<DataSource> result = new ArrayList<DataSource>();
		try {
			List<DataSource> dataSources = getAvailableDataSources();
			for (DataSource ds : dataSources) {
				RDFStatsDataset statsDs = statsModel.getDataset(ds.getSPARQLEndpointURL());
				if (statsDs == null) {
					result.add(ds);
					log.warn("Data source " + ds + " selected as relevant for " + new Triple(subject, predicate, object) + " because there a no RDFStats statistics available.");
				} else {
					Integer estimate = statsDs.triplesForFilteredPattern(subject, predicate, object, filter);
					if (estimate == null) {
						result.add(ds);
						log.warn("Data source " + ds + " selected as relevant for " + new Triple(subject, predicate, object) + " because the RDFStats dataset failed to calculate a cardinality estimate.");
					} else if (estimate > 0)
						result.add(ds);
				}
			}
			return result;
		} catch (RDFStatsModelException e) {
			throw new RegistryException("Failed to get relevant data sources for triple pattern { " + subject + " " + predicate + " " + object + "}.", e);
		}
	}
//	
//	public FOAFAgent getFOAFAgent(String uri) {
//		FOAFAgent a = null;
//		globalStore.enterCriticalSection(Lock.READ);
//		try {
//			Resource res = globalStore.getResource(uri);
//			if (res != null) {
//				if (res.hasProperty(RDF.type, FOAF.Person))
//					a = new FOAFPerson(res);
//				else if (res.hasProperty(RDF.type, FOAF.Organization))
//					a = new FOAFOrganization(res);
//				else if (res.hasProperty(RDF.type, FOAF.Group))
//					a = new FOAFGroup(res);
//				else if (res.hasProperty(RDF.type, FOAF.Agent))
//					a = new FOAFAgent(res);
//				else
//					throw new RuntimeException("Invalid FOAF agent: <" + uri + ">, check the type of the resource!");
//			}
//		} finally {
//			globalStore.leaveCriticalSection();
//		}
//		return a;
//	}

	/* (non-Javadoc)
	 * @see at.faw.semwiq.mediator.registry.Registry#getAvailableMonitoringProfiles()
	 */
	public List<MonitoringProfile> getAvailableMonitoringProfiles() throws RegistryException {
		List<MonitoringProfile> list = new ArrayList<MonitoringProfile>();
		list.addAll(MonitoringProfile.getAvailableDefaultProfiles());
		
		globalStore.enterCriticalSection(Lock.READ);
		try {
			QueryExecution qe = QueryExecutionFactory.create(Constants.QUERY_PREFIX +
					"SELECT ?p WHERE { \n" +
					"	{ ?p	rdf:type	sdv:RemoteMonitoringProfile } \n" +
					"	UNION \n" +
					"	{ ?p	rdf:type	sdv:CentralizedMonitoringProfile } \n" +
					"	UNION \n" +
					"	{ ?p	rdf:type	sdv:VoidMonitoringProfile } \n" +
					"}\n", globalStore);
			ResultSet r = qe.execSelect();			
			Resource res;
			try {
				while (r.hasNext()) {
					res = r.nextSolution().getResource("p");
					list.add(MonitoringProfile.create(res));
				}
			} finally { qe.close(); }
		} finally { globalStore.leaveCriticalSection(); }
		
		return list;
	}

	/* (non-Javadoc)
	 * @see at.faw.semwiq.mediator.registry.Registry#getMonitoringProfile(java.lang.String)
	 */
	public MonitoringProfile getMonitoringProfile(String uri) throws RegistryException {
		globalStore.enterCriticalSection(Lock.READ);
		try {
			Resource r = globalStore.getResource(uri);
			if (r != null)
				return MonitoringProfile.create(r);
			else
				return null;
		} finally {
			globalStore.leaveCriticalSection();
		}
	}
	
}
