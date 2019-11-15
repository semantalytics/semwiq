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
package at.jku.semwiq.mediator.federator.inst;

import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.ontology.OntClass;

/**
 * @author dorgon
 *
 * minimal conjuctive type set for a BGP which has triple patterns with equal subject
 * 
 * To match a BGP, the conjunction of all classes in the associated set has to be satisfied.
 */
public class SubjectTypeCache {
	private final Map<Node, Set<OntClass>> cache;

	/**
	 * constructor
	 */
	public SubjectTypeCache() {
		cache = new Hashtable<Node, Set<OntClass>>();
	}
	
	/**
	 * @param subject
	 * @param type
	 * @return true if the cache didn't already have type for subject
	 */
	public boolean addType(Node subject, OntClass type) {
		Set<OntClass> set = getTypeSet(subject);
		if (set == Collections.EMPTY_SET) {
			set = new HashSet<OntClass>();
			cache.put(subject, set);
		}
		return set.add(type);
	}

	/**
	 * 
	 * @param subject
	 * @param types
	 */
	public void setTypeSet(Node subject, Set<OntClass> types) {
		cache.put(subject, types);
	}

	/**
	 * @param subject
	 * @return
	 */
	public Set<OntClass> getTypeSet(Node subject) {
		Set<OntClass> types = cache.get(subject);
		if (types != null)
			return types;
		else
			return Collections.EMPTY_SET;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Node n : cache.keySet()) {
			sb.append(n)
			.append(": ")
			.append(cache.get(n))
			.append("\n");
		}
		return sb.toString();
	}

}
