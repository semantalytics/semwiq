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
import at.jku.semwiq.mediator.engine.op.OpExecutorSemWIQ;

import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.OpAsQuery;
import com.hp.hpl.jena.sparql.algebra.opt.Optimize;
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
import com.hp.hpl.jena.sparql.engine.iterator.QueryIteratorBase;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIteratorCheck;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIteratorWrapper;
import com.hp.hpl.jena.sparql.engine.main.OpExecutor;
import com.hp.hpl.jena.sparql.engine.main.OpExecutorFactory;
import com.hp.hpl.jena.sparql.engine.main.QC;
import com.hp.hpl.jena.sparql.engine.main.QueryEngineMain;
import com.hp.hpl.jena.sparql.serializer.QuerySerializer;
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
	
	static {
		ARQ.setStrictMode();
		ARQ.getContext().set(ARQ.filterPlacement, true);
		log.info("ARQ strict mode enabled");
	}
	
	/** Constructor: delegate to QueryEngineMain */
	public MediatorQueryEngine(Query query, DatasetGraph dataset, Binding initial, Context context) {
		super(query, dataset, initial, context);
		context.set(Constants.QUERY, query);
		context.set(Constants.EXEC_TIME_START, System.currentTimeMillis());
	}

	/** Constructor: delegate to QueryEngineMain */
	public MediatorQueryEngine(Query query, DatasetGraph dataset) {
		this(query, dataset, null, null);
	}

    public MediatorQueryEngine(Op op, DatasetGraph dataset, Binding input, Context context) {
    	super(op, dataset, input, context) ;
    	context.set(Constants.QUERY, OpAsQuery.asQuery(op)); // back into query, usually this method is not used
		context.set(Constants.EXEC_TIME_START, System.currentTimeMillis());
    }

    @Override
    public QueryIterator eval(Op op, DatasetGraph dsg, Binding input, Context context) {
    	// call with empty dataset to prevent ARQ from calling graphBaseFind() infinite loops cause this again create a new query execution on the SemWIQDataset

    	QC.setFactory(context, new OpExecutorFactory() {
    		public OpExecutor create(ExecutionContext execCxt) { return new OpExecutorSemWIQ(execCxt); }
    	});

    	QueryIterator queryIter = super.eval(op, new DataSourceGraphImpl(), input, context);
    	
    	// intercept moveToNextBinding() in order to measure times
    	final Context ctx = context;
    	QueryIterator iter = new QueryIteratorWrapper(queryIter) {
    		private boolean first = true;
    		
    		@Override
    		protected boolean hasNextBinding() {
    			boolean has = super.hasNextBinding();
    			if (!has) // no more results, at the end
    				ctx.set(Constants.EXEC_TIME_ALLRESULTS, System.currentTimeMillis() - (Long) ctx.get(Constants.EXEC_TIME_START));
    			return has;
    		}
    		
    		@Override
    		protected Binding moveToNextBinding() {
    			if (first) { // first result
    				ctx.set(Constants.EXEC_TIME_FIRSTRESULT, System.currentTimeMillis() - (Long) ctx.get(Constants.EXEC_TIME_START));
    				first = false;
    			}
    			return super.moveToNextBinding();
    		}
    	};
    	return iter;
    }
    
	@Override
	protected Op modifyOp(Op op) {
		try {
			context.set(Constants.OP_ORIGINAL, op);
			
	    	// ARQ optimizations (simplify join identities, delabel, bind expr functions, property functions, break conjunctions, 
	    	// transform equality filters, filter placement, join/ljoin => sequence/conditional, flatten prop paths
			long start = System.currentTimeMillis();
	    	Op opt = Optimize.optimize(op, context);
	    	context.set(Constants.EXEC_TIME_OPTIMIZE, System.currentTimeMillis() - start); 
			context.set(Constants.OP_OPTIMIZED, opt);

			Mediator mediator = ((SemWIQDatasetGraph) dataset).getMediator();

			Long[] estimates = new Long[3]; // collect estimates
			start = System.currentTimeMillis();
			Op fed = mediator.getFederator().federate(op, context, estimates);
			context.set(Constants.EXEC_TIME_FEDERATE, System.currentTimeMillis() - start); 
			context.set(Constants.OP_FEDERATED, fed);

			context.set(Constants.ESTIMATED_MIN_RESULTS, estimates[0]);
			context.set(Constants.ESTIMATED_AVG_RESULTS, estimates[1]);
			context.set(Constants.ESTIMATED_MAX_RESULTS, estimates[2]);

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

		public boolean accept(Query query, DatasetGraph dsg, Context context) {
			return (dsg instanceof SemWIQDatasetGraph);
		}

		public Plan create(Query query, DatasetGraph dsg, Binding initial, Context context) {
			MediatorQueryEngine engine = new MediatorQueryEngine(query, dsg, initial, context);
			return engine.getPlan();
		}

		public boolean accept(Op op, DatasetGraph dsg, Context context) {
			return (dsg instanceof SemWIQDatasetGraph);
		}

		public Plan create(Op op, DatasetGraph dsg, Binding initial, Context context) {
			MediatorQueryEngine engine = new MediatorQueryEngine(op, dsg, initial, context);
			return engine.getPlan();
		}

	};

}
