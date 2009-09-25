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
import at.jku.semwiq.mediator.registry.model.VoidMonitoringProfile;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;

/**
 * @author dorgon, Andreas Langegger, al@jku.at
 *
 */
public class VoidUpdateWorker extends RemoteUpdateWorkerBase {
	private static final Logger log = LoggerFactory.getLogger(VoidUpdateWorker.class);
	public static final int CONNECT_TIMEOUT = 5000; // ms
	public static final int READ_TIMEOUT = 5000; // ms
	
	protected final VoidMonitoringProfile profile;

	/**
	 * @param ds
	 * @param registry
	 * @param profile
	 */
	public VoidUpdateWorker(DataSource ds, DataSourceRegistry registry, VoidMonitoringProfile profile) {
		super(ds, registry);
		this.profile = profile;
	}

	/* (non-Javadoc)
	 * @see at.jku.semwiq.mediator.registry.monitor.UpdateWorkerBase#doWork()
	 */
	@Override
	public void doWork() {
		try {
			// describe service and get void:Dataset URI
			Model serviceDesc;
			Query query = QueryFactory.create("DESCRIBE SERVICE", Syntax.syntaxARQ);
			QueryExecution qe = null;
			try {
				qe = QueryExecutionFactory.sparqlService(ds.getSPARQLEndpointURL(), query);
				serviceDesc = qe.execDescribe();
			} catch (Exception e) {
				throw new RuntimeException("Failed to get service description and void:Dataset URI from " + ds + ".", e);
			} finally { if (qe != null) qe.close(); }
			
			// get void:Dataset URI from service description
			String voidUri = null;
			try {
				qe = QueryExecutionFactory.create("SELECT * WHERE { ?s a <http://darq.sf.net/dose/0.1#Service> ; \n" +
						"	<http://www.w3.org/2005/03/saddle/#dataSet> ?void . }", serviceDesc);
				ResultSet r = qe.execSelect();
				if (r.hasNext()) {
					QuerySolution s = r.next();
					RDFNode voiD = s.get("void");
					if (voiD.isLiteral())
						voidUri = s.getLiteral("void").getString();
					else
						voidUri = s.getResource("void").getURI();
				} else {
					throw new RuntimeException("No void:Dataset in service description for " + ds.getSPARQLEndpointURL() + ".");
				}
			} catch (Exception e) {
				throw new RuntimeException("Failed to get void:Dataset from service description.", e);
			} finally { if (qe != null) qe.close(); }
			
			// update from voidUri
			RDFStatsUpdatableModelExt stats = registry.getRDFStatsUpdatableModel();
			Date lastDownload = stats.getLastDownload(ds.getSPARQLEndpointURL());
			
			checkAndDownload(voidUri, stats, lastDownload, profile.updateOnlyIfNewer());
			setUnavailable(ds, false); // remove unavailable flag if existed
			scheduleNextUpdate(profile.getInterval());
//			registry.reloadVocabularies(ds);
		} catch (Throwable e) {
			setUnavailable(ds, true);
			log.error("Failed to update " + ds + ".", e);
		}
	}

	
}
