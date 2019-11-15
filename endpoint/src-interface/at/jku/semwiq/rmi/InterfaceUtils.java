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

import java.net.URLEncoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author dorgon
 * 
 */
public class InterfaceUtils {
	private static final Logger log = LoggerFactory.getLogger(InterfaceUtils.class);
	
	/**
	 * @param str
	 * @return
	 */
	public static DaemonRegistry getDaemonRegistry(String str) {
		if (str.startsWith("http://"))
			str = str.substring(7, str.length());
		int split = str.indexOf(":");
		if (split < 0)
			return new DaemonRegistry(str);
		else
			return new DaemonRegistry(str.substring(0, split), Integer.parseInt(str.substring(split + 1, str.length())));
	}

	public static String getRMIServiceName(String hostname, int port) {
		return "rmi://" + hostname + ":" + port + "/" + CommonConstants.RMI_SERVICE_NAME;
	}


	/**
	 * returns "http://" + HOSTNAME + ":" + PORT + "/" + path
	 *         if port == 80, the port section is omitted
	 *         
	 * @param port
	 * @param path
	 * @return
	 */
	public static String createURI(int port, String path) {
		StringBuilder sb = new StringBuilder();
		sb.append("http://").append(CommonConstants.HOSTNAME);
		if (port != 80)
			sb.append(":" + port);
		sb.append("/");
		sb.append(path);
		return sb.toString();
	}

	/**
	 * returns "http://" + HOSTNAME + ":" + PORT + "/" + path
	 *         if port == 80, the port section is omitted
	 *         
	 * @param port
	 * @param path
	 * @return
	 */
	public static String createEncodedURI(int port, String pathToEncode) {
		StringBuilder sb = new StringBuilder();
		sb.append("http://").append(CommonConstants.HOSTNAME);
		if (port != 80)
			sb.append(":" + port);
		sb.append("/");
		sb.append(URLEncoder.encode(pathToEncode));
		return sb.toString();
	}

}
