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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import at.jku.semwiq.mediator.Constants;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.core.Var;

/**
 * @author dorgon, Andreas Langegger, al@jku.at
 *
 */
public class QueryProcessingTaskResultSet extends QueryProcessingTask<Long, QuerySolution> {
	private Long estimatedResults;
	private long results = 0;

	private List<String> resultVars;
	private boolean resultInitialized = false;

	/**
	 * @param client
	 * @param q
	 */
	public QueryProcessingTaskResultSet(SwingApp client, Query q) {
		super(client, q);
	}

	@Override
	protected Long queryInBackground() {
		ResultSet resultSet = queryExec.execSelect();

		// fetch symbol values set by MediatorQueryEngine...
		estimatedResults = (Long) queryExec.getContext().get(Constants.ESTIMATED_AVG_RESULTS);
		resultVars = resultSet.getResultVars();		
		
		while (resultSet.hasNext() && !isCancelled()) {
//			if (results == 1)
//				timeFirst = System.currentTimeMillis() - timeStart;
			
			publish(resultSet.nextSolution());
			results++;
			
			if (estimatedResults != null && estimatedResults > 0) {
				int val = (int) (100 * (results / (float) estimatedResults));
				if (val >= 0 && val <= 100)
					setProgress(val);
				
			} else if (results == 1) // once, for first time only
				 client.getProgressBar().setIndeterminate(true);
			}

		return results;
	}

	@Override
	protected void process(List<QuerySolution> result) {
		if (!resultInitialized && resultVars != null) {
			client.getTab().initResultsTable(resultVars);
			resultInitialized = true;
		}
		
		for (QuerySolution s : result)
			client.getTab().appendResultRow(s, query.getPrefixMapping());
	}
	
    @Override
    public void queryDone() throws InterruptedException, ExecutionException {
    	Long total = get();
    	if (total == 0)
			client.getTab().initResultsTable(new ArrayList<Var>()); // in case of empty result
    	client.getProgressBar().setString(total + " results retrieved");
    }
}
