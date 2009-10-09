<%/**
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
 */%>
<%@page import="at.jku.semwiq.mediator.Mediator"%>
<%@page import="java.util.Iterator"%>
<%@page import="at.jku.semwiq.mediator.registry.model.DataSource"%>
<%@page import="com.hp.hpl.jena.ontology.OntClass"%>
<%@page import="at.jku.semwiq.mediator.MediatorImpl"%>
<%@page import="com.hp.hpl.jena.rdf.model.Resource"%>
<%@page import="at.jku.rdfstats.RDFStatsModel"%>
<%@page import="java.util.List"%>
<%@page import="com.hp.hpl.jena.query.Dataset"%>
<%@page import="at.jku.rdfstats.RDFStatsDataset"%>
<%
	Mediator mediator = Webapp.fromServletContext(getServletContext()); 
if (!(mediator != null && mediator.isReady())) {
%><jsp:forward page="/not-available.jsp"/><%
	}
%>
<%@page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<?xml version="1.0" encoding="ISO-8859-1" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@page import="java.net.URLEncoder"%>
<%@page import="at.jku.semwiq.mediator.registry.monitor.DataSourceMonitor"%>
<%@page import="at.jku.semwiq.mediator.registry.model.MonitoringProfile"%>

<%@page import="at.jku.semwiq.webapp.Webapp"%><html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
	<title>SemWIQ - Statistics</title>
	<link rel="stylesheet" title="default style" href="<%=getServletContext().getContextPath() %>/styles.css"/>
</head>
<body>
<%
	if (mediator.isReady()) {
		DataSourceMonitor monitor = mediator.getDataSourceRegistry().getMonitor();
%>

<h1>Semantic Web Integrator and Query Processor (SemWIQ) - Statistics</h1>

<%
	RDFStatsModel s = mediator.getDataSourceRegistry().getRDFStatsModel();
	RDFStatsDataset dataset;
	
	Iterator<DataSource> it = mediator.getDataSourceRegistry().getRegisteredDataSources().iterator();
	DataSource ds;
	Iterator<Resource> classIt;
	Resource c;
	while (it.hasNext()) { 
		ds = it.next();
%>
	<h2><%=(ds.getLabel() != null) ? ds.getLabel() : ds.getUri()%></h2>
	<p>
		<%
			if (!ds.isEnabled()) {
		%><span style="color: red; font-weight: bold">DISABLED</span><%
			} else if (!ds.isAvailable()) {
		%><span style="color: red; font-weight: bold">OFFLINE</span><%
			} else {
		%><span style="color: green; font-weight: bold">ONLINE</span><%
			}
		%><br /> 
		<b>URI: </b><%=(ds.getUri() != null) ? "<a href=\"" + ds.getUri() + "\">" + ds.getUri() + "</a>" : "n/a"%><br />
		<b>Endpoint: </b><%=(ds.getSPARQLEndpointURL() != null) ? "<a href=\"" + ds.getSPARQLEndpointURL() + "\">" + ds.getSPARQLEndpointURL() + "</a>" : "n/a"%><br />
		<b>Monitor: </b><% if (ds.getMonitoringProfile() != null) {
			MonitoringProfile profile = ds.getMonitoringProfile(); %>
			<%= profile.getClass().getName() + " (interval: " + profile.getInterval() + " seconds" + ((monitor.isUpdating(ds)) ? ", currently updating..." : ")") %>
			<% } else { %>
				none
			<% } %><br />
		<b>Operations: </b><a href="<%=request.getContextPath() %>/registry?cmd=unregister&amp;endpointUri=<%=ds.getSPARQLEndpointURL() %>">Unregister</a> - <a href="<%=request.getContextPath() %>/registry?cmd=updateStats&amp;endpointUri=<%=ds.getSPARQLEndpointURL() %>">Schedule update</a><br />
		<%
		// TODO remove:
			s.getWrappedModel().write(System.out);
			dataset = s.getDataset(ds.getSPARQLEndpointURL());
			if (dataset == null) { // if no stat
		%>
			<b>No statistics available</b><br />
		<%
			} else { // stats available
			String uri = dataset.getSourceUrl();
		%>
			<b>Statistics from</b> <%=dataset.getDate() %><br />
			<b>Instances: </b> <%=dataset.getSubjectsTotal() %> subjects (<%=dataset.getAnonymousSubjectsTotal() %> anonymous) <br />
			<b>Histograms: LINK // TODO</b>
		<% } // stats available %>
	</p>
<%  } // next ds %>

<% } else { %>
<p>Sorry, the mediator is currently not available.</p>
<% } %>
</body>
</html>