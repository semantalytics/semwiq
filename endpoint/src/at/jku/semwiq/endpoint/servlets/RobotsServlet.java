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
package at.jku.semwiq.endpoint.servlets;

import java.io.BufferedReader;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import at.jku.semwiq.endpoint.Constants;
import at.jku.semwiq.endpoint.Utils;
import at.jku.semwiq.rmi.EndpointMetadata;
import at.jku.semwiq.rmi.RuntimeReplacements;
import at.jku.semwiq.rmi.SpawnedEndpointMetadata;

/**
 * @author dorgon
 *
 */
public class RobotsServlet extends HttpServlet {
	private static final long serialVersionUID = 3269086907214616944L;
	
	private static String content;
	
	/* (non-Javadoc)
	 * @see javax.servlet.GenericServlet#init()
	 */
	@Override
	public void init() throws ServletException {
		StringBuilder sb = new StringBuilder();
		try {
			BufferedReader r = new BufferedReader(Utils.getReader(Constants.DISCOVERY_BASE_ROBOTS));
			String line;
			while ((line = r.readLine()) != null)
				sb.append(line).append('\n');
			
			content = sb.toString();
			if (RuntimeReplacements.matches(content))
				content = RuntimeReplacements.apply(content, (SpawnedEndpointMetadata) getServletContext().getAttribute(Constants.ENDPOINT_METADATA_ATTRIB));

		} catch (Exception e) {
			throw new ServletException("Failed to load base robots.txt: " + Constants.DISCOVERY_BASE_ROBOTS, e);
		}
	}
	
	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		res.setContentType("text/plain");
		res.getOutputStream().println(content);
	}
}
