package at.jku.semwiq.rmi;

import java.net.InetAddress;
import java.rmi.registry.Registry;
import java.util.Hashtable;
import java.util.Map;

import org.joseki.JosekiVocab;

import com.hp.hpl.jena.assembler.JA;

public class CommonConstants {
	
	/** acquire local hostname */
	public static String HOSTNAME;
	static {
		try {
			HOSTNAME = InetAddress.getLocalHost().getCanonicalHostName();
		} catch (Exception e) {
			throw new RuntimeException("Cannot determine hostname, which is required to register at the controller.", e);
		}
	}
	
	/** default prefixes map and strings */
	public static final Map<String, String> DEFAULT_PREFIXES = new Hashtable<String, String>() {
		private static final long serialVersionUID = -6167513556069402751L;
		{
			put("rdfs",	"http://www.w3.org/2000/01/rdf-schema#");
			put("rdf",	"http://www.w3.org/1999/02/22-rdf-syntax-ns#");
			put("xsd",	"http://www.w3.org/2001/XMLSchema#");
			put("foaf",	"http://xmlns.com/foaf/0.1/");
			put("ex",	"http://example.org/");
			put("joseki", JosekiVocab.NS);
			put("ja",   JA.uri);
		}};
		
	public static final String DEFAULT_PREFIXES_N3;
	public static final String DEFAULT_PREFIXES_SPARQL;

	static {
		StringBuilder sbN3 = new StringBuilder();
		StringBuilder sbSPARQL= new StringBuilder();
		for (String pfx : DEFAULT_PREFIXES.keySet()) {
			sbN3.append("@prefix ").append(pfx).append(":\t\t<").append(DEFAULT_PREFIXES.get(pfx)).append("> .\n");
			sbSPARQL.append("PREFIX ").append(pfx).append(":\t\t<").append(DEFAULT_PREFIXES.get(pfx)).append(">\n");
		}
		DEFAULT_PREFIXES_N3 = sbN3.toString();
		DEFAULT_PREFIXES_SPARQL = sbSPARQL.toString();
	}
	
	public static final String DATASETDESC_LANGUAGE = "N3";
	public static final String DATASET_LANGUAGE = "N3";	

	// sparql endpoint path is fixed (according to with web.xml)
	public static final String SPARQL_ENDPOINT_PATH_PREFIX = "sparql";

	// pubby resource prefix is fixed
	public static final String PUBBY_RESOURCE_PREFIX = "resource";

	// RMI configuration
	public static int RMI_REGISTRY_PORT = Registry.REGISTRY_PORT;
	public static final String RMI_SERVICE_NAME = "semwiq/endpoint-daemon";

	// runtime replacement keywords:
	public static final String REPLACE_DETECT = "__";
	public static final String REPLACE_HOSTNAME = "__HOSTNAME__";
	public static final String REPLACE_PORT = "__PORT__";
	public static final String REPLACE_TITLE = "__TITLE__"; // project title
	public static final String DEFAULT_TITLE = "SemWIQ Endpoint";
	public static final String REPLACE_DESCRIPTION = "__DESCRIPTION__"; 
	public static final String DEFAULT_DESCRIPTION = "A SemWIQ-powered Linked Data endpoint"; 
	public static final String REPLACE_HOMEPAGE = "__HOMEPAGE__"; // project homepage
	public static final String REPLACE_SPARQLPATH = "__SPARQLPATH__";
	public static final String REPLACE_SPARQLENDPOINT = "__SPARQLENDPOINT__";
	public static final String REPLACE_DATASETBASE = "__DATASETBASE__"; // dataset base prefix for Pubby




}
