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

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;

/**
 * @author dorgon
 *
 */
public interface VocabularyManager {
	
	/**
	 * the sub-process must take care of correctly locking the model when reading/writing!
	 * @return
	 */
	public OntModel getVocabularyModel();
	
	public void loadVocabulary(String uri);
	
	public OntClass getOntClass(String uri);
	
	public OntProperty getOntProperty(String uri);
	
	public void close();
	
}
