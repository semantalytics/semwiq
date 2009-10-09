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
package at.jku.semwiq.endpoint;

import java.io.File;

import de.fuberlin.wiwiss.pubby.servlets.BaseServlet;

/**
 * @author dorgon
 *
 */
public class Constants {
	
	// Joseki
	public static final String JOSEKI_WEBAPP_DIR = "webapps/joseki";
	public static String JOSEKI_BASE_CONFIGFILE = "file:etc/joseki-base-config.ttl";
	public static final String JOSEKI_DATASET_URI = "http://semwiq.sourceforge.net/dataset";

	// Pubby
	public static String PUBBY_BASE_CONFIGFILE = "file:etc/pubby-base-config.ttl";
	public static final String PUBBY_CONFIG_URI = "http://semwiq.sourceforge.net/pubbyConfig";
	
	// Dataset metadata discovery (sitemap.xml/voiD)
	public static String DISCOVERY_BASE_ROBOTS = "file:etc/robots-base.txt";
	public static String SITEMAP_XML_NS = "http://www.sitemaps.org/schemas/sitemap/0.9";
	public static String DISCOVERY_BASE_SITEMAP = "file:etc/sitemap-base.xml";
	public static String DISCOVERY_BASE_VOID_INCLUDE = "file:etc/void-include.ttl";
	
	// Servlet context attributes
	public static final String PUBBY_CONFIG_ATTRIB = BaseServlet.SERVER_CONFIGURATION; // now public // BaseServlet.class.getName() + ".serverConfiguration";
	public static final String PREFIX_MAPPING_ATTRIB = "semwiq.endpoint.prefixmap";
	public static final String ENDPOINT_METADATA_ATTRIB = "semwiq.endpoint.metadata";
	public static final String RDFSTATS_MODEL_ATTRIB = "semwiq.endpoint.rdfstats";
	
	/** remote endpoint daemon specific defaults */
	public static final int START_PORT = 8900;

	/** temp directory for auto-created TDB stores */
	public static final String TDB_TEMP_DIR;
	static {
		String tmpDir = System.getProperty("java.io.tmpdir");
		if (!tmpDir.endsWith(File.separator)) tmpDir += File.separator;
		TDB_TEMP_DIR = tmpDir + "semwiq-endpoint" + File.separator;
	}

}
