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
package at.jku.semwiq.mediator.registry.monitor;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;

/**
 * @author dorgon
 *
 */
public class Scheduler extends ScheduledThreadPoolExecutor {

	/**
	 * @param corePoolSize
	 * @param handler
	 */
	public Scheduler(int corePoolSize,
			RejectedExecutionHandler handler) {
		super(corePoolSize, handler);
	}

	/**
	 * @param corePoolSize
	 * @param threadFactory
	 * @param handler
	 */
	public Scheduler(int corePoolSize, ThreadFactory threadFactory,
			RejectedExecutionHandler handler) {
		super(corePoolSize, threadFactory, handler);
	}

	/**
	 * @param corePoolSize
	 * @param threadFactory
	 */
	public Scheduler(int corePoolSize, ThreadFactory threadFactory) {
		super(corePoolSize, threadFactory);
	}

	/**
	 * @param corePoolSize
	 */
	public Scheduler(int corePoolSize) {
		super(corePoolSize);
	}

	/**
	 * @return number of currently active workers
	 */
	public int getActiveCount() {
		return super.getActiveCount();
	}

}
