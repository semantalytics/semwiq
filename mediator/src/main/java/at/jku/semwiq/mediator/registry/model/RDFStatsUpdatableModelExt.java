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
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.shared.Lock;

import at.jku.rdfstats.RDFStatsDataset;
import at.jku.rdfstats.RDFStatsModelException;
import at.jku.rdfstats.RDFStatsUpdatableModelImpl;
import at.jku.semwiq.mediator.vocabulary.SDV;

/**
 * @author dorgon, Andreas Langegger, al@jku.at
 * 
 * extends the RDFStatsUpdatableModel with methods for maintaining swq:lastDownload
 */
public class RDFStatsUpdatableModelExt extends RDFStatsUpdatableModelImpl {
	private static final Logger log = LoggerFactory.getLogger(RDFStatsUpdatableModelExt.class);
	
	public RDFStatsUpdatableModelExt(Model wrappedModel) {
		super(wrappedModel);
	}
	
	public Date getLastDownload(String sourceUrl) throws RDFStatsModelException {
		RDFStatsDataset ds = getDataset(sourceUrl);
		if (ds == null)
			return null;
		else
			return getLastDownload(ds);
	}
	
	public Date getLastDownload(RDFStatsDataset ds) throws RDFStatsModelException {
		model.enterCriticalSection(Lock.READ);
		try {
			Statement s = ds.getWrappedResource().getProperty(SDV.lastDownload);
			if (s == null)
				return null;
			else {
				Object o = s.getLiteral().getValue();
				XSDDateTime dt = (XSDDateTime) o;
				return dt.asCalendar().getTime();
			}
		} catch (Exception e) {
			log.error("Failed to get " + SDV.lastDownload + " from RDFStastDataset " + ds + ".", e);
			return null;
		} finally {
			model.leaveCriticalSection();
		}
	}
	
	public void setLastDownload(String sourceUrl, Date timestamp) throws RDFStatsModelException {
		setLastDownload(getDataset(sourceUrl), timestamp);
	}
	
	public void setLastDownload(RDFStatsDataset ds, Date timestamp) throws RDFStatsModelException {
		requestExclusiveWriteLock(ds);
		try {
			Calendar cal = Calendar.getInstance();
			cal.setTime(timestamp);
			Statement s = ds.getWrappedResource().getProperty(SDV.lastDownload);
			if (s == null)
				ds.getWrappedResource().addLiteral(SDV.lastDownload, cal);
			else
				s.changeObject(model.createTypedLiteral(cal));
		} catch (Exception e) {
			log.error("Failed to set " + SDV.lastDownload + " for RDFStatsDataset " + ds + ".", e);
		} finally {
			returnExclusiveWriteLock(ds);
		}
	}
}
