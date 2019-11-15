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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.xml.serialize.DOMSerializer;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import at.jku.semwiq.endpoint.Constants;
import at.jku.semwiq.rmi.EndpointMetadata;
import at.jku.semwiq.rmi.RuntimeReplacements;
import at.jku.semwiq.rmi.SpawnedEndpointMetadata;

/**
 * @author dorgon
 *
 */
public class SitemapServlet extends HttpServlet {
	private static final long serialVersionUID = -658585715584030694L;

	private static String content;
	
	/* (non-Javadoc)
	 * @see javax.servlet.GenericServlet#init()
	 * 
	 * we parse as XML, could possible add other XML elements
	 */
	@Override
	public void init() throws ServletException {
		DocumentBuilder builder;
		Document doc;
		try {
			builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			doc = builder.parse(Constants.DISCOVERY_BASE_SITEMAP);
		} catch (IOException e) {
			throw new ServletException("Failed to load sitemap base document: " + Constants.DISCOVERY_BASE_SITEMAP, e);
		} catch (ParserConfigurationException e) {
			throw new ServletException("Failed to initialize XML parser.", e);
		} catch (SAXException e) {
			throw new ServletException("Failed to parse sitemap XML document.", e);
		}
		
		try {
			replacements(doc.getDocumentElement());
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			XMLSerializer ser = new XMLSerializer(out, new OutputFormat("XML", "UTF-8", true));
			DOMSerializer domSer = ser.asDOMSerializer();
			domSer.serialize(doc);
			
			content = new String(out.toByteArray());
		} catch (IOException e) {
			throw new ServletException("Failed to serialize sitemap XML document.", e);
		}
	}
	
	/**
	 * @param el
	 */
	private void replacements(Node n) {
		NodeList children = n.getChildNodes();
		for (int i=0; i<children.getLength(); i++) {
			Node c = children.item(i);
			if (c instanceof Text) {
				String s = ((Text) c).getNodeValue();
				if (RuntimeReplacements.matches(s))
					c.setNodeValue(RuntimeReplacements.apply(s, (SpawnedEndpointMetadata) getServletContext().getAttribute(Constants.ENDPOINT_METADATA_ATTRIB)));
			} else
				replacements(c);
		}
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		res.setContentType("application/xml");
		res.getOutputStream().println(content);
	}
	
}
