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
package at.jku.semwiq.mediator;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.jku.rdfstats.vocabulary.SCOVO;
import at.jku.rdfstats.vocabulary.Stats;
import at.jku.semwiq.mediator.conf.MediatorConfig;
import at.jku.semwiq.mediator.vocabulary.Config;
import at.jku.semwiq.mediator.vocabulary.SDV;
import at.jku.semwiq.mediator.vocabulary.voiD;

import com.hp.hpl.jena.sparql.util.Symbol;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.XSD;

/**
 * @author dorgon
 *
 */
public class Constants {
	public static final String SYSTEMPROPERTY_SEMWIQ_MEDIATOR_HOME = "semwiq.home";
	public static final String SEMWIQ_MEDIATOR_HOME;
	static {
		String home = System.getProperty(SYSTEMPROPERTY_SEMWIQ_MEDIATOR_HOME);
		if (home != null)
			SEMWIQ_MEDIATOR_HOME = home;
		else
			SEMWIQ_MEDIATOR_HOME = System.getProperty("user.dir");
	}

	public static final String SYSTEMPROPERTY_CONFIGFILE = "semwiq.config";
	public static final String DEFAULT_CONFIG_FILE = SEMWIQ_MEDIATOR_HOME + File.separator + "etc" + File.separator + "semwiq-config.ttl";	
	
	public static final String VERSION_STRING_FILE = "VERSION";
	public static final String VERSION_STRING;
	static {
		BufferedReader reader = new BufferedReader(new InputStreamReader(MediatorConfig.class.getClassLoader()
				.getResourceAsStream(VERSION_STRING_FILE)));
		String v;
		try { v = reader.readLine(); } catch (IOException e) { v = "n/a"; }
		VERSION_STRING = v;
	}
	
	public static String HOSTNAME; // can be overridden
	static {
		try {
			HOSTNAME = InetAddress.getLocalHost().getCanonicalHostName();
		} catch (Exception e) {
			throw new RuntimeException("Cannot determine hostname, which is required to register at the controller.", e);
		}
	}
	
	// default prefix for internal SPARQL queries
	public static final String QUERY_PREFIX = 
		"PREFIX rdfs:  <" + RDFS.getURI() + ">\n" +
		"PREFIX rdf:   <" + RDF.getURI() + ">\n" +
		"PREFIX xsd:   <" + XSD.getURI() + ">\n" +
		"PREFIX dc:	   <" + DC.getURI() + ">\n" +
		"PREFIX sdv:   <" + SDV.getURI() + ">\n" +
		"PREFIX sqc:   <" + Config.getURI() + ">\n" +
		"PREFIX scv:   <" + SCOVO.getURI() + ">\n" +
		"PREFIX stats: <" + Stats.getURI() + ">\n" +
		"PREFIX void:  <" + voiD.getURI() + ">\n";

	// query execution context symbols
	public static final Symbol QUERY = Symbol.create("semwiq.query"); // Query
	public static final Symbol OP_ORIGINAL = Symbol.create("semwiq.plan.orig"); // Op
	public static final Symbol OP_PREOPTIMIZED = Symbol.create("semwiq.plan.preopt");
	public static final Symbol OP_FEDERATED = Symbol.create("semwiq.plan.fed");
	public static final Symbol OP_POSTOPTIMIZED = Symbol.create("semwiq.plan.postopt");
	
	public static final Symbol ESTIMATED_MIN_RESULTS = Symbol.create("semwiq.estimate.min");		// Long, possibly null
	public static final Symbol ESTIMATED_AVG_RESULTS = Symbol.create("semwiq.estimate.avg");		// Long, possibly null
	public static final Symbol ESTIMATED_MAX_RESULTS = Symbol.create("semwiq.estimate.max");		// Long, possibly null
	
	public static final Symbol EXEC_TIME_START = Symbol.create("semwiq.time.start"); // long - timestamp [ms]
	public static final Symbol EXEC_TIME_PREOPTIMIZE = Symbol.create("semwiq.time.preopt"); // long - durations [ms]
	public static final Symbol EXEC_TIME_FEDERATE = Symbol.create("semwiq.time.fed"); // long - durations [ms]
	public static final Symbol EXEC_TIME_POSTOPTIMIZE = Symbol.create("semwiq.time.postopt"); // long - durations [ms]
	public static final Symbol EXEC_TIME_FIRSTRESULT = Symbol.create("semwiq.time.first");  // long [ms]
	public static final Symbol EXEC_TIME_ALLRESULTS = Symbol.create("semwiq.time.all");  // long [ms]
	
	public static final int BLOCK_SIZE = 200; // bindings
	
	public static final String DEFAULT_SUPERUSER_PASSWORD = "semwiq";

	// graph viz output of query plans
	public static final String RENDER_PLANS_DUMMYLOGGER = "render-plans";
	public static final String RENDER_ORIGINAL_FILENAME = "plan_orig";
	public static final String RENDER_PREOPT_FILENAME = "plan_preoptimized";
	public static final String RENDER_FEDERATED_FILENAME = "plan_federated";
	public static final String RENDER_POSTOPT_FILENAME = "plan_final";
}
