<%
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
%><?xml version="1.0" encoding="ISO-8859-1" ?>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@page import="java.util.Iterator"%>
<%@page import="com.hp.hpl.jena.query.QuerySolution"%>
<%@page import="com.hp.hpl.jena.query.ResultSet"%>
<%@page import="java.util.List"%>
<%@page import="com.hp.hpl.jena.rdf.model.RDFNode"%>
<%@page import="com.hp.hpl.jena.rdf.model.Resource"%>
<%@page import="com.hp.hpl.jena.shared.PrefixMapping"%>
<%@page import="com.hp.hpl.jena.query.Query"%>
<%@page import="com.hp.hpl.jena.rdf.model.Literal"%>
<%@page autoFlush="true" %>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
	<title>SemWIQ - Query Results</title>
	<link rel="stylesheet" title="default style" href="<%=config.getServletContext().getContextPath() %>/styles.css"/>
</head>
<body>

<h1>Semantic Web Integrator and Query Processor (SemWIQ) - Query results</h1>

<p><a href="javascript:history.go(-1)">go back</a></p>
<table class="results">
<tr>
<%
Query query = (Query) request.getAttribute("query");
ResultSet result = (ResultSet) request.getAttribute("resultSet");

List vars = result.getResultVars();
Iterator it = vars.iterator();
while(it.hasNext()) { %>
	<th><%=(String) it.next() %></th>
<% } %>
</tr>
<% 
// already flush buffer, first solution may be rather delayed for expensive queries
response.flushBuffer();

QuerySolution s;
while (result.hasNext()) {
	out.print("<tr>\n");
	s = result.nextSolution();
	PrefixMapping map = query.getPrefixMapping();
	RDFNode n;
	String prefix, uri;
	it = vars.iterator();
	while (it.hasNext()) {
		out.print("<td class=\"results\">");
		n = s.get((String)it.next());
		if (n == null) {
			out.print("&nbsp;");
			continue;
		}
		else if (n instanceof Resource) {
			prefix = map.getNsURIPrefix(((Resource)n).getNameSpace());
			uri = ((Resource) n).getURI();
			if (prefix == null) out.print("&lt;<a href=\"" + uri + "\">" + uri + "</a>&gt;");
			else out.print(prefix + ":" + ((Resource)n).getLocalName());
		} 
		else if (n instanceof Literal) {
			out.print(((Literal)n).getLexicalForm());
		}
		out.print("&nbsp;&nbsp;</td>");
	}
out.print("</tr>\n");
response.flushBuffer();
}
%>
</table>
<jsp:include page="footer.jsp"></jsp:include>
</body>
</html>