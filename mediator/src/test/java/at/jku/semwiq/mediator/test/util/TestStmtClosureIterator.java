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
package at.jku.semwiq.mediator.test.util;

import java.io.StringReader;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.TreeSet;

import com.hp.hpl.jena.graph.GraphUtil;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.FileManager;

import at.jku.semwiq.mediator.util.StmtClosureIterator;
import junit.framework.TestCase;

/**
 * @author dorgon, Andreas Langegger, al@jku.at
 *
 */
public class TestStmtClosureIterator extends TestCase {

	public void testIterator() {
		String n3 = 
			"@prefix : <http://example.com/> .\n" +
			":x :a :A ; :p1 'foo' ; :p2 :u .\n" +
			":y :a :B ; :p2 :u .\n" +
			":p1 :a :property ; :label 'foo2' ; :p2 :u .\n" +
			":other :x :foo ; :p3 'foo' .\n" +
			":u :p3 'bar' ; :p4 :z .\n" +
			":z :p2 :x .\n" +
			":x :p9 :y .\n";
		
		Model m = ModelFactory.createDefaultModel();
		m.read(new StringReader(n3), null, "N3");
		
		Model target = ModelFactory.createDefaultModel();
		StmtClosureIterator it = new StmtClosureIterator(m.getResource("http://example.com/x"));
		while (it.hasNext())
			target.add(it.next());
		
		String q =
			"PREFIX : <http://example.com/>\n" +
			"ASK WHERE {\n" +
			"	:x :a :A ; :p1 'foo' ; :p2 :u .\n" +
			"	:y :a :B ; :p2 :u .\n" +
			"	:p1 :a :property ; :label 'foo2' ; :p2 :u .\n" +
			"	:u :p3 'bar' ; :p4 :z .\n" +
			"	:z :p2 :x .\n" +
			"	:x :p9 :y .\n" +
			"}\n";
		QueryExecution qe = QueryExecutionFactory.create(q, target);
		assertTrue(qe.execAsk());
		qe.close();
		assertEquals(12, target.size());
	}
}
