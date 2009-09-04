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

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.jku.semwiq.mediator.registry.DataSourceRegistry;
import at.jku.semwiq.mediator.registry.model.DataSource;
import at.jku.semwiq.mediator.registry.model.RDFStatsUpdatableModelExt;
import at.jku.semwiq.mediator.registry.model.RemoteMonitoringProfile;
import at.jku.semwiq.mediator.vocabulary.SDV;

/**
 * @author dorgon
 *
 */
public class RemoteUpdateWorker extends RemoteUpdateWorkerBase {
	private static final Logger log = LoggerFactory.getLogger(RemoteUpdateWorker.class);
	
	protected final RemoteMonitoringProfile profile;
	
	public RemoteUpdateWorker(DataSource ds, DataSourceRegistry reg, RemoteMonitoringProfile profile) {
		super(ds, reg);
		this.profile = profile;
	}
	
	@Override
	public void doWork() {
		String statsUrl = null;
		try {
			RDFStatsUpdatableModelExt stats = registry.getRDFStatsUpdatableModel();
			Date lastDownload = stats.getLastDownload(ds.getSPARQLEndpointURL()); // may be null if stats are new

			// where to download?
			statsUrl = ((RemoteMonitoringProfile) profile).getStatisticsUrl(ds);
			if (statsUrl == null)
				throw new DataSourceMonitorException(profile + " has no " + SDV.statsUrl + ", please check the monitoring configuration.");

			checkAndDownload(statsUrl, stats, lastDownload, profile.updateOnlyIfNewer());
			setUnavailable(ds, false); // remove unavailable flag if existed
			scheduleNextUpdate(profile.getInterval());
//			registry.reloadVocabularies(ds);

		} catch (Throwable e) {
			setUnavailable(ds, true);
			log.error("Failed to update " + ds + " (Statistics URL: " + statsUrl + ")", e);
		}
	}


}
