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

/**
 * @author dorgon
 *
 */
public class DataSourceMonitorException extends Exception {
	private static final long serialVersionUID = 2567951124929318774L;

	/**
	 * 
	 */
	public DataSourceMonitorException() {
	}

	/**
	 * @param message
	 */
	public DataSourceMonitorException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public DataSourceMonitorException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public DataSourceMonitorException(String message, Throwable cause) {
		super(message, cause);
	}

}
