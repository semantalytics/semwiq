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
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sun.reflect.ReflectionFactory.GetReflectionFactoryAction;

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

		if (req.getParameter("cmd") != null && req.getParameter("endpointUri") != null && req.getParameter("endpointUri").length() > 0) {
			String cmd = req.getParameter("cmd");
			if (cmd.equals("register")) {
				String ct = req.getContentType();

				if (ct == null || ct.startsWith("text/plain")) {
					try {
						Webapp.fromServletContext(getServletContext()).getDataSourceRegistry().getManager().register(req.getParameter("endpointUri").trim(), MonitoringProfile.getDefaultVoidProfile());
						System.out.println(req.getHeader("Referer"));
						forward("Endpoint registered, monitor has been triggered to update the statistics. Successful registration does not mean that the endpoint will be compatible and become available. Please check the statistics page.", req, resp);
					} catch (Exception e) {
						log.error("Error registereing data source(s).", e);
						forward("Error registereing data source(s): " + e.getMessage(), req, resp);
					}
				}

			} else if (cmd.equals("updateStats")) {
				if (req.getParameter("endpointUri") != null) {
					try {
						DataSourceRegistry reg = mediator.getDataSourceRegistry();
						reg.getMonitor().triggerUpdate(reg.getDataSourceByEndpointUri(req.getParameter("endpointUri")));						
						forward("Monitor has been triggered to update the statistics.", req, resp);
					} catch (Exception e) {
						log.error("Error registereing data source(s).", e);
						forward("Error registereing data source(s): " + e.getMessage(), req, resp);
					}
				} else {
					log.error("Cannot schedule update. No endpointUri specified.");
					forward("Cannot schedule update. No endpointUri specified.", req, resp);
				}

			} else if (cmd.equals("unregister")) {
				if (req.getParameter("endpointUri") != null) {
					try {
						DataSourceRegistry reg = mediator.getDataSourceRegistry();
						String uri = req.getParameter("endpointUri");
						reg.getManager().unregister(uri);
						forward("Data source with endpoint URI <" + uri + "> has been unregistered.", req, resp);
					} catch (Exception e) {
						log.error("Error registereing data source(s).", e);
						forward("Error registereing data source(s): " + e.getMessage(), req, resp);
					}
				} else {
					log.error("No endpointUri specified.");
					forward("No endpointUri specified.", req, resp);
				}
			}
		} else {
			log.error("Invalid registry service request from " + req.getRemoteAddr());
			forward("Invalid registry service request.", req, resp);
		}
	}

	private void forward(String msg, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			msg = URLEncoder.encode(msg, "utf-8");
		} catch (Exception ignore) {
			msg = URLEncoder.encode(msg);
		}
		
		String target = "/"; // may result in endless loop if coming from servlet itself...
//		String referer = req.getHeader("Referer");
//		if (referer != null) {
//			Matcher m = Pattern.compile("http:\\/\\/[^\\/]*\\/(.*)").matcher(referer);
//			if (m.find())
//				target = "/" + m.group(1);
//		}
		
		RequestDispatcher d = getServletContext().getRequestDispatcher(target + "?msg=" + msg);
		d.forward(req, resp);
	}

}