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
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Map;
import java.util.Random;

import org.joseki.Dispatcher;
import org.joseki.RDFServer;
import org.joseki.Registry;
import org.joseki.ServiceRegistry;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.HashSessionIdManager;
import org.mortbay.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.jku.semwiq.rmi.CommonConstants;
import at.jku.semwiq.rmi.SemWIQInterfaceException;
import at.jku.semwiq.rmi.SpawnedEndpointMetadata;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.impl.RDFReaderFImpl;
import com.hp.hpl.jena.tdb.TDB;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.tdb.assembler.VocabTDB;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.RDF;

import de.fuberlin.wiwiss.pubby.Configuration;

/**
 * @author dorgon
 *
 */
public class JosekiInstance {
	private static final Logger log = LoggerFactory.getLogger(JosekiInstance.class);
	
	private static JosekiInstance lastCreatedInstance;
	
	private final SpawnedEndpointMetadata meta;
	private final String pubbyPrefix;
	private final String usingTempDir;
	
	private final Server server;
	private final WebAppContext cxt;
	
	private boolean running = false;
	
	/**
	 * create instance and load data from InputStream
	 * 
	 * @param meta
	 * @param data
	 * @param baseUri
	 * @param dataFormat
	 * @param serverBaseUrl
	 * @throws SemWIQInterfaceException 
	 */
	public static JosekiInstance createInstance(SpawnedEndpointMetadata meta, InputStream data, String baseUri, String dataFormat) throws SemWIQInterfaceException {
		// initialize TDB model and load dataset
		String tdbLoc = Utils.getTempDir(meta);
		if (dataFormat == null)
			dataFormat = CommonConstants.DATASET_LANGUAGE;
		
		log.info("Initializing TDB dataset for endpoint '" + meta.getTitle() + "' in '" + tdbLoc + "'...");
		
		try {
			File f = new File(tdbLoc);
			if (f.exists())
				Filesystem.deleteDirectory(f);
			Filesystem.makeDirectory(f);
			
			TDB.init();
			RDFReaderFImpl.setBaseReaderClassName("N3", com.hp.hpl.jena.n3.turtle.TurtleReader.class.getName()); // use old Jena reader for N3
			RDFReaderFImpl.setBaseReaderClassName("TTL", com.hp.hpl.jena.n3.turtle.TurtleReader.class.getName()); // use old Jena reader for N3
			Dataset ds = TDBFactory.createDataset(tdbLoc);
			
			Map<String, String> pMap = new Hashtable<String, String>();
			
			log.info("Loading dataset (" + dataFormat + ") from input stream...");
			Model m = ds.getDefaultModel();
			m.read(data, baseUri, dataFormat);
			pMap = m.getNsPrefixMap();
			TDB.sync(ds);
			
//			// create RDFStats
//			RDFStatsGeneratorSPARQL statsGen = new RDFStatsGeneratorSPARQL(meta.getSparqlEndpointUri());
//			statsGen.generate();
//			Model statsModel = statsGen.getRDFStatsModel().getWrappedModel();
			
			// initialize Joseki configuration and start server
			Model josekiConfig = createJosekiConfig(meta, tdbLoc);
			JosekiInstance instance = new JosekiInstance(meta, josekiConfig, tdbLoc, pMap);
			return instance;
		} catch (Throwable t) {
			throw new SemWIQInterfaceException("Failed to initialize Joseki endpoint.", t);
		}		
	}
	
	/**
	 * @return
	 */
	private static String getBaseURL(SpawnedEndpointMetadata meta) {
		return "http://" + meta.getHostname() + ":" + meta.getPort() + "/";
	}

	/**
	 * @param meta
	 * @param josekiCfg
	 * @throws SemWIQInterfaceException
	 */
	public static JosekiInstance createInstance(SpawnedEndpointMetadata meta, String josekiCfg) throws SemWIQInterfaceException {
		Model cfg = FileManager.get().loadModel(josekiCfg);
		return new JosekiInstance(meta, cfg, null, cfg.getNsPrefixMap());
	}
	
