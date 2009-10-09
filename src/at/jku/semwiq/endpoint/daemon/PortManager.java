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
package at.jku.semwiq.endpoint.daemon;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * @author dorgon
 *
 */
class PortManager {
	private final int startPort;
	private int nextPort;
	private Deque<Integer> deque = new ArrayDeque<Integer>();

	/**
	 * constructor
	 */
	public PortManager(int startPort) {
		this.startPort = startPort;
		this.nextPort = startPort;
	}
	
	public synchronized int getFreePort() {
		if (deque.isEmpty())
			return nextPort++;
		else
			return deque.pop();
	}
	
	public synchronized void freePort(int port) {
		deque.push(port);
	}
	
	public int getStartPort() {
		return startPort;
	}
}
