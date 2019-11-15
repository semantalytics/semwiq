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
import at.jku.semwiq.mediator.Constants;
import at.jku.semwiq.mediator.conf.DataSourceRegistryConfig;
import at.jku.semwiq.mediator.registry.model.DataSource;
import at.jku.semwiq.mediator.registry.model.MonitoringProfile;
import at.jku.semwiq.mediator.util.StmtClosureIterator;
import at.jku.semwiq.mediator.vocabulary.SDV;
import at.jku.semwiq.mediator.vocabulary.voiD;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * @author dorgon, Andreas Langegger, al@jku.at
 *
 */
public class DataSourceRegistryManagerImpl extends DataSourceRegistryImpl implements DataSourceRegistryManager {
	private static final Logger log = LoggerFactory.getLogger(DataSourceRegistryManagerImpl.class);
	
	/**
	 * constructor
	 * @throws RegistryException 
	 */
	public DataSourceRegistryManagerImpl(DataSourceRegistryConfig config, Model globalStore) throws RegistryException {
		super(config, globalStore);
	}
	
	/* (non-Javadoc)
	 * @see at.jku.semwiq.mediator.registry.DataSourceRegistryManager#register(java.lang.String)
	 */
	public boolean register(String sparqlEndpoint) throws RegistryException {
		return register(sparqlEndpoint, null);
	}
	
	/**
	 * register by SPARQL endpoint URI
	 * 
	 * @param uri
	 * @param monitProfile
	 * @return
	 * @throws RegistryException
	 */
	public boolean register(String sparqlEndpoint, MonitoringProfile monitProfile) throws RegistryException {
		Model regData = ModelFactory.createDefaultModel();
		boolean registered = false;
		if (monitProfile == null)
			monitProfile = MonitoringProfile.getDefaultCentralizedProfile();
		
		try {
			// create a basic void:Dataset
			Resource ds = regData.createResource(null, voiD.Dataset);
			ds.addProperty(RDFS.label, "Dataset of SPARQL endpoint " + sparqlEndpoint);
			ds.addProperty(voiD.sparqlEndpoint, regData.createResource(sparqlEndpoint));
			ds.addProperty(SDV.monitoringProfile, monitProfile.getWrappedResource());
			
			// use normal register method with regData
			registered = register(regData, ds);
		} catch (Exception e) {
			throw new RegistryException("Error registering data source for endpoint <" + sparqlEndpoint + ">.", e);
		} finally {
			regData.close();
		}		
		return registered;
	}
	
	/**
	 * register void:Dataset r from voidModel model
	 */
	public boolean register(Model voidModel, Resource voidDataset) throws RegistryException {
		DataSource newDataSource = new DataSource(voidDataset);
		DataSource existing = getDataSourceByEndpointUri(newDataSource.getSPARQLEndpointURL());

		// already registered?
		boolean registeredNew;
		if (existing == null)
			registeredNew = true;
		else {
			log.info(existing + " is already registered, will unregister first...");
			registeredNew = false;
			unregister(existing);
		}
		
		globalStore.enterCriticalSection(Lock.WRITE);
		try {
			log.info("Registering " + newDataSource + " ...");
			
			// import all statements (closure) into globalStore
			StmtClosureIterator it = new StmtClosureIterator(voidDataset);
			while (it.hasNext())
				globalStore.add(it.next());

			if (newDataSource.isEnabled())
				// need to re-fetch correct DataSource instance whose
				// wrapped individual is part of the globalStore
				monitor.triggerUpdate(getDataSourceByEndpointUri(newDataSource.getSPARQLEndpointURL()));
		} catch (Exception e) {
			throw new RegistryException(e);
		} finally {
			globalStore.leaveCriticalSection();
		}
		
		return registeredNew;
	}

	/**
	 * will only delete void:Dataset - TODO: clean unused statements
	 * 
	 * @param ds
	 * @return
	 * @throws RegistryException
	 */
	public boolean unregister(DataSource ds) throws RegistryException {
		String endpointUri = ds.getSPARQLEndpointURL();
		
		List<Statement> toDel = new ArrayList<Statement>();		
		globalStore.enterCriticalSection(Lock.WRITE);
		try {
			globalStore.removeAll(ds.getWrappedResource(), null, null);
			ds = null;
			// TODO remove other unused linked resources...
			
			// delete statistics
			RDFStatsDataset statsDs = statsModel.getDataset(endpointUri);
			try {
				statsModel.requestExclusiveWriteLock(statsDs);
				statsModel.removeDataset(statsDs);
			} catch (Exception e) {
				log.error("Failed to clear statistics for " + statsDs + ".", e);
			} finally {
				statsModel.returnExclusiveWriteLock(statsDs);
			}
			
		} catch (Exception e) {
			throw new RegistryException("Error unregistering Data source <" + endpointUri + ">.", e);
		} finally {
			globalStore.leaveCriticalSection();
		}
		
		log.info("Data source <" + endpointUri + "> unregistered.");
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see at.jku.semwiq.mediator.registry.DataSourceRegistryManager#unregister(java.lang.String)
	 */
	public boolean unregister(String sparqlEndpoint) throws RegistryException {
		DataSource ds = getDataSourceByEndpointUri(sparqlEndpoint);
		if (ds == null)
			throw new RegistryException("No data source for endpoint URI <" + sparqlEndpoint + "> found in registry.");
		else return unregister(ds);
	}

	/**
	 * does unregister() and register()
	 * @param ds
	 * @param m
	 * @return
	 * @throws RegistryException
	 */
	public boolean update(DataSource ds, Model m) throws RegistryException {
		unregister(ds);
		return register(m, ds.getWrappedResource());
	}

	public int register(Model voidModel) throws RegistryException {
		int numFound = 0;
		int numRegistered = 0;

		QueryExecution qe = null;
		try {
			qe = QueryExecutionFactory.create(Constants.QUERY_PREFIX + 
					"SELECT ?ds WHERE {\n" +
					"	?ds a void:Dataset ;\n" +
					"		void:sparqlEndpoint ?endpoint .\n" +
					"}", voidModel);
			
			ResultSet res = qe.execSelect();
			while (res.hasNext()) {
				numFound++;
				if (register(voidModel, res.next().getResource("ds")))
					numRegistered++;
			}
		} catch (Exception e) {
			throw new RegistryException("Failed to query for void:Dataset instances in supplied model.", e);
		} finally {
			if (qe != null) qe.close();
		}
		
		if (numFound == 0) {
			log.warn("No data source instance found in supplied model.");
			return 0;
		} else {
			log.info(numRegistered + " data source(s) registered.");
			return numRegistered;
		}
	}
}
