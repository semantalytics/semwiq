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

import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.jku.rdfstats.RDFStatsDataset;
import at.jku.rdfstats.RDFStatsDatasetImpl;
import at.jku.rdfstats.RDFStatsModel;
import at.jku.semwiq.mediator.vocabulary.RDFStatsExt;

import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.shared.Lock;

/**
 * @author dorgon
 *
 */
public class RDFStatsDatasetExt extends RDFStatsDatasetImpl {
	private static final Logger log = LoggerFactory.getLogger(RDFStatsDatasetExt.class);
	
	/**
	 * @param instance
	 */
	public RDFStatsDatasetExt(Resource instance, RDFStatsModel m) {
		super(instance, m);
	}

	/**
	 * 
	 */
	public RDFStatsDatasetExt(RDFStatsDataset ds, RDFStatsModel m) {
		super(ds.getWrappedResource(), m);
	}
	
	/**
	 * @return
	 */
	public Calendar getLastDownload() {
		Calendar date = null;
		model.enterCriticalSection(Lock.READ);
		Object o;
		try {
			Statement s = resource.getProperty(RDFStatsExt.lastDownload);
			if (s != null) {
				o = s.getLiteral().getValue();
				XSDDateTime dt = (XSDDateTime) o;
				date = dt.asCalendar();
			}
		} catch (Exception e) {
			log.error("Exception when parsing <" + RDFStatsExt.lastDownload + "> from " + toString() + ".", e);
		} finally {
			model.leaveCriticalSection();
		}
		return date;
	}

	/**
	 * @param instance
	 */
	public void updateLastDownload(Calendar c) {
		model.enterCriticalSection(Lock.WRITE);
		try {
			Statement s = resource.getProperty(RDFStatsExt.lastDownload);
			if (s == null)
				resource.addProperty(RDFStatsExt.lastDownload, model.createTypedLiteral(c));
			// TODO: possible bug in tdb creates wrong literal when adding, but correct if changing, so change always after creating first time:
			resource.getProperty(RDFStatsExt.lastDownload).changeObject(model.createTypedLiteral(c));
		} catch (Exception e) {
			log.error("Couldn't update <" + RDFStatsExt.lastDownload + "> for " + toString() + ".", e);
		} finally {
			model.leaveCriticalSection();
		}
	}

	/* (non-Javadoc)
	 * @see at.faw.rdfstats.RDFStatsDataset#toString()
	 */
	@Override
	public String toString() {
		return super.toString() + " (last download: " + getLastDownload().getTime() + ")";
	}
}
