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
package at.jku.semwiq.mediator.dataset;

import at.jku.semwiq.mediator.Mediator;
import at.jku.semwiq.mediator.MediatorImpl;
import at.jku.semwiq.mediator.vocabulary.Config;

import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.assembler.Mode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.sparql.core.assembler.DatasetAssembler;

/**
 * @author dorgon, Andreas Langegger, al@jku.at
 */
public class SemWIQDatasetAssembler extends DatasetAssembler {
	
	@Override
	public Object open(Assembler ass, Resource description, Mode mode) {
		Statement s = description.getProperty(Config.configFile);
		String configFile;
		Mediator m;
		try {
			if (s != null) {
				if (s.getObject().isLiteral())
					configFile = s.getString();
				else
					configFile = s.getResource().getURI();			
				m = new MediatorImpl(configFile);
			} else
				m = new MediatorImpl();
		
			return new SemWIQDataset(m);
		} catch (Exception e) {
			throw new RuntimeException("Failed to assemble virtual dataset: " + e);
		} 
	}
}
