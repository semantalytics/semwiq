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

import at.jku.semwiq.mediator.registry.UserRegistry;
import at.jku.semwiq.mediator.registry.UserRegistryImpl;
import at.jku.semwiq.mediator.vocabulary.SDV;
import at.jku.semwiq.mediator.vocabulary.SUV;

import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;

/**
 * @author dorgon, Andreas Langegger, al@jku.at
 *
 */
public class User extends FOAFPerson {
	private static final Logger log = LoggerFactory.getLogger(User.class);
	
	public User(Resource wrappedResource) {
		super(wrappedResource);
	}
	
	/**
	 * returns false if user has property "disabled" set to true
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
			log.error("Failed to get " + SDV.disabled + " from user " + toString());
		} finally {
			model.leaveCriticalSection();
		}
		return false; // assume false on errors
	}
	
	public String getPassword() {
		String pwd = null;
		model.enterCriticalSection(Lock.READ);
		try {
			Statement s = resource.getProperty(SUV.password);
			if (s != null && s.getObject().isLiteral())
				pwd= s.getString();
		} catch (Exception e) {
			log.error("Failed to get " + SUV.password + " from suv:User " + toString(), e);
		} finally {
			model.leaveCriticalSection();
		}
		return pwd;
	}
	
}
