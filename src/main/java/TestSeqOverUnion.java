import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.StringReader;
import java.util.Iterator;

import at.jku.semwiq.mediator.engine.op.OpExecutorSemWIQ;
import at.jku.semwiq.mediator.registry.model.DataSource;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_Variable;
import com.hp.hpl.jena.graph.Triple;
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
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.OpAsQuery;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP;
import com.hp.hpl.jena.sparql.algebra.op.OpJoin;
import com.hp.hpl.jena.sparql.algebra.op.OpSequence;
import com.hp.hpl.jena.sparql.algebra.op.OpUnion;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.DataSourceGraphImpl;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryExecutionBase;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap;
import com.hp.hpl.jena.sparql.engine.main.OpExecutor;
import com.hp.hpl.jena.sparql.engine.main.OpExecutorFactory;
import com.hp.hpl.jena.sparql.engine.main.QC;
import com.hp.hpl.jena.sparql.engine.main.QueryEngineMain;
import com.hp.hpl.jena.sparql.util.Context;
import com.hp.hpl.jena.vocabulary.RDF;

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
public class TestSeqOverUnion {

	/**
	 * @param args
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException {
		String data = "@prefix : <http://example.com/> .\n" +
				":a1 a :A ; :p 'a1' ; :q 'q' .\n" +
				":a2 a :A ; :p 'a2' ; :q 'q' .\n" +
				":b1 a :B ; :p 'b1' .\n" +
				":b2 a :B ; :p 'b2' ; :q 'q' .\n" +
				":b3 a :B ; :p 'b3' ; :q 'q' .\n" +
				":c1 a :C ; :p 'c1' ; :q 'q' .\n";
		Model m = ModelFactory.createDefaultModel();
		m.read(new StringReader(data), null, "N3");
		
		Context context = ARQ.getContext().copy();
		ARQ.setStrictMode(context);
		context.set(ARQ.filterPlacement, true);
		
		QC.setFactory(context, new OpExecutorFactory() {
			public OpExecutor create(ExecutionContext execCxt) {
				return new OpExecutorTest(execCxt);
			}
		});
		
		Query q;
		QueryExecution qe;
		ResultSet r;
		q = QueryFactory.create("PREFIX : <http://example.com/>\n" +
				"SELECT * WHERE {\n" +
				"	{ ?s :p ?label ; :q ?q}\n" +
				"	{\n" +
				"     { ?s a :A ; a ?type }\n" +
				"       UNION \n" +
				"	  { ?s a :B ; a ?type }\n" +
				"   }\n" +
				"}", Syntax.syntaxARQ);
		qe = QueryExecutionFactory.create(q, m);
		r = qe.execSelect();
		ResultSetFormatter.out(r);
	}

	static class OpExecutorTest extends OpExecutor {
		
		public OpExecutorTest(ExecutionContext cxt) {
			super(cxt);
		}
		
		@Override
		protected QueryIterator execute(OpJoin opJoin, QueryIterator input) {
	        QueryIterator qIter = input ;
	        
	        Op left = opJoin.getLeft();
	        qIter = executeOp(left, qIter);
	        Op right = opJoin.getRight();
	        qIter = executeOp(right, qIter);
	        
	        return qIter ;
		}
	}
}
