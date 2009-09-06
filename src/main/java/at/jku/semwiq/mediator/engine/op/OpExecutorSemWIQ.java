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

import com.hp.hpl.jena.sparql.algebra.op.OpExt;
import com.hp.hpl.jena.sparql.algebra.op.OpService;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.main.OpExecutor;
import com.hp.hpl.jena.sparql.engine.main.iterator.QueryIterService;

/**
 * @author dorgon, Andreas Langegger, al@jku.at
 *
 */
public class OpExecutorSemWIQ extends OpExecutor {

	public OpExecutorSemWIQ(ExecutionContext cxt) {
		super(cxt);
	}
	
	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.sparql.engine.main.OpExecutor#execute(com.hp.hpl.jena.sparql.algebra.op.OpExt, com.hp.hpl.jena.sparql.engine.QueryIterator)
	 */
	@Override
	protected QueryIterator execute(OpExt opExt, QueryIterator input) {
		return super.execute(opExt, input);
	}
	
	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.sparql.engine.main.OpExecutor#execute(com.hp.hpl.jena.sparql.algebra.op.OpService, com.hp.hpl.jena.sparql.engine.QueryIterator)
	 */
	@Override
	protected QueryIterator execute(OpService opService, QueryIterator input) {
		 return new QueryIterBlockedService(input, opService, execCxt) ;
	}
}
