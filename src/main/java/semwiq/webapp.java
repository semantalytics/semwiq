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
package semwiq;

import java.io.File;
import java.net.InetAddress;
import java.util.Random;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.HashSessionIdManager;
import org.mortbay.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.jku.semwiq.mediator.Constants;
import at.jku.semwiq.mediator.conf.MediatorConfig;

/**
 * @author dorgon
 *
 */
public class webapp {
	private static Options opts;
	private static final Logger log = LoggerFactory.getLogger(webapp.class);
	public static final int DEFAULT_PORT = 8080;
	public static final String CONTEXT_DIR = "webapps" + File.separator + "semwiq";

	private static int port;
	private static String hostname;
	private static Server server;
	private static WebAppContext context;

	private webapp(int p, String contextDir) {
		this.port = p;
		try {
			hostname = InetAddress.getLocalHost().getHostAddress();
		} catch (Exception ignore) {
			hostname = "localhost";
		}

		log.info("Launching SemWIQ Mediator web application at <" + hostname + ":" + port + "> ...");

		try {
			server = new Server(port);

			// use Random (/dev/urandom) instead of SecureRandom to generate session keys - otherwise Jetty may hang during startup waiting for enough entropy
			// see http://jira.codehaus.org/browse/JETTY-331 and http://docs.codehaus.org/display/JETTY/Connectors+slow+to+startup
			server.setSessionIdManager(new HashSessionIdManager(new Random()));
			
			context = new WebAppContext(server, contextDir, "");
			server.addHandler(context);
			server.start();

			// wait until Joseki is ready
			while (server != null && server.isStarting())
				try { Thread.sleep(100); } catch (InterruptedException ignore) {}
				
			// register shutdown hook at JVM (not required, already handled by Jetty...)
			Runtime.getRuntime().addShutdownHook(new ShutdownThread());			
		} catch (Exception e) {
			log.error("Failed to launch the SemWIQ Mediator web application.", e);
			shutdown();
		}
	}
	
	public static void main(String[] args) {
		Option port = new Option("p", "port", true, "port");
		port.setArgName("port");
		port.setOptionalArg(true);
		Option cfgFile = new Option("c", "config", true, "SemWIQ Mediator configuration file");
		cfgFile.setArgName("filename");
		cfgFile.setOptionalArg(true);
		Option help = new Option("h", "help", false, "help");
		help.setOptionalArg(true);
		Option newWebApp = new Option("n", "new", false, "run the newer IceFaces-based web application");
		newWebApp.setOptionalArg(true);
		
		opts = new Options();
		opts.addOption(port);
		opts.addOption(cfgFile);
		opts.addOption(help);
		opts.addOption(newWebApp);
		
		// create the parser
	    CommandLineParser parser = new BasicParser();
	    try {
	        CommandLine cmd = parser.parse(opts, args);
	        
	        // requires input file or config file
	        if (cmd.hasOption("h")) { 
	        	printUsage(null);
	        } else {
	        	if (cmd.hasOption("c")) System.setProperty(Constants.SYSTEMPROPERTY_CONFIGFILE, cmd.getOptionValue("c"));
//	        	if (cmd.hasOption("l")) System.setProperty(MediatorConfiguration.SYSTEMPROPERTY_LOGCONFIGFILE, cmd.getOptionValue("l"));
	        	
	        	int p = cmd.hasOption("p") ? Integer.parseInt((String) cmd.getOptionValue("p")) : DEFAULT_PORT;
				String contextDir = !cmd.hasOption("n") ? CONTEXT_DIR + "-old" : CONTEXT_DIR;
	        	new webapp(p, contextDir);
	        }
	    } catch (Exception e) {
	    	printUsage(e.getMessage());
	    }
	}
	
	private static void printUsage(String msg) {
		System.out.println("SemWIQ Mediator Web server " + Constants.VERSION_STRING);
		System.out.println("(C)2008, Institute for Applied Knowledge Processing, J. Kepler University Linz, Austria");
		if (msg != null) System.out.println(msg + '\n');
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("parameters: ", opts);
	}
	
	public static void shutdown() {
		if (context != null)
			try { context.stop(); } catch (Exception ignore) {} // this will call contextDestroyed of the StartupListener and shutdown the mediator
		if (server != null)
			try { server.stop(); } catch (Exception ignore) {}
	}

	class ShutdownThread extends Thread {
		public void run() {
			shutdown();
		}
	}
}
