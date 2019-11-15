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

import java.util.List;

import at.jku.semwiq.mediator.registry.DataSourceRegistry;
import at.jku.semwiq.mediator.registry.RegistryException;
import at.jku.semwiq.mediator.registry.model.DataSource;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.table.TableN;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterConcat;
import com.hp.hpl.jena.sparql.engine.main.QC;
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
    protected List<Op> subOps;
    
    /**
     * @param input
     * @param subOps
     * @param context
     */
    public QueryIterBlockedUnion(QueryIterBlocked input, List<Op> subOps, ExecutionContext context) {
        super(input, context);
        this.subOps = subOps;
    }
    
    @Override
    protected QueryIterBlocked nextStage(TableN bindings) {
    	QueryIterConcat unionQIter = new QueryIterConcat(getExecContext());
    	
        for (Op subOp : subOps) {
        	QueryIterator parent = bindings.iterator(getExecContext());
        	QueryIterator qIter = QC.execute(subOp, parent, getExecContext());
        	unionQIter.add(qIter);
        }
        
        return new QueryIterBlocked(unionQIter, getExecContext());
    }

    @Override
    public void output(IndentedWriter out, SerializationContext sCxt)
    { 
        out.println(Utils.className(this)) ;
        out.incIndent() ;
        for (Op op : subOps)
            op.output(out, sCxt) ;
        out.decIndent() ;
        out.ensureStartOfLine() ;
    }
}
