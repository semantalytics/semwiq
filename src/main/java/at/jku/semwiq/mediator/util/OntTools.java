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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * @author dorgon
 *
 */
public class OntTools extends com.hp.hpl.jena.ontology.OntTools {
	
	/**
	 * minimizes the set of types in typeSet to most specific types
	 * @param typeSet
	 */
	public static Set<OntClass> minimizeTypeSet(Set<OntClass> typeSet) {
		Set<OntClass> newSet = new HashSet<OntClass>();
		Set<OntClass> superClosure = new HashSet<OntClass>();
		OntClass next;
		
		for (OntClass t : typeSet) {
			ExtendedIterator<OntClass> it = t.listSuperClasses();
			while (it.hasNext()) {
				next = it.next();
				superClosure.add(next); // fill closure to the top
				newSet.remove(next); // remove from newSet any super class
			}
			
			if (!superClosure.contains(t)) // only add, if not alraady in the super closure
				newSet.add(t);
		}
		
		return newSet;
	}

	/**
	 * @param subTypes
	 * @return
	 */
	public static List<Set<OntClass>> generateTypeCombinations(Map<OntClass, Set<OntClass>> subTypes) {
		int numComb = 1;
		for (Set<OntClass> subSet : subTypes.values())	// 4
			numComb = numComb * subSet.size();			// 3*2*2*1 = 12
		
		List<Set<OntClass>> combinations = new ArrayList<Set<OntClass>>(numComb); // 12 sets with 4 entries
		for (int i=0; i<numComb; i++)
			combinations.add(new HashSet<OntClass>()); // initialize
		
		int repeatType = numComb;
		int repeatSeq;
		for (Set<OntClass> subSet : subTypes.values()) { // 4 times
			int idx = 0; // each iteration from 0..12
			
			// repeat 12 times in total
			repeatType /= subSet.size();
			repeatSeq = numComb/repeatType/subSet.size();
			for (int i=0; i<repeatSeq; i++) {
				for (OntClass subType : subSet) {
					// repeat type
					for (int j=0; j<repeatType; j++) {
						combinations.get(idx++).add(subType);
					}					
				}
			}
		}
		
		return combinations;
	}
}
