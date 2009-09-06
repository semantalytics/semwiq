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

import java.util.NoSuchElementException;

import com.hp.hpl.jena.sparql.algebra.table.TableN;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIter1;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterRepeatApply;
import com.hp.hpl.jena.sparql.util.ALog;
import com.hp.hpl.jena.sparql.util.Utils;

/**
 * @author dorgon, Andreas Langegger, al@jku.at
 * 
 */
public abstract class QueryIterBlockedRepeatApply extends QueryIter1 {
	private QueryIterator currentStage;
	public static final int BLOCK_SIZE = 50; // bindings

	public QueryIterBlockedRepeatApply(QueryIterator input, ExecutionContext context) {
		super(input, context);
		this.currentStage = null;

		if (input == null) {
			ALog
					.fatal(this,
							"[QueryIterRepeatApply] Repeated application to null input iterator");
			return;
		}
	}

	protected QueryIterator getCurrentStage() {
		return currentStage;
	}

	protected abstract QueryIterator nextStage(TableN bindings);

	@Override
	protected boolean hasNextBinding() {
		if (isFinished())
			return false;

		for (;;) {
			if (currentStage == null)
				currentStage = makeNextStage();

			if (currentStage == null)
				return false;

			if (currentStage.hasNext())
				return true;

			// finish this step
			currentStage.close();
			currentStage = null;
			// loop
		}
		// Unreachable
	}

	@Override
	protected Binding moveToNextBinding() {
		if (!hasNextBinding())
			throw new NoSuchElementException(Utils.className(this)
					+ ".next()/finished");
		return currentStage.nextBinding();

	}

	private QueryIterator makeNextStage() {

		if (getInput() == null)
			return null;

		if (!getInput().hasNext()) {
			getInput().close();
			return null;
		}

		TableN nextBindings = new TableN();
		QueryIterator input = getInput();
		for (int i=0; i<BLOCK_SIZE && input.hasNext(); i++) // will have at least one in final stage
			nextBindings.addBinding(input.next());

		QueryIterator iter = nextStage(nextBindings);
		return iter;
	}

	@Override
	protected void closeSubIterator() {
		if (currentStage != null)
			currentStage.close();
	}

}
