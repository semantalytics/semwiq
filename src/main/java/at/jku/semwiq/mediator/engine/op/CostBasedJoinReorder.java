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
package at.jku.semwiq.mediator.engine.op;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import at.jku.rdfstats.PlanCalculator;
import at.jku.rdfstats.RDFStatsDataset;
import at.jku.rdfstats.RDFStatsModel;
import at.jku.rdfstats.RDFStatsModelException;
import at.jku.semwiq.mediator.registry.DataSourceRegistry;
import atlas.logging.Log;

import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.OpVisitor;
import com.hp.hpl.jena.sparql.algebra.OpVisitorBase;
import com.hp.hpl.jena.sparql.algebra.OpWalker;
import com.hp.hpl.jena.sparql.algebra.TransformCopy;
import com.hp.hpl.jena.sparql.algebra.Transformer;
import com.hp.hpl.jena.sparql.algebra.op.OpConditional;
import com.hp.hpl.jena.sparql.algebra.op.OpJoin;
import com.hp.hpl.jena.sparql.algebra.op.OpLabel;
import com.hp.hpl.jena.sparql.algebra.op.OpNull;
import com.hp.hpl.jena.sparql.algebra.op.OpSequence;
import com.hp.hpl.jena.sparql.algebra.op.OpService;

/**
 * @author dorgon, Andreas Langegger, al@jku.at
 *
 * Walks bottom-up and reorders join sequences and left/right subplan of joins based on costs.
 * Tracks service and loads RDFStatsDataset while going down.
 */
public class CostBasedJoinReorder extends TransformCopy {
	private final RDFStatsModel stats;
	private RDFStatsDataset currentDataset;
	
	/** tracks OpService and sets currentDataset top-down */
	private final OpVisitor preTracker = new OpVisitorBase() {
		@Override
	    public void visit(OpService op) {
			if (currentDataset != null)
				throw new RuntimeException("Unexpected state: currentDataset not null, looks like plan has nested SERVICE operators.");
	    	String uri = op.getService().getURI();
	    	try {
				currentDataset = stats.getDataset(uri);
			} catch (RDFStatsModelException e) {
				throw new RuntimeException("Couldn't get RDFStatsDataset for SERVICE uri <" + uri + ">.", e);
			}
	    }
	};
	private final OpVisitor postTracker = new OpVisitorBase() {
		@Override
	    public void visit(OpService op) {
			if (currentDataset == null)
				throw new RuntimeException("Unexpected state: currentDataset is null.");
			currentDataset = null;
	    }
	};
	
	/** public static caller
	 * 
	 * @param stats
	 * @param op
	 * @return transformed op
	 */
	public static Op applyTransformations(RDFStatsModel stats, Op op) {
		CostBasedJoinReorder t = new CostBasedJoinReorder(stats);
		return t.applyTransformations(op);
	}
	
	/**
	 * @param stats
	 */
	private CostBasedJoinReorder(RDFStatsModel stats) {
		this.stats = stats;
	}

	private Op applyTransformations(Op op) {
		return Transformer.transform(this, op, preTracker, postTracker);
	}
	
	@Override
	public Op transform(OpJoin opJoin, Op left, Op right) {
		Long[] cLeft = new PlanCalculator(stats, currentDataset).calculate(left);
		Long[] cRight = new PlanCalculator(stats, currentDataset).calculate(right);
		
		if (cLeft[PlanCalculator.MAX] == 0 && cRight[PlanCalculator.MAX] == 0)
			return OpNull.create();
		
		if (cRight[PlanCalculator.AVG] < cLeft[PlanCalculator.AVG])
			return OpJoin.create(right, left);
		return super.transform(opJoin, left, right);
	}
	
	@Override
	public Op transform(OpSequence opSequence, List<Op> elts) {
		TreeMap<Long, List<Op>> cSub = new TreeMap<Long, List<Op>>();
		long maxTotal = 0;
		for (Op sub : elts) {
			Long[] cost = new PlanCalculator(stats, currentDataset).calculate(sub);
			if (cost == null)
				cost = new Long[] { Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE }; // assume maximum for unknown
			maxTotal += cost[PlanCalculator.MAX];
			List<Op> entry = cSub.get(cost[PlanCalculator.AVG]);
			if (entry == null) {
				entry = new ArrayList<Op>();
				cSub.put(cost[PlanCalculator.AVG], entry);
			}
			entry.add(sub);
		}
		
		if (maxTotal == 0)
			return OpNull.create();
		
		// create new sequence with ops ordered by costs
		OpSequence seq = OpSequence.create();
		for (List<Op> entry : cSub.values()) {
			for (Op sub : entry)
				seq.add(sub);
		}
		return seq;
	}
	
}
