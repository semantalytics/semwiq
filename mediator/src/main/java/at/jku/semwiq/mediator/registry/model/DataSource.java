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

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.jku.semwiq.mediator.registry.RegistryException;
import at.jku.semwiq.mediator.registry.monitor.DataSourceMonitor;
import at.jku.semwiq.mediator.vocabulary.DCTerms;
import at.jku.semwiq.mediator.vocabulary.SDV;
import at.jku.semwiq.mediator.vocabulary.voiD;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * @author dorgon
 * 
 * A data source is a void:Dataset with a void:sparqlEndpoint.
 */
public class DataSource extends UpdatableResource {
	private static final Logger log = LoggerFactory.getLogger(DataSource.class);
	
	public DataSource(Resource resource) {
		super(resource);
	}

	/**
	 * @return FOAFAgent/Person/Group/Organization, a simple String (if a literal) or null
	 */
	public Object getCreator() {
		return getFOAFAgent(DCTerms.created);
	}

	/**
	 * @return FOAFAgent/Person/Group/Organization, a simple String (if a literal) or null
	 */
	public Object getContributor() {
		return getFOAFAgent(DCTerms.contributor);
	}

	/**
	 * @return FOAFAgent/Person/Group/Organization, a simple String (if a literal) or null
	 */
	public Object getPublisher() {
		return getFOAFAgent(DCTerms.publisher);
	}

	/** 
	 * @return FOAFAgent/Person/Group/Organization, a simple String (if a literal) or null
	 * @param p property
	 * @return
	 */
	private Object getFOAFAgent(Property p) {
		model.enterCriticalSection(Lock.READ);
		try {
			Statement s = resource.getProperty(p);
			if (s != null) {
				if (s.getObject().isResource()) {
					if (s.getResource().hasProperty(RDF.type, FOAF.Person))
						return new FOAFPerson(s.getResource());
					if (s.getResource().hasProperty(RDF.type, FOAF.Organization))
						return new FOAFOrganization(s.getResource());
					if (s.getResource().hasProperty(RDF.type, FOAF.Group))
						return new FOAFGroup(s.getResource());
					if (s.getResource().hasProperty(RDF.type, FOAF.Agent))
						return new FOAFAgent(s.getResource());
				} else if (s.getObject().isLiteral())
					return s.getString();
			}
		} catch (Exception e) {
			log.error("Failed to get " + p.getURI() + " from data source " + toString());
		} finally {
			model.leaveCriticalSection();
		}
		return null;
	}

	/** may return null if datasource has no endpoint-uri */
	public String getSPARQLEndpointURL() {
		String uri = null;
		model.enterCriticalSection(Lock.READ);
		try {
			Statement s = resource.getProperty(voiD.sparqlEndpoint);
			if (s != null && s.getObject().isResource())
				return s.getResource().getURI();
			else if (s != null && s.getObject().isLiteral())
				return s.getString();
		} catch (Exception e) {
			log.error("Failed to get " + voiD.sparqlEndpoint + " from data source " + toString());
		} finally {
			model.leaveCriticalSection();
		}
		return uri;
	}
	
	/**
	 *
	 * @return associated monitoring profile
	 */
	public MonitoringProfile getMonitoringProfile() {
		MonitoringProfile profile = null;
		model.enterCriticalSection(Lock.READ);
		try {
			Statement s = resource.getProperty(SDV.monitoringProfile);
			if (s != null && s.getObject().isResource())
				profile = MonitoringProfile.create(s.getResource());
		} catch (Exception e) {
			log.error("Failed to get " + SDV.monitoringProfile + " from data source " + toString());
		} finally {
			model.leaveCriticalSection();
		}
		return profile;
	}
	
	/**
	 * @return
	 */
	public Set<String> getLiteralSubjects() {
		HashSet<String> subjects = new HashSet<String>();
		model.enterCriticalSection(Lock.READ);
		StmtIterator it = null;
		try {
			it = model.listStatements(resource, DCTerms.subject, (RDFNode) null);
			Statement s;
			while (it.hasNext()) {
				s = it.nextStatement();
				if (s.getObject().isLiteral())
					subjects.add(s.getString());
			}
		} catch (Exception e) {
			log.error("Failed to get literal " + DCTerms.subject + " from data source " + toString());
		} finally {
			model.leaveCriticalSection();
			if (it != null) it.close();
		}
		return subjects;
	}
	
	/**
	 * @return
	 */
	public Set<Resource> getURISubjects() {
		HashSet<Resource> subjects = new HashSet<Resource>();
		model.enterCriticalSection(Lock.READ);
		StmtIterator it = null;
		try {
			it = model.listStatements(resource, DCTerms.subject, (RDFNode) null);
			Statement s;
			while (it.hasNext()) {
				s = it.nextStatement();
				if (s.getObject().isURIResource())
					subjects.add(s.getResource());
			}
		} catch (Exception e) {
			log.error("Failed to get URI " + DCTerms.subject + " from data source " + toString());
		} finally {
			model.leaveCriticalSection();
			if (it != null) it.close();
		}
		return subjects;
	}
	
