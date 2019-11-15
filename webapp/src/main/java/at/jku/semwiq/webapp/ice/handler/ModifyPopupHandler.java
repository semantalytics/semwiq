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
import at.jku.semwiq.mediator.registry.DataSourceRegistry;
import at.jku.semwiq.mediator.registry.RegistryException;
import at.jku.semwiq.mediator.registry.model.DataSource;
import at.jku.semwiq.mediator.registry.model.MonitoringProfile;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;



public class ModifyPopupHandler {
	
	private static final Logger log = LoggerFactory.getLogger(ModifyPopupHandler.class);
	private boolean visible;
	private String currentProfile;
	
	// integrating the informationHandler for access to the currentDatasource
	private InformationHandler informationHandler;
	public void setInformationHandler(InformationHandler informationHandler) {
		this.informationHandler = informationHandler;
	}
	
	private SemWIQHandler semwiqHandler;
	public void setSemwiqHandler(SemWIQHandler semwiqHandler) {
		this.semwiqHandler = semwiqHandler;
	}
	
	

	// constructer
	public ModifyPopupHandler() {
		visible = false;
	}
	
	
	// getters and setters
	
	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}
	
	
	// business logic
	public void closePopup() {
		informationHandler.loadDatasourceList();
        visible = false;
    }
	
	public String openPopup() {
		if (informationHandler.getCurrentDatasource()!=null) {
 			visible = true;
 		}
    	return "popup datasource";
    }

	public String modifyDatasource() {
		DataSourceRegistry reg = semwiqHandler.getMediator().getDataSourceRegistry();
		
// TODO re-register...
//		DataSource ds = reg.getDataSourceByEndpointUri(this.getSemwiq_informationURI());
//		try {
//			ds.requestExclusiveWriteLock();
//			ds.setLabel(this.getSemwiq_informationEndpoint());
//			Resource providerType = null;
//			System.out.println("providerType: "+getSemwiq_informationProviderType());
//			System.out.println("FOAF-Person: "+FOAF.Person.getURI());
//			if (this.getSemwiq_informationProviderType().equalsIgnoreCase(FOAF.Person.getURI())) {
//				providerType = FOAF.Person;
//			}
//			else if (this.getSemwiq_informationProviderType().equalsIgnoreCase(FOAF.Organization.getURI())) {
//				providerType = FOAF.Organization;
//			}
//			else if (this.getSemwiq_informationProviderType().equalsIgnoreCase(FOAF.Group.getURI())) {
//				providerType = FOAF.Group;
//			}
//			Agent provider = reg.getOrCreateAgent(ds.getServiceEndpoint()+"#provider", providerType);
//			provider.requestExclusiveWriteLock();
//			try {
//				provider.setLabel(this.getSemwiq_informationProviderName());
//				ds.setProvider(provider);
//			} finally {
//				provider.returnExclusiveWriteLock();
//			}
//			Person maintainer = reg.getOrCreateAgent(ds.getServiceEndpoint()+"#maintainer", FOAF.Person).asPerson();
//			maintainer.requestExclusiveWriteLock();
//			try {
//				maintainer.setLabel(this.getSemwiq_informationMaintainer());
//				maintainer.setSameAs(this.getSemwiq_informationMaintainerSameAs());
//				ds.setMaintainer(maintainer);
//			} finally {
//				maintainer.returnExclusiveWriteLock();
//			}
//			ds.setMaintainer(maintainer);
//			List<MonitoringProfile> profileList = semwiqHandler.getMediator().getRegistry().getAvailableMonitoringProfiles();
//			for (MonitoringProfile x : profileList) {
//				if (x.getUri().equalsIgnoreCase(this.getCurrentProfile())) {
//					ds.setMonitoringProfile(x);
//				}
//			}
//			
//			visible = false;
//			return "modifying success";
//		} catch (RegistryException e) {
//			log.error(e.getMessage(), e);
			return "modifying failed";
//		} finally {
//			ds.returnExclusiveWriteLock();
//		}
 		
	}
	
	
	// getters and setters for access to the currentDatasource
	public String getSemwiq_informationState() {
		if (informationHandler.getSemwiq_informationState()!=null)
			return informationHandler.getSemwiq_informationState();
		else
			return "";
	}
	
	public void setSemwiq_informationState(String semwiq_informationState) {
		informationHandler.setSemwiq_informationState(semwiq_informationState);
	}
	
	public String getSemwiq_informationURI() {
		if (informationHandler.getSemwiq_informationURI()!=null)
			return informationHandler.getSemwiq_informationURI();
		else
			return "";
	}
	
	public void setSemwiq_informationURI(String semwiq_informationURI) {
		informationHandler.setSemwiq_informationURI(semwiq_informationURI);
	}
	
	public String getSemwiq_informationURILabel() {
		if (informationHandler.getSemwiq_informationURILabel()!=null)
			return informationHandler.getSemwiq_informationURILabel();
		else
			return "";
	}
	
	public void setSemwiq_informationURILabel(String semwiq_informationURILabel) {
		informationHandler.setSemwiq_informationURILabel(semwiq_informationURILabel);
	}
	
	public String getSemwiq_informationEndpoint() {
		if (informationHandler.getSemwiq_informationEndpoint()!=null)
			return informationHandler.getSemwiq_informationEndpoint();
		else
			return "";
	}
	
	public void setSemwiq_informationEndpoint(String semwiq_informationEndpoint) {
		informationHandler.setSemwiq_informationEndpoint(semwiq_informationEndpoint);
	}
	
	public String getSemwiq_informationEndpointLabel() {
		if (informationHandler.getSemwiq_informationEndpointLabel()!=null)
			return informationHandler.getSemwiq_informationEndpointLabel();
		else
			return "";
	}
	
	public void setSemwiq_informationEndpointLabel(
			String semwiq_informationEndpointLabel) {
		informationHandler.setSemwiq_informationEndpointLabel(semwiq_informationEndpointLabel);
	}
	
	public String getSemwiq_informationProviderType() {
		if (informationHandler.getSemwiq_informationProviderType()!=null)
			return informationHandler.getSemwiq_informationProviderTypeURI();
		else
			return "";
	}
	
	public void setSemwiq_informationProviderType(String semwiq_informationProviderType) {
		Resource type = null;
		if (semwiq_informationProviderType.equalsIgnoreCase("1")) {
			type = FOAF.Person;
		}
		else if (semwiq_informationProviderType.equalsIgnoreCase("2")) {
			type = FOAF.Organization;
		}
		else if (semwiq_informationProviderType.equalsIgnoreCase("3")) {
			type = FOAF.Group;
		}
		informationHandler.setSemwiq_informationProviderType(type);
	}
	
	public String getSemwiq_informationProviderName() {
		if (informationHandler.getSemwiq_informationProviderName()!=null)
			return informationHandler.getSemwiq_informationProviderName();
		else
			return "";
	}
	
	public void setSemwiq_informationProviderName(String semwiq_informationProvider) {
		informationHandler.setSemwiq_informationProviderName(semwiq_informationProvider);
	}
	
	public String getSemwiq_informationMaintainer() {
		if (informationHandler.getSemwiq_informationMaintainer()!=null)
			return informationHandler.getSemwiq_informationMaintainer();
		else
			return "";
	}
	
	public void setSemwiq_informationMaintainer(String semwiq_informationMaintainer) {
		informationHandler.setSemwiq_informationMaintainer(semwiq_informationMaintainer);
	}
	
