package at.jku.semwiq.ctrl;

import at.jku.semwiq.rmi.CommonConstants;


public class Constants {

	/** controller specific defaults */
	public static final String DATASETDESC_TEMPLATE = CommonConstants.DEFAULT_PREFIXES_N3 +
			"[] rdf:type ja:RDFDataset ;\n" +
			"   rdfs:label \"example\" ;\n" +
			"   ja:defaultGraph [\n" +
			"      a ja:MemoryModel ;\n" +
			"   ] .\n";

}
