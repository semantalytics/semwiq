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
package at.jku.semwiq.mediator.examples;

import java.io.StringReader;

import at.jku.semwiq.mediator.Mediator;
import at.jku.semwiq.mediator.dataset.SemWIQDataset;
import at.jku.semwiq.mediator.registry.RegistryException;
import at.jku.semwiq.mediator.vocabulary.Config;

import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * @author dorgon, Andreas Langegger, al@jku.at
 *
 */
public class AssembledSemWIQDatasetExample {

	/**
	 * @param args
	 * @throws RegistryException 
	 */
	public static void main(String[] args) throws RegistryException {
		// example assembler configuration
		String assConf = 
			"@prefix ja:	<http://jena.hpl.hp.com/2005/11/Assembler#> .\n" +
			"@prefix sq:	<http://purl.org/semwiq/mediator/config#> .\n" +
			"@prefix :		<http://example.com/> .\n" +
			":semwiq a sq:SemWIQDataset ; \n" +
			"	sq:configFile <file:etc/semwiq-config.ttl> . # optional";
		
		Model conf = ModelFactory.createDefaultModel();
		conf.read(new StringReader(assConf), null, "N3");
		conf.read(Config.getURI()); // load semwiq config definitions (sq:SemWIQDataset rdfs:subClassOf ja:Object, etc.)
		Resource semwiq = conf.getResource("http://example.com/semwiq");
		
		// assemble dataset and register one endpoint for testing...
		SemWIQDataset mediatedDataset = (SemWIQDataset) Assembler.general.open(semwiq);
		Mediator m = mediatedDataset.getMediator();
		m.getDataSourceRegistry().getManager().register(ExampleConstants.testEndpoint, ExampleConstants.monitoringProfile);
		m.getDataSourceRegistry().getMonitor().waitUntilFinished(); // blocks until monitor has finished updating
		
		QueryExecution qe = QueryExecutionFactory.create("SELECT DISTINCT ?t WHERE { ?s a ?t }", mediatedDataset);
		ResultSet r = qe.execSelect();
		ResultSetFormatter.out(r);
		
		mediatedDataset.close();
	}

}
