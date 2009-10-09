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

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tools.ant.taskdefs.Replace.Replacefilter;

import at.jku.semwiq.log.LogBuffer;
import at.jku.semwiq.log.LogBufferDispatcher;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.LoggingEvent;

/**
 * @author dorgon
 *
 */
public class LoggingServlet extends HttpServlet {
	private static final long serialVersionUID = -7415830699850379841L;

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#getLastModified(javax.servlet.http.HttpServletRequest)
	 */
	@Override
	protected long getLastModified(HttpServletRequest req) {
		return System.currentTimeMillis();
	}
	
	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void service(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		ServletOutputStream o = res.getOutputStream();
		
		o.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\">");
		o.println("<html><head>");
		o.println("<link href=\"styles.css\" rel=\"stylesheet\" type=\"text/css\" />");
		
		// JS doesn't execute before body is completely loaded.... not possible like this:
//		o.println("<script language=\"JavaScript\">\n" +
//				"	var timer = window.setInterval(window.scrollTo(Number.MAX_VALUE), 200);" +
//				"</script>");
		o.println("</head>\n<body>");
		
		LogBuffer logBuffer = LogBufferDispatcher.createLogBuffer();
		if (logBuffer!=null) {
			try {
				LoggingEvent event;
				String level;
				SimpleDateFormat df = new SimpleDateFormat();
				while (logBuffer.isActive()) {
					StringBuilder sb = new StringBuilder();
					event = logBuffer.nextEvent();
					level = (event.getLevel().equals(Level.ERROR) || event.getLevel().equals(Level.WARN)) ? "log_level_error" : "log_level";

					sb.append("<table class=\"log_table\"><tr><td class=\"").append(level).append("\">").append(event.getLevel().levelStr)
						.append("</td> <td class=\"log_timestamp\">").append(df.format(new Date(event.getTimeStamp())))
						.append("</td> <td class=\"log_msg\">").append(replaceHtmlEntities(event.getFormattedMessage())).append("<br>");
					if (event.getCallerData().length > 0)
						sb.append("<span class=\"log_details\">").append(event.getCallerData()[0]).append(" (Thread ").append(event.getThreadName()).append(")</span>");
					sb.append("</td></tr>");
					if (event.getThrowableProxy() != null) {
						sb.append("<tr><td colspan=\"2\">&nbsp;</td><td class=\"log_details\">");
						for (StackTraceElement el : event.getThrowableProxy().getThrowable().getStackTrace())
							sb.append(replaceHtmlEntities(el.toString())).append("<br>");
						sb.append("</td></tr>");
					}
					sb.append("</table>\n");
					
					o.println(sb.toString());
					o.flush();
				}
			} catch (Throwable e) {
				logBuffer.close();
				throw new ServletException("Log buffer interrupted.", e);
			}
		} else
			throw new ServletException("Logging not available");
	}

	/**
	 * @param formattedMessage
	 * @return
	 */
	private String replaceHtmlEntities(String s) {
		return s.replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("&", "&amp;").replaceAll("\"", "&quot;");
	}

}
