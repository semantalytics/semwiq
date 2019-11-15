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
import java.util.Set;

/**
 * @author dorgon, Andreas Langegger, al@jku.at
 *
 */
public class Misc {

	public static Set<Class<? extends Throwable>> getExceptionChain(Throwable t) {
		Set<Class<? extends Throwable>> chain = new HashSet<Class<? extends Throwable>>();
		chain.add(t.getClass());

		Throwable next = t;
		while ((next = next.getCause()) != null)
			chain.add(next.getClass());
		return chain;
	}
}
