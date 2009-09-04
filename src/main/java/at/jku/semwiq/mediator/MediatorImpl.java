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

package at.jku.semwiq.mediator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.jku.rdfstats.test.model.ModelTestPackage;
import at.jku.semwiq.mediator.conf.ConfigException;
import at.jku.semwiq.mediator.conf.MediatorConfig;
import at.jku.semwiq.mediator.dataset.SemWIQDataset;
import at.jku.semwiq.mediator.engine.MediatorQueryEngine;
import at.jku.semwiq.mediator.engine.describe.MediatorDescribeHandlerFactory;
import at.jku.semwiq.mediator.federator.Federator;
import at.jku.semwiq.mediator.federator.FederatorFactory;
import at.jku.semwiq.mediator.registry.DataSourceRegistry;
import at.jku.semwiq.mediator.registry.DataSourceRegistryManager;
import at.jku.semwiq.mediator.registry.DataSourceRegistryManagerImpl;
import at.jku.semwiq.mediator.registry.UserRegistry;
import at.jku.semwiq.mediator.registry.UserRegistryImpl;

import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.describe.DescribeHandlerRegistry;
import com.hp.hpl.jena.tdb.TDB;
import com.hp.hpl.jena.tdb.store.GraphTDB;

/**
 * @author dorgon
 */
public class MediatorImpl implements Mediator {
	private static final Logger log = LoggerFactory.getLogger(MediatorImpl.class);
	private static final Logger renderPlans = LoggerFactory.getLogger("render-plans");
	
	/** configuration */
	private final MediatorConfig config;
	
	/** global store */
	private final Model store;
	
	/** data source registry */
	private final DataSourceRegistryManager dsRegistry;

	/** user registry */
	private final UserRegistry userRegistry;
	
	/** federator */
	private final Federator federator;
	
	/** mediator ready and accepting queries? */
	private boolean isReady = false;

	/** was mediator shutdown? */
	private boolean terminated = false;
	
	/** create a new mediator instance with default configuration 
	 * @throws MediatorException */
	public MediatorImpl() throws ConfigException, MediatorException {
		this(null, null);
	}

	/** create a new mediator instance using configFile 
	 * @throws MediatorException */
	public MediatorImpl(String configFile) throws ConfigException, MediatorException {
		this(new MediatorConfig(configFile), null);
	}
	
	/** create a new mediator instance based on config and reg 
	 * @throws MediatorException */
	public MediatorImpl(MediatorConfig config, DataSourceRegistryManager reg) throws ConfigException, MediatorException {
		log.info("=== Initializing SemWIQ mediator on " + Constants.HOSTNAME + " ===");			
		log.info("Java runtime: " + System.getProperty("java.version") + ", " + System.getProperty("os.arch"));

		String sysPropCfg = System.getProperty(Constants.SYSTEMPROPERTY_CONFIGFILE);
		if (config != null)
			this.config = config;
		else if (sysPropCfg != null)
			this.config = new MediatorConfig(sysPropCfg);
		else
			this.config = new MediatorConfig();

		Resource storeAssemblerModelDesc = this.config.getStoreAssemblerModelDesc();
		if (storeAssemblerModelDesc != null) {
			Model m = null;
			try {
				log.info("Initializing global RDF store...");
				m = (Model) Assembler.general.openModel(storeAssemblerModelDesc);
			} catch (Exception e) {
				log.error("Failed to intialize the global RDF store. Starting with empty in-memory (volatile) RDF store.", e);
				m = ModelFactory.createDefaultModel();
			}
			this.store = m;
		} else {
			log.info("Initializing global RDF store with empty in-memory (volatile) RDF store.");
			this.store = ModelFactory.createDefaultModel();
		}
		
		try {
			if (reg != null)
				this.dsRegistry = reg;
			else
				this.dsRegistry = new DataSourceRegistryManagerImpl(this.config.getDataSourceRegistryConfig(), this.store);
		} catch (Exception e) {
			throw new MediatorException("Failed to start mediator." + e);
		}
		
		this.userRegistry = new UserRegistryImpl(this.config.getUserRegistryConfig(), this.store);
		this.federator = FederatorFactory.create(this.config.getFederatorConfig(), this.dsRegistry, this.userRegistry);

		// register mediator query engine and describe handler at ARQ registry
		MediatorQueryEngine.register();
		DescribeHandlerRegistry.get().clear();
		DescribeHandlerRegistry.get().add(new MediatorDescribeHandlerFactory());
		
		isReady = true;
		log.info("The mediator is ready and accepting global queries.");
	}
	
	public boolean isReady() {
		return isReady;
	}

	public MediatorConfig getConfig() {
		return config;
	}
	
	public DataSourceRegistry getDataSourceRegistry() {
		return dsRegistry;
	}
	
	public UserRegistry getUserRegistry() {
		return userRegistry;
	}
	
	public Model getGlobalStore() {
		return store;
	}
	
	public Federator getFederator() {
		return federator;
	}

	public synchronized void shutdown() {
		if (!isTerminated()) {
			isReady = false;
			log.info("Shutting down mediator...");
	
			if (dsRegistry != null)
				dsRegistry.shutdown();
	
			if (userRegistry != null)
				userRegistry.shutdown();
			
			log.info("Shutting down global RDF store...");
			store.close();
			if (store.getGraph() instanceof GraphTDB)
				TDB.sync(store);
			
			log.info("Mediator shut down cleanly.");
			terminated = true;
		} else
			log.error("Mediator already shut down.");
	}

	public boolean isTerminated() {
		return terminated;
	}
	
	public QueryExecution createQueryExecution(Query query) {
		QueryExecution qe = QueryExecutionFactory.create(query, new SemWIQDataset(this));
		return qe;
	}

	public QueryExecution createQueryExecution(String qryStr) {
		return createQueryExecution(createQuery(qryStr));
	}
	
	private Query createQuery(String qryStr) {
		return QueryFactory.create(qryStr, Syntax.syntaxARQ);
	}
	
//	public Op federate(Op op, Query qry) throws FederatorException {
//		if (renderPlans.isInfoEnabled())
//			GraphVizWriter.write(op, qry, "plan_original"); // done here not in compile() because if the QueryEngine is used, Algebra.compile() is done by QueryEngineBase
//		
//		Op federated = federator.federate(op);
//		if (renderPlans.isInfoEnabled())
//			GraphVizWriter.write(federated, qry, "plan_federated");
//		
//		return federated;
//	}
//	
//	public Op optimize(Op op, Query qry) throws OptimizerException {
////		Op optimized = optimizer.optimize(op);
//		Op optimized = op;
//		
//		if (renderPlans.isInfoEnabled())
//			GraphVizWriter.write(optimized, qry, "plan_optimized");
//		
//		return optimized;
//	}
//	
//	public Model explainQuery(Query query) throws FederatorException, OptimizerException {
//		Op origOp = compile(query);
//
//		System.out.println("==<<Query>>=========================================\n"
//				+ query.toString() + "\n\n" + "--- Original query plan -->\n"
//				+ origOp.toString(query.getPrefixMapping()) + "\n\n");
//
//		Op fedOp = federate(origOp, query);
//		System.out.println("--- Federated query plan -->\n" // need to show federated plan now, because it will be overridden by the optimized plan
//			+ fedOp.toString(query.getPrefixMapping()) + "\n\n");
//
//		Op optOp = optimize(fedOp, query);
//
//		System.out.println("--- Optimized query plan -->\n"
//			+ optOp.toString(query.getPrefixMapping()) + "\n\n");
//
//		// TODO
////		long avgResults = optOp.getAvgResults();
////		long minResults = optOp.getMinResults();
////		long maxResults = optOp.getMaxResults();
////		System.out.println("Estimated numer of results: \t" + avgResults + " (min: " + minResults + ", max: " + maxResults + ")");
////		System.out.println("Federation & Optimization: \t" +  + " \tms (may be much slower when loggers are set to debug levels!)");
//		
//		return ModelFactory.createDefaultModel(); // TODO fill model with explain description
//	}

}
