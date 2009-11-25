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

import com.hp.hpl.jena.sparql.ARQNotImplemented;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.table.TableN;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterDefaulting;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterRoot;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterSingleton;
import com.hp.hpl.jena.sparql.engine.main.QC;
import com.hp.hpl.jena.sparql.serializer.SerializationContext;
import com.hp.hpl.jena.sparql.util.IndentedWriter;
import com.hp.hpl.jena.sparql.util.Utils;

/**
 * @author dorgon, Andreas Langegger, al@jku.at
 *
 */
public class QueryIterBlockedOptionalIndex extends QueryIterBlockedRepeatApply {
    private Op op ;

    public QueryIterBlockedOptionalIndex(QueryIterBlocked input, Op op, ExecutionContext context) {
        super(input, context);
        this.op = op;
    }

    @Override
    protected QueryIterBlocked nextStage(TableN bindings)
    {
//        Op op2 = QC.substitute(op, binding) ;
    	// TODO
//    	((OpInputBindings) op).setBindings(bindings);
//        QueryIterator thisStep = QueryIterRoot.create(getExecContext()) ;
//        
//        QueryIterator cIter = QC.execute(op, thisStep, super.getExecContext()) ;
//        cIter = new QueryIterDefaulting(cIter, binding, getExecContext()) ;
//    	return null; // cIter ;
    	throw new ARQNotImplemented();
    }
    
    @Override
    protected void details(IndentedWriter out, SerializationContext sCxt)
    {
        out.println(Utils.className(this)) ;
        out.incIndent() ;
        op.output(out, sCxt) ;
        out.decIndent() ;
    }

}
