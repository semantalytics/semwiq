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

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.jku.semwiq.ctrl.Config;
import at.jku.semwiq.ctrl.EndpointController;
import at.jku.semwiq.ctrl.EndpointControllerImpl;
import at.jku.semwiq.ctrl.SemWIQControllerException;
import at.jku.semwiq.ctrl.swing.ControllerFrame;


/**
 * @author dorgon
 *
 */
public class controller {
	private static final Logger log = LoggerFactory.getLogger(controller.class);
	
	private static Options opts;
	
	/**
	 * @param args
	 * @throws SemWIQControllerException 
	 */
	public static void main(String[] args) throws SemWIQControllerException {
		Option endpoints = new Option("e", "endpoints", true, "endpoints file");
		endpoints.setArgName("file");
		endpoints.setOptionalArg(true);
		
		Option help = new Option("h", "help", false, "help");
		help.setOptionalArg(true);
		
		opts = new Options();
		opts.addOption(endpoints);
		opts.addOption(help);
		
		// create the parser
	    CommandLineParser parser = new BasicParser();
        CommandLine cmd;
        Config cfg;
		try {
			cmd = parser.parse(opts, args);
			cfg = Config.create(cmd.getOptionValue("e"));
	    } catch (Exception e) {
	    	throw new SemWIQControllerException("Failed to initialize: " + e.getMessage() + ".", e);
	    }
        
        // requires input file or config file
        if (cmd.hasOption("h")) { 
        	printUsage(null);
        } else {
        	EndpointController ctrl = new EndpointControllerImpl(cfg);
        	new ControllerFrame(ctrl);
        }
	}
	
	private static void printUsage(String msg) {
		System.out.println("SemWIQ Endpoint Controller");
		System.out.println("(C)2009, Andreas Langegger, al@jku.at, Institute for Applied Knowledge Processing, J. Kepler University Linz, Austria");
		if (msg != null) System.out.println(msg + '\n');
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("parameters: ", opts);
	}

}
