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

import java.util.concurrent.LinkedBlockingQueue;

import ch.qos.logback.classic.spi.LoggingEvent;

/**
 * @author thomas
 *
 */
public class LogBuffer {
	/** keep reference for deregistration */
	private final LogBufferDispatcher dispatcher;
	
	/** buffer for this instance */
	private final LinkedBlockingQueue<LoggingEvent> buffer;
	
	/** flag for "closed" */
	private boolean closed = false;
	
	protected LogBuffer(LogBufferDispatcher dispatcher) {
		this.dispatcher = dispatcher;
		this.dispatcher.register(this);
		this.buffer = new java.util.concurrent.LinkedBlockingQueue<LoggingEvent>();
	}

	public void receiveEvent(LoggingEvent e) {
		buffer.offer(e);
	}
	
	public LoggingEvent nextEvent() throws InterruptedException {
		return buffer.take();
	}

	public boolean isActive() {
		return !closed;
	}

	public void close() {
		dispatcher.unregister(this);
		closed = false;
	}
}
