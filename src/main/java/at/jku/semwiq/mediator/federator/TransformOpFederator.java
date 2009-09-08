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
package at.jku.semwiq.mediator.federator;

import at.jku.semwiq.mediator.engine.op.OpFederate;

import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.TransformBase;
import com.hp.hpl.jena.sparql.algebra.TransformCopy;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP;
import com.hp.hpl.jena.sparql.algebra.op.OpFilter;

/**
 * @author dorgon, Andreas Langegger, al@jku.at
 *
 */
public class TransformOpFederator extends TransformCopy {
	private final FederatorBase federator;
	
	/**
	 * 
	 */
	public TransformOpFederator(FederatorBase federator) {
		this.federator = federator;
	}
	
	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.sparql.algebra.TransformBase#transform(com.hp.hpl.jena.sparql.algebra.op.OpFilter, com.hp.hpl.jena.sparql.algebra.Op)
	 */
	@Override
	public Op transform(OpFilter opFilter, Op subOp) {
		// add filter expressions to OpFederate, this is bottom up - OpFederate exists
		if (subOp instanceof OpFederate) {
			((OpFederate) subOp).setExprList(opFilter.getExprs());
			return subOp;
		} else
			return opFilter;
	}
	
	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.sparql.algebra.TransformBase#transform(com.hp.hpl.jena.sparql.algebra.op.OpBGP)
	 */
	@Override
	public Op transform(OpBGP opBGP) {
		return new OpFederate(opBGP, federator);
	}
}
