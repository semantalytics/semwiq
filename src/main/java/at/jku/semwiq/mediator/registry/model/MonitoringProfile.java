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

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.jku.rdfstats.RDFStatsConfiguration;
import at.jku.semwiq.mediator.util.WrappedResource;
import at.jku.semwiq.mediator.vocabulary.SDV;

import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * @author dorgon
 *
 */
public abstract class MonitoringProfile extends WrappedResource {
	private static final Logger log = LoggerFactory.getLogger(MonitoringProfile.class);
	
	/** default monitoring profile URI */
	private static final String defaultVoidProfileUri = "http://semwiq.sourceforge.net/monitor#defaultVoidProfile";
	private static final String defaultCentralizedProfileUri = "http://semwiq.sourceforge.net/monitor#defaultCentralizedProfile";
	private static final String defaultRemoteProfileUri = "http://semwiq.sourceforge.net/monitor#defaultRemoteProfile";

	/** default monitoring profile */
	private static final MonitoringProfile defaultVoidProfile;
	private static final MonitoringProfile defaultCentralizedProfile;
	private static final MonitoringProfile defaultRemoteProfile;

	// initialize default profiles
	static {
		Resource voidProfile = ModelFactory.createDefaultModel().createResource(defaultVoidProfileUri, SDV.VoidMonitoringProfile);
		defaultVoidProfile = new VoidMonitoringProfile(voidProfile) {
			@Override public int getInterval() { return 600; }
			@Override public String getLabel() { return "default voiD monitoring profile"; }
			@Override public boolean updateOnlyIfNewer() { return true; }
		};

		Resource centProfile = ModelFactory.createDefaultModel().createResource(defaultCentralizedProfileUri, SDV.CentralizedMonitoringProfile);
		defaultCentralizedProfile = new CentralizedMonitoringProfile(centProfile) {
			@Override public int getInterval() { return 600; }
			@Override public String getLabel() { return "default centralized monitoring profile"; }
			@Override public RDFStatsConfiguration getStatsSettings() { return RDFStatsConfiguration.getDefault(); }
		};
		
		Resource remProfile = ModelFactory.createDefaultModel().createResource(defaultRemoteProfileUri, SDV.RemoteMonitoringProfile);
		defaultRemoteProfile = new RemoteMonitoringProfile(remProfile) {
			@Override public int getInterval() { return 600; }
			@Override public String getLabel() { return "default remote monitoring profile"; }
			@Override public boolean updateOnlyIfNewer() { return true; }
			@Override public String getStatisticsUrl(DataSource ds) { return RemoteMonitoringProfile.DEFAULT_STATSURL; }
		};
	}


	public static MonitoringProfile getDefaultVoidProfile() {
		return defaultVoidProfile;
	}
	
	public static MonitoringProfile getDefaultCentralizedProfile() {
		return defaultCentralizedProfile;
	}
	
	public static MonitoringProfile getDefaultRemoteProfile() {
		return defaultRemoteProfile;
	}

	/** factory
	 * 
	 * @param pRes
	 * @return the concrete profile impl
	 */
	public static MonitoringProfile create(Resource pRes) {
		// default types - hardcoded
		if (pRes.getURI().equals(defaultVoidProfileUri))
			return defaultVoidProfile;
		else if (pRes.getURI().equals(defaultCentralizedProfileUri))
			return defaultCentralizedProfile;
		else if (pRes.getURI().equals(defaultRemoteProfileUri))
			return defaultRemoteProfile;
		
		// user-defined types, construct based on type
		if (pRes.hasProperty(RDF.type, SDV.RemoteMonitoringProfile))
			return new RemoteMonitoringProfile(pRes);
		else if (pRes.hasProperty(RDF.type, SDV.CentralizedMonitoringProfile))
			return new CentralizedMonitoringProfile(pRes);
		else if (pRes.hasProperty(RDF.type, SDV.VoidMonitoringProfile))
			return new VoidMonitoringProfile(pRes);
		else
			return null;
	}
	
	/**
	 * @param instance
	 */
	protected MonitoringProfile(Resource instance) {
		super(instance);
	}

	public boolean updateOnStartup() {
		model.enterCriticalSection(Lock.READ);
		try {
			Statement s = resource.getProperty(SDV.updateOnStartup);
			return s != null && s.getBoolean();
		} catch (Exception e) {
			log.error("Failed to get " + SDV.updateOnStartup + " from monitoring profile " + toString() + ".", e);
		} finally {
			model.leaveCriticalSection();
		}
		return false;
	}
	
	public int getInterval() {
		model.enterCriticalSection(Lock.READ);
		try {
			Statement s = resource.getProperty(SDV.interval);
			if (s != null) return s.getInt();
		} catch (Exception e) {
			log.error("Failed to get " + SDV.interval + " from monitoring profile " + toString() + ".", e);
		} finally {
			model.leaveCriticalSection();
		}
		return 0;
	}

	/**
	 * @return
	 */
	public static List<MonitoringProfile> getAvailableDefaultProfiles() {
		List<MonitoringProfile> list = new ArrayList<MonitoringProfile>();
		list.add(defaultCentralizedProfile);
		list.add(defaultRemoteProfile);
		list.add(defaultVoidProfile);
		return list;
	}

	@Override
	public abstract String toString();
}
