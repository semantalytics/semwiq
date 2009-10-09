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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import at.jku.semwiq.mediator.Constants;
import at.jku.semwiq.mediator.Mediator;

public class AdministrationHandler {
	
	private final String LOGFILE_PATH="logs/semwiq-mediator.log";
	
	//methods for other Server Faces handlers that are used in this one
	private SemWIQHandler semwiqHandler;
	public void setSemwiqHandler(SemWIQHandler semwiqHandler) {
		this.semwiqHandler = semwiqHandler;
	}

	private InformationPopupHandler informationPopupHandler;
	public void setInformationPopupHandler(InformationPopupHandler informationPopupHandler) {
		this.informationPopupHandler = informationPopupHandler;
	}
	
	
	// methods for the semwiq_administration_configuration-JSPX

	private String configurationOutput;
	
	public String getConfigurationOutput() {
		StringBuffer buffer = new StringBuffer();
        try {
        	String configFilePath = semwiqHandler.getMediatorConfiguration().getConfigFile();
    		BufferedReader reader = new BufferedReader(new FileReader(configFilePath));
            
            String line = null;
			while ((line=reader.readLine()) != null) {
			    buffer.append(line);
			    buffer.append("\n");
			}
			 reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return buffer.toString();
	}
	
	public void setConfigurationOutput(String config) {
		this.configurationOutput = config;
	}
	
	public String saveChanges() {
		String configFilePath = semwiqHandler.getMediatorConfiguration().getConfigFile();
		try {
    		BufferedWriter writer = new BufferedWriter(new FileWriter(configFilePath));
            writer.write(configurationOutput);
            writer.close();
//          semwiqHandler.setRestartMessage(true);
            String extendInfo="In order to use the new configuration for SemWIQ you have to restart it in the maintenance section.";
            informationPopupHandler.addInformationDetails("SemWIQ Information", "SemWIQ Restart necessary", extendInfo);
            informationPopupHandler.openPopup();
            return "success";
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "failure";
		}
	}
	
//	public String getOutputLog() {
//		/*StringBuffer buffer = new StringBuffer();
//        try {
//    		BufferedReader reader = new BufferedReader(new FileReader(LOGFILE_PATH));
//            
//            String line = null;
//			while ((line=reader.readLine()) != null) {
//			    buffer.append(line);
//			    buffer.append("\n");
//			}
//			 reader.close();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return buffer.toString();*/
//		return semwiqHandler.getLogStream().toString();
//	}
	
	
	// methods for the semwiq_administration_maintainance-JSPX

	private String semwiqStatus;
	private String semwiqVersion;
	private String optimizerStatus;
	private String configurationFile;
	private String hostname;
	
	
	public String getSemwiqStatus() {
		Mediator tmpMediator = semwiqHandler.getMediator();
		if (tmpMediator.isReady()) {
			semwiqStatus = "is running";
		}
		else {
			semwiqStatus = "is stopped";
		}
		return semwiqStatus;
	}

	public void setSemwiqStatus(String semwiqStatus) {
		this.semwiqStatus = semwiqStatus;
	}

	public String getSemwiqVersion() {
		semwiqVersion = Constants.VERSION_STRING;
		return semwiqVersion;
	}

	public void setSemwiqVersion(String semwiqVersion) {
		this.semwiqVersion = semwiqVersion;
	}

	public String getConfigurationFile() {
		configurationFile = semwiqHandler.getMediator().getConfig().getConfigFile();
		return configurationFile;
	}

	public void setConfigurationFile(String configurationFile) {
		this.configurationFile = configurationFile;
	}

	public String getHostname() {
		FacesContext context = FacesContext.getCurrentInstance();
 		ExternalContext ec = context.getExternalContext();
 		HttpServletRequest request = (HttpServletRequest)ec.getRequest();
 		String serverURI = request.getServerName();
 		int serverPort = request.getServerPort();
 		String path = request.getContextPath();
		this.hostname = serverURI+":"+serverPort+path;
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}
	
	
	
	
}
