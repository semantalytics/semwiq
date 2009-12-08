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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import at.jku.semwiq.mediator.Mediator;
import at.jku.semwiq.mediator.MediatorImpl;
import at.jku.semwiq.mediator.federator.Federator;
import at.jku.semwiq.mediator.federator.inst.InstanceBasedFederator;
import at.jku.semwiq.webapp.Webapp;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.shared.Lock;

/**
 * Servlet implementation class for Servlet: VocabularyServlet
 * 
 */
public class VocabularyServlet extends javax.servlet.http.HttpServlet implements
		javax.servlet.Servlet {
	static final long serialVersionUID = 1L;

	protected void service(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
    	Mediator mediator = Webapp.fromServletContext(getServletContext());
    	if (!(mediator != null && mediator.isReady())) {
			getServletContext().getRequestDispatcher("/not-available.jsp").forward(req, res);
			return;
		}

 		res.setContentType("application/rdf+xml");
 		Federator f = Webapp.fromServletContext(getServletContext()).getFederator();
 		if (f instanceof InstanceBasedFederator) {
 			OntModel vocModel = ((InstanceBasedFederator) f).getVocabularyManager().getVocabularyModel();
	 		vocModel.enterCriticalSection(Lock.READ);
	 		try {
	 			vocModel.write(res.getOutputStream(), "RDF/XML");
	 		} finally {
	 			vocModel.leaveCriticalSection();
	 		}
 		} else
 			throw new ServletException("Not using instance based federator, no VocabularyManager used.");
	}
}
