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
package at.jku.semwiq.mediator.vocabulary;

import org.slf4j.Logger;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * @author dorgon, Andreas Langegger, al@jku.at
 *
 */
public class VocabUtils {
	
	public static void unknownPropertyWarnings(Resource resource, Model schema, Logger log) {
		// TODO
//		if (resource == null)
//			return;
//		
//		StmtIterator it = resource.listProperties();
//		Statement s;
//		while (it.hasNext()) {
//			s = it.nextStatement();
//			if (!(s.getPredicate().getURI() == RDF.type.getURI()) && !schema.listStatements(s.getPredicate(), null, (RDFNode) null).hasNext())
//				log.warn("Unknown property: <" + s.getPredicate().getURI() + ">.");
//		}
	}
	
}
