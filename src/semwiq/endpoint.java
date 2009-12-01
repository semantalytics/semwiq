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

import java.io.FileInputStream;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.jku.semwiq.endpoint.Constants;
import at.jku.semwiq.endpoint.JosekiInstance;
import at.jku.semwiq.log.LogBufferDispatcher;
import at.jku.semwiq.rmi.CommonConstants;
import at.jku.semwiq.rmi.SemWIQInterfaceException;
import at.jku.semwiq.rmi.SpawnedEndpointMetadata;

import com.hp.hpl.jena.util.FileUtils;

/**
 * @author dorgon
 *
 */
public class endpoint {
	private static final Logger log = LoggerFactory.getLogger(endpoint.class);
	private static Options opts;
	private static JosekiInstance endpoint;
	private static Thread shutdownThread = new Thread() {
		public void run() {
			try {
				endpoint.shutdown();
			} catch (SemWIQInterfaceException e) {
				log.error("Failed to shutdown endpoint.", e);
			}
		}
	};
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		opts = new Options();

		Option host = new Option("h", "host", true, "hostname (if it cannot be correctly determined by the JVM)");
		host.setArgName("hostname");
		host.setOptionalArg(true);
		opts.addOption(host);
		Option port = new Option("p", "port", true, "port (default: " + Constants.START_PORT + ")");
		port.setArgName("port");
		port.setOptionalArg(true);
		opts.addOption(port);
		Option data = new Option("d", "data", true, "RDF data (.n3, .ttl, .rdf, .xml)");
		data.setArgName("port");
		data.setOptionalArg(true);
		opts.addOption(data);
		Option cfg = new Option("c", "config", true, "Joseki config file");
		cfg.setArgName("port");
		cfg.setOptionalArg(true);
		opts.addOption(cfg);
		
		// meta data to be included in voiD/sitemap.xml
		Option title = new Option("T", "title", true, "Dataset title");
		title.setArgName("text");
		title.setOptionalArg(true);
		opts.addOption(title);
		Option desc = new Option("D", "desc", true, "Dataset description");
		desc.setArgName("text");
		desc.setOptionalArg(true);
		opts.addOption(desc);
		Option hp = new Option("H", "homepage", true, "Homepage");
		hp.setArgName("url");
		hp.setOptionalArg(true);
		opts.addOption(hp);
		Option base = new Option("B", "dataset-base", true, "Dataset base URI prefix for Pubby (see http://www4.wiwiss.fu-berlin.de/pubby/)");
		base.setArgName("uri-prefix");
		base.setOptionalArg(true);
		opts.addOption(base);
		
		Option help = new Option("?", "help", false, "help");
		help.setOptionalArg(true);
		opts.addOption(help);
		
		
		// create the parser
	    CommandLineParser parser = new BasicParser();
	    try {
	        CommandLine cmd = parser.parse(opts, args);
	        
	        if (cmd.hasOption("?")) { 
	        	printUsage(null);
	        } else {
	        	LogBufferDispatcher.init(-1);
	        	int p = cmd.hasOption("p") ? 
	        		Integer.parseInt(cmd.getOptionValue("p")) : Constants.START_PORT;
	    		
				if (cmd.getOptionValue("h") != null)
					CommonConstants.HOSTNAME = cmd.getOptionValue("h"); // override static HOSTNAME constant

				SpawnedEndpointMetadata meta = new SpawnedEndpointMetadata(CommonConstants.HOSTNAME, p, CommonConstants.SPARQL_ENDPOINT_PATH_PREFIX);

				if (cmd.hasOption("T"))
					meta.setTitle(cmd.getOptionValue("T"));
				if (cmd.hasOption("D"))
					meta.setDescription(cmd.getOptionValue("D"));
				if (cmd.hasOption("H"))
					meta.setHomepage(cmd.getOptionValue("H"));
				if (cmd.hasOption("B"))
					meta.setDataSetBase(cmd.getOptionValue("B"));

				if (cmd.hasOption("d")) {
					endpoint = JosekiInstance.createInstance(meta, new FileInputStream(cmd.getOptionValue("d")), cmd.getOptionValue("d"), FileUtils.guessLang(cmd.getOptionValue("d")));
					// register shutdown hook at JVM (not required, already handled by Jetty...)
					Runtime.getRuntime().addShutdownHook(shutdownThread);
				} else if (cmd.hasOption("c")) {
					endpoint = JosekiInstance.createInstance(meta, cmd.getOptionValue("c"));
					// register shutdown hook at JVM (not required, already handled by Jetty...)
					Runtime.getRuntime().addShutdownHook(shutdownThread);
				} else
					printUsage("Missing config file (-c) or RDF data file (-d)!");
				
	        }
	    } catch (Exception e) {
	    	log.error(e.getMessage(), e);
	    }
	}
	
	private static void printUsage(String msg) {
		System.out.println("\nSemWIQ Endpoint");
		System.out.println("(C)2009, Andreas Langegger, al@jku.at, Institute for Applied Knowledge Processing, J. Kepler University Linz, Austria");
		if (msg != null) System.out.println('\n' + msg + '\n');
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("specify an RDF data file (-d) or a Joseki config file (-c)", opts);
	}
	
}
