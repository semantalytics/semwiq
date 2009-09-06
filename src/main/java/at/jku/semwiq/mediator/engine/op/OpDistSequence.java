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

import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.op.OpExt;
import com.hp.hpl.jena.sparql.algebra.op.OpService;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.serializer.SerializationContext;
import com.hp.hpl.jena.sparql.util.IndentedWriter;
import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap;

/**
 * @author dorgon, Andreas Langegger, al@jku.at
 *
 */
public class OpDistSequence extends OpExt {
	public static final String distSeqTag = "distSequence";
	private Op orig;
	private OpService left, right;
	
	public static Op create(Op orig, OpService left, OpService right) {
        // Avoid stages of nothing
        if ( left == null && right == null )
            return null ;
        // Avoid stages of one.
        if ( left == null )
            return right ;
        if ( right == null )
            return left ;
		return new OpDistSequence(orig, left, right);
	}
	
	/**
	 * 
	 */
	private OpDistSequence(Op orig, OpService left, OpService right) {
		super(distSeqTag);
		this.left = left;
		this.right = right;
	}
	
	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.sparql.algebra.op.OpExt#effectiveOp()
	 */
	@Override
	public Op effectiveOp() {
		return orig;
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.sparql.algebra.op.OpExt#eval(com.hp.hpl.jena.sparql.engine.QueryIterator, com.hp.hpl.jena.sparql.engine.ExecutionContext)
	 */
	@Override
	public QueryIterator eval(QueryIterator input, ExecutionContext execCxt) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.sparql.algebra.op.OpExt#outputArgs(com.hp.hpl.jena.sparql.util.IndentedWriter, com.hp.hpl.jena.sparql.serializer.SerializationContext)
	 */
	@Override
	public void outputArgs(IndentedWriter out, SerializationContext cxt) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.sparql.algebra.op.OpBase#equalTo(com.hp.hpl.jena.sparql.algebra.Op, com.hp.hpl.jena.sparql.util.NodeIsomorphismMap)
	 */
	@Override
	public boolean equalTo(Op other, NodeIsomorphismMap labelMap) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.sparql.algebra.op.OpBase#hashCode()
	 */
	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return 0;
	}

}
