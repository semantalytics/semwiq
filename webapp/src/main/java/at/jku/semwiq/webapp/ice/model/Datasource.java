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
package at.jku.semwiq.webapp.ice.model;

import java.util.List;

import com.hp.hpl.jena.rdf.model.Resource;

public class Datasource {
	
	private String semwiq_informationState;
	private String semwiq_informationURI;
	private String semwiq_informationURILabel;
	private String semwiq_informationEndpoint;
	private String semwiq_informationEndpointLabel;
	private Resource semwiq_informationProviderType;
	private String semwiq_informationProviderName;
	private String semwiq_informationMaintainer;
	private String semwiq_informationMaintainerSameAs;
	private String semwiq_informationMonitoringProfile;
	private String semwiq_informationStatDate;
	
	private List<WebAppRessource> resource;


	// getters and setters
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
	
	public String getSemwiq_informationMaintainer() {
		return semwiq_informationMaintainer;
	}
	
	public void setSemwiq_informationMaintainer(String semwiq_informationMaintainer) {
		this.semwiq_informationMaintainer = semwiq_informationMaintainer;
	}
	
	public String getSemwiq_informationStatDate() {
		return semwiq_informationStatDate;
	}
	
	public void setSemwiq_informationStatDate(String semwiq_informationStatDate) {
		this.semwiq_informationStatDate = semwiq_informationStatDate;
	}
	
	public List<WebAppRessource> getResource() {
		return resource;
	}

	public void setResource(List<WebAppRessource> resource) {
		this.resource = resource;
	}

	public Resource getSemwiq_informationProviderType() {
		return semwiq_informationProviderType;
	}

	public void setSemwiq_informationProviderType(
			Resource type) {
		this.semwiq_informationProviderType = type;
	}

	public String getSemwiq_informationProviderName() {
		return semwiq_informationProviderName;
	}

	public void setSemwiq_informationProviderName(
			String semwiq_informationProviderName) {
		this.semwiq_informationProviderName = semwiq_informationProviderName;
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

}
