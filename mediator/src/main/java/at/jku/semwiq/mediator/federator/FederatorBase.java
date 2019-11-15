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
package at.jku.semwiq.mediator.federator;

import com.hp.hpl.jena.rdf.model.Resource;

import at.jku.semwiq.mediator.registry.DataSourceRegistry;
import at.jku.semwiq.mediator.registry.UserRegistry;


/**
 * @author dorgon, Andreas Langegger, al@jku.at
 *
 */
public abstract class FederatorBase implements Federator {
	protected final DataSourceRegistry dsRegistry;
	protected final UserRegistry userRegistry;

	/**
	 * constructor
	 * @param conf configuration resource
	 * @param userRegistry 
	 * @param dsRegistry 
	 */
	public FederatorBase(Resource conf, DataSourceRegistry dsRegistry, UserRegistry userRegistry) {
		this.dsRegistry = dsRegistry;
		this.userRegistry = userRegistry;
		
		// if (conf != null) set global conf properties from conf....
	}

	/**
	 * @return the dsRegistry
	 */
	public DataSourceRegistry getDataSourceRegistry() {
		return dsRegistry;
	}
	
	/**
	 * @return the userRegistry
	 */
	public UserRegistry getUserRegistry() {
		return userRegistry;
	}
}
