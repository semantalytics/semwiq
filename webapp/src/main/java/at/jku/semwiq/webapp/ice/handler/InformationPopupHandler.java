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


public class InformationPopupHandler {
	
	private boolean visible;	
	
	// constructer
	public InformationPopupHandler() {
		visible = false;
	}
	
	// getters and setters
	public boolean isVisible() {
		return visible;
	}
	
	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	
	// control logic for handling the visibility og the popupt
	public void closePopup() {
        visible = false;
    }
    
    public String openPopup() {
    	this.setVisible(true);
    	return "success";
    }
    
    
    // data that is displayed inside the popup
    
    private String popUpTitle;
    private String outputSimpleInfo;
    private String outputExtendInfo;
;

	public String getPopUpTitle() {
		return popUpTitle;
	}

	public void setPopUpTitle(String popUpTitle) {
		this.popUpTitle = popUpTitle;
	}

	public String getOutputSimpleInfo() {
		return outputSimpleInfo;
	}

	public void setOutputSimpleInfo(String outputSimpleInfo) {
		this.outputSimpleInfo = outputSimpleInfo;
	}

	public String getOutputExtendInfo() {
		return outputExtendInfo;
	}

	public void setOutputExtendInfo(String outputExendInfo) {
		this.outputExtendInfo = outputExendInfo;
	}
    
	
	public void addInformationDetails(String title, String simpleInfo, String extendInfo) {
		this.setPopUpTitle(title);
		this.setOutputSimpleInfo(simpleInfo);
		this.setOutputExtendInfo(extendInfo);
	}
    
}