	/**
	 * @param meta
	 * @param cfg
	 * @param usingTempDir
	 * @param prefixMap
	 * @throws SemWIQInterfaceException
	 */
	private JosekiInstance(SpawnedEndpointMetadata meta, Model cfg, String usingTempDir, Map<String, String> prefixMap) throws SemWIQInterfaceException {
		lastCreatedInstance = this;
		this.meta = meta;
		this.usingTempDir = usingTempDir;
		
		log.info("Starting new Joseki instance on port " + meta.getPort() + "...");
		
		try {
			ServiceRegistry registry = (ServiceRegistry) Registry.find(RDFServer.ServiceRegistryName) ;
			if (registry == null) { // first time, create new
				registry = new ServiceRegistry();
				Registry.add(RDFServer.ServiceRegistryName, registry) ;
				registry = (ServiceRegistry) Registry.find(RDFServer.ServiceRegistryName) ;
			}

			Configuration pubbyConfig = createPubbyConfig(meta);
			this.pubbyPrefix = ((de.fuberlin.wiwiss.pubby.Dataset) pubbyConfig.getDatasets().iterator().next()).getWebResourcePrefix();
			
			new org.joseki.Configuration(cfg, registry, getBaseURL(meta));
			Dispatcher.setConfiguration(cfg, getBaseURL(meta));
			server = new Server(meta.getPort());

			// use Random (/dev/urandom) instead of SecureRandom to generate session keys - otherwise Jetty may hang during startup waiting for enough entropy
			// see http://jira.codehaus.org/browse/JETTY-331 and http://docs.codehaus.org/display/JETTY/Connectors+slow+to+startup
			server.setSessionIdManager(new HashSessionIdManager(new Random()));
			
			cxt = new WebAppContext(server, Constants.JOSEKI_WEBAPP_DIR, "");
			cxt.setAttribute(Constants.ENDPOINT_METADATA_ATTRIB, meta); // required by diverse servlets at runtime			
			cxt.setAttribute(Constants.PUBBY_CONFIG_ATTRIB, pubbyConfig); // used by Pubby via (equal to BaseServlet.SERVER_CONFIGURATION)
			cxt.setAttribute(Constants.PREFIX_MAPPING_ATTRIB, prefixMap); // required by NamespaceServlet

			server.addHandler(cxt);
			server.start();
			
			// wait until Joseki is ready
			while (server.isStarting())
				try { Thread.sleep(100); } catch (InterruptedException ignore) {}

			running = true;
		} catch (Throwable e) {
			String msg = "Failed to launch Joseki on port " + meta.getPort() + ".";
			log.error(msg, e);
			throw new SemWIQInterfaceException(msg, e);
		}
	}
	
	/**
	 * @param meta
	 * @return
	 * @throws SemWIQInterfaceException
	 */
	private Configuration createPubbyConfig(SpawnedEndpointMetadata meta) throws SemWIQInterfaceException {
		log.info("Using Pubby base configuration: " + Constants.PUBBY_BASE_CONFIGFILE);

		try {
			TDB.init(); // use new RIOT parser, in this case the old one is troublesome
			Model m  = Utils.loadFiltered(Constants.PUBBY_BASE_CONFIGFILE, meta);
			return new Configuration(m);
		} catch (Exception e) {
			throw new SemWIQInterfaceException("Failed to load Pubby base configuration " + Constants.PUBBY_BASE_CONFIGFILE + ".", e);
		}		
	}

	/**
	 * @param meta
	 * @param tdbLoc
	 * @return
	 */
	private static Model createJosekiConfig(SpawnedEndpointMetadata meta, String tdbLoc) throws SemWIQInterfaceException {
		log.info("Using Joseki base configuration: " + Constants.JOSEKI_BASE_CONFIGFILE);

		try {
			// get the plain swq:dataset and add all necessary configuration parameters
			Model m = Utils.loadFiltered(Constants.JOSEKI_BASE_CONFIGFILE, meta);
			Resource ds = m.getResource(Constants.JOSEKI_DATASET_URI);
			ds.addProperty(RDF.type, VocabTDB.tDatasetTDB);
			ds.addProperty(VocabTDB.pLocation, tdbLoc);
			return m;
		} catch (Exception e) {
			throw new SemWIQInterfaceException("Failed to load Joseki base configuration " + Constants.JOSEKI_BASE_CONFIGFILE + ".", e);
		}
	}

	/**
	 * @return true if server is still starting up
	 */
	public boolean isStarting() {
		return server.isStarted();
	}
	
	/**
	 * @return the running
	 */
	public boolean isRunning() {
		return running;
	}
	
	public void shutdown() throws SemWIQInterfaceException {
		try {
			if (server != null) server.stop();
			if (cxt != null) cxt.destroy();
			running = false;
			
			if (usingTempDir != null)
				log.info("Deleting temporary files in " + usingTempDir + "...");
			
			log.info("Joseki instance on port " + meta.getPort() + " successfully shut down.");
		} catch (Exception e) {
			String msg = "Failed to shutdown Joseki running on port " + meta.getPort() + ".";
			log.error(msg, e);
			throw new SemWIQInterfaceException(msg, e);
		}
	}

	/**
	 * @return the usingTempDir or null if no temp data was loaded
	 */
	public String getTempDir() {
		return usingTempDir;
	}
	
	/**
	 * @return
	 */
	public int getPort() {
		return meta.getPort();
	}

	/** get last endpoint metadata (used for singleton usage of JosekiInstance) */
	public static SpawnedEndpointMetadata getLastSpawnedEndpointMetadata() {
		return lastCreatedInstance.meta;
	}
	
	/** get last pubby path prefix (used for singleton usage of JosekiInstance) */
	public static String getLastPubbyPathPrefix() {
		return lastCreatedInstance.pubbyPrefix;
	}
}
