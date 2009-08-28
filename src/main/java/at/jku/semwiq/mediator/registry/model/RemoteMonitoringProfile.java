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
package at.jku.semwiq.mediator.registry.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.jku.semwiq.mediator.vocabulary.SDV;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.shared.Lock;

/**
 * @author dorgon
 *
 */
public class RemoteMonitoringProfile extends MonitoringProfile {
	private static final Logger log = LoggerFactory.getLogger(RemoteMonitoringProfile.class);
	
	public static final String DEFAULT_STATSURL = "%%PROTOCOL%%://%%DOMAIN%%:%%PORT%%/rdfstats-%%PORT%%.nt.zip";
	
	/**
	 * @param instance
	 */
	public RemoteMonitoringProfile(Resource instance) {
		super(instance);
	}

	/**
	 * @param ds
	 * @return the statistics URL, given the datasource ds
	 */
	public String getStatisticsUrl(DataSource ds) {
		String endpoint = ds.getSPARQLEndpointURL();
		model.enterCriticalSection(Lock.READ);
		try {
			Statement s = resource.getProperty(SDV.statsUrl);
			if (s == null) { // build default
				return replaceMacros(DEFAULT_STATSURL, endpoint);
			} else {
				if (s.getObject().isLiteral())
					return replaceMacros(s.getString(), endpoint);
				else if (s.getObject().isURIResource())
					return replaceMacros(s.getResource().getURI(), endpoint);
				else
					return null;				
			}
		} catch (Exception e) {
			log.error("Failed to get " + SDV.statsUrl + " from monitoring profile " + toString() + ".", e);
			return null;
		} finally {
			model.leaveCriticalSection();
		}
	}
	
	public boolean updateOnlyIfNewer() {
		model.enterCriticalSection(Lock.READ);
		try {
			if (resource.hasProperty(SDV.updateOnlyIfNewer))
				return resource.getProperty(SDV.updateOnlyIfNewer).getBoolean();
		} catch (Exception e) {
			log.error("Failed to get " + SDV.updateOnlyIfNewer + " from monitoring profile " + toString() + ".", e);
		} finally {
			model.leaveCriticalSection();
		}
		return false;
	}
	
	private String replaceMacros(String url, String endpoint) {
		if (endpoint == null)
			return url;
		
		Matcher m = Pattern.compile("^(\\w+):(?://)?([_\\-\\w\\d\\.]*)(?:\\:(\\d*))?/([_\\-\\w\\d/\\.]*)(?:\\?(.*))?$").matcher(endpoint);
									   // 1 protocol     2 domain           3 port       4 path         5 query string
		if (m.find()) {
			url = url.replaceAll("%%ENDPOINT-URI%%", endpoint);
			if (m.group(1) != null)
				url = url.replaceAll("%%PROTOCOL%%", m.group(1));
			if (m.group(2) != null)
				url = url.replaceAll("%%DOMAIN%%", m.group(2));
			if (m.group(3) != null)
				url = url.replaceAll("%%PORT%%", m.group(3));
			else
				url = url.replaceAll("%%PORT%%", "80"); // default port
			if (m.group(4) != null)
				url = url.replaceAll("%%PATH%%", m.group(4));
			if (m.group(5) != null)
				url = url.replaceAll("%%QUERY%%", m.group(5));
		}	
		return url;
	}
	
	/* (non-Javadoc)
	 * @see at.jku.semwiq.mediator.registry.model.MonitoringProfile#toString()
	 */
	@Override
	public String toString() {
		return "RemoteMonitoringProfile" + (resource.isURIResource() ? " <" + getUri() + ">" : "");
	}
}
