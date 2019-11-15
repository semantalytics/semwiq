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
public class RuntimeReplacements {

	/**
	 * check if s contains replacement keywords
	 * @param s
	 * @return
	 */
	public static boolean matches(String s) {
		return s.contains(CommonConstants.REPLACE_DETECT);
	}

	/**
	 * replace all keywords in s
	 * @param s
	 * @param meta
	 * @return replaced string
	 */
	public static String apply(String s, SpawnedEndpointMetadata meta) {	
		// first apply atoms like hostname, port, and sparql path
		s = s.replaceAll(CommonConstants.REPLACE_HOSTNAME, meta.getHostname());
		if (meta.getPort() == 80)
			s = s.replaceAll(CommonConstants.REPLACE_PORT, "");
		else
			s = s.replaceAll(CommonConstants.REPLACE_PORT, ":" + meta.getPort());
		s = s.replaceAll(CommonConstants.REPLACE_SPARQLPATH, meta.getSparqlPath());

		// apply URIs
		s = s.replaceAll(CommonConstants.REPLACE_HOMEPAGE, meta.getHomepage());
		s = s.replaceAll(CommonConstants.REPLACE_DATASETBASE, meta.getDataSetBase());
		s = s.replaceAll(CommonConstants.REPLACE_SPARQLENDPOINT, meta.getSparqlEndpointUri());

		// finally apply text fields
		s = s.replaceAll(CommonConstants.REPLACE_TITLE, meta.getTitle());
		s = s.replaceAll(CommonConstants.REPLACE_DESCRIPTION, meta.getDescription());
		return s;
	}
}