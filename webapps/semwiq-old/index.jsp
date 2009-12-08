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
%>
<%@page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@page import="java.util.Iterator"%>
<%@page import="java.util.List"%>
<%@page import="java.net.URLEncoder"%>
<%@page import="com.hp.hpl.jena.ontology.OntClass"%>
<%@page import="com.hp.hpl.jena.rdf.model.Resource"%>
<%@page import="com.hp.hpl.jena.query.Dataset"%>
<%@page import="at.jku.rdfstats.RDFStatsModel"%>
<%@page import="at.jku.rdfstats.RDFStatsDataset"%>
<%@page import="at.jku.semwiq.mediator.conf.MediatorConfig"%>
<%@page import="at.jku.semwiq.mediator.Constants"%>
<%@page import="at.jku.semwiq.mediator.Mediator"%>
<%@page import="at.jku.semwiq.mediator.MediatorImpl"%>
<%@page import="at.jku.semwiq.mediator.registry.monitor.DataSourceMonitor"%>
<%@page import="at.jku.semwiq.mediator.registry.model.MonitoringProfile"%>
<%@page import="at.jku.semwiq.mediator.registry.model.DataSource"%>
<%@page import="at.jku.semwiq.webapp.simple.QueryServlet"%>
<%@page import="at.jku.semwiq.webapp.simple.RegistryServlet"%>
<%@page import="at.jku.semwiq.webapp.StartupListener"%>
<%@page import="at.jku.semwiq.webapp.Webapp"%>
<% 

Mediator mediator = Webapp.fromServletContext(getServletContext()); 
if (!(mediator != null && mediator.isReady())) {
%><jsp:forward page="/not-available.jsp"/>
<% }

String root = config.getServletContext().getContextPath();
String sparqlEndpoint = "http://" + request.getServerName() + ':' + request.getServerPort() + '/' + Webapp.SPARQL_SERVICE_NAME;
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
	<title>Semantic Web Integrator and Query Processor (SemWIQ)</title>
	<link rel="stylesheet" title="default style" href="<%=root %>/styles.css"/>
	<script src="<%=root %>/js/debugtools.js" type="text/javascript"></script>
	<script type="text/javascript">
		appRoot = '<%=root %>';
	</script>
</head>
<body>

<h1>Semantic Web Integrator and Query Processor (SemWIQ)</h1>

<h2>Information</h2>
<b>SemWIQ mediator version: </b> <%=Constants.VERSION_STRING %><br />
<b>Using configuration file: </b> <%=mediator.getConfig().getConfigFile() %><br />
<b>Mediator SPARQL endpoint:</b> <a href="<%=sparqlEndpoint %>" target="_blank" title="SPARQL endpoint"><%=sparqlEndpoint %></a><br />
<b>Data sources registered: </b> <%=mediator.getDataSourceRegistry().getRegisteredDataSources().size() %> (<%=mediator.getDataSourceRegistry().getAvailableDataSources().size() %> available)<br />
<b>Using federator: </b> <%=mediator.getFederator().getName() %><br />


<h2>Links</h2>
<ul>
<li><a href="<%=root %>/snorql" title="Snorql">Query query interface</a> (Snorql known from <a href="http://www4.wiwiss.fu-berlin.de/bizer/d2r-server/" target="_blank">D2R-Server</a>)</li>
<li><a href="<%=root %>/logs" target="_blank">Server logs</a></li>
<li><a href="<%=root %>/vocab" title="View cached vocabularies">View cached vocabularies</a></li>
</ul>

<h2>Registered Data Sources</h2>

<%
DataSourceMonitor monitor = mediator.getDataSourceRegistry().getMonitor();
RDFStatsModel s = mediator.getDataSourceRegistry().getRDFStatsModel();
RDFStatsDataset dataset;

Iterator<DataSource> it = mediator.getDataSourceRegistry().getRegisteredDataSources().iterator();
DataSource ds;
Iterator<Resource> classIt;
Resource c;

%>
<table>
	<th>Label</th>
	<th>Status</th>
	<th>SPARQL Endpoint</th>
	<th>Monitoring Profile</th>
	<th>Size (triples)</th>
	<th>Operations</th>
</th>
<%
while (it.hasNext()) { 
	ds = it.next();
	dataset = s.getDataset(ds.getSPARQLEndpointURL());
%>
	<tr>
		<td><%=((ds.getLabel() != null) ? ds.getLabel() + "<br/>" : "") + "<" + ds.getUri() + ">" %></td>
		<%  if (!ds.isEnabled()) {
		%><td style="color: red; font-weight: bold">disabled</td><%
			} else if (!ds.isAvailable()) {
		%><td style="color: red; font-weight: bold">offline</td><%
			} else {
		%><td style="color: green; font-weight: bold">online</td><%
			}
		%>
		<td><a href="<%=ds.getSPARQLEndpointURL() %>"><%=ds.getSPARQLEndpointURL() %></a></td>
		<td><%
			MonitoringProfile profile = ds.getMonitoringProfile(); 
			if (profile != null) { %>
				<%=profile.getLabel() %><br />
				last update: <%=(dataset != null) ? dataset.getDate() : "n/a" %><br />
				interval: <%=profile.getInterval()%> seconds<br />
				<% if (monitor.isUpdating(ds)) { %>
					<span style="text-decoration: blink;">updating...</span>
				<% } %>
			<% } else { %>
				none
			<% } %></td>
		<td><%
			if (dataset != null) {
				int tt = dataset.getTriplesTotal();
				int st = dataset.getSubjectsTotal();
				int tps = tt/st;
			%>
			<%=tt %> triples<br />
			<%=st %> subjects<br />
			&Oslash; <%=tps %> t/s
			<% } else { %>
				n/a
			<% } %></td>
		
		<td>
			<a href="<%=root %>/histograms?endpointUri=<%=ds.getSPARQLEndpointURL() %>">View statistics</a><br />
		 	<a href="<%=root %>/registry?cmd=updateStats&amp;endpointUri=<%=ds.getSPARQLEndpointURL() %>">Update statistics</a><br />
			<a href="<%=root %>/registry?cmd=unregister&amp;endpointUri=<%=ds.getSPARQLEndpointURL() %>">Unregister</a></td>
	</tr>
<%  } // next ds %>
</table>

<p>
<form action="<%=root %>/registry" method="get" id="register" name="register">
Register by SPARQL endpoint: <input name="endpointUri" value="http://" style="width: 200px"/> &nbsp; <input type="submit" value="Register" /><input type="hidden" name="cmd" value="register" /><br />
</form>
Note: you can set a data source online by triggering a statistics update.
</p>

<p>&nbsp;</p>
<p class="footertext">&copy; <a href="http://www.faw.jku.at">Institute for Application-Oriented Knowledge Processing (FAW)</a>, <a href="http://www.jku.at">Johannes Kepler University Linz</a></p>
<p>&nbsp;</p>

<script language="JavaScript">
function Werteliste (querystring) {
	  if (querystring == '') return;
	  var wertestring = querystring.slice(1);
	  var paare = wertestring.split("&");
	  var paar, name, wert;
	  for (var i = 0; i < paare.length; i++) {
	    paar = paare[i].split("=");
	    name = paar[0];
	    wert = paar[1];
	    name = unescape(name).replace("+", " ");
	    wert = unescape(wert).replace("+", " ");
	    this[name] = wert;
	  }
}
var liste = new Werteliste(location.search);

if (liste['msg'])
	alert(liste['msg'])
</script>
</body>
</html>
