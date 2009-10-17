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
package at.jku.semwiq.mediator.federator.triple;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import at.jku.semwiq.mediator.engine.op.OpFilteredBGP;
import at.jku.semwiq.mediator.registry.RegistryException;
import at.jku.semwiq.mediator.registry.model.DataSource;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.TransformCopy;
import com.hp.hpl.jena.sparql.algebra.Transformer;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP;
import com.hp.hpl.jena.sparql.algebra.op.OpJoin;
import com.hp.hpl.jena.sparql.algebra.op.OpNull;
import com.hp.hpl.jena.sparql.algebra.op.OpService;
import com.hp.hpl.jena.sparql.algebra.op.OpUnion;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.expr.ExprList;

/**
 * @author dorgon, Andreas Langegger, al@jku.at
 *
 */
public class TripleBasedFederatorTransform extends TransformCopy {
	private final TripleBasedFederator federator;
	
	public static Op apply(TripleBasedFederator fed, Op op) {
		return Transformer.transform(new TripleBasedFederatorTransform(fed), op);
	}
	
	/**
	 * constructor
	 */
	private TripleBasedFederatorTransform(TripleBasedFederator fed) {
		this.federator = fed;
	}
	
	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.sparql.algebra.TransformCopy#transform(com.hp.hpl.jena.sparql.algebra.op.OpBGP)
	 */
	@Override
	public Op transform(OpBGP opBGP) {
		ExprList exprs = (opBGP instanceof OpFilteredBGP) ? ((OpFilteredBGP) opBGP).getFilterReference().getExprs() : null;
		Map<DataSource, BasicPattern> dsPlans = new Hashtable<DataSource, BasicPattern>();
		
		Op prevJoin = null;
		Iterator<Triple> it = opBGP.getPattern().iterator();
		while (it.hasNext()) {
			Triple tp = it.next();
			List<DataSource> D;
			try {
				D = federator.getDataSourceRegistry().getAvailableRelevantDataSources(tp.getSubject(), tp.getPredicate(), tp.getObject(), exprs);
			} catch (RegistryException e) {
				throw new RuntimeException("Failed to select relevant data sources for triple pattern " + tp + ".", e);
			}
			
			if (D.size() == 0)
				return OpNull.create(); // already return with OpNull, BGP cannot be answered
			
			else if (D.size() == 1) {
				DataSource d = D.get(0);
				BasicPattern pattern = dsPlans.get(d);
				if (pattern == null) {
					pattern = new BasicPattern();
					dsPlans.put(d, pattern);
				}
				pattern.add(tp);
			
			// multiple data sources relevant for tp
			} else {
				Op prevUnion = null;
				for (DataSource d : D) {
					BasicPattern pattern = new BasicPattern();
					pattern.add(tp);
					OpBGP bgp = new OpBGP(pattern);
					OpService s = new OpService(Node.createURI(d.getSPARQLEndpointURL()), bgp);
					prevUnion = (prevUnion == null) ? s : new OpUnion(prevUnion, s);
				}
				
				// add to join sequence
				prevJoin = (prevJoin == null) ? prevUnion : OpJoin.create(prevJoin, prevUnion);
			}
			
		}

		// add data source specific plans to join sequence
		if (dsPlans.size() > 0) {
			for (DataSource d : dsPlans.keySet()) {
				OpBGP bgp = new OpBGP(dsPlans.get(d));
				OpService s = new OpService(Node.createURI(d.getSPARQLEndpointURL()), bgp);
				prevJoin = (prevJoin == null) ? s : OpJoin.create(prevJoin, s);
			}
		}
		
		if (prevJoin == null)
			return OpNull.create();
		else
			return prevJoin;
	}
	
//	public QueryIterator federate(QueryIterator input, OpFederate opFed, ExecutionContext context) throws FederatorException {
//		// build a hashset for triple patterns
//		Set<Triple> triplePatterns = new HashSet<Triple>();
//		triplePatterns.addAll(opFed.getPattern().getList());
//		
//		// calculate costs, order triple patterns
//		TreeMap<Integer, Triple> costs = new TreeMap<Integer, Triple>();
//		for (Triple t : triplePatterns) {
//			try {
//				for (DataSource ds : dsRegistry.getAvailableDataSources()) {
//					RDFStatsDataset stats = dsRegistry.getRDFStatsModel().getDataset(ds.getSPARQLEndpointURL());
//					int c = stats.triplesForFilteredPattern(t.getSubject(), t.getPredicate(), t.getObject(), opFed.getExprList());
//					costs.put(c, t);
//				}
//			} catch (Exception e) {
//				throw new FederatorException("Failed to calculate costs for triple pattern " + t + ".", e);
//			}
//		}
//		
//		QueryIterator iter = input;
//		for (Iterator<Triple> it = opFed.getPattern().iterator(); it.hasNext(); ) {
//			Triple tp = it.next();
//			// TODO only hand over expressions relevant to tp
//			iter = new QueryIterBlockedUnion(iter, tp, opFed.getExprList(), getDataSourceRegistry(), context);
//		}
//		return iter;
//	}
//	

}
