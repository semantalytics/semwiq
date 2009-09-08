import java.io.StringReader;

import at.jku.semwiq.mediator.Constants;
import at.jku.semwiq.mediator.engine.MediatorQueryEngine;
import at.jku.semwiq.mediator.engine.op.OpExecutorSemWIQ;

import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.main.OpExecutor;
import com.hp.hpl.jena.sparql.engine.main.OpExecutorFactory;
import com.hp.hpl.jena.sparql.engine.main.QC;
import com.hp.hpl.jena.sparql.resultset.ResultSetFormat;
import com.hp.hpl.jena.sparql.util.Context;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.util.FileManager;

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

/**
 * @author dorgon, Andreas Langegger, al@jku.at
 *
 */
public class TestUnionIterator {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Query q;
		QueryExecution qe;
		ResultSet r;
		
//		String data = "@prefix : <http://example.com/> .\n" +
//				":a1 a :A ; :p 'a1' ; :q 'q' .\n" +
//				":a2 a :A ; :p 'a2' ; :q 'q' .\n" +
//				":b1 a :B ; :p 'b1' .\n" +
//				":b2 a :B ; :p 'b2' ; :q 'q' .\n" +
//				":b3 a :B ; :p 'b3' ; :q 'q' .\n" +
//				":c1 a :C ; :p 'c1' ; :q 'q' .\n";
//		Model m = ModelFactory.createDefaultModel();
//		m.read(new StringReader(data), null, "N3");
//		
//		q = QueryFactory.create("PREFIX : <http://example.com/>\n" +
//				"SELECT * WHERE {\n" +
//				"	{ ?s :p ?label ; :q ?q}\n" +
//				"	{\n" +
//				"     { ?s a :A ; a ?type }\n" +
//				"       UNION \n" +
//				"	  { ?s a :B ; a ?type }\n" +
//				"   }\n" +
//				"}", Syntax.syntaxARQ);
//		qe = QueryExecutionFactory.create(q, m);
//		r = qe.execSelect();
//		ResultSetFormatter.out(r);
//
//		q = QueryFactory.create(Constants.QUERY_PREFIX + "PREFIX foaf: <" + FOAF.getURI() + ">\n" +
//				"SELECT * WHERE {\n" +
//				"	{ ?s rdfs:label ?name ; dc:date ?date ; a <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/Product> }\n" +
//				"	{\n" +
//				"     { ?s a <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/ProductType3> }\n" +
//				"       UNION \n" +
//				"	  { ?s a <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/ProductType2> }\n" +
//				"	}\n" +
//				"} ORDER BY ?s LIMIT 50", Syntax.syntaxARQ);
//		qe = QueryExecutionFactory.create(q, FileManager.get().loadModel("../BSBM/dataset.n3", "N3"));
//		r = qe.execSelect();
//		ResultSetFormatter.out(r);

    	ARQ.setStrictMode();
    	ARQ.getContext().set(ARQ.filterPlacement, true);
    	
    	QC.setFactory(ARQ.getContext(), new OpExecutorFactory() {
    		public OpExecutor create(ExecutionContext execCxt) { return new OpExecutorSemWIQ(execCxt); }
    	});
		
		q = QueryFactory.create(Constants.QUERY_PREFIX + "PREFIX foaf: <" + FOAF.getURI() + ">\n" +
				"SELECT * WHERE {\n" +
				"    {\n" +
				"      SERVICE <http://vancouver:8900/sparql/endpoint8900> { ?s a <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/ProductType3> }\n" +
				"    } UNION {\n" +
				"      SERVICE <http://vancouver:8901/sparql/endpoint8901> { ?s a <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/ProductType3> }\n" +
				"    }\n" +
				"    {\n" +
				"      SERVICE <http://vancouver:8900/sparql/endpoint8900> { ?s rdfs:label ?name }\n" +
				"    } UNION {\n" +
				"      SERVICE <http://vancouver:8901/sparql/endpoint8901> { ?s rdfs:label ?name }\n" +
				"    }\n" +
				"}", Syntax.syntaxARQ);
		
		qe = QueryExecutionFactory.create(q, ModelFactory.createDefaultModel());
		r = qe.execSelect();
		ResultSetFormatter.out(r);
	}

}
