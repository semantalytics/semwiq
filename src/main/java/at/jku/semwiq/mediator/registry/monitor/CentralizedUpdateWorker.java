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
package at.jku.semwiq.mediator.registry.monitor;

import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.jku.rdfstats.RDFStatsConfiguration;
import at.jku.rdfstats.generator.RDFStatsGeneratorBase;
import at.jku.rdfstats.generator.RDFStatsGeneratorFactory;
import at.jku.semwiq.mediator.registry.DataSourceRegistry;
import at.jku.semwiq.mediator.registry.model.CentralizedMonitoringProfile;
import at.jku.semwiq.mediator.registry.model.DataSource;

/**
 * @author dorgon
 *
 */
public class CentralizedUpdateWorker extends UpdateWorkerBase {
	private static final Logger log = LoggerFactory.getLogger(CentralizedUpdateWorker.class);
	protected final CentralizedMonitoringProfile profile;
	
	/**
	 * 
	 */
	public CentralizedUpdateWorker(DataSource ds, DataSourceMonitorImpl monitor, DataSourceRegistry reg, CentralizedMonitoringProfile profile) {
		super(ds, monitor, reg);
		this.profile = profile;
	}
	
	@Override
	public void doWork() {
		try {
			log.debug("Data source monitor is fetching statistics for " + ds + "...");		
			
			// use local RDFStats config if exists...
			RDFStatsConfiguration conf = ((CentralizedMonitoringProfile) profile).getStatsSettings();
			if (conf == null)
				conf = registry.getConfig().getRdfStatsConfig(); // otherwise use global configuration
			else {
				// replace model with global stats model:
				conf = RDFStatsConfiguration.create(registry.getConfig().getRdfStatsConfig().getStatsModel(),
						null, null, conf.getPrefSize(),
						null, null, conf.getStrHistMaxLength(), conf.quickMode(),
						conf.getDefaultTimeZone());
			}
			RDFStatsGeneratorBase generator = RDFStatsGeneratorFactory.generatorSPARQL(conf, ds.getSPARQLEndpointURL());
			generator.generate();
			
			setUnavailable(ds, false);
			scheduleNextUpdate(profile.getInterval());
//			registry.reloadVocabularies(ds);
			
		} catch (Throwable e) {
			setUnavailable(ds, true);
			
			log.error("Failed to update " + ds + ". ", e);
		}		
	}

}
