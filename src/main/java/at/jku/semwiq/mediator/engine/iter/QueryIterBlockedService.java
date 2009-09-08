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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sun.print.resources.serviceui;

import at.jku.semwiq.mediator.engine.op.ProjectedBindingsCache;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecException;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.OpAsQuery;
import com.hp.hpl.jena.sparql.algebra.OpVars;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP;
import com.hp.hpl.jena.sparql.algebra.op.OpFilter;
import com.hp.hpl.jena.sparql.algebra.op.OpService;
import com.hp.hpl.jena.sparql.algebra.table.TableN;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.http.HttpParams;
import com.hp.hpl.jena.sparql.engine.http.HttpQuery;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIter;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterRoot;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIteratorResultSet;
import com.hp.hpl.jena.sparql.engine.main.iterator.QueryIterService;
import com.hp.hpl.jena.sparql.expr.ExprList;

/**
 * @author dorgon, Andreas Langegger, al@jku.at
 *
 */
public class QueryIterBlockedService extends QueryIter {

	private static final Logger log = LoggerFactory.getLogger(QueryIterService.class);
	private final TableN bindings;
	private final String endpointURL;
	private final Triple tp;
	private final ExprList exprs;
	
	private final QueryIterator iterator;
	
    /**
	 * @param bindings
	 * @param endpointURL
	 * @param tp
	 * @param exprs
	 */
	public QueryIterBlockedService(TableN bindings, String endpointURL, Triple tp, ExprList exprs, ExecutionContext execCxt) {
		super(execCxt);
		this.bindings = bindings;
		this.endpointURL = endpointURL;
		this.tp = tp;
		this.exprs = exprs;
		
		this.iterator = init();
	}

	private QueryIterator init() {
    	try {
    		Op op;
    		BasicPattern bp = new BasicPattern();
    		bp.add(tp);
    		op = new OpBGP(bp);
    		if (exprs != null)
    			op = OpFilter.filter(exprs, op);
    		
    		Query query = OpAsQuery.asQuery(op);
    		
    		ProjectedBindingsCache cache = new ProjectedBindingsCache(bindings, OpVars.allVars(op));
    	    query.setInitialBindingTable(cache.getProjectedBindingsTable());
    	    
	        HttpQuery httpQuery = new HttpQuery(endpointURL);
	        httpQuery.addParam(HttpParams.pQuery, query.toString() );
	        httpQuery.setAccept(HttpParams.contentTypeResultsXML) ;
	        InputStream in = httpQuery.exec() ;
	        
	        ResultSet rs = ResultSetFactory.fromXML(in) ;
	        QueryIterator qIter = new QueryIteratorResultSet(rs) ;
	        
	        QueryIterator qIter2 = cache.addCachedBindings(qIter, getExecContext());
	        return qIter2;
    	} catch (Exception e) {
    		log.error("Error during query processing. Ignoring data for { " + tp + " } from endpoint <" + endpointURL + ">.", e);
    		return QueryIterRoot.create(getExecContext());
    	}
	}
	
	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.sparql.engine.iterator.QueryIteratorBase#closeIterator()
	 */
	@Override
	protected void closeIterator() {
		iterator.close();
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.sparql.engine.iterator.QueryIteratorBase#hasNextBinding()
	 */
	@Override
	protected boolean hasNextBinding() {
		return iterator.hasNext();
	}
	
	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.sparql.engine.iterator.QueryIteratorBase#moveToNextBinding()
	 */
	@Override
	protected Binding moveToNextBinding() {
		return iterator.next();
	}
    
}
