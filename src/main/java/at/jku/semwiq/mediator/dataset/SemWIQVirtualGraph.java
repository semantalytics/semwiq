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
package at.jku.semwiq.mediator.dataset;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.jku.rdfstats.RDFStatsDataset;
import at.jku.rdfstats.RDFStatsModel;
import at.jku.semwiq.mediator.Mediator;

import com.hp.hpl.jena.graph.Capabilities;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_Literal;
import com.hp.hpl.jena.graph.Node_Variable;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.TripleMatch;
import com.hp.hpl.jena.graph.impl.GraphBase;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;
import com.hp.hpl.jena.sparql.util.FmtUtils;
import com.hp.hpl.jena.util.iterator.ClosableIterator;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.util.iterator.NiceIterator;
import com.hp.hpl.jena.util.iterator.NullIterator;

/**
 * @author dorgon, Andreas Langegger, al@jku.at
 *
 * Represents the virtual global graph integrated by SemWIQ
 */
public class SemWIQVirtualGraph extends GraphBase implements Graph {
	private Logger log = LoggerFactory.getLogger(SemWIQVirtualGraph.class);
	private final Mediator mediator;
	
	/**
	 * constructor
	 */
	public SemWIQVirtualGraph(Mediator m) {
		this.mediator = m;
	}
	
	private final Capabilities capabilities = new SemWIQCapabilities();

	public void close() {
		mediator.shutdown();
	}

	public Capabilities getCapabilities() { 
		return this.capabilities;
	}

	/** size based on RDFStats, no queries required */
	protected int graphBaseSize() {
		try {
			int total = 0;
			RDFStatsModel stats = mediator.getDataSourceRegistry().getRDFStatsModel();
			List<RDFStatsDataset> dsList = stats.getDatasets();
			for (RDFStatsDataset ds : dsList)
				total += ds.getTriplesTotal();
			return total;
		} catch (Exception e) {
			log.error("Failed to obtain global graph size!", e);
			return 0;
		}
	}
	
	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.graph.impl.GraphBase#graphBaseContains(com.hp.hpl.jena.graph.Triple)
	 */
	@Override
	protected boolean graphBaseContains(Triple t) {
		try {
			RDFStatsModel stats = mediator.getDataSourceRegistry().getRDFStatsModel();
			List<RDFStatsDataset> dsList = stats.getDatasets();
			for (RDFStatsDataset ds : dsList)
				if (ds.triplesForPattern(t.getSubject(), t.getPredicate(), t.getObject()) > 0)
					return true;
			return false;
		} catch (Exception e) {
			log.error("Failed to query RDFStats to check if global virtual graph contains " + t + ").", e);
			return super.graphBaseContains(t);
		}
	}
	
	public ExtendedIterator<Triple> graphBaseFind( TripleMatch m ) {
		String qry = "SELECT * WHERE { " + toQueryNode(m.getMatchSubject(), "s") + " " + toQueryNode(m.getMatchPredicate(), "p") + " " + toQueryNode(m.getMatchObject(), "o") + " }";
		QueryExecution qe = mediator.createQueryExecution(qry);
		ResultSet r = qe.execSelect();
		
		ExtendedIterator<Triple> result = NullIterator.emptyIterator();
		return result.andThen(new SolutionsAsTriplesIterator(qe, m, r)); // pass qe to close after iteration
    }

	private String toQueryNode(Node n, String pos) {
		if (n == null || n.isVariable())
			return "?" + pos;
		else
			return FmtUtils.stringForNode(n, getPrefixMapping());
	}
	
	/**
	 * @return the mediator
	 */
	public Mediator getMediator() {
		return mediator;
	}
	
	class SolutionsAsTriplesIterator extends NiceIterator<Triple> {
		private final QueryExecution qe;
		private final ResultSet r;
		private final TripleMatch tm;
		
		public SolutionsAsTriplesIterator(QueryExecution qe, TripleMatch tm, ResultSet r) {
			this.qe = qe;
			this.tm = tm;
			this.r = r;
		}
		
		public boolean hasNext() {
			boolean b = r.hasNext();
			// Jena's NiceIterator.andThen() assumes exhausted iterators to auto-close,
			// see Jena Bug 2565071
			if (!b)
				close();
			return b;
		}
		
		public Triple next() {
			QuerySolution sol = r.next();
			Node s, p, o;
			
			if (tm.getMatchSubject() == null)
				s = sol.get("s").asNode();
			else
				s = tm.getMatchSubject();
			if (tm.getMatchPredicate() == null)
				p = sol.get("p").asNode();
			else
				p = tm.getMatchPredicate();
			if (tm.getMatchObject() == null)
				o = sol.get("o").asNode();
			else
				o = tm.getMatchObject();
			
			return new Triple(s, p, o);
		}
		
		public void close() {
			qe.close();
			super.close();
		}
		
		public void remove() {
			throw new RuntimeException("remove() not supported by SolutionsAsTriplesIterator");
		}
		
	}
	
    class SemWIQCapabilities implements Capabilities {
        public boolean sizeAccurate() { return true; }
        public boolean addAllowed() { return addAllowed( false ); }
        public boolean addAllowed( boolean every ) { return false; }
        public boolean deleteAllowed() { return deleteAllowed( false ); }
        public boolean deleteAllowed( boolean every ) { return false; }
        public boolean canBeEmpty() { return true; }
        public boolean iteratorRemoveAllowed() { return false; }
        public boolean findContractSafe() { return false; }
        public boolean handlesLiteralTyping() { return true; }
    }
    
}
