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
package at.jku.semwiq.mediator.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.jku.semwiq.mediator.Constants;
import at.jku.semwiq.mediator.dataset.SemWIQDataset;
import at.jku.semwiq.mediator.dataset.SemWIQDatasetGraph;

import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.engine.QueryEngineFactory;
import com.hp.hpl.jena.sparql.engine.QueryEngineRegistry;
import com.hp.hpl.jena.sparql.util.ALog;
import com.hp.hpl.jena.sparql.util.Context;

/**
 * @author dorgon
 *
 */
public class MediatorQueryExecutionFactory {
	private static final Logger log = LoggerFactory.getLogger(MediatorQueryExecutionFactory.class);
	
	private MediatorQueryExecutionFactory() {}
	
    static private MediatorQueryExecution make(Query query, SemWIQDataset dataset) {
    	return make(query, dataset, null);
    }

    static private MediatorQueryExecution make(Query query, SemWIQDataset dataset, Context context) {
        query.validate();
        if (context == null)
        	context = ARQ.getContext().copy();
        
        SemWIQDatasetGraph dsg = null;
        if (dataset != null)
            dsg = (SemWIQDatasetGraph) dataset.asDatasetGraph();
        
        QueryEngineFactory f = findFactory(query, dsg, context);
        if (f == null) {
            ALog.warn(QueryExecutionFactory.class, "Failed to find a QueryEngineFactory for query: " + query);
            return null;
        }
        return new MediatorQueryExecution(query, dataset, context, f);
    }

    /** always returns a new {@link MediatorQueryEngine} ignoring this parameters */
    static private QueryEngineFactory findFactory(Query query, DatasetGraph dataset, Context context) {
        return MediatorQueryEngine.getFactory();
    }
    
	/**
	 * @param query
	 * @param virtualDataset
	 * @return
	 */
	public static MediatorQueryExecution create(Query query, SemWIQDataset virtualDataset) {
		return make(query, virtualDataset);
	}

}
