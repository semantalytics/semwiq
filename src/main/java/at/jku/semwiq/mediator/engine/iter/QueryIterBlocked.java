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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.jku.semwiq.mediator.Constants;

import com.hp.hpl.jena.sparql.algebra.table.TableN;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIter;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIter1;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIteratorBase;
import com.hp.hpl.jena.sparql.serializer.SerializationContext;
import com.hp.hpl.jena.sparql.util.IndentedWriter;

/**
 * @author dorgon, Andreas Langegger, al@jku.at
 *
 * wraps an iterator and provides blocked access to input iterator
 */
public class QueryIterBlocked extends QueryIter1 {
	private static final Logger log = LoggerFactory.getLogger(QueryIterBlocked.class);
	
//	public static QueryIterBlocked create(QueryIterator input, ExecutionContext execCxt) {
//		if (input instanceof QueryIterBlocked)
//			return (QueryIterBlocked) input;
//		else
//			return new QueryIterBlocked(input, execCxt);
//	}

	/**
	 * @param input - if not QueryIterBlocked, will wrap it and make blocked
	 * @param execCxt
	 */
	public QueryIterBlocked(QueryIterator input, ExecutionContext execCxt) {
		super(input, execCxt);
	}

	@Override
	protected boolean hasNextBinding() {
		return getInput().hasNext();
	}

	@Override
	protected Binding moveToNextBinding() {
		return getInput().nextBinding();
	}

	@Override
	protected void closeSubIterator() {
		getInput().close();
	}

	/**
	 * has more binding blocks?
	 * @return
	 */
	protected boolean hasNextBindingsBlock() {
		return hasNextBinding();
	}
	
	/**
	 * get a block of next bindings
	 * @return
	 */
	protected TableN nextBindingsBlock() {
		TableN nextBindings = new TableN();
		QueryIterator input = getInput();
		for (int i=0; i<Constants.BLOCK_SIZE && input.hasNext(); i++) { // will have at least one in final stage cause input has next
			Binding b = input.next();
			nextBindings.addBinding(b);
		}
		return nextBindings;
	}
}
