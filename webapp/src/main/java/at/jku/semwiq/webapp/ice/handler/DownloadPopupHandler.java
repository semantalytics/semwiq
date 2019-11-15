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

import at.jku.semwiq.mediator.registry.model.User;

/**
 * @author thomas, Andreas Langegger, al@jku.at
 *
 */
public class DownloadPopupHandler {
	
	private boolean visible;
	private String userName;
	private String userPassword;
	private User user;

	private final String GUESTACCOUNT = "Guest";
	private final String ADMINACCOUNT = "Admin";
	
	//methods for other Server Faces handlers that are used in this one
	private SourceHandler sourceHandler;
	public void setSourceHandler(SourceHandler sourceHandler) {
		this.sourceHandler = sourceHandler;
	}
	
	
	// constructer
	public DownloadPopupHandler() {
		visible = false;
	}
	
	// getters and setters
	public boolean isVisible() {
		return visible;
	}
	
	public void setVisible(boolean visible) {
		this.visible = visible;
	}
	
	
	
	// program logic
	public void closePopup() {
        visible = false;
    }
    
    public String openPopup() {
		visible = true;
		return "popUp";
    }
    
   public com.icesoft.faces.context.Resource getDownload() {
	   return sourceHandler.getDownload();
   }

}
