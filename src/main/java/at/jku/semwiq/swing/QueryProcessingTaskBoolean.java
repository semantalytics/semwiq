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
package at.jku.semwiq.swing;

import java.util.concurrent.ExecutionException;

import com.hp.hpl.jena.query.Query;

/**
 * @author dorgon, Andreas Langegger, al@jku.at
 *
 */
public class QueryProcessingTaskBoolean extends QueryProcessingTask<Boolean, Void> {

	/**
	 * @param client
	 * @param q
	 */
	public QueryProcessingTaskBoolean(SwingApp client, Query q) {
		super(client, q);
	}

	/* (non-Javadoc)
	 * @see at.jku.semwiq.swing.QueryProcessingTask#queryInBackground()
	 */
	@Override
	protected Boolean queryInBackground() {
		return queryExec.execAsk();
	}
	
	@Override
	protected void queryDone() throws InterruptedException, ExecutionException {
		boolean answer = get();
		client.getTab().initResultTextArea("Answer: " + answer);
    	client.getProgressBar().setString("Answer: " + answer);
    }
}
