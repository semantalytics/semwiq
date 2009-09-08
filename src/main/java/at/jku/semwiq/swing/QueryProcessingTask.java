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
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.jku.semwiq.mediator.Constants;
import at.jku.semwiq.mediator.Mediator;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.sparql.util.Context;

/**
 * returns number of results and intermediate QuerySolutions
 * 
 * @author dorgon
 */
public abstract class QueryProcessingTask<T, V> extends SwingWorker<T, V> {
	protected final SwingApp client;
	
	protected final Query query;
	protected QueryExecution queryExec;

	public QueryProcessingTask(SwingApp client, Query q) {
		this.client = client;
		this.query = q;
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.SwingWorker#doInBackground()
	 */
	@Override
	protected final T doInBackground() throws Exception {
		Mediator m = client.getMediator();
		queryExec = m.createQueryExecution(query);
        client.getProgressBar().setIndeterminate(true);
		T result = queryInBackground();
		queryExec.close();
		return result;
	}

	/* (non-Javadoc)
	 * @see javax.swing.SwingWorker#done()
	 */
	@Override
	protected final void done() {
//        Toolkit.getDefaultToolkit().beep();
        client.getToolBar().getButton(ClientToolBar.Action.RUN).setEnabled(true);
        client.setCursor(null); //turn off the wait cursor
        client.getProgressBar().setIndeterminate(false);

        if (isCancelled())
        	client.getProgressBar().setString("Canceled");
        else {
        	try {
        		queryDone();
        		Logger log = LoggerFactory.getLogger(QueryProcessingTask.class);
        		if (log.isInfoEnabled()) {
        			Context ctx = queryExec.getContext();
        			log.info("Total time: " + ctx.get(Constants.EXEC_TIME_ALLRESULTS) +
        					" ms, first result: " + ctx.get(Constants.EXEC_TIME_FIRSTRESULT) +
        					" ms, optimize: " + ctx.get(Constants.EXEC_TIME_OPTIMIZE));
        		}
            } catch (Exception e) {
            	client.getProgressBar().setString("Failed");
            	throw new RuntimeException("Failed to execute query: " + e.getMessage(), e);
            }
        }
	}
	
	protected abstract T queryInBackground();
	
	protected abstract void queryDone() throws InterruptedException, ExecutionException;
}