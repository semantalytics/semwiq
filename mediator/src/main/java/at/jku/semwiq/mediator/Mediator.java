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

import at.jku.semwiq.mediator.conf.MediatorConfig;
import at.jku.semwiq.mediator.federator.Federator;
import at.jku.semwiq.mediator.federator.FederatorBase;
import at.jku.semwiq.mediator.registry.DataSourceRegistry;
import at.jku.semwiq.mediator.registry.UserRegistry;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.rdf.model.Model;

/**
 * @author dorgon
 *
 */
public interface Mediator {
	
	/** return true if ready for accepting queries */
	public boolean isReady();
	
	/** get configuration */
	public MediatorConfig getConfig();
	
	/** get the data source registry */
	public DataSourceRegistry getDataSourceRegistry();

	/** get the user registry */
	public UserRegistry getUserRegistry();
	
	/** get global store */
	public Model getGlobalStore();
	
	/** get federator */
	public FederatorBase getFederator();
	
	/** initiate shutdown */
	public void shutdown();
	
	/** true after termination */
	public boolean isTerminated();
	
	// create query execution
	
	/** create a new query execution from query string */
	public QueryExecution createQueryExecution(String qryStr);
	
	/** create a new query execution from Jena Query instance */
	public QueryExecution createQueryExecution(Query qry);

//	/** parse query */
//	public Query createQuery(String qryStr);
//	
//	/** compile query plan */
//	public Op compile(Query qry);
//	
//	/** federate query plan */
//	public Op federate(Op op, Query qry) throws FederatorException;
//	
//	/** optimize query plan */
//	public Op optimize(Op op, Query qry) throws OptimizerException;
//	
//	/** federate and optimize query plan */
//	public Op federateAndOptimize(Op op, Query qry) throws FederatorException, OptimizerException;
//
//	/** explain query */
//	public Model explainQuery(Query query) throws FederatorException, OptimizerException;
}
