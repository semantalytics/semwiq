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
package at.jku.semwiq.mediator.registry.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.jku.rdfstats.ConfigurationException;
import at.jku.rdfstats.RDFStatsConfiguration;
import at.jku.semwiq.mediator.vocabulary.SDV;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.Lock;

/**
 * @author dorgon
 *
 */
public class CentralizedMonitoringProfile extends MonitoringProfile {
	private static final Logger log = LoggerFactory.getLogger(CentralizedMonitoringProfile.class);
	
	/**
	 * @param instance
	 */
	public CentralizedMonitoringProfile(Resource instance) {
		super(instance);
	}

	public RDFStatsConfiguration getStatsSettings() {
		model.enterCriticalSection(Lock.READ);
		try {
			if (resource.hasProperty(SDV.statsConfig))
				return RDFStatsConfiguration.create(resource.getProperty(SDV.statsConfig).getResource());
		} catch (ConfigurationException e) {
			log.error("Failed to get " + SDV.statsConfig + " from monitoring profile " + toString(), e);
		} finally {
			model.leaveCriticalSection();
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see at.jku.semwiq.mediator.registry.model.MonitoringProfile#getName()
	 */
	@Override
	public String getName() {
		return "Centralized monitoring profile";
	}
	
	/* (non-Javadoc)
	 * @see at.jku.semwiq.mediator.registry.model.MonitoringProfile#toString()
	 */
	@Override
	public String toString() {
		return getName() + (resource.isURIResource() ? " <" + getUri() + ">" : "");
	}
}