	/**
	 * @return
	 */
	public boolean isAvailable() {
		model.enterCriticalSection(Lock.READ);
		try {
			Statement s = resource.getProperty(SDV.unavailable);
			return s == null || s.getObject().isLiteral() && !s.getBoolean(); // either no unavailable property or it is false
		} catch (Exception e) {
			log.error("Failed to get " + SDV.unavailable + " from data source.", e);
		} finally {
			model.leaveCriticalSection();
		}
		return false; // assume false on errors
	}

	/**
	 * returns false if data source has property "disabled" set to true
	 * otherwise returns true
	 * 
	 * @return
	 */
	public boolean isEnabled() {
		model.enterCriticalSection(Lock.READ);
		try {
			Statement s = resource.getProperty(SDV.disabled);
			return s == null || s.getObject().isLiteral() && !s.getBoolean(); // either no disabled property or it is false
		} catch (Exception e) {
			log.error("Failed to get " + SDV.disabled + " from data source " + toString());
		} finally {
			model.leaveCriticalSection();
		}
		return false; // assume false on errors
	}

	/* (non-Javadoc)
	 * @see at.faw.semwiq.mediator.util.RdfWrapperBase#toString()
	 */
	@Override
	public String toString() {
		return "Data source (" + getSPARQLEndpointURL() + ((isAvailable()) ? "" : " - OFFLINE") + ")";
	}

	/**
	 * @return
	 */
	public boolean hasSubsumption() {
		model.enterCriticalSection(Lock.READ);
		try {
			Statement s = resource.getProperty(SDV.subsumption);
			if (s != null && s.getObject().isLiteral()) 
				return s.getBoolean();
		} catch (Exception e) {
			log.error("Failed to get " + SDV.subsumption + " from data source " + toString());
		} finally {
			model.leaveCriticalSection();
		}
		return false; // assume false if unset
	}
	
	// update support

	/**
	 * @param state
	 * @param monitor reference to the registry's DataSourceMonitor (may be null)
	 * @param updateIfReactivated if true, will start update immediately if datasource was previously deactivated and now activated
	 */
	public void setDisabled(boolean disabled, DataSourceMonitor monitor, boolean updateIfReactivated) throws RegistryException {
		checkLock();

		boolean reactivated = false;
		boolean deactivated = false;
		
		try {
			Statement s = resource.getProperty(SDV.disabled);

			boolean prevDisabled = false;
			if (s != null && s.getBoolean())
				prevDisabled = true; // disabled property exists and set to true
			
			reactivated = prevDisabled && !disabled; // prev disabled, now enable
			deactivated = !prevDisabled && disabled; // prev enabled, now disable
			
			if (!disabled) // enable => just remove property
				resource.removeAll(SDV.disabled);
			else if (disabled && s == null) // property previously not existed => add and set to true
				resource.addLiteral(SDV.disabled, true);
			
			// if monitor reference available, create or remove monitor worker
			if (monitor != null) {
				if (reactivated)
					monitor.startMonitoring(this, updateIfReactivated);
				else if (deactivated)
					monitor.stopMonitoring(this);
			}
		} catch (Exception e) {
			log.error("Failed to set " + SDV.disabled + " for data source " + toString() + ".", e);
		}
	}
	
	/**
	 * @param state (set by semwiq mediator (data source monitor) only!)
	 */
	public void setUnavailable(boolean unavail) throws RegistryException {
		checkLock();
		
		try {
			Statement s = resource.getProperty(SDV.unavailable);
			if (!unavail) // remove
				resource.removeAll(SDV.unavailable);
			else if (unavail && s == null) // add and set to true
				resource.addLiteral(SDV.unavailable, true);
			
		} catch (Exception e) {
			log.error("Failed to set " + SDV.unavailable + " for data source " + toString() + ".", e);
		} 
	}

	public void setSubsumption(boolean state) throws RegistryException {
		checkLock();
		
		try {
			Statement s = resource.getProperty(SDV.subsumption);
			if (s != null)
				s.changeLiteralObject(state);
			else
				resource.addLiteral(SDV.subsumption, state);
		} catch (Exception e) {
			log.error("Failed to set " + SDV.subsumption + " for data source " + toString() + ".", e);
		} 
	}

	public void setMonitoringProfile(MonitoringProfile p) throws RegistryException {
		checkLock();

		try {
			Statement s = resource.getProperty(SDV.monitoringProfile);
			if (s != null)
				s.changeObject(p.getWrappedResource());
			else
				resource.addLiteral(SDV.monitoringProfile, p.getWrappedResource());
		} catch (Exception e) {
			log.error("Failed to set " + SDV.monitoringProfile + " for data source " + toString() + ".", e);
		} 
	}

}
