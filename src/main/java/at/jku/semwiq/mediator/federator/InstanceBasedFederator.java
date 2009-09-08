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

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.table.TableN;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIter;
import com.hp.hpl.jena.sparql.util.Context;


import at.jku.semwiq.mediator.engine.op.OpFederate;
import at.jku.semwiq.mediator.registry.DataSourceRegistry;
import at.jku.semwiq.mediator.registry.UserRegistry;
import at.jku.semwiq.mediator.vocabulary.Config;
import at.jku.semwiq.mediator.vocabulary.VocabUtils;

/**
 * @author dorgon, Andreas Langegger, al@jku.at
 *
 */
public class InstanceBasedFederator extends FederatorBase {
	private static final Logger log = LoggerFactory.getLogger(InstanceBasedFederator.class);
	
	public final static boolean DEFAULT_INFERTYPES = true;
	public final static boolean DEFAULT_SUBSUMPTION = true;
	
	/** type inference */
	private boolean inferTypes;

	/** subsumption reasoning */
	private boolean subsumption;
	
	public InstanceBasedFederator(DataSourceRegistry dsRegistry, UserRegistry userRegistry) {
		this(null, dsRegistry, userRegistry);
	}
	
	public InstanceBasedFederator(Resource conf, DataSourceRegistry dsRegistry, UserRegistry userRegistry) {
		super(conf, dsRegistry, userRegistry);
		
		if (conf != null) {
			VocabUtils.unknownPropertyWarnings(conf, Config.inferTypes.getModel(), log);
			
			inferTypes = (conf.hasProperty(Config.inferTypes)) ?
					conf.getProperty(Config.inferTypes).getBoolean() : DEFAULT_INFERTYPES;

			subsumption = (conf.hasProperty(Config.subsumption)) ?
					conf.getProperty(Config.subsumption).getBoolean() : DEFAULT_INFERTYPES;				 
		} else {
			this.inferTypes = DEFAULT_INFERTYPES;
			this.subsumption = DEFAULT_SUBSUMPTION;
		}

	}

	/**
	 * @return the inferTypes
	 */
	public boolean isInferTypes() {
		return inferTypes;
	}
	
	/**
	 * @return the subsumption
	 */
	public boolean isSubsumption() {
		return subsumption;
	}

	/* (non-Javadoc)
	 * @see at.jku.semwiq.mediator.federator.Federator#federate(com.hp.hpl.jena.sparql.engine.QueryIterator, at.jku.semwiq.mediator.engine.op.OpFederate, com.hp.hpl.jena.sparql.engine.ExecutionContext)
	 */
	public QueryIterator federate(QueryIterator input, OpFederate op, ExecutionContext context) throws FederatorException {
		throw new FederatorException("Instance-based federation not implemented");
	}
	
}
