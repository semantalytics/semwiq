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

package at.jku.semwiq.webapp.simple;


import java.io.IOException;
import java.io.PrintWriter;
import java.net.SocketException;
import java.util.List;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.HttpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.jku.semwiq.mediator.Mediator;
import at.jku.semwiq.mediator.MediatorImpl;
import at.jku.semwiq.mediator.util.Misc;
import at.jku.semwiq.webapp.StartupListener;
import at.jku.semwiq.webapp.Webapp;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.resultset.ResultSetFormat;

/**
 * Servlet implementation class for Servlet: QueryServlet
 * 
 */
public class QueryServlet extends javax.servlet.http.HttpServlet implements
		javax.servlet.Servlet {

	static final long serialVersionUID = 1L;
	private static final Logger log = LoggerFactory.getLogger(QueryServlet.class);

	public static enum ResultFormat {
		XHTML("XHTML (pretty printed)"), RDF_XML_ABBREV("RDF/XML-ABBREV"), SPARQL_XML(
				"SPARQL Query Results XML Format"), JSON("JSON"), CSV(
				"Textfile (Comma-separated values)");

		public String printableName;

		ResultFormat(String n) {
			printableName = n;
		}

		public static ResultFormat getDefault() {
			return XHTML;
		}
	}

	protected void service(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
    	Mediator mediator = Webapp.fromServletContext(getServletContext());
    	if (!(mediator != null && mediator.isReady())) {
			getServletContext().getRequestDispatcher("/not-available.jsp").forward(req, res);
			return;
		}

		ServletOutputStream out = null;

		String qryStr = req.getParameter("q");
		if (qryStr == null)
			throw new ServletException(new IllegalArgumentException(
					"No query string \"q\" specified."));
		log.info("Executing query: " + qryStr);

		Query query = QueryFactory.create(qryStr, Syntax.syntaxARQ);
		QueryExecution qe = mediator.createQueryExecution(query);
		ResultSet r = null;
		
		try {
			int qType = query.getQueryType();

			switch (qType) {
				case Query.QueryTypeSelect :
					
					// which output format?
					ResultFormat fmt;
					if (req.getParameter("fmt") != null)
						fmt = ResultFormat.valueOf(req.getParameter("fmt"));
					else
						fmt = ResultFormat.getDefault();

					r = qe.execSelect();
					
					switch (fmt) {
						case XHTML:
							req.setAttribute("query", query);
							req.setAttribute("resultSet", r);
	
							RequestDispatcher d = getServletContext().getRequestDispatcher("/results.jsp");
							d.forward(req, res);
							break;
						case JSON:
							// res.setContentType("application/json");
							out = res.getOutputStream();
							ResultSetFormatter.output(out, r, ResultSetFormat.syntaxJSON);
							break;
						case RDF_XML_ABBREV:
							// res.setContentType("application/rdf+xml");
							res.setContentType("text/xml");
							out = res.getOutputStream();
							ResultSetFormatter
									.output(out, r, ResultSetFormat.syntaxRDF_XML);
							break;
						case SPARQL_XML:
							res.setContentType("text/xml");
							out = res.getOutputStream();
							ResultSetFormatter.output(out, r, ResultSetFormat.syntaxXML);
							break;
						case CSV:
							res.setContentType("text/plain");
							List<String> resultVars = r.getResultVars();

							out = res.getOutputStream();
							for (String var : resultVars)
								out.print(var + ";");
							out.println();
							
							QuerySolution s;
							while (r.hasNext()) {
								s = r.nextSolution();
								for (String var : resultVars) {
									out.print("\"");
									out.print(s.get(var).toString());
									out.print("\";");
								}
								out.println();
							}
							out.flush();
							break;
						}

					break;
				case Query.QueryTypeAsk :
					out = res.getOutputStream();
					out.println(qe.execAsk());
					break;
				case Query.QueryTypeConstruct :
					out = res.getOutputStream();
					res.setContentType("text/xml");
					Model constructResult = qe.execConstruct();
					constructResult.write(out);
					constructResult.close();
					break;
				case Query.QueryTypeDescribe :
					out = res.getOutputStream();
					res.setContentType("text/xml");
					Model describeResult = qe.execDescribe();
					describeResult.write(out);
					describeResult.close();
					break;
			}

		} catch (Throwable e) {
			// exception stack is different for various servlet implementations, so we need to trace the whole cause stack...
			Set<Class<? extends Throwable>> trace = Misc.getExceptionChain(e);
			if (trace.contains(HttpException.class)) {
				log.error("Error executing query via servlet. Failed to connect to remote server. Query execution aborted. Please update statistics.", e);
				if (out == null)
					out = res.getOutputStream();
				out.println("Failed to connect to remote server. Query execution aborted. Please update statistics.");
				e.printStackTrace(new PrintWriter(out));
			} else if (trace.contains(SocketException.class)) {
				if (log.isDebugEnabled())
					log.warn("Broken pipe. Most probably the user has canceled the request, query execution canceled.", e);
				else
					log.warn("Broken pipe. Most probably the user has canceled the request, query execution canceled."); // without exception trace
			} else {
				log.error("Error executing query via servlet.", e);
				if (out == null)
					out = res.getOutputStream();
				out.println("Error executing query.");
				e.printStackTrace(new PrintWriter(out));
			}
		} finally {
			if (qe != null)
				qe.close();
		}
	}
}