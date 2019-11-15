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
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import at.jku.semwiq.mediator.Mediator;
import at.jku.semwiq.mediator.registry.DataSourceRegistry;
import at.jku.semwiq.mediator.registry.DataSourceRegistryManager;
import at.jku.semwiq.mediator.registry.RegistryException;
import at.jku.semwiq.mediator.registry.model.DataSource;


public class RemovePopupHandler {
	
	private boolean visible;
	
	private SemWIQHandler semwiqHandler;
	public void setSemwiqHandler(SemWIQHandler semwiqHandler) {
		this.semwiqHandler = semwiqHandler;
	}
	
	private InformationHandler informationHandler;
	public void setInformationHandler(InformationHandler informationHandler) {
		this.informationHandler = informationHandler;
	}
	
	
	
	// constructer
	public RemovePopupHandler() {
		visible = false;
	}
	
	// getters and setters
	public boolean isVisible() {
		return visible;
	}
	
	public void setVisible(boolean visible) {
		this.visible = visible;
	}
	
	public String getDatasourceURI(){
		FacesContext context = FacesContext.getCurrentInstance();
 		ExternalContext ec = context.getExternalContext();
 		HttpServletRequest request = (HttpServletRequest)ec.getRequest();
 		InformationHandler ih = ((InformationHandler)request.getAttribute("informationHandler"));
 		return ih.getSemwiq_informationURI();
	}
	
	
	
	// program logic
	public void closePopup() {
        visible = false;
    }
    
    public String openPopup() {
    	FacesContext context = FacesContext.getCurrentInstance();
 		ExternalContext ec = context.getExternalContext();
 		HttpServletRequest request = (HttpServletRequest)ec.getRequest();
 		InformationHandler ih = ((InformationHandler)request.getAttribute("informationHandler"));
 		if (ih.getCurrentDatasource()!=null) {
 			visible = true;
 		}
    	return "popup";
    }
    
    public String deleteDatasource() { 		
 		 Mediator mediator = semwiqHandler.getMediator();
 		if (mediator==null || !mediator.isReady()) {
 			semwiqHandler.setErrorMessage("Mediator is currently offline or not available.");
 			return "failure";
 		}
 		else {
 			try {
	 			DataSourceRegistryManager reg = mediator.getDataSourceRegistry().getManager();
	 			DataSource delDs = reg.getDataSourceByEndpointUri((informationHandler.getCurrentDatasource().getSemwiq_informationEndpoint()));
				reg.unregister(delDs);
				informationHandler.loadDatasourceList();
			} catch (RegistryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	
	    	visible = false;
			return "ds deleted";
 		}	
    }
 
}
