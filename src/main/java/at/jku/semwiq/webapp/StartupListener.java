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

package at.jku.semwiq.webapp;


import java.io.File;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.joseki.Joseki;
import org.joseki.RDFServer;
import org.joseki.Registry;
import org.joseki.Service;
import org.joseki.ServiceRegistry;
import org.joseki.processors.SPARQL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.jku.semwiq.log.LogBufferDispatcher;
import at.jku.semwiq.mediator.Constants;
import at.jku.semwiq.mediator.Mediator;
import at.jku.semwiq.mediator.MediatorImpl;
import at.jku.semwiq.mediator.dataset.SemWIQDataset;

/**
 * @author dorgon
 *
 * Logging is subject to the servlet container, no logging initialization here...
 */
public class StartupListener implements ServletContextListener {
	private static final Logger log = LoggerFactory.getLogger(StartupListener.class);
	
	private Mediator mediator;
	
	public void contextInitialized(ServletContextEvent event) {

		try {
			LogBufferDispatcher.init(-1);

			// if the webapp has been deployed as a WAR, etc is present in the WEB-INF directory. Thus, we reconfigure automatically...
			String deployedConfigFile = event.getServletContext().getRealPath("/WEB-INF/etc/semwiq-config.ttl");
			if (System.getProperty(Constants.SYSTEMPROPERTY_CONFIGFILE) == null && new File(deployedConfigFile).exists())
				System.setProperty(Constants.SYSTEMPROPERTY_CONFIGFILE, deployedConfigFile);
			
			mediator = new MediatorImpl();
			Webapp.putIntoServletContext(mediator, event.getServletContext());

			// register SPARQL Service endpoint at Joseki
			ServiceRegistry registry = (ServiceRegistry) Registry.find(RDFServer.ServiceRegistryName) ;
			if (registry == null) // first time, create new
				registry = new ServiceRegistry();
			
			Service service = new Service(new SPARQL(), Webapp.SPARQL_SERVICE_NAME, new SemWIQDatasetDesc(new SemWIQDataset(mediator)));
			registry.add(Webapp.SPARQL_SERVICE_NAME, service);
			Registry.add(RDFServer.ServiceRegistryName, registry);
			
			// init joseki HTTP User/Password for application-level Auth
			Joseki.httpUser = System.getProperty("httpUser");
			Joseki.httpPassword = System.getProperty("httpPassword");
		} catch (Exception e) {
			log.error("Error initializing SemWIQ Web Application.", e);
		}
	}	

	public void contextDestroyed(ServletContextEvent event) {
		mediator.shutdown();
	}

	
}
