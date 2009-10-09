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
package at.jku.semwiq.webapp.ice.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author thomas
 *
 */
public class DownloadServlet extends HttpServlet {
	
	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void service(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		ServletOutputStream o = res.getOutputStream();
		
		o.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\">");
		o.println("<html><head>");
		o.println("<link href=\"/pages/css/semwiq_style.css\" rel=\"stylesheet\" type=\"text/css\" />");
		o.println("</head>\n<body>");
		
		if (req.getSession().getAttribute("queryResult")!=null) {
			String result = (String) req.getSession().getAttribute("queryResult");
	 		
	 		char c;
	 		StringBuffer temp = new StringBuffer();
	 		for (int i=0; i<result.length(); i++) {
	 			c = result.charAt(i);
	 			if (c=='<') {
	 				System.out.println("charpos "+i+": "+c);
	 				temp.append("&lt;");
	 			} else if (c=='>') {
	 				temp.append("&gt;");
	 			} else {
	 				temp.append(c);
	 			}
	 			
	 		}
	 		o.println("<textarea cols=\"20\" id=\"logging_textarea\" name=\"Logging_textarea\" onmousedown=\"this.focus;\" readonly=\"readonly\" rows=\"2\" style=\"width: 100%; height: 400px\">"+temp.toString()+"</textarea>");
	 		o.println("<a href=\"/downloadFile\">save file</a>");
		} else {
			o.println("<p>Sorry, but no query was run. You must execute a query before downloading results.</p>");
		}
		o.println("\n</body>");
		
	}

}
