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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.ARQNotImplemented;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.OpVars;
import com.hp.hpl.jena.sparql.algebra.table.TableN;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIter;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterDefaulting;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterDistinct;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterProject;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterRoot;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterSingleton;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIteratorCopy;
import com.hp.hpl.jena.sparql.engine.main.QC;
import com.hp.hpl.jena.sparql.engine.main.iterator.QueryIterJoin;
import com.hp.hpl.jena.sparql.engine.main.iterator.QueryIterLeftJoin;
import com.hp.hpl.jena.sparql.serializer.SerializationContext;
import com.hp.hpl.jena.sparql.util.IndentedWriter;
import com.hp.hpl.jena.sparql.util.Utils;

/**
 * @author dorgon, Andreas Langegger, al@jku.at
 *
 */
public class QueryIterBlockedOptionalIndex extends QueryIterBlockedRepeatApply {
    private Op right ;

    public QueryIterBlockedOptionalIndex(QueryIterBlocked input, Op right, ExecutionContext context) {
        super(input, context);
        this.right = right;
    }

    @Override
    protected QueryIterBlocked nextStage(TableN bindings) {
//    	OptionalBindingsCache cache = new OptionalBindingsCache(bindings, OpVars.allVars(right));
//    	TableN projected = cache.getProjectedBindingsTable();
//    	if (projected != null)
//    		bindings = projected;
    	ExecutionContext execCxt = getExecContext();

    	// get distinct left join bindings
    	ResultSet l = bindings.toResultSet();
		List<String> leftVars = l.getResultVars();
		Set<Var> origBindingVars = new HashSet<Var>();
		for (String v : leftVars)
			origBindingVars.add(Var.alloc(v));		
    	
		Set<Var> joinVars = new HashSet<Var>();
		joinVars.addAll(origBindingVars);
		joinVars.retainAll(OpVars.allVars(right));
		
    	List<Var> varList = new ArrayList<Var>(joinVars);
    	QueryIterator left = bindings.iterator(execCxt);
    	QueryIterator proj = new QueryIterProject(left, varList, execCxt);
    	QueryIterator semiLeft = new QueryIterDistinct(proj, execCxt);

    	// execute with right plan
    	QueryIterator optionals = QC.execute(right, semiLeft, execCxt);
    	
    	// OpConditional has no filter expressions (wrapped in filter)
    	QueryIterator result = new QueryIterLeftJoin(bindings.iterator(execCxt), optionals, null, execCxt);    	
        return new QueryIterBlocked(result, execCxt);
    }
    
    @Override
    protected void details(IndentedWriter out, SerializationContext sCxt)
    {
        out.println(Utils.className(this)) ;
        out.incIndent() ;
        right.output(out, sCxt) ;
        out.decIndent() ;
    }

}
