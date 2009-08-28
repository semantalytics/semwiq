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

package at.jku.semwiq.mediator.conf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.jku.semwiq.mediator.vocabulary.Config;
import at.jku.semwiq.mediator.vocabulary.VocabUtils;

import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * @author dorgon
 *
 */
public class MediatorConfig {
	private static final Logger log = LoggerFactory.getLogger(MediatorConfig.class);
	
	public static final String DEFAULT_ADMIN_PASSWORD = "semwiq";

	/** using config file (can be null if constructed via API), kept for log output */
	protected final String configFile;
	
	/** admin password / currently just use single password and no user model */
	private final String adminPassword;
	
	/** assembler model Config for global store */
	protected final Resource storeAssemblerModel;
	
	/* Configs for sub-systems */
	protected final DataSourceRegistryConfig dataSourceRegistryConfig;
	protected final UserRegistryConfig userRegistryConfig;
	protected final GUIConfig guiConfig;
	protected final FederatorConfig federatorConfig;
	
	/** create default Config
	 *  
	 * @throws ConfigException
	 */
	public MediatorConfig() throws ConfigException {
		this(null);
	}
	
	/**
	 * creates a new mediator Config from configFile
	 * Fails if some of the required Config settings is not found throwing a ConfigException.
	 * 
	 * @throws ConfigException
	 */
	public MediatorConfig(String configFile) throws ConfigException {
		this.configFile = configFile;

		if (this.configFile != null) {
			Model configModel = null;
			
			try {
				configModel = FileManager.get().loadModel(this.configFile);
				Resource mediatorConfig = getConfigResource(configModel);			
				VocabUtils.unknownPropertyWarnings(mediatorConfig, Config.MediatorConfig.getModel(), log);
	
				// initialize global store
				Statement s = mediatorConfig.getProperty(Config.store);
				if (s != null && s.getObject().isResource())
					storeAssemblerModel = s.getResource();
				else
					storeAssemblerModel = null;

				// admin password
				s = mediatorConfig.getProperty(Config.adminPassword);
				if (s != null && s.getObject().isLiteral())
					adminPassword = s.getString();
				else
					throw new ConfigException("Admin password not set in Config!");
				
				// initialize sub Configs
				s = mediatorConfig.getProperty(Config.dataSourceRegistryConfig);
				if (s != null && s.getObject().isResource())
					dataSourceRegistryConfig = new DataSourceRegistryConfig(s.getResource());
				else
					dataSourceRegistryConfig = new DataSourceRegistryConfig(null);
	
				s = mediatorConfig.getProperty(Config.userRegistryConfig);
				if (s != null && s.getObject().isResource())
					userRegistryConfig = new UserRegistryConfig(s.getResource());
				else
					userRegistryConfig = new UserRegistryConfig(null);
	
				s = mediatorConfig.getProperty(Config.guiConfig);
				if (s != null && s.getObject().isResource())
					guiConfig = new GUIConfig(s.getResource());
				else
					guiConfig = new GUIConfig(null);
				
				s = mediatorConfig.getProperty(Config.federatorConfig);
				if (s != null && s.getObject().isResource())
					federatorConfig = new FederatorConfig(s.getResource());
				else
					federatorConfig = new FederatorConfig(null);
	
			} catch (Exception e) {
				throw new ConfigException("Initialization failed. (Config file: '" + configFile + "'.)", e);	
			}
		} else {
			configFile = null;
			storeAssemblerModel = null;
			adminPassword = DEFAULT_ADMIN_PASSWORD;
			
			guiConfig = new GUIConfig();
			federatorConfig = new FederatorConfig();
			userRegistryConfig = new UserRegistryConfig();
			dataSourceRegistryConfig = new DataSourceRegistryConfig();
		}
	}
	
	/** search for instance of <http://purl.org/semwiq/mediator/config#MediatorConfig> */
	private Resource getConfigResource(Model configModel) throws ConfigException {
		StmtIterator it = configModel.listStatements(null, RDF.type, Config.MediatorConfig);
		Resource r;
		if (it.hasNext()) {
			r = it.nextStatement().getSubject();
			if (it.hasNext())
				log.warn("WARNING! Found multiple instances of <" + Config.MediatorConfig + "> in " + configFile + " (using first)!");
			return r;
		} else {
			try { configModel.close(); } catch (Exception ignore) {}
			throw new ConfigException("Couldn't find an instance of <" + Config.MediatorConfig + "> in '" + configFile + "'.");
		}
	}
	
	public String getAdminPassword() {
		return adminPassword;
	}
	
	public String getConfigFile() {
		return configFile;
	}

//	get sub Configs
	
	public GUIConfig getGuiConfig() {
		return guiConfig;
	}

	public DataSourceRegistryConfig getDataSourceRegistryConfig() {
		return dataSourceRegistryConfig;
	}
	
	public UserRegistryConfig getUserRegistryConfig() {
		return userRegistryConfig;
	}
	
	public FederatorConfig getFederatorConfig() {
		return federatorConfig;
	}

	/**
	 * @return the storeAssemblerModel
	 */
	public Resource getStoreAssemblerModelDesc() {
		return storeAssemblerModel;
	}
}

