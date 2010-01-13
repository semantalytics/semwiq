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
import at.jku.semwiq.mediator.registry.UserRegistry;
import at.jku.semwiq.mediator.registry.UserRegistryImpl;
import at.jku.semwiq.mediator.registry.model.User;


public class LoginPopupHandler {
	
	private boolean visible;
	private User currentUser;
	
	private String userName;
	private String userPassword;

	//methods for other Server Faces handlers that are used in this one
	private SemWIQHandler semwiqHandler;
	public void setSemwiqHandler(SemWIQHandler semwiqHandler) {
		this.semwiqHandler = semwiqHandler;
	}
	
	// constructer
	public LoginPopupHandler() {
		visible = false;
		currentUser = semwiqHandler.getMediator().getUserRegistry().getGuestUser();
	}
	
	// getters and setters
	public boolean isVisible() {
		return visible;
	}
	
	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	// popup data
	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getUserPassword() {
		return userPassword;
	}

	public void setUserPassword(String pwd) {
		userPassword = pwd;
	}
	
	// current user data
	public User getUser() {
		return currentUser;
	}
	
	public String getAccountName() {
		return currentUser.getName();
	}
	
	// program logic
	public void closePopup() {
        visible = false;
    }
    
    public String openPopup() {
    	FacesContext context = FacesContext.getCurrentInstance();
 		ExternalContext ec = context.getExternalContext();
 		HttpServletRequest request = (HttpServletRequest)ec.getRequest(); 		
 		
 		if(semwiqHandler.getMediator().getUserRegistry().isGuestUser(currentUser)) {
 			visible = true;
 			return "popUp";
 		} else {
 			visible = false;
 			return verifyUser(); //TODO ask Thomas, why this?
 		}
    }
    
    public String verifyUser() {
    	UserRegistry userReg = semwiqHandler.getMediator().getUserRegistry();
    	User user = userReg.getUser(userName, userPassword);
    	if (user != null && userReg.isSuperUser(user)) {
    		this.setAdminRender(true);
    		currentUser = user;
    		visible = false;
    		return "adminLogin";
    	} else {
    		visible = false;
    		return "noentry";
    	}
	}
    
	public String logout() {
		FacesContext context = FacesContext.getCurrentInstance();
 		ExternalContext ec = context.getExternalContext();
 		final HttpServletRequest request = (HttpServletRequest)ec.getRequest();
 		destroyUserDetails();
 	    return "navigation_admin_logout";
	}
	
	private void destroyUserDetails() {
		UserRegistry userReg = semwiqHandler.getMediator().getUserRegistry();
		currentUser = userReg.getGuestUser();
		this.setAdminRender(false);
	}
	
	
	// code that is needed for displaying the correct navigation items
	
	private boolean adminRender = false;
	
	public boolean isAdminRender() {
		return adminRender;
	}
	
	public boolean isAdminNavRender() {
		return adminRender;
	}

	public void setAdminRender(boolean adminRender) {
		this.adminRender = adminRender;
	}
	
	public boolean isNormalNavRender() {
		return !adminRender;
	}
	
	public String getColumnClasses() {
		if (isAdminRender()) {
			return "navCol1, navCol2, navCol3, navCol4, navCol4, navCol4, navCol5";
		}
		else {
			return "navCol1, navCol2, navCol3, navCol4, navCol5";
		}
	}
}
