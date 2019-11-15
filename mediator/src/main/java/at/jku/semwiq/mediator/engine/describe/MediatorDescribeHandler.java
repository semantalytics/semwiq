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
package at.jku.semwiq.mediator.engine.describe;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.jku.semwiq.mediator.dataset.SemWIQDataset;
import at.jku.semwiq.mediator.registry.RegistryException;
import at.jku.semwiq.mediator.registry.model.DataSource;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.ARQConstants;
import com.hp.hpl.jena.sparql.core.describe.DescribeHandler;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;
import com.hp.hpl.jena.sparql.util.Context;

/**
 * @author dorgon
 *
 */
public class MediatorDescribeHandler implements DescribeHandler {
	private static final Logger log = LoggerFactory.getLogger(MediatorDescribeHandler.class);
	
	private Dataset dataset;
    private Model acc ;
	
    public void start(Model accumulateResultModel, Context cxt) {
        this.acc = accumulateResultModel;
        this.dataset = (Dataset)cxt.get(ARQConstants.sysCurrentDataset) ;
    }

    public void describe(Resource r) {
    	// only use this describe handler for the pseudo VirtualDataset
    	if (dataset instanceof SemWIQDataset) {
	        List<DataSource> list;
			try {
				list = ((SemWIQDataset) dataset).getMediator().getDataSourceRegistry().getAvailableDescribingDataSources(r.getURI());
				for (DataSource ds : list) {
					Query query = QueryFactory.create("DESCRIBE <" + r + ">");
					QueryEngineHTTP http = QueryExecutionFactory.createServiceRequest(ds.getSPARQLEndpointURL(), query);
					http.execDescribe(acc);
				}
			} catch (RegistryException e) {
				log.warn("Failed to get available describing data sources for " + r + ".", e);
			}
    	}
    }
    
    public void finish() {}
}
