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

import java.io.StringWriter;
import java.util.concurrent.ExecutionException;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.rdf.model.Model;

/**
 * @author dorgon, Andreas Langegger, al@jku.at
 *
 */
public class QueryProcessingTaskModel extends QueryProcessingTask<Model, Void> {

    /**
	 * @param client
	 * @param q
	 */
	public QueryProcessingTaskModel(SwingApp client, Query q) {
		super(client, q);
	}

	/* (non-Javadoc)
	 * @see at.jku.semwiq.swing.QueryProcessingTask#queryInBackground()
	 */
	@Override
	protected Model queryInBackground() {
		if (query.isExplainQuery())
			return queryExec.execExplain();
		else if (query.isConstructType())
			return queryExec.execConstruct();
		else if (query.isDescribeType())
			return queryExec.execDescribe();
		else return null; // cannot occur
	}
	
	@Override
	protected void queryDone() throws InterruptedException, ExecutionException {
		Model modelResult = get();
		StringWriter w = new StringWriter();
		modelResult.write(w, "N3");
		w.flush();
		
		client.getTab().initResultTextArea(w.getBuffer().toString());
    	client.getProgressBar().setString("Done (" + modelResult.size() + " triples)");
    }
}
