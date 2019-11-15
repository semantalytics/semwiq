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
package at.jku.semwiq.mediator.conf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.jku.semwiq.mediator.vocabulary.Config;
import at.jku.semwiq.mediator.vocabulary.VocabUtils;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class FederatorConfig {
	private static final Logger log = LoggerFactory.getLogger(FederatorConfig.class);
	
	/** federator config description (an RDF instance of a Federator RDFS subclass) */
	private final Resource confResource;
	
	/**
	 * @throws ConfigException
	 */
	public FederatorConfig() throws ConfigException {
		this(null);
	}
	
	public FederatorConfig(Resource resource) throws ConfigException {
		if (resource != null)
			VocabUtils.unknownPropertyWarnings(resource, Config.MediatorConfig.getModel(), log);
		confResource = resource;
	}
	
	/**
	 * @return the confResource, may be null
	 */
	public Resource getConfigResource() {
		return confResource;
	}
}
