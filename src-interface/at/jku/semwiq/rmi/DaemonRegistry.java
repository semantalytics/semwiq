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



/**
 * @author dorgon
 *
 */
public class DaemonRegistry {
	private final String hostname;
	private final int port;
	
	/**
	 * 
	 */
	public DaemonRegistry(String hostname) {
		this.hostname = hostname;
		this.port = CommonConstants.RMI_REGISTRY_PORT;
	}
	
	/**
	 * 
	 */
	public DaemonRegistry(String hostname, int port) {
		this.hostname = hostname;
		this.port = port;
	}
	
	/**
	 * @return the hostname
	 */
	public String getHostname() {
		return hostname;
	}
	
	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		return hashCode() == obj.hashCode();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return hostname.hashCode() ^ (port << 2);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "[" + hostname + ":" + port + "]";
	}
}
