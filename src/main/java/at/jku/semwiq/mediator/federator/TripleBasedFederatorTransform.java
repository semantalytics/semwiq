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

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.jku.semwiq.mediator.registry.RegistryException;
import at.jku.semwiq.mediator.registry.model.DataSource;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.TransformCopy;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP;
import com.hp.hpl.jena.sparql.algebra.op.OpJoin;
import com.hp.hpl.jena.sparql.algebra.op.OpNull;
import com.hp.hpl.jena.sparql.algebra.op.OpSequence;
import com.hp.hpl.jena.sparql.algebra.op.OpService;
import com.hp.hpl.jena.sparql.algebra.op.OpUnion;
import com.hp.hpl.jena.sparql.core.BasicPattern;

/**
 * @author dorgon, Andreas Langegger, al@jku.at
 *
 */
public class TripleBasedFederatorTransform extends TransformCopy {
	private static final Logger log = LoggerFactory.getLogger(TripleBasedFederatorTransform.class);
	
	private final FederatorBase federator;
	private final Map<String, BasicPattern> subqueries;
	
	
	/**
	 * @param config
	 * @param registry
	 */
	public TripleBasedFederatorTransform(FederatorBase federator) {
		this.federator = federator;
		this.subqueries = new Hashtable<String, BasicPattern>();
	}

	@Override
	public Op transform(OpBGP opBGP) {
		try {
			Op prevJoinOp = null;
			
			Iterator<Triple> tp = opBGP.getPattern().iterator();
			while (tp.hasNext()) {
				// include unions over all sites
				Op prevUnionOp = null;

				Triple t = tp.next();
				List<DataSource> dataSources = federator.getDataSourceRegistry().getAvailableRelevantDataSources(t.getSubject(), t.getPredicate(), t.getObject(), null);
				
				if (dataSources.size() == 0)
					continue;
				
				else if (dataSources.size() == 1) { // single endpoint
					DataSource endpoint = dataSources.get(0);
					BasicPattern pt = subqueries.get(endpoint.getSPARQLEndpointURL());
					if (pt == null) {
						pt = new BasicPattern(); // add triple later
						
						OpBGP newBGP = new OpBGP(pt);
						OpService newService = new OpService(Node.createURI(endpoint.getSPARQLEndpointURL()), newBGP);
						prevUnionOp = (prevUnionOp == null) ? newService : new OpUnion(prevUnionOp, newService);												
					}

					// add pattern
					pt.add(new Triple(t.getSubject(), t.getPredicate(), t.getObject()));
				
				// multiple endpoints
				} else if (dataSources.size() > 1) {
					for (DataSource endpoint : dataSources) {
						BasicPattern pt = new BasicPattern();
						pt.add(new Triple(t.getSubject(), t.getPredicate(), t.getObject()));

						OpBGP newBGP = new OpBGP(pt);
						OpService newService = new OpService(Node.createURI(endpoint.getSPARQLEndpointURL()), newBGP);
						newService.setSubOp(newBGP);
						prevUnionOp = (prevUnionOp == null) ? newService : new OpUnion(prevUnionOp, newService);												
					}
				}
				
				prevJoinOp = (prevJoinOp == null) ? prevUnionOp : OpSequence.create(prevJoinOp, prevUnionOp); // OpJoin.create(prevJoinOp, prevUnionOp);
			}
			
			if (prevJoinOp == null)
				return OpNull.create();
			else
				return prevJoinOp;
		} catch (RegistryException e) {
			throw new RuntimeException("Failed to federate query.", e);
		}
	}

}
