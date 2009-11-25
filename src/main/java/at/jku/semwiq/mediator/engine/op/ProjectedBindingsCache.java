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
package at.jku.semwiq.mediator.engine.op;

import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.ARQInternalErrorException;
import com.hp.hpl.jena.sparql.algebra.table.TableN;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterConvert;
import com.hp.hpl.jena.sparql.util.ALog;

/**
 * @author dorgon, Andreas Langegger, al@jku.at
 *
 * Used for row-blocking based execution of distributed semi-join.
 * Projects bindings from a TableN, caches them, and merges them again to the obtained results.
 */
public class ProjectedBindingsCache {
	private final Collection<Var> origBindingVars;
	private final Collection<Var> joinVars;
	private final TableN projected;
	private final Hashtable<Integer, Binding> projToOrig = new Hashtable<Integer, Binding>();

	public ProjectedBindingsCache(TableN table, Set<Var> scopedVars) {
		ResultSet r = table.toResultSet();
		List<String> vars = r.getResultVars();
		origBindingVars = new HashSet<Var>();
		for (String v : vars)
			origBindingVars.add(Var.alloc(v));
		
		joinVars = new HashSet<Var>();
		joinVars.addAll(origBindingVars);
		joinVars.retainAll(scopedVars);

		if (joinVars.size() == 0)
			projected = null;
		else {
    		TableN result = new TableN();
    		while (r.hasNext()) {
    			Binding orig = r.nextBinding();
    			Binding proj = project(orig, joinVars);
    			if (proj.size() > 0) { // add empty binding only if orig was empty
    				result.addBinding(proj);
    				projToOrig.put(proj.hashCode(), orig);
    			}
    		}
    		projected = result;
		}
	}
	
	public Binding project(Binding b, Collection<Var> vars) {
		Binding proj = new BindingMap();
		for (Var var : vars)
			if (vars.contains(var) && b.contains(var))
				proj.add(var, b.get(var));
		return proj;
	}
	
	/**
	 * @return the reduced
	 */
	public TableN getProjectedBindingsTable() {
		return projected;
	}
	
	public void addOriginalBindings(Binding other) {
		Binding orig = projToOrig.get(project(other, joinVars).hashCode());
		if (orig == null)
			return;
		
		for (Var var : origBindingVars) {
			if (!joinVars.contains(var)) { // for all unscoped vars
                if (other.contains(var)) {
                    Node nOrig = orig.get(var);
                	Node nOther = other.get(var);
                    if (nOrig.equals(nOther) )
                        ALog.warn(this, "Binding already for "+ var +" (same value)" ) ;
                    else {
                        ALog.fatal(this, "Binding already for "+ var +" (different values)" ) ;
                        throw new ARQInternalErrorException("QueryIteratorResultSet: Incompatible bindings for "+var) ;
                    }
                } else
                	other.add(var, orig.get(var));
			}
		}
	}
	
	public int size() {
		return projToOrig.size();
	}

	/**
	 * @param iter
	 * @param execContext
	 * @return
	 */
	public QueryIterator addCachedBindings(QueryIterator iter, ExecutionContext execContext) {
        return new QueryIterConvert(iter, new ConverterExtendBlock(this), execContext);
	}
	
    // Extend (with checking) an iterator stream of binding to have a common parent. 
    class ConverterExtendBlock implements QueryIterConvert.Converter {
        private ProjectedBindingsCache parentBindings ;
        
        ConverterExtendBlock(ProjectedBindingsCache parent) { parentBindings = parent ; }
        
        public Binding convert(Binding b) {
            if ( parentBindings == null || parentBindings.size() == 0 )
                return b ;  
            
            parentBindings.addOriginalBindings(b);
            return b;
        }
    }

}
