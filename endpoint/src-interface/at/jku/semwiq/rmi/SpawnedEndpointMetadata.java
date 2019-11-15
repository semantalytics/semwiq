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

import org.slf4j.LoggerFactory;

import org.slf4j.Logger;

/**
 * @author dorgon
 * 
 * unique endpoint is identified by hostname + port (only one endpoint per domain/port assumed)
 * 
 * wraps {@link EndpointMetadata} and provides a reference to the DaemonRegistry
 */
public class SpawnedEndpointMetadata extends EndpointMetadata implements Serializable {
	private static final long serialVersionUID = 1542561340567362927L;
	private static final Logger log = LoggerFactory.getLogger(SpawnedEndpointMetadata.class);

	/** reference to remote registry */
	private transient DaemonRegistry reg; // set by the EndpointController after spawning succeeded
	
	private final String hostname;
	private final int port;
	
	private String sparqlPath;
	private final String sparqlEndpointUri;

	public SpawnedEndpointMetadata(String hostname, int port) {
		this(hostname, port, new EndpointMetadata());
	}
	
	public SpawnedEndpointMetadata(String hostname, int port, String sparqlPath) {
		this(hostname, port, sparqlPath, new EndpointMetadata());
	}
	
	public SpawnedEndpointMetadata(String hostname, int port, EndpointMetadata meta) {
		this(hostname, port, null, meta);
	}

	/**
	 * @param hostname
	 * @param port
	 * @param meta
	 */
	private SpawnedEndpointMetadata(String hostname, int port, String sparqlPath, EndpointMetadata meta) {
		this.hostname = hostname;
		this.port = port;
		
		this.sparqlPath = (sparqlPath == null) ? CommonConstants.SPARQL_ENDPOINT_PATH_PREFIX + "/endpoint" + port : sparqlPath;
		this.sparqlEndpointUri = InterfaceUtils.createURI(port, this.sparqlPath);
		
		if (meta.getDataSetBase() != null)
			dataSetBase = RuntimeReplacements.apply(meta.getDataSetBase(), this);
		else
			dataSetBase = InterfaceUtils.createURI(port, CommonConstants.PUBBY_RESOURCE_PREFIX);

		if (meta.getHomepage() != null)
			homepage = RuntimeReplacements.apply(meta.getHomepage(), this);
		else
			homepage = null;

		if (meta.getTitle() != null)
			title = RuntimeReplacements.apply(meta.getTitle(), this);
		else
			title = "Untitled Dataset";

		if (meta.getDescription() != null)
			desc = RuntimeReplacements.apply(meta.getDescription(), this);
		else
			desc = null;
		if (log.isDebugEnabled())
			log.debug("Created spawned endpoint meta data: " + toString() + " (sparqlPath = " + this.sparqlPath + ", sparqlEndpointUri = " + this.sparqlEndpointUri + ", dataSetBase = " + this.dataSetBase + ", " +
				"homepage = " + this.homepage + ", title = " + this.title + ", desc = " + this.desc + ")");
	}

	/**
	 * @param reg the DaemonRegistry to set
	 */
	public void setDaemonRegistry(DaemonRegistry reg) {
		this.reg = reg;
	}

	// various getters

	public String getHostname() {
		return hostname;
	}
	
	public int getPort() {
		return port;
	}
	
	public String getSparqlEndpointUri() {
		return sparqlEndpointUri;
	}

	public String getSparqlPath() {
		return sparqlPath;
	}

	public DaemonRegistry getDaemonRegistry() {
		return reg;
	}
	
	public String toString() {
		return getTitle() + " on " + getHostname() + ":" + getPort();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		return this.hashCode() == obj.hashCode();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return getHostname().hashCode() ^ getPort() << 2;
	}

}
