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

import java.io.InputStream;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecException;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.OpAsQuery;
import com.hp.hpl.jena.sparql.algebra.Table;
import com.hp.hpl.jena.sparql.algebra.TableFactory;
import com.hp.hpl.jena.sparql.algebra.TransformCopy;
import com.hp.hpl.jena.sparql.algebra.op.OpService;
import com.hp.hpl.jena.sparql.algebra.op.OpTable;
import com.hp.hpl.jena.sparql.engine.http.HttpParams;
import com.hp.hpl.jena.sparql.engine.http.HttpQuery;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIteratorResultSet;

/**
 * @author dorgon
 *
 */
public class CentralizedPlanTransform extends TransformCopy {

	@Override
	public Op transform(OpService service, Op subOp) {
        Query query = OpAsQuery.asQuery(subOp) ;
        
        if (!service.getService().isURI())
            throw new QueryExecException("Service URI not bound: " + service.getService());
        
        HttpQuery httpQuery = new HttpQuery(service.getService().getURI()) ;
        httpQuery.addParam(HttpParams.pQuery, query.toString() );
        httpQuery.setAccept(HttpParams.contentTypeResultsXML) ;
        InputStream in = httpQuery.exec() ;
        ResultSet rs = ResultSetFactory.fromXML(in);
        Table table = TableFactory.create(new QueryIteratorResultSet(rs));
        return OpTable.create(table);
	}
	
}
