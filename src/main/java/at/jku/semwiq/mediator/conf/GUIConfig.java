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

import java.util.Hashtable;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.jku.semwiq.mediator.vocabulary.Config;
import at.jku.semwiq.mediator.vocabulary.VocabUtils;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl;

/**
 * @author dorgon
 *
 * initial GUI settings
 */
public class GUIConfig {
	private static final Logger log = LoggerFactory.getLogger(GUIConfig.class);
	
	/** default prefix mappings to show in the query widget */
	private final PrefixMapping prefixMap = new PrefixMappingImpl();
	
	/** prefix mappings as string for SPARQL query */
	private final String prefixMapString;
	
	/** sample queries to show in the GUI as tabs */
	private final Map<String, String> sampleQueries = new Hashtable<String, String>();
	

	/** a counter for tab titles of unnamed queries */
	private int unnamedQueryCounter = 1;
	
	public GUIConfig() {
		this(null);
	}

	public GUIConfig(Resource resource) {
		if (resource != null) {
			VocabUtils.unknownPropertyWarnings(resource, Config.MediatorConfig.getModel(), log);
			
			StmtIterator it = resource.listProperties(Config.prefixMapping);
			Resource map;
			while (it.hasNext()) {
				map = it.nextStatement().getResource();
				if (map.hasProperty(Config.prefix) && !map.hasProperty(Config.namespace))
					log.error("Invalid prefixMapping '" + map.getProperty(Config.prefix).getString() + "': missing <" + Config.namespace + ">");
				else if (!map.hasProperty(Config.prefix) && map.hasProperty(Config.namespace))
					log.error("Invalid prefixMapping for <" + map.getProperty(Config.namespace).getResource().getURI() + ">: missing <" + Config.prefix + ">");
				else if (!map.hasProperty(Config.prefix) && !map.hasProperty(Config.namespace))
					log.error("Invalid prefixMapping, each " + Config.prefixMapping + "> requires a <" + Config.prefix + "> and a <" + Config.namespace + "> property.");
				else
					prefixMap.setNsPrefix(map.getProperty(Config.prefix).getString(), map.getProperty(Config.namespace).getResource().getURI());
			}

			it = resource.listProperties(Config.sampleQuery);
			Resource q;
			while(it.hasNext()) {
				q = it.nextStatement().getResource();
				String name = (q.hasProperty(Config.queryName)) ?
						q.getProperty(Config.queryName).getString() : "Sample Query " + (unnamedQueryCounter++);
				if (!q.hasProperty(Config.queryString))
					log.error("Warning: sample query '" + name + "' has no <" + Config.queryString.getURI() + ">. Check your GUI Config.");

				sampleQueries.put(name, q.getProperty(Config.queryString).getString());
			}

			// prefix mapping as string
			StringBuilder sb = new StringBuilder(200);
			for (Object prefix : prefixMap.getNsPrefixMap().keySet())
				sb.append("PREFIX ").append(prefix).append(":\t<").append(prefixMap.getNsPrefixURI((String) prefix)).append(">\n");
			prefixMapString = sb.toString();

			it.close();
		} else
			prefixMapString = "";
	}

	public Map<String, String> getSampleQueries() {
		return sampleQueries;
	}

	public PrefixMapping getPrefixMapping() {
		return prefixMap;
	}

	public String getPrefixMappingString() {
		return prefixMapString;
	}
	
}
