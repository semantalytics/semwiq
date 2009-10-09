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
package at.jku.semwiq.rmi;

import java.io.Serializable;

/**
 * @author dorgon
 *
 */
public class EndpointMetadata implements Serializable {
	private static final long serialVersionUID = 2903393461483579064L;

	protected String title;
	protected String desc;
	protected String homepage;
	protected String dataSetBase;

	/** constructor, called by controller */
	public EndpointMetadata() {
		// defaults
		this.title = CommonConstants.DEFAULT_TITLE;
		this.desc = CommonConstants.DEFAULT_DESCRIPTION;
		this.homepage = "http://" + CommonConstants.REPLACE_HOSTNAME + CommonConstants.REPLACE_PORT + "/";
		this.dataSetBase = homepage + "resource";
	}
	
	// getters
	
	public String getTitle() {
		return title;
	}
	
	public String getDescription() {
		return desc;
	}
	
	public String getHomepage() {
		return homepage;
	}
	
	public String getDataSetBase() {
		return dataSetBase;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public void setDescription(String desc) {
		this.desc = desc;
	}
	
	public void setHomepage(String homepage) {
		this.homepage = homepage;
	}
	
	public void setDataSetBase(String dataSetBase) {
		this.dataSetBase = dataSetBase;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return title;
	}

}
