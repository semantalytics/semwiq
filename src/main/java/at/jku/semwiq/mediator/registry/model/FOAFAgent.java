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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;

/**
 * @author dorgon
 * 
 * wrapper class for foaf:Agent
 */
public class FOAFAgent extends WrappedResource {
	private static final Logger log = LoggerFactory.getLogger(FOAFAgent.class);
	
	/**
	 * @param resource
	 */
	public FOAFAgent(Resource resource) {
		super(resource);
	}

	public Iterator<FOAFPerson> getMembers() {
		List<FOAFPerson> members = new ArrayList<FOAFPerson>();
		StmtIterator it = null;
		model.enterCriticalSection(Lock.READ);
		try {
			it = model.listStatements(resource, FOAF.member, (RDFNode) null);
			Resource res;
			while (it.hasNext()) {
				res = (Resource) it.nextStatement().getObject().as(Resource.class);
				members.add(new FOAFPerson(res));
			}
		} catch (Exception e) {
			log.error("Failed to get " + FOAF.member + " from foaf:Agent " + toString(), e);
		} finally {
			model.leaveCriticalSection();
			if (it != null) it.close();
		}
		return members.iterator();
	}

	public String getName() {
		String name = "n/a";
		model.enterCriticalSection(Lock.READ);
		try {
			Statement s = resource.getProperty(FOAF.name);
			if (s != null && s.getObject().isLiteral())
				name = s.getString();
		} catch (Exception e) {
			log.error("Failed to get " + FOAF.name + " from foaf:Agent " + toString(), e);
		} finally {
			model.leaveCriticalSection();
		}
		return name;
	}

	public boolean isPerson() {
		return hasType(FOAF.Person);
	}
	
	public boolean isGroup() {
		return hasType(FOAF.Group);
	}
	
	public boolean isOrganization() {
		return hasType(FOAF.Organization);	
	}
	
	/** cast to person */
	public FOAFPerson asPerson() {
		return FOAFPerson.class.cast(this);
	}

	/** cast to organization */
	public FOAFOrganization asOrganization() {
		return FOAFOrganization.class.cast(this);
	}

	/** cast to group */
	public FOAFGroup asGroup() {
		return FOAFGroup.class.cast(this);
	}
	
	@Override
	public String toString() {
		if (isPerson())
			return "foaf:Person '" + getLabel() + "'";
		else if (isGroup())
			return "foaf:Group '" + getLabel() + "'";
		else if (isOrganization())
			return "foaf:Organization '" + getLabel() + "'";
		else
			return "foaf:Agent '" + getLabel() + "'";
	}

}
