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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.jku.semwiq.mediator.Mediator;
import at.jku.semwiq.mediator.MediatorImpl;
import at.jku.semwiq.mediator.registry.DataSourceRegistry;
import at.jku.semwiq.mediator.registry.RegistryException;
import at.jku.semwiq.mediator.registry.model.DataSource;
import at.jku.semwiq.mediator.registry.model.MonitoringProfile;
import at.jku.semwiq.webapp.StartupListener;
import at.jku.semwiq.webapp.Webapp;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * Servlet implementation class for Servlet: RegistryServlet
 * 
 */
public class RegistryServlet extends javax.servlet.http.HttpServlet implements
		javax.servlet.Servlet {

	static final long serialVersionUID = 1L;
	private static final Logger log = LoggerFactory.getLogger(QueryServlet.class);

	public RegistryServlet() {
		super();
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

    	Mediator mediator = Webapp.fromServletContext(getServletContext());
	    if (!(mediator != null && mediator.isReady())) {
			getServletContext().getRequestDispatcher("/not-available.jsp").forward(req, resp);
			return;
		}

		PrintWriter out = resp.getWriter();

		if (req.getParameter("cmd") != null) {
			try {
				String cmd = req.getParameter("cmd");
				if (cmd.equals("register")) {
					String ct = req.getContentType();

					if (ct == null || ct.startsWith("text/plain")) {
						Webapp.fromServletContext(getServletContext()).getDataSourceRegistry().getManager().register(req.getParameter("endpoint").trim(), MonitoringProfile.getDefaultCentralizedProfile());
						out
							.println("Endpoint registered, monitor has been triggered to update the statistics. Successful registration does not mean that the endpoint will be compatible and become available. Please check the statistics page.");
					}

				} else if (cmd.equals("updateStats")) {
					if (req.getParameter("endpointUri") != null) {
						DataSourceRegistry reg = mediator.getDataSourceRegistry();
						reg.getMonitor().triggerUpdate(reg.getDataSourceByEndpointUri(req.getParameter("endpointUri")));
						out
							.println("Monitor has been triggered to update the statistics.");
					} else {
						throw new RegistryException("Cannot schedule update. No endpointUri specified.");
					}

				} else if (cmd.equals("unregister")) {
					if (req.getParameter("endpointUri") != null) {
						DataSourceRegistry reg = mediator.getDataSourceRegistry();
						String uri = req.getParameter("endpointUri");
						reg.getManager().unregister(uri);
						out.println("Data source with endpoint URI <" + uri + "> has been unregistered.");
					} else {
						throw new RegistryException("No endpointUri specified.");
					}
				} else if (cmd.equals("update")) {
					resp
							.sendError(
									501,
									"Not implemented. Please restart the server to flush all registered data sources");
				}

			} catch (RegistryException e) {
				resp.sendError(500, "Error registering data source(s): "
						+ e.getMessage());
				// e.printStackTrace(out);
			} catch (IOException e) {
				resp.sendError(500,
						"I/O Error while reading data from HTTP request."
								+ e.getMessage());
				// e.printStackTrace(out);
			}

		} else {
			resp
					.sendError(
							500,
							"Invalid request.");
		}
	}

}