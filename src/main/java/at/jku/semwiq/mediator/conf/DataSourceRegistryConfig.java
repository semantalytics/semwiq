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

import at.jku.rdfstats.RDFStatsConfiguration;
import at.jku.semwiq.mediator.vocabulary.Config;
import at.jku.semwiq.mediator.vocabulary.VocabUtils;

import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

public class DataSourceRegistryConfig {
	private static final Logger log = LoggerFactory.getLogger(DataSourceRegistryConfig.class);
		
	/** the RDFStats Config */
	private RDFStatsConfiguration globalStatsConfig;
	
	/**
	 * default constructor
	 * @throws ConfigException
	 */
	public DataSourceRegistryConfig() throws ConfigException {
		this(null);
	}

	/**
	 * @param resource must not be null, required
	 * @throws ConfigException
	 */
	public DataSourceRegistryConfig(Resource resource) throws ConfigException {
		if (resource != null) {
			VocabUtils.unknownPropertyWarnings(resource, Config.MediatorConfig.getModel(), log);
	
			Statement s = resource.getProperty(Config.globalStatsConfig);
			if (s != null && s.getObject().isResource()) {
				Resource statsResource = s.getResource();

				// also warn for unknown RDFStats properties...
				VocabUtils.unknownPropertyWarnings(statsResource, at.jku.rdfstats.vocabulary.Config.Configuration.getModel(), log);
					
				try {
					globalStatsConfig = RDFStatsConfiguration.create(statsResource);	
				} catch (Exception e) {
					throw new ConfigException("Failed to initialize global RDFStats config.", e);
				}
			} else
				throw new ConfigException("Cannot initialize the mediator without a <" + Config.globalStatsConfig + "> property.");
		
		} else {
			this.globalStatsConfig = RDFStatsConfiguration.create(
					ModelFactory.createDefaultModel(),
					null,
					null,
					RDFStatsConfiguration.DEFAULT_PREFSIZE,
					null,
					null,
					RDFStatsConfiguration.DEFAULT_STRHIST_MAXLEN,
					RDFStatsConfiguration.DEFAULT_QUICK_MODE,
					null);
		}
	}
	
	public RDFStatsConfiguration getRdfStatsConfig() {
		return globalStatsConfig;
	}
	
}
