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

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.OpVisitorByType;
import com.hp.hpl.jena.sparql.algebra.op.Op0;
import com.hp.hpl.jena.sparql.algebra.op.Op1;
import com.hp.hpl.jena.sparql.algebra.op.Op2;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP;
import com.hp.hpl.jena.sparql.algebra.op.OpExt;
import com.hp.hpl.jena.sparql.algebra.op.OpFilter;
import com.hp.hpl.jena.sparql.algebra.op.OpLabel;
import com.hp.hpl.jena.sparql.algebra.op.OpN;
import com.hp.hpl.jena.sparql.algebra.op.OpOrder;
import com.hp.hpl.jena.sparql.algebra.op.OpProject;
import com.hp.hpl.jena.sparql.algebra.op.OpService;
import com.hp.hpl.jena.sparql.algebra.op.OpTable;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprList;

/**
 * @author dorgon
 *
 */
public class GraphVizWriter {
	private static final Logger log = LoggerFactory.getLogger(GraphVizWriter.class);
	public static final String DOT_PATH = "/opt/local/bin/dot";
	
	protected Op op;
	protected Query query;
	protected String filename;

	protected FileWriter out;
	protected Map<Op, String> ids = new IdentityHashMap<Op, String>();
	protected Map<Op, String> labels = new IdentityHashMap<Op, String>();
	protected int sequence = 1;

	/**
	 * @param op
	 * @param filename
	 */
	public static void write(Op op, Query query, String filename) {
		new GraphVizWriter(op, query, filename).write();
	}

	public GraphVizWriter(Op op, Query query, String filename) {
		this.op = op;
		this.query = query;
		this.filename = filename;		
	}
	
	private void write() {
		try {
			out = new FileWriter(filename + ".dot");
			out.write("digraph plan {\n");
			out.write("\tnode [shape=ellipse]\n");
	
			// write connections
		 	op.visit(new GraphVizWriterVisitor());
			
			// write labels
			out.write("\n");
			for (Op o : labels.keySet()) {
				String label = labels.get(o);
				label = label.replaceAll("\"", "'");
				out.write("\t" + ids.get(o) + " [label=\"" + label + "\"];\n");
			}
			out.write("}\n");
			out.flush();
			out.close();
			log.info("Graphviz file '" + filename + ".dot' successfully generated.");
		} catch (IOException e) {
			log.warn("Error writing graphviz .dot file for Op plan.", e);
			return;
		}
		
		// run graphviz and generate images
		Process p = null;
		String err = "";
		try {
			p = Runtime.getRuntime().exec(DOT_PATH + " -Tpng -o" + filename + ".png " + filename + ".dot");
			
			String line;
			BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
			StringBuffer sbOut = new StringBuffer();
			while ((line = input.readLine()) != null)
				sbOut.append(line).append('\n');
			input.close();
			
			input = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			StringBuffer sbErr = new StringBuffer();
			while ((line = input.readLine()) != null)
				sbErr.append(line).append('\n');
			input.close();
			
			String out = sbOut.toString();
			err = sbErr.toString();
			if (err == "") err = out;
			
			Thread.sleep(10); // wait for p to exit
			if (p.exitValue() > 0)
				log.warn("Error executing graphviz: " + err);
			else {
				//new File(filename + ".dot").delete();
				log.info("PNG image '" + filename + ".png' successfully generated. " + out);
			}
		} catch (Exception e) {
			log.warn("Couldn't create graphviz images, graphviz not installed or failed executiong: " + err + " (" + e.getMessage() + ")");
			if (p != null) p.destroy();
		}
	}

	/** visitor for Op */
	private class GraphVizWriterVisitor extends OpVisitorByType {
		@Override
		public void visit(OpLabel op) { op.getSubOp().visit(this); }
		
