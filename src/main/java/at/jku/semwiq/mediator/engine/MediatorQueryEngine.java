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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.jku.semwiq.mediator.Constants;
import at.jku.semwiq.mediator.Mediator;
import at.jku.semwiq.mediator.dataset.SemWIQDatasetGraph;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.core.DataSourceGraphImpl;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.core.Substitute;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.Plan;
import com.hp.hpl.jena.sparql.engine.QueryEngineFactory;
import com.hp.hpl.jena.sparql.engine.QueryEngineRegistry;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterRoot;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIteratorCheck;
import com.hp.hpl.jena.sparql.engine.main.QC;
import com.hp.hpl.jena.sparql.engine.main.QueryEngineMain;
import com.hp.hpl.jena.sparql.util.Context;
import com.hp.hpl.jena.tdb.graph.GraphFactory;

/**
 * Extended QueryEngineMain for the SemWIQ mediator.
 * 
 * A reference to the corresponding mediator instance is stored in a
 * context symbol Mediator.MEDIATOR_REF_SYMBOL
 * 
 * @author dorgon
 */

public class MediatorQueryEngine extends QueryEngineMain {
	private static final Logger log = LoggerFactory.getLogger(MediatorQueryEngine.class);
	
	private long start, stop;
	
	/** Constructor: delegate to QueryEngineMain */
	public MediatorQueryEngine(Query query, DatasetGraph dataset, Binding initial, Context context) {
		super(query, dataset, initial, context);
		start = System.currentTimeMillis();
		context.set(Constants.QUERY, query);
	}

	/** Constructor: delegate to QueryEngineMain */
	public MediatorQueryEngine(Query query, DatasetGraph dataset) {
		this(query, dataset, null, null);
	}

    public MediatorQueryEngine(Op op, DatasetGraph dataset, Binding input, Context context) {
    	super(op, dataset, input, context) ;
    }

    @Override
    public QueryIterator eval(Op op, DatasetGraph dsg, Binding input, Context context) {
    	// call with empty dataset
    	return super.eval(op, new DataSourceGraphImpl(), input, context);
    }
    
    /** returns annotated op */
	@Override
	protected Op modifyOp(Op op) {
		try {
	    	if (context.get(Constants.USING_MEDIATOR_QUERYEXECUTION) == null || !(Boolean) context.get(Constants.USING_MEDIATOR_QUERYEXECUTION))
	    		log.warn("Not using MediatorQueryExecution, cannot process query in centralized mode if it would be cheaper. If you want to make use of " +
	    				"full optimizations, please use Mediator.createQueryExecution().");

			context.set(Constants.ORIGINAL_OP, op);

			Mediator mediator = ((SemWIQDatasetGraph) dataset).getMediator();
			Query qry = (Query) context.get(Constants.QUERY); // set by MediatorQueryEngine constructor
			
			Op opt = op;
			start = System.currentTimeMillis();
			Op fed = mediator.getFederator().federate(op);
			stop = System.currentTimeMillis();
			context.set(Constants.FEDERATION_TIME, new Long(stop-start));
			context.set(Constants.FEDERATED_OP, fed);
//			
//			start = System.currentTimeMillis();
//			Op opt = mediator.optimize(fed, qry);
//			stop = System.currentTimeMillis();
//			context.set(Constants.OPTIMIZATION_TIME, new Long(System.currentTimeMillis() - start));
//			context.set(Constants.OPTIMIZED_OP, opt);

			//TODO
//			context.set(Constants.ESTIMATED_MIN_RESULTS, opt.getMinResults());
//			context.set(Constants.ESTIMATED_AVG_RESULTS, opt.getAvgResults());
//			context.set(Constants.ESTIMATED_MAX_RESULTS, opt.getMaxResults());
			
			return fed;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	static public QueryEngineFactory getFactory() {
		return factory;
	}

	static public void register() {
		QueryEngineRegistry.addFactory(factory);
	}

	static public void unregister() {
		QueryEngineRegistry.removeFactory(factory);
	}

	/**
	 * factory
	 */
	private static QueryEngineFactory factory = new QueryEngineFactory() {

		public boolean accept(Query query, DatasetGraph dataset, Context context) {
			return (dataset instanceof SemWIQDatasetGraph);
		}

		public Plan create(Query query, DatasetGraph dataset, Binding initial, Context context) {
			MediatorQueryEngine engine = new MediatorQueryEngine(query, dataset, initial, context);
			return engine.getPlan();
		}

		public boolean accept(Op op, DatasetGraph dataset, Context context) {
			return (dataset instanceof SemWIQDatasetGraph);
		}

		public Plan create(Op op, DatasetGraph dataset, Binding initial, Context context) {
			MediatorQueryEngine engine = new MediatorQueryEngine(op, dataset, initial, context);
			return engine.getPlan();
		}

	};

}
