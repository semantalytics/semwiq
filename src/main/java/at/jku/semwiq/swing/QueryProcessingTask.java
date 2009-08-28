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

import java.awt.Toolkit;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingWorker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.jku.semwiq.mediator.Constants;
import at.jku.semwiq.mediator.Mediator;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.core.Var;

/**
 * returns number of results and intermediate QuerySolutions
 * 
 * @author dorgon
 */
public class QueryProcessingTask extends SwingWorker<Void, QuerySolution> {
	private String qryStr;
	private Query query;
	private QueryExecution queryExec;
	private ResultSet resultSet;
	private long estimatedResults;
	
	// different result types
	private boolean failed = false;
	
	private long nSelectResults; // SELECT -> will publish intermediate results
	private Model modelResult; // CONSTRUCT, DESCRIBE, EXPLAIN queries
	private boolean booleanResult; // ASK
	
	// only accessed by the event dispatch thread:
	private final SwingApp client;
	private List<String> resultVars;
	private boolean resultInitialized = false;
	
	private long timeStart;
	private long timeCompilation; // query compilation time
	private long timeFirstResult; // only for SELECT queries
	private long timeTotal;

	public QueryProcessingTask(SwingApp client, String qryStr) {
		this.client = client;
		this.qryStr = qryStr;
	}

	/* (non-Javadoc)
	 * @see javax.swing.SwingWorker#doInBackground()
	 */
	@Override
	protected Void doInBackground() {
		Logger log = LoggerFactory.getLogger(QueryProcessingTask.class);
		Mediator m = client.getMediator();

//		try {
//			query = m.createQuery(qryStr);
//			queryExec = m.createQueryExecution(query);
//
//			int qType = query.getQueryType();
//			timeStart = System.currentTimeMillis();
//			if (query.isExplainQuery())
//				modelResult = m.explainQuery(query);
//			
//			else switch (qType) {
//				case Query.QueryTypeSelect :
//					resultSet = queryExec.execSelect();
//					fetchSelectResults();
//					break;
//				case Query.QueryTypeAsk :
//					booleanResult = queryExec.execAsk();
//					break;
//				case Query.QueryTypeConstruct :
//					modelResult = queryExec.execConstruct();
//					break;
//				case Query.QueryTypeDescribe :
//					modelResult = queryExec.execDescribe();
//					break;
//			}
//
//		} catch (Throwable e) {
//			log.error("Failed executing query: " + query.toString(), e); //TODO does not log?!?!?
//			failed = true;
//		
//		} finally {
//			timeCompilation = (Long) queryExec.getContext().get(Constants.OPTIMIZATION_TIME);
//			timeTotal = System.currentTimeMillis() - timeStart;
//			if (queryExec != null)
//				queryExec.close();
//			if (resultSet != null)
//				resultSet = null;
//		}
//		
		return null;
	}

	private void fetchSelectResults() {
		// fetch symbol values set by MediatorQueryEngine...
		estimatedResults = (Long) queryExec.getContext().get(Constants.ESTIMATED_AVG_RESULTS);
		resultVars = resultSet.getResultVars();
		
		nSelectResults = 0;
		while (resultSet.hasNext() && !isCancelled()) {
			if (nSelectResults == 1)
				timeFirstResult = System.currentTimeMillis() - timeStart;
			
			publish(resultSet.nextSolution());
			nSelectResults++;
			if (estimatedResults > 0) {
				int val = (int) (100 * (nSelectResults / (float) estimatedResults));
				if (val >= 0 && val <= 100)
					setProgress(val);
			} else
				client.getProgressBar().setIndeterminate(true);
		}
	}

	/**
	 * @return the compileTime
	 */
	public long getTimeCompilation() {
		return timeCompilation;
	}
	
	/**
	 * @return the timeFirstResult
	 */
	public long getTimeFirstResult() {
		return timeFirstResult;
	}
	
	/**
	 * @return the timeTotal
	 */
	public long getTimeTotal() {
		return timeTotal;
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.SwingWorker#process(java.util.List)
	 */
	@Override
	protected void process(List<QuerySolution> result) {
		if (!resultInitialized && resultVars != null) {
			client.getTab().initResultsTable(resultVars);
			resultInitialized = true;
		}
		
		for (QuerySolution s : result)
			client.getTab().appendResultRow(s, query.getPrefixMapping());
	}
	
    /*
     * Executed in event dispatching thread
     */
    @Override
    public void done() {
    	Logger log = LoggerFactory.getLogger(QueryProcessingTask.class);
        Toolkit.getDefaultToolkit().beep();
        client.getToolBar().getButton(ClientToolBar.Action.RUN).setEnabled(true);
        client.setCursor(null); //turn off the wait cursor
        client.getProgressBar().setIndeterminate(false);

        if (query == null) // an exception occurred 
        	return;
        
        if (query.isExplainQuery()) {
        	if (failed) {
        		client.getProgressBar().setString("Failed");
        	} else if (isCancelled()) {
        		client.getProgressBar().setString("Canceled");
        	} else
        		showModelResult();
        
        } else switch (query.getQueryType()) {
        	case Query.QueryTypeSelect :
        		if (failed) {
        			client.getProgressBar().setString("Failed" + ((nSelectResults > 0) ? " (" + nSelectResults + " results retrieved)" : ""));
        		} else if (isCancelled()) {
                	client.getProgressBar().setString("Canceled" + ((nSelectResults > 0) ? " (" + nSelectResults + " results retrieved)" : ""));
                } else {
                	if (nSelectResults == 0)
            			client.getTab().initResultsTable(new ArrayList<Var>());
                	client.getProgressBar().setString(nSelectResults + " results retrieved");
                	client.getProgressBar().setValue(100);
                }
        		log.info(nSelectResults + " results");
        		break;
        	case Query.QueryTypeAsk :
        		if (failed) {
        			client.getProgressBar().setString("Failed");
        		} else if (isCancelled()) {
                	client.getProgressBar().setString("Canceled");
                } else {
            		client.getTab().initResultTextArea(booleanResult ? "true" : "false");
                	client.getProgressBar().setString("Done: " + booleanResult);
                	client.getProgressBar().setValue(100);
                }
        		break;
        	case Query.QueryTypeDescribe :
        		if (failed) {
        			client.getProgressBar().setString("Failed");
        		} else if (isCancelled()) {
                	client.getProgressBar().setString("Canceled");
                } else
            		showModelResult();
        		break;
        	case Query.QueryTypeConstruct :
        		if (failed) {
        			client.getProgressBar().setString("Failed");
        		} else if (isCancelled()) {
                	client.getProgressBar().setString("Canceled");
                } else
                	showModelResult();
        		break;
        }
        
        try {
    		if (!isCancelled() && log.isInfoEnabled()) {
    			log.info("Federation & Optimization: " + timeCompilation + " ms (may be much slower when loggers are set to debug levels!)");
    			log.info("Response time: " + timeFirstResult + " ms until first result");
    			log.info("Total time: " + timeTotal + " ms");
    		}
		} catch (Exception ignore) {
		}
    }
    
    private void showModelResult() {
    	StringWriter w = new StringWriter();
		modelResult.write(w);
		w.flush();
		client.getTab().initResultTextArea(w.getBuffer().toString());
    	client.getProgressBar().setString("Done (" + modelResult.size() + " triples)");
    	client.getProgressBar().setValue(100);
    }
}