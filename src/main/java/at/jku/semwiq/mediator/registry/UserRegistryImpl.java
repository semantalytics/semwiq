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
package at.jku.semwiq.mediator.registry;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;

import at.jku.semwiq.mediator.conf.UserRegistryConfig;
import at.jku.semwiq.mediator.registry.model.User;
import at.jku.semwiq.mediator.vocabulary.SUV;

/**
 * @author dorgon, Andreas Langegger, al@jku.at
 *
 */
public class UserRegistryImpl implements UserRegistry {

	// special users
	private final User GUEST, SUPERUSER;
	
	public UserRegistryImpl(UserRegistryConfig config, Model store) {
		Resource guest = ModelFactory.createDefaultModel().createResource(FOAF.Person);
		guest.addLiteral(FOAF.name, "guest");		
		GUEST = new User(guest);

		Resource su = ModelFactory.createDefaultModel().createResource(FOAF.Person);
		su.addLiteral(FOAF.name, "superuser");
		su.addLiteral(SUV.password, config.getSuperUserPassword());
		SUPERUSER = new User(su);
	}
	
	/* (non-Javadoc)
	 * @see at.jku.semwiq.mediator.registry.UserRegistry#shutdown()
	 */
	public void shutdown() {}
	
	/* (non-Javadoc)
	 * @see at.jku.semwiq.mediator.registry.UserRegistry#getUser(java.lang.String, java.lang.String)
	 */
	public User getUser(String username, String password) {
		// handle special users first
		if (username.equals(SUPERUSER) && password.equals(SUPERUSER))
			return SUPERUSER;
		else
			return GUEST;
	}
	
	public User getGuestUser() {
		return GUEST;
	}
	
	public boolean isSuperUser(User user) {
		return user.equals(SUPERUSER);
	}
	
	public boolean isGuestUser(User user) {
		return user.equals(GUEST);
	}
}
