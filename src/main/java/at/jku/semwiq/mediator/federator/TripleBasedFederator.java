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
package at.jku.semwiq.mediator.federator;

import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.jku.semwiq.mediator.engine.iter.QueryIterBlockedUnion;
import at.jku.semwiq.mediator.engine.op.OpFederate;
import at.jku.semwiq.mediator.registry.DataSourceRegistry;
import at.jku.semwiq.mediator.registry.UserRegistry;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;

/**
 * @author dorgon, Andreas Langegger, al@jku.at
 *
 */
public class TripleBasedFederator extends FederatorBase {
	private static final Logger log = LoggerFactory.getLogger(TripleBasedFederator.class);
	
	public TripleBasedFederator(Resource conf, DataSourceRegistry dsRegistry, UserRegistry userRegistry) {
		super(conf, dsRegistry, userRegistry);
	}

	public QueryIterator federate(QueryIterator input, OpFederate opFed, ExecutionContext context) throws FederatorException {
		QueryIterator iter = input;
		for (Iterator<Triple> it = opFed.getPattern().iterator(); it.hasNext(); ) {
			Triple tp = it.next();
			iter = new QueryIterBlockedUnion(iter, tp, opFed.getExprList(), getDataSourceRegistry(), context);
		}
		return iter;
	}
	
//	private class QueryIterBlockedSubQuery extends QueryIterBlockedRepeatApply {
//		
//		/**
//		 * @param input
//		 * @param context
//		 */
//		public QueryIterBlockedSubQuery(QueryIterator input, ExecutionContext context) {
//			super(input, context);
//		}
//
//		/* (non-Javadoc)
//		 * @see at.jku.semwiq.mediator.engine.op.QueryIterBlockedRepeatApply#nextStage(com.hp.hpl.jena.sparql.algebra.table.TableN)
//		 */
//		@Override
//		protected QueryIterator nextStage(TableN bindings) {
//			// based on bindings decide next triple pattern to execute...
//			try {
//				List<DataSource> sources = dsRegistry.getAvailableRelevantDataSources(tp.getSubject(), tp.getPredicate(), tp.getObject(), exprs);
//				for (DataSource ds : sources) {
////					TableN outerBindings = new TableN();
////		    		ProjectedBindingsCache cache = new ProjectedBindingsCache(outerBindings, VarUtils.getVars(tp));
////		    	    query.setInitialBindingTable(cache.getProjectedBindingsTable());
//		    	    
//					BasicPattern bgp = new BasicPattern();
//					bgp.add(tp);
//					Op opBGP = new OpBGP(pattern);
//					OpService opService = new OpService(Node.createURI(ds.getSPARQLEndpointURL()), opBGP);			        
//			        HttpQuery httpQuery = new HttpQuery(opService.getService().getURI()) ;
//			        httpQuery.addParam(HttpParams.pQuery, OpAsQuery.asQuery(opService).toString());
//			        httpQuery.setAccept(HttpParams.contentTypeResultsXML) ;
//			        InputStream in = httpQuery.exec() ;
//			        
//			        ResultSet rs = ResultSetFactory.fromXML(in) ;
//			        QueryIterator qIter = new QueryIteratorResultSet(rs) ;
//			        
//			        // Need to put the outerBinding as parent to every binding of the service call.
//			        // There should be no variables in common because of the OpSubstitute.substitute
//			        
//			        // TODO insert bindings (join outerBindings with result bindings)
////			        QueryIterator qIter2 = cache.addCachedBindings(qIter, getExecContext());
//			        qIter2.add(qIter);
//				}
//				return qIter2;
//			} catch (RegistryException e) {
//				throw new FederatorException("Failed to federate because of problems with the data source registry.", e);
//			}		}
//		
//	}
//	
//	private class Scheduler {
//		private final BasicPattern pattern;
//		private final ExprList exprs;
//		private final Iterator<Triple> it;
//		
//		public Scheduler(BasicPattern pattern, ExprList exprs) {
//			this.pattern = pattern;
//			this.exprs = exprs;
//			this.it = pattern.iterator();
//		}
//		
//		public boolean hasNext() {
//			return it.hasNext();
//		}
//
//		public QueryIterator next(QueryIterator input, ExecutionContext context) throws FederatorException {
//	        QueryIterConcat qIter2 = new QueryIterConcat(context);
//			Triple tp = it.next();
//
//		}
//
//		public void remove() { throw new RuntimeException("Not implemented: PatternScheduler.remove()."); }
//	}
//	
//	private class FederateWorker {
//		OpFederate opFed;
//		QueryIterator input;
//		ExecutionContext context;
//		
//		public FederateWorker(QueryIterator input, OpFederate opFed, ExecutionContext context) {
//			this.input = input;
//			this.opFed = opFed;
//			this.context = context;
//		}
//
//		public QueryIterator execute() throws FederatorException {
//			List<BasicPattern> bgpGroups = groupDependentPatterns(opFed.getPattern());
//			if (bgpGroups.size() == 0)
//				return input;
//			else if (bgpGroups.size() > 1)
//				throw new FederatorException("Not implemented: nested loop join over independent BGP groups: " + opFed);
//			
//			Scheduler sched = new Scheduler(bgpGroups.get(0), opFed.getExprList());
//			QueryIterConcat iter = new QueryIterConcat(context);
//			Triple next;
//			
//			while (sched.hasNext())
//				iter.add(sched.next(iter, context));
//			
//			return iter;
//		}

//		public Op transform(OpBGP opBGP) {
//			// place OpJoin around multiple independent triple pattern groups
//			List<BasicPattern> bgpGroups = groupDependentPatterns(opBGP.getPattern());
//			for (BasicPattern bgp : bgpGroups) {
//				newFed = null;
//				prevOpSeq = null;
//				
//				// place OpSequence around single dependent triple patterns
//				Iterator<Triple> tp = bgp.iterator();
//				while (tp.hasNext()) {
//					newFed = new OpFederate(tp.next(), federator);				
//					prevOpSeq = (prevOpSeq == null) ? newFed : OpSequence.create(newFed, prevOpSeq);
//				}
//	
//				prevOpJoin = (prevOpJoin == null) ? prevOpSeq : OpJoin.create(prevOpSeq, prevOpJoin);
//			}
//			
//			return (prevOpJoin == null) ? OpNull.create() : prevOpJoin;
//		}
	
//		/**
//		 * @param pattern
//		 * @return
//		 */
//		private List<BasicPattern> groupDependentPatterns(BasicPattern pattern) {
//			List<BasicPattern> patterns = new ArrayList<BasicPattern>();
//			Iterator<Triple> it = pattern.iterator();
//			Triple t;
//			while (it.hasNext()) {
//				t = it.next();
//				BasicPattern bp = findMatching(patterns, t);
//				bp.add(t);
//			}
//			return patterns;
//		}
//		
//		private BasicPattern findMatching(List<BasicPattern> patterns, Triple t) {
//			Set<Var> tVars = VarUtils.getVars(t);
//			for (BasicPattern bp : patterns) {
//				Iterator<Triple> it = bp.iterator();
//				while (it.hasNext()) {
//					Set<Var> commonVars = VarUtils.getVars(it.next());
//					commonVars.retainAll(tVars);
//					if (commonVars.size() > 0)
//						return bp;
//				}
//			}
//	
//			BasicPattern bp = new BasicPattern();
//			patterns.add(bp);
//			return bp;
//	
//		}
//	}
}