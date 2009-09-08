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

import at.jku.semwiq.mediator.registry.DataSourceRegistry;
import at.jku.semwiq.mediator.registry.RegistryException;
import at.jku.semwiq.mediator.registry.model.DataSource;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.algebra.table.TableN;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterConcat;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.serializer.SerializationContext;
import com.hp.hpl.jena.sparql.util.FmtUtils;
import com.hp.hpl.jena.sparql.util.IndentedWriter;
import com.hp.hpl.jena.sparql.util.Utils;

/**
 * @author dorgon, Andreas Langegger, al@jku.at
 *
 */
public class QueryIterBlockedUnion extends QueryIterBlockedRepeatApply {
	private final Triple tp;
	private final ExprList exprs;
	private final DataSourceRegistry dsRegistry;
	
    /**
     * @param input
     * @param opFed
     * @param context
     */
    public QueryIterBlockedUnion(QueryIterator input, Triple tp, ExprList exprs, DataSourceRegistry dsRegistry, ExecutionContext context) {
        super(input, context);
        this.tp = tp;
        this.exprs = exprs;
        this.dsRegistry = dsRegistry;
    }
    
    /* (non-Javadoc)
     * @see at.jku.semwiq.mediator.engine.op.QueryIterBlockedRepeatApply#nextStage(com.hp.hpl.jena.sparql.algebra.table.TableN)
     */
    @Override
    protected QueryIterator nextStage(TableN bindings) {
    	QueryIterConcat unionQIter = new QueryIterConcat(getExecContext()) ;
    	
        try {
			for (DataSource ds : dsRegistry.getAvailableRelevantDataSources(tp.getSubject(), tp.getPredicate(), tp.getObject(), exprs)) {        	
//            subOp = QC.substitute(subOp, binding) ;
//            QueryIterator parent = new QueryIterSingleton(binding, getExecContext()) ;
//            QueryIterator qIter = QC.execute(subOp, parent, getExecContext()) ;
//            unionQIter.add(qIter) ;

				QueryIterBlockedService qIter = new QueryIterBlockedService(bindings, ds.getSPARQLEndpointURL(), tp, exprs, getExecContext());
			    unionQIter.add(qIter);
			}
		} catch (RegistryException e) {
			throw new RuntimeException("Failed to federate " + tp + ".", e);
		}
        
        return unionQIter ;
    }
    
    @Override
    public void output(IndentedWriter out, SerializationContext sCxt)
    { 
        out.println(Utils.className(this)) ;
        out.incIndent() ;
        if (exprs != null)
        	out.print(exprs);
        out.print(FmtUtils.stringForTriple(tp));
        out.decIndent() ;
        out.ensureStartOfLine() ;
    }

}
