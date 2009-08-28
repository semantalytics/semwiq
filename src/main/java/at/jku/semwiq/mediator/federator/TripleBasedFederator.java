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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.jku.semwiq.mediator.registry.DataSourceRegistry;
import at.jku.semwiq.mediator.registry.UserRegistry;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.Transformer;

/**
 * @author dorgon, Andreas Langegger, al@jku.at
 *
 */
public class TripleBasedFederator extends FederatorBase {
	private static final Logger log = LoggerFactory.getLogger(TripleBasedFederator.class);
	
	public TripleBasedFederator(Resource conf, DataSourceRegistry dsRegistry, UserRegistry userRegistry) {
		super(conf, dsRegistry, userRegistry);
		
		// if (conf != null) ...
	}

	/*
	 * (non-Javadoc)
	 * @see at.jku.semwiq.mediator.federator.Federator#federate(com.hp.hpl.jena.sparql.algebra.Op)
	 */
	public Op federate(Op op) throws FederatorException {
		try {
			TripleBasedFederatorTransform transform = new TripleBasedFederatorTransform(this);
			return Transformer.transform(transform, op);
		} catch (Throwable e) {
			throw new FederatorException(e);
		}
	}
	
}
