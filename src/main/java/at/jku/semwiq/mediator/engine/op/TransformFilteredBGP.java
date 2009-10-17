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
import com.hp.hpl.jena.sparql.algebra.TransformCopy;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP;
import com.hp.hpl.jena.sparql.algebra.op.OpFilter;

/**
 * @author dorgon, Andreas Langegger, al@jku.at
 *
 */
public class TransformFilteredBGP extends TransformCopy {

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.sparql.algebra.TransformCopy#transform(com.hp.hpl.jena.sparql.algebra.op.OpFilter, com.hp.hpl.jena.sparql.algebra.Op)
	 */
	@Override
	public Op transform(OpFilter opFilter, Op sub) {
		if (sub instanceof OpBGP) {
			Op op = new OpFilteredBGP(((OpBGP) sub).getPattern(), opFilter);
			return opFilter; // only add reference, still keep OpFilter in plan
		} else
			return super.transform(opFilter, sub);
	}
}
