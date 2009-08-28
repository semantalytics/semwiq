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
package at.jku.semwiq.mediator.util;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Stack;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.rdf.model.impl.StmtIteratorImpl;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.util.iterator.Filter;
import com.hp.hpl.jena.util.iterator.Map1;

/**
 * @author dorgon, Andreas Langegger, al@jku.at
 *
 */
public class StmtClosureIterator {

	private Statement next;
	private StmtIterator nextIter;
	private Stack<Resource> toExpand = new Stack<Resource>();
	private HashSet<Resource> done = new HashSet<Resource>();
	
	/**
	 * 
	 */
	public StmtClosureIterator(Resource r) {
		nextIter = r.listProperties();
		done.add(r); // add root to done
		setNext();
	}
	
	private void setNext() {
		if (nextIter.hasNext()) {
			next = nextIter.next();
			if (next.getObject().isResource()) {
				Resource r = next.getResource();
				if (!done.contains(r)) {
					toExpand.push(r); // add resource object to stack
					done.add(r);
				}
			}
			if (next.getPredicate().isResource()) {
				Resource r = next.getPredicate();
				if (!done.contains(r)) {
					toExpand.push(r); // add predicate to stack
					done.add(r);
				}
			}
		} else
			next = null;
	}
	
	public boolean hasNext() {
		return next != null;
	}
	
	public Statement next() {
		Statement s = next;
		
		// proceed
		if (nextIter.hasNext())
			setNext();
		// next stage
		else if (toExpand.size() > 0) {
			do {
				nextIter = toExpand.pop().listProperties();
			} while (!nextIter.hasNext() && toExpand.size() > 0); // proceed as long as next resource to expand has no statements
			setNext();
		} else
			next = null; // finish
		
		return s;
	}

}
