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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.jku.semwiq.mediator.federator.FederatorBase;
import at.jku.semwiq.mediator.federator.FederatorException;

import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP;
import com.hp.hpl.jena.sparql.algebra.op.OpExt;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.serializer.SerializationContext;
import com.hp.hpl.jena.sparql.util.IndentedWriter;
import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap;

/**
 * @author dorgon, Andreas Langegger, al@jku.at
 *
 */
public class OpFederate extends OpExt {
//	private static final Logger log = LoggerFactory.getLogger(OpFederate.class);
	
	private static String fedLabel = "federate";
	private final FederatorBase federator;
	private final OpBGP bgp;
	private ExprList exprs;
	
	public OpFederate(OpBGP bgp, FederatorBase federator) {
		super(fedLabel);
		this.bgp = bgp;
		this.federator = federator;
		exprs = null;
	}
	
	/**
	 * @param federator 
	 * @param name
	 */
	public OpFederate(OpBGP bgp, ExprList exprs, FederatorBase federator) {
		super(fedLabel);
		this.bgp = bgp;
		this.exprs = exprs;
		this.federator = federator;
	}
	
	/**
	 * @return the federator
	 */
	public FederatorBase getFederator() {
		return federator;
	}
	
	/**
	 * @param exprs
	 */
	public void setExprList(ExprList exprs) {
		this.exprs = exprs;
	}

	/**
	 * @return the tp
	 */
	public BasicPattern getPattern() {
		return bgp.getPattern();
	}
	
	/**
	 * @return the exprs
	 */
	public ExprList getExprList() {
		return exprs;
	}
	
	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.sparql.algebra.op.OpExt#effectiveOp()
	 */
	@Override
	public Op effectiveOp() {
		return bgp;
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.sparql.algebra.op.OpExt#eval(com.hp.hpl.jena.sparql.engine.QueryIterator, com.hp.hpl.jena.sparql.engine.ExecutionContext)
	 */
	@Override
	public QueryIterator eval(QueryIterator input, ExecutionContext execCxt) {
		try {
			return federator.federate(input, this, execCxt);
		} catch (FederatorException e) {
			throw new RuntimeException("Failed to federate " + this + ".");
		}
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.sparql.algebra.op.OpExt#outputArgs(com.hp.hpl.jena.sparql.util.IndentedWriter, com.hp.hpl.jena.sparql.serializer.SerializationContext)
	 */
	@Override
	public void outputArgs(IndentedWriter out, SerializationContext cxt) {
		out.ensureStartOfLine();
		if (exprs != null)
			out.print(exprs);
		out.print(bgp.toString(cxt.getPrefixMapping()));
	}
	
	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.sparql.algebra.op.OpBase#hashCode()
	 */
	@Override
	public int hashCode() {
		return fedLabel.hashCode() ^ bgp.hashCode() ^ exprs.hashCode();
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.sparql.algebra.op.OpBase#equalTo(com.hp.hpl.jena.sparql.algebra.Op, com.hp.hpl.jena.sparql.util.NodeIsomorphismMap)
	 */
	@Override
	public boolean equalTo(Op op2, NodeIsomorphismMap labelMap) {
		if ( ! ( op2 instanceof OpFederate) ) return false ;
        return bgp.equals(((OpFederate) op2).bgp) && exprs.equals(((OpFederate) op2).exprs); 
	}

}
