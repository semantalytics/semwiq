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
package at.jku.semwiq.webapp.ice.handler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.faces.model.SelectItem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sun.management.Agent;
import at.jku.semwiq.mediator.Mediator;
import at.jku.semwiq.mediator.registry.DataSourceRegistry;
import at.jku.semwiq.mediator.registry.DataSourceRegistryManager;
import at.jku.semwiq.mediator.registry.RegistryException;
import at.jku.semwiq.mediator.registry.model.DataSource;
import at.jku.semwiq.mediator.registry.model.MonitoringProfile;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;



public class DatasourcePopupHandler {
	
	private static final Logger log = LoggerFactory.getLogger(ModifyPopupHandler.class);
	private boolean visible;
	private String datasourceURI;
	private String currentProfile;
	
	private String semwiq_informationState;
	private String semwiq_informationURI;
	private String semwiq_informationURILabel;
	private String semwiq_informationEndpoint;
	private String semwiq_informationEndpointLabel;
	private String semwiq_informationProviderType;
	private String semwiq_informationProviderName;
	private String semwiq_informationMaintainer;
	private String semwiq_informationMaintainerSameAs;
	private String semwiq_informationMonitoringProfile;
	private String semwiq_informationStatDate;
	
	//methods for other Server Faces handlers that are used in this one
	private InformationHandler informationHandler;
	public void setInformationHandler(InformationHandler informationHandler) {
		this.informationHandler = informationHandler;
	}
	
	private SemWIQHandler semwiqHandler;
	public void setSemwiqHandler(SemWIQHandler semwiqHandler) {
		this.semwiqHandler = semwiqHandler;
	}
	

	// constructer
	public DatasourcePopupHandler() {
		visible = false;
	}
	
	
	// getters and setters
	public String getDatasourceURI() {
		return datasourceURI;
	}

	public void setDatasourceURI(String datasourceURI) {
		this.datasourceURI = datasourceURI;
	}
	
	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}
	
	
	// business logic
	public void closePopup() {
        visible = false;
    }
	
	public String openPopup() {
    	visible = true;
    	return "popup datasource";
    }
	
	
	
	public String getCurrentProfile() {
		return currentProfile;
	}


	public void setCurrentProfile(String currentProfile) {
		this.currentProfile = currentProfile;
	}


	public String getSemwiq_informationState() {
		return semwiq_informationState;
	}


	public void setSemwiq_informationState(String semwiq_informationState) {
		this.semwiq_informationState = semwiq_informationState;
	}


	public String getSemwiq_informationURI() {
		return semwiq_informationURI;
	}


	public void setSemwiq_informationURI(String semwiq_informationURI) {
		this.semwiq_informationURI = semwiq_informationURI;
	}


	public String getSemwiq_informationURILabel() {
		return semwiq_informationURILabel;
	}


	public void setSemwiq_informationURILabel(String semwiq_informationURILabel) {
		this.semwiq_informationURILabel = semwiq_informationURILabel;
	}


	public String getSemwiq_informationEndpoint() {
		return semwiq_informationEndpoint;
	}


	public void setSemwiq_informationEndpoint(String semwiq_informationEndpoint) {
		this.semwiq_informationEndpoint = semwiq_informationEndpoint;
	}


	public String getSemwiq_informationEndpointLabel() {
		return semwiq_informationEndpointLabel;
	}


	public void setSemwiq_informationEndpointLabel(
			String semwiq_informationEndpointLabel) {
		this.semwiq_informationEndpointLabel = semwiq_informationEndpointLabel;
	}


	public String getSemwiq_informationProviderType() {
		return semwiq_informationProviderType;
	}


	public void setSemwiq_informationProviderType(String semwiq_informationProviderType) {
		if (semwiq_informationProviderType.equalsIgnoreCase("1")) {
			this.semwiq_informationProviderType = FOAF.Person.getURI();
		}
		else if (semwiq_informationProviderType.equalsIgnoreCase("2")) {
			this.semwiq_informationProviderType = FOAF.Organization.getURI();
		}
		else if (semwiq_informationProviderType.equalsIgnoreCase("3")) {
			this.semwiq_informationProviderType = FOAF.Group.getURI();
		}
	}


	public String getSemwiq_informationProviderName() {
		return semwiq_informationProviderName;
	}


	public void setSemwiq_informationProviderName(
			String semwiq_informationProviderName) {
		this.semwiq_informationProviderName = semwiq_informationProviderName;
	}


	public String getSemwiq_informationMaintainer() {
		return semwiq_informationMaintainer;
	}


	public void setSemwiq_informationMaintainer(String semwiq_informationMaintainer) {
		this.semwiq_informationMaintainer = semwiq_informationMaintainer;
	}


	public String getSemwiq_informationMaintainerSameAs() {
		return semwiq_informationMaintainerSameAs;
	}


	public void setSemwiq_informationMaintainerSameAs(
			String semwiq_informationMaintainerSameAs) {
		this.semwiq_informationMaintainerSameAs = semwiq_informationMaintainerSameAs;
	}


	public String getSemwiq_informationMonitoringProfile() {
		return semwiq_informationMonitoringProfile;
	}


	public void setSemwiq_informationMonitoringProfile(
			String semwiq_informationMonitoringProfile) {
		this.semwiq_informationMonitoringProfile = semwiq_informationMonitoringProfile;
	}


	public String getSemwiq_informationStatDate() {
		return semwiq_informationStatDate;
	}


	public void setSemwiq_informationStatDate(String semwiq_informationStatDate) {
		this.semwiq_informationStatDate = semwiq_informationStatDate;
	}


	public List getSemwiq_informationMonitoringProfiles() {
		List tmp;
		List<SelectItem> monitoringProfiles = new ArrayList<SelectItem>();
		Mediator mediator = semwiqHandler.getMediator();
		if (mediator==null || !mediator.isReady()) {
			semwiqHandler.setErrorMessage("Mediator is currently offline or not available.");
			return null;
		}
		else {
			try {
				tmp = mediator.getDataSourceRegistry().getAvailableMonitoringProfiles();
				for (Iterator iter = tmp.iterator(); iter.hasNext();) {
					MonitoringProfile temp = (MonitoringProfile) iter.next();
					monitoringProfiles.add(new SelectItem(temp.getUri(), (String) temp.getLocalName()));
				}
			} catch (RegistryException e) {
				// TODO Auto-generated catch block
				log.error(e.getMessage(), e);
			}
			return monitoringProfiles;
		}
	}
	

	public String addDatasource() {
		Mediator mediator = semwiqHandler.getMediator();
		if (mediator==null || !mediator.isReady()) {
			semwiqHandler.setErrorMessage("Mediator is currently offline or not available.");
			return "failure";
		}
		else {
			DataSourceRegistryManager reg = mediator.getDataSourceRegistry().getManager();
			String uri = this.getSemwiq_informationEndpoint();
	//		DataSource ds = reg.getOrCreateDataSource(this.getSemwiq_informationURI(), this.getSemwiq_informationEndpoint());

			// lookup monitoring profile without a Converter:
			MonitoringProfile profile = null;
			try {
				List<MonitoringProfile> profileList = semwiqHandler.getMediator().getDataSourceRegistry().getAvailableMonitoringProfiles();
				for (MonitoringProfile x : profileList) {
					if (x.getUri().equalsIgnoreCase(this.getCurrentProfile()))
						profile = x;
				}

				if (reg.register(this.getSemwiq_informationEndpoint(), profile)) {
					informationHandler.loadDatasourceList();
					visible = false;
					return "modifying success";
				}
			} catch (RegistryException e) {
				log.error(e.getMessage(), e);
			}
			return "modifying failed";
		}
	}
}
