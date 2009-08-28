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

import at.jku.semwiq.mediator.conf.ConfigException;
import at.jku.semwiq.mediator.conf.FederatorConfig;
import at.jku.semwiq.mediator.registry.DataSourceRegistry;
import at.jku.semwiq.mediator.registry.UserRegistry;
import at.jku.semwiq.mediator.vocabulary.Config;

import com.hp.hpl.jena.vocabulary.RDF;

/**
 * @author dorgon, Andreas Langegger, al@jku.at
 *
 */
public class FederatorFactory {

	public static Federator create(FederatorConfig config, DataSourceRegistry dsRegistry, UserRegistry userRegistry) throws ConfigException {
		// by default use triple-based federator (if not configured)
		if (config.getConfigResource() == null)
			return new TripleBasedFederator(null, dsRegistry, userRegistry);
		
		else if (config.getConfigResource().hasProperty(RDF.type, Config.TripleBasedFederatorConfig))
			return new TripleBasedFederator(config.getConfigResource(), dsRegistry, userRegistry);
		
		else if (config.getConfigResource().hasProperty(RDF.type, Config.InstanceBasedFederatorConfig))
			return new InstanceBasedFederator(config.getConfigResource(), dsRegistry, userRegistry);
		
		else
			throw new ConfigException("Unknown federator type.");
	}

}