		protected void visit0(Op0 op) {
			try {
				out.write("\t" + getId(op) + ";\n");
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		
		protected void visitExt(OpExt op) {}

		protected void visit1(Op1 op) throws RuntimeException {
			try {
				out.write("\t" + getId(op) + " -> " + getId(op.getSubOp()) + "\n");
				op.getSubOp().visit(this);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		protected void visit2(Op2 op) {
			try {
				Op left = op.getLeft();
				Op right = op.getRight();
				out.write("\t" + getId(op) + " -> " + getId(left) + " [label=left];\n");
				left.visit(this);
				out.write("\t" + getId(op) + " -> " + getId(right) + " [label=right];\n");
				right.visit(this);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}			
		}

		protected void visitN(OpN op) {
			try {
				Iterator<Op> it = op.getElements().iterator();
				while (it.hasNext()) {
					Op sub = it.next();
					out.write("\t" + getId(op) + " -> " + getId(sub) + ";\n");
					sub.visit(this);
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

//	/** visitor for AnnotatedOp */
//	private GraphVizWriterVisitor extends AnnotatedOpVisitorSimple {
//		@Override
//		public void visit(AnnotatedOpLabel op) { op.getSubOp().visit(this); }
//		@Override
//		public void visit(AnnotatedOpRootDummy op) { op.getSubOp().visit(this); }
//		
//		protected void visit0(AnnotatedOp0 op) {
//			try {
//				out.write("\t" + getId(op) + ";\n");
//			} catch (IOException e) {
//				throw new RuntimeException(e);
//			}
//		}
//
//		protected void visit1(AnnotatedOp1 op) throws RuntimeException {
//			try {
//				out.write("\t" + getId(op) + " -> " + getId(op.getSubOp()) + ";\n");
//				op.getSubOp().visit(this);
//			} catch (IOException e) {
//				throw new RuntimeException(e);
//			}
//		}
//
//		protected void visit2(AnnotatedOp2 op) {
//			try {
//				AnnotatedOp left = op.getLeft();
//				AnnotatedOp right = op.getRight();
//				out.write("\t" + getId(op) + " -> " + getId(left) + " [label=left];\n");
//				left.visit(this);
//				out.write("\t" + getId(op) + " -> " + getId(right) + " [label=right];\n");
//				right.visit(this);
//			} catch (IOException e) {
//				throw new RuntimeException(e);
//			}
//		}
//
//		protected void visitN(AnnotatedOpN op) {
//			try {
//				Iterator<AnnotatedOp> it = op.getElements().iterator();
//				while (it.hasNext()) {
//					Op sub = it.next();
//					out.write("\t" + getId(op) + " -> " + getId(sub) + ";\n");
//				}
//			} catch (IOException e) {
//				throw new RuntimeException(e);
//			}
//		}
//	}
//
	protected String getId(Op op) {
		String id = ids.get(op);
		if (id == null) {
			id = "node" + sequence++;
			ids.put(op, id);
			String name = getName(op);

//			if (op instanceof AnnotatedOp) {
//				AnnotatedOp aOp = (AnnotatedOp) op;
//				name += "\\n";
//
//				Set<Var> uniqueVars = aOp.getUniqueValueVars();
//				if (uniqueVars.size() > 0)
//					name += "Unique: " + uniqueVars + "\\n";
//
//				if (aOp.providesEstimations())
//					name += "(min " + aOp.getMinResults() + ", ~" + aOp.getAvgResults() + ", max " + aOp.getMaxResults() + ")";
//				else
//					name += "(estimates n/a)";
//			}
			labels.put(op, name);
		}
		return id;
	}
	
	protected String getName(Op op) {
		if (op instanceof OpService) {
			return "SERVICE\\n" + ((OpService) op).getService().getURI();
			
		} else if (op instanceof OpFilter) {
			StringBuilder sb = new StringBuilder();
			sb.append("FILTER");
			ExprList expr = ((OpFilter) op).getExprs();
			if (expr != null) {
				sb.append("\\n");
				Iterator<Expr> e = expr.iterator();
				while (e.hasNext()) {
					sb.append(e.next());
					if (e.hasNext()) sb.append("\\n");
				}
			}
			return sb.toString();

		} else if (op instanceof OpProject)
			return "PROJECT\\n" + ((OpProject) op).getVars();
		
		else if (op instanceof OpOrder)
			return "ORDER\\n" + ((OpOrder) op).getConditions();
		
		else if (op instanceof OpBGP) {
			StringBuilder sb = new StringBuilder();
			sb.append("BGP\\n");
			Iterator<Triple> p = ((OpBGP) op).getPattern().iterator();
			while (p.hasNext()) {
				sb.append(p.next().toString(query.getPrefixMapping()));
				if (p.hasNext()) sb.append("\\n");
			}
			return sb.toString();
		
		} else if (op instanceof OpTable) {
			return "TABLE\\n" + ((OpTable) op).getTable().size() + " bindings";
			
		} else
			return op.getName().toUpperCase();
	}
	
}
