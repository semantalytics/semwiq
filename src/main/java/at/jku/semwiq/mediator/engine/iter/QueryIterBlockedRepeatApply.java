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

import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.sparql.algebra.table.TableN;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.util.ALog;
import com.hp.hpl.jena.sparql.util.Utils;

/**
 * @author dorgon, Andreas Langegger, al@jku.at
 * 
 * stage with bindings block
 * initialized with a normal or a blocked iterator with TableN bindings
 */
public abstract class QueryIterBlockedRepeatApply extends QueryIterBlocked {
	private static final Logger log = LoggerFactory.getLogger(QueryIterBlockedRepeatApply.class);
	private QueryIterBlocked currentStage;

	/**
	 * @param input
	 * @param context
	 */
	public QueryIterBlockedRepeatApply(QueryIterBlocked input, ExecutionContext context) {
		super(input, context);
		this.currentStage = null;

		if (input == null) {
			ALog.fatal(this, "[QueryIterBlockedRepeatApply] Repeated application to null input iterator");
			return;
		}
	}

	@Override
	protected QueryIterBlocked getInput() {
		return (QueryIterBlocked) super.getInput();
	}
	
	protected QueryIterBlocked getCurrentStage() {
		return currentStage;
	}

	/**
	 * @param bindings
	 * @return
	 */
	protected abstract QueryIterBlocked nextStage(TableN bindings);

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
			throw new NoSuchElementException(Utils.className(this) + ".next()/finished");
		return currentStage.nextBinding();
	}

	protected TableN moveToNextBindingsBlock() {
		if (!hasNextBindingsBlock())
			throw new NoSuchElementException(Utils.className(this) + ".next()/finished");
		return currentStage.nextBindingsBlock();
	}
	
	private QueryIterBlocked makeNextStage() {
		QueryIterBlocked input = getInput();
		if (input == null)
			return null;
		
		if (!input.hasNextBindingsBlock()) {
			input.close();
			return null;
		}
		
		TableN bindings = ((QueryIterBlocked) input).nextBindingsBlock();
		QueryIterBlocked iter = nextStage(bindings);
		return iter;
	}

	@Override
	protected void closeSubIterator() {
		if (currentStage != null)
			currentStage.close();
	}

}
