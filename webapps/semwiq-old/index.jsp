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
<%@page import="at.jku.semwiq.mediator.Mediator"%>
<%@page import="java.util.Iterator"%>
<%@page import="at.jku.semwiq.webapp.simple.QueryServlet"%>
<%@page import="at.jku.semwiq.mediator.conf.MediatorConfig"%>
<%@page import="at.jku.semwiq.webapp.simple.RegistryServlet"%>
<%@page import="at.jku.semwiq.mediator.MediatorImpl"%>
<% Mediator mediator = Webapp.fromServletContext(getServletContext()); 
if (!(mediator != null && mediator.isReady())) {
%><jsp:forward page="/not-available.jsp"/><% } %>
<%@page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<?xml version="1.0" encoding="ISO-8859-1" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%@page import="at.jku.semwiq.webapp.StartupListener"%>
<%@page import="at.jku.semwiq.mediator.Constants"%>
<%@page import="at.jku.semwiq.webapp.Webapp"%><html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
	<title>Semantic Web Integrator and Query Processor (SemWIQ)</title>
	<link rel="stylesheet" title="default style" href="<%=config.getServletContext().getContextPath() %>/styles.css"/>
	<script src="<%=config.getServletContext().getContextPath() %>/js/debugtools.js" type="text/javascript"></script>
	<script type="text/javascript">
		appRoot = '<%=config.getServletContext().getContextPath() %>';
	</script>
</head>
<body>

<h1>Semantic Web Integrator and Query Processor (SemWIQ)</h1>

<table style="border: none; padding: 0px; border-spacing: 0px; width: 100%">
<tr>
	<td style="vertical-align: bottom">
		<p>
			<b>Mediator service endpoint:</b> http://<%=request.getServerName() + ':' + request.getServerPort() + request.getContextPath() %><br />
			<b>SemWIQ Mediator Version: </b> <%=Constants.VERSION_STRING %><br />
		</p>
	</td>
	<td style="vertical-align: bottom; text-align: right">
		<p>
			<a href="<%=request.getContextPath() %>/catalog" style="text-decoration: none"><img src="<%=request.getContextPath() %>/images/stats-icon.gif" alt="Registered data sources" border="0" /> Registered data sources</a> &nbsp; 
			<a href="<%=request.getContextPath() %>/statistics.jsp" style="text-decoration: none"><img src="<%=request.getContextPath() %>/images/stats-icon.gif" alt="Statistics" border="0" /> Statistics</a>
			<a href="<%=request.getContextPath() %>/vocab" style="text-decoration: none"><img src="<%=request.getContextPath() %>/images/rdf-icon.gif" alt="Cached Vocabulary" border="0" /> Cached Vocabulary</a>
		</p>
		<p>You should install the <a href="http://dig.csail.mit.edu/2007/tab/" target="_blank">Tabulator Firefox extension</a> to be able to<br />
		   browse the statistics provided as RDF/XML-ABBREV only.</p>
	</td>
</tr>
</table>

<table style="border: none; padding: 0px; border-spacing: 10px; width: 100%">
<tr>
<td style="width: 50%; vertical-align: top;">
<form action="<%=request.getContextPath() %>/query" method="post" name="queryform">
<fieldset>
	<legend>Global SPARQL query</legend>
	<div>
		<textarea name="q" rows="10" cols="150" style="height:240px; width: 100%; font-size: 8pt; font-family: Monaco,Courier,Sans-serif">
<%=mediator.getConfig().getGuiConfig().getPrefixMappingString().replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;") %>
SELECT * WHERE {
	
}
		</textarea></div>
</fieldset>
<fieldset>
	<legend>Results format</legend>
	<div>
	<% QueryServlet.ResultFormat def = QueryServlet.ResultFormat.getDefault();
	   for (QueryServlet.ResultFormat fmt : QueryServlet.ResultFormat.values()) { %>
		<input type="radio" name="fmt" <%=(fmt.equals(def)) ? "checked=\"checked\" " : ""%>value="<%=fmt.name() %>" /> <%=fmt.printableName %><br />
	<% } %>
	</div>
	<div>
		<input type="reset" value="Clear" style="color: gray"/>
		<input type="button" value="Explain Plan" onclick="explain()" />
		<input type="submit" value="Execute" style="font-weight: bold"/>
	</div>
</fieldset>
</form>
</td>
<td style="width: 50%; vertical-align: top; display: none" id="dbgcell">
<form action="none" id="dbgform" name="dbgform">
<fieldset>
	<legend>Debugging</legend>
	<div>
		<textarea id="dbg" name="dbg" rows="10" cols="150" style="height:368px; width: 100%; font-size: 8pt; font-family: Monaco,Courier,Sans-serif">
		</textarea>
	</div>
	<div style="text-align: right">
		<input type="button" value="Hide" onclick="document.getElementById('dbgcell').style.display='none';" />
	</div>
</fieldset>
</form>
</td>
</tr>
</table>

<div style="border-spacing: 10px; width: 100%">
<form action="<%=request.getContextPath() %>/registry" accept-charset="utf-8" method="get" id="register" name="register" style="width: 100%">
<fieldset>
	<legend>Register SPARQL Endpoint</legend>
	<p>To quickly register an endpoint without explicitly providing meta-data about it, just enter the SPARQL endpoint and click <em>Register</em>:</p> 
	<p><input name="endpoint" value="http://" style="width: 400px"/> <input type="submit" value="Register" /><input type="hidden" name="cmd" value="register" /></p>
</fieldset>
</form>
</div>

<p>Note that the XML-based result formats may be displayed delayed by some browsers, because they provide special XML-tree rendering and will load all data first.</p>
<jsp:include page="footer.jsp"></jsp:include>

</body>
</html>