//	public String getSemwiq_informationMonitoringProfile() {
//		if (informationHandler.getSemwiq_informationMonitoringProfile()!=null)
//			return informationHandler.getSemwiq_informationMonitoringProfile();
//		else
//			return "";
//	}
	
//	public void setSemwiq_informationMonitoringProfile(String semwiq_informationMonitoringProfile) {
//		informationHandler.setSemwiq_informationMonitoringProfile(semwiq_informationMonitoringProfile);
//	}
	
	public String getSemwiq_informationMaintainerSameAs() {
		if (informationHandler.getSemwiq_informationMaintainerSameAs()!=null)
			return informationHandler.getSemwiq_informationMaintainerSameAs();
		else
			return "";
	}
	
	public void setSemwiq_informationMaintainerSameAs(String semwiq_informationMaintainerSameAs) {
		informationHandler.setSemwiq_informationMaintainerSameAs(semwiq_informationMaintainerSameAs);
	}
	
	/*
	public String getSemwiq_informationStatDate() {
		if (informationHandler.getSemwiq_informationStatDate()!=null)
			return informationHandler.getSemwiq_informationStatDate();
		else
			return "";
	}
	
	public void setSemwiq_informationStatDate(String semwiq_informationStatDate) {
		informationHandler.setSemwiq_informationStatDate(semwiq_informationStatDate);
	}
	*/
	
	public String getCurrentProfile() {
		return currentProfile;
	}


	public void setCurrentProfile(String currentProfile) {
		this.currentProfile = currentProfile;
	}



	public List getSemwiq_informationMonitoringProfiles() {
		List tmp;
		List<SelectItem> monitoringProfiles = new ArrayList<SelectItem>();
		try {
			tmp = semwiqHandler.getMediator().getDataSourceRegistry().getAvailableMonitoringProfiles();
			if(tmp!=null) {
				for (Iterator iter = tmp.iterator(); iter.hasNext();) {
					MonitoringProfile temp = (MonitoringProfile) iter.next();
					monitoringProfiles.add(new SelectItem(temp.getUri(), (String) temp.getLocalName()));
				}
			}
		} catch (RegistryException e) {
			// TODO Auto-generated catch block
			log.error(e.getMessage(), e);
		}
		return monitoringProfiles;
	}

}
