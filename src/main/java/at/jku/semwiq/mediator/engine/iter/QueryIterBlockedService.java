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
package at.jku.semwiq.mediator.engine.iter;

import java.io.InputStream;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.TrackedNode;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.OpAsQuery;
import com.hp.hpl.jena.sparql.algebra.OpVars;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP;
import com.hp.hpl.jena.sparql.algebra.op.OpFilter;
import com.hp.hpl.jena.sparql.algebra.op.OpService;
import com.hp.hpl.jena.sparql.algebra.table.TableN;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap;
import com.hp.hpl.jena.sparql.engine.http.HttpParams;
import com.hp.hpl.jena.sparql.engine.http.HttpQuery;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIter;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterConvert;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterRoot;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIteratorResultSet;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterConvert.Converter;
import com.hp.hpl.jena.sparql.engine.main.iterator.QueryIterService;
import com.hp.hpl.jena.sparql.expr.ExprList;

/**
 * @author dorgon, Andreas Langegger, al@jku.at
 *
 */
public class QueryIterBlockedService extends QueryIterBlockedRepeatApply {

	private static final Logger log = LoggerFactory.getLogger(QueryIterBlockedService.class);
	private OpService opService;
	private Query query;
	
    /**
	 * @param bindings
	 */
	public QueryIterBlockedService(QueryIterBlocked input, OpService opService, ExecutionContext execCxt) {
		super(input, execCxt);
		this.opService = opService;
		this.query = OpAsQuery.asQuery(opService.getSubOp());
	}

	@Override
	protected QueryIterBlocked nextStage(TableN bindings) {
		String endpointUri = opService.getService().getURI();

		try {
    		ProjectedBindingsCache cache = new ProjectedBindingsCache(bindings, OpVars.allVars(opService));
    	    query.setInitialBindingTable(cache.getProjectedBindingsTable());
			
    	    String queryStr = query.toString(query.getSyntax());
    	    log.debug("Delegated sub-query: " + queryStr);
	        HttpQuery httpQuery = new HttpQuery(endpointUri);
	        httpQuery.addParam(HttpParams.pQuery, queryStr);
	        httpQuery.setAccept(HttpParams.contentTypeResultsXML);
	        InputStream in = httpQuery.exec();
	        
	        ResultSet rs = ResultSetFactory.fromXML(in);
	        QueryIterator qIter = new QueryIteratorResultSet(rs);

	        // provenance first
	        QueryIterator qIterProvenance = new QueryIterConvert(qIter, new ProvenanceConverter(rs.getResultVars(), endpointUri), getExecContext());
	        QueryIterator qIter2 = cache.addCachedBindings(qIterProvenance, getExecContext());
	        QueryIterBlocked blockedIter = new QueryIterBlocked(qIter2, getExecContext());
	        return blockedIter;
    	} catch (Exception e) {
    		log.error("Error during query processing. Ignoring data from endpoint <" + endpointUri + ">.", e);
    		return new QueryIterBlocked(QueryIterRoot.create(getExecContext()), getExecContext());
    	}
	}
	
	class ProvenanceConverter implements Converter {
		private final List<String> vars;
		private final String sourceUri;
		
		public ProvenanceConverter(List<String> resultVars, String sourceUri) {
			this.vars = resultVars;
			this.sourceUri = sourceUri;
		}
		
		/* (non-Javadoc)
		 * @see com.hp.hpl.jena.sparql.engine.iterator.QueryIterConvert.Converter#convert(com.hp.hpl.jena.sparql.engine.binding.Binding)
		 */
		public Binding convert(Binding b) {
			Binding newBinding = new BindingMap();
			for (String v : vars) {
				Var var = Var.alloc(v);
//				newBinding.add(var, new TrackedNode(b.get(var), sourceUri));
				Node n = b.get(var);
				n.setSource(sourceUri);
				newBinding.add(var, n);
			}
			return newBinding;
		}
	}
	
}
