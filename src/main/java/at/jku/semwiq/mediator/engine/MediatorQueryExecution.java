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
package at.jku.semwiq.mediator.engine;

import at.jku.semwiq.mediator.Constants;
import at.jku.semwiq.mediator.dataset.SemWIQDataset;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.Transform;
import com.hp.hpl.jena.sparql.algebra.Transformer;
import com.hp.hpl.jena.sparql.engine.Plan;
import com.hp.hpl.jena.sparql.engine.QueryEngineFactory;
import com.hp.hpl.jena.sparql.engine.QueryExecutionBase;
import com.hp.hpl.jena.sparql.engine.binding.BindingRoot;
import com.hp.hpl.jena.sparql.engine.main.QueryEngineMain;
import com.hp.hpl.jena.sparql.util.Context;

/**
 * @author dorgon
 *
 * intercept additional query processing routines for mediator queries
 */
public class MediatorQueryExecution extends QueryExecutionBase {
	private final boolean processAtMediator = false;
	
	/**
	 * @param query
	 * @param dataset
	 * @param context
	 * @param qeFactory
	 */
	public MediatorQueryExecution(Query query, SemWIQDataset dataset, Context context, QueryEngineFactory qeFactory) {
		super(query, dataset, context, qeFactory);
		
		context.set(Constants.USING_MEDIATOR_QUERYEXECUTION, true);
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.sparql.engine.QueryExecutionBase#execAsk()
	 */
	@Override
	public boolean execAsk() {
		QueryExecution cheaper = checkForCheaperExecution();
		if (cheaper != null)
			return cheaper.execAsk();
		else
			return super.execAsk();
	}
	
	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.sparql.engine.QueryExecutionBase#execSelect()
	 */
	@Override
	public ResultSet execSelect() {
		QueryExecution cheaper = checkForCheaperExecution();
		if (cheaper != null)
			return cheaper.execSelect();
		else
			return super.execSelect();
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.sparql.engine.QueryExecutionBase#execConstruct()
	 */
	@Override
	public Model execConstruct() {
		QueryExecution cheaper = checkForCheaperExecution();
		if (cheaper != null)
			return cheaper.execConstruct();
		else
			return super.execConstruct();
	}
	
	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.sparql.engine.QueryExecutionBase#execDescribe()
	 */
	@Override
	public Model execDescribe() {
		QueryExecution cheaper = checkForCheaperExecution();
		if (cheaper != null)
			return cheaper.execDescribe();
		else
			return super.execDescribe();
	}
	
	private QueryExecution checkForCheaperExecution() {
		if (true)
			return null;
//		//TODO
//		
//		Op op = getPlan().getOp(); // trigger early plan generation
//		AnnotatedOp aOp = (AnnotatedOp) getContext().get(Constants.OPTIMIZED_OP);
//		long avg = aOp.getAvgResults();
//
//		// cheaper
//		if (false) {
//			Transform centralized = new CentralizedPlanTransform();
//			Op newPlan = Transformer.transform(centralized, aOp);
//			Plan plan = QueryEngineMain.getFactory().create(newPlan, getDataset().asDatasetGraph(), BindingRoot.create(), getContext());
//			return QueryExecutionFactory.create("");
//		} else
			return null;
	}
}
