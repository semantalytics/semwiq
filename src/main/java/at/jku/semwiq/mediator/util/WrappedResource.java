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

package at.jku.semwiq.mediator.util;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * 
 * Wrapper for Jena graph resources
 * 
 * @author dorgon
 * 
 * Please note that resources may become expired if deleted from the model by another thread.
 */
public abstract class WrappedResource {
	private static final Logger log = LoggerFactory.getLogger(WrappedResource.class);

	protected Resource resource;
	protected Model model;
	
	/**
	 * @param instance
	 */
	public WrappedResource(Resource instance) {
		this.model = instance.getModel();
		this.resource = instance;
	}

	/**
	 * can be used to check if the resource is still available
	 * a resource is considered as expired if there are no more statements about it or the model was closed
	 * 
	 * @return false if expired
	 */
	public boolean isExpired() {
		if (model.isClosed())
			return true;
		
		boolean expired = true;
		model.enterCriticalSection(Lock.READ);
		try {
			expired = model.listStatements(resource, null, (RDFNode) null).hasNext();
		} finally {
			model.leaveCriticalSection(); 
		}
		return expired;
	}
	
	/**
	 * @return the resource URI
	 */
	public String getUri() {
		return resource.getURI();
	}

	/**
	 * @return
	 */
	public String getLocalName() {
		return resource.getLocalName();
	}

	/**
	 * @return the rdfs:label if exists
	 */
	public String getLabel() {
		String label = null;
		model.enterCriticalSection(Lock.READ);
		try {
			Statement s = resource.getProperty(RDFS.label);
			if (s != null && s.getObject().isLiteral()) label = s.getString();
		} finally {
			model.leaveCriticalSection();
		}
		return label;
	}

	/**
	 * get any string literal
	 * 
	 * @param p
	 * @return
	 */
	public String getString(Property p) {
		String str = null;
		model.enterCriticalSection(Lock.READ);
		try {
			Statement s = resource.getProperty(p);
			if (s != null && s.getObject().isLiteral())
				str = s.getString();
		} catch (Exception e) {
			log.error("Failed to get members from foaf:Agent." + e);
		} finally {
			model.leaveCriticalSection();
		}
		return str;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		if (resource.isAnon())
			return resource.getId().toString();
		else
			return resource.getURI();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		if (resource.isAnon())
			return 1 + 31 * resource.getId().hashCode();
		else
			return 1 + 31 * getUri().hashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		return this.resource.equals(((WrappedResource) obj).resource);
	}
	
	/** 
	 * Attention! When getting the wrapped resource reference, make sure to fulfill the
	 * locking contract and use resource.getModel().enterCriticalSection(Lock lock) / leaveCriticalSection()!!!
	 * 
	 * @return the wrapped resource
	 */
	public Resource getWrappedResource() {
		return resource;
	}
	
	/** list owl:sameAs resources
	 * 
	 * @return
	 */
	public List<Resource> listSameAs() {
		List<Resource> same = new ArrayList<Resource>();
		
		model.enterCriticalSection(Lock.READ);
		try {
			StmtIterator it = resource.listProperties(OWL.sameAs);
			while (it.hasNext())
				same.add(it.nextStatement().getResource());
		} finally {
			model.leaveCriticalSection();
		}
		
		return same;
	}
	
	/** get only one owl:sameAs resource (if multiple available, will return arbitrary one of them) */
	public Resource getSameAs() {
		List<Resource> list = listSameAs();
		if (list.size() > 0)
			return list.get(0);
		else
			return null;
	}
	
	/** lists all known RDF types of the wrapped resource */
	public List<Resource> listRDFTypes() {
		List<Resource> types = new ArrayList<Resource>();
		
		model.enterCriticalSection(Lock.READ);
		try {
			StmtIterator it = resource.listProperties(RDF.type);
			while (it.hasNext())
				types.add(it.nextStatement().getResource());
		} finally {
			model.leaveCriticalSection();
		}
		
		return types;
	}
	
	public boolean hasType(Resource type) {
		model.enterCriticalSection(Lock.READ);
		try {
			return resource.hasProperty(RDF.type, type);
		} finally {
			model.leaveCriticalSection();
		}
	}
	
}
