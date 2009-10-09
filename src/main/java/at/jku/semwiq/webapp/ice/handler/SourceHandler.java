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
package at.jku.semwiq.webapp.ice.handler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.ArrayDataModel;
import javax.faces.model.DataModel;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.jku.semwiq.mediator.Constants;
import at.jku.semwiq.mediator.Mediator;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.resultset.ResultSetFormat;
import com.icesoft.faces.async.render.RenderManager;
import com.icesoft.faces.async.render.Renderable;
import com.icesoft.faces.context.ByteArrayResource;
import com.icesoft.faces.webapp.xmlhttp.PersistentFacesState;
import com.icesoft.faces.webapp.xmlhttp.RenderingException;

import edu.emory.mathcs.backport.java.util.concurrent.LinkedBlockingQueue;
import edu.emory.mathcs.backport.java.util.concurrent.ThreadPoolExecutor;
import edu.emory.mathcs.backport.java.util.concurrent.TimeUnit;

//import at.faw.semwiq.mediator.Mediator;

/**
 * @author thomas
 *
 */
public class SourceHandler implements Renderable {
	
	private static final Logger log = LoggerFactory.getLogger(SourceHandler.class);
	
	// variable declarations with JSF bindings
	private String semwiq_sourceMediatorEndpoint;
	private String semwiq_sourceVersion;
	private String semwiq_sourceMonitor;
	private String semwiq_sourceOptimizer;
	
	private String sparqlQuery;
	private String resultFormat;
	private String explainOutput;
	private boolean showExplainOutput = true;
	private int percentComplete;
	
	private FacesContext context;
	private ExternalContext ec;
	private HttpServletRequest request;
	private Map am;
	
	private RenderManager renderManager;
	
	
	private String sessionId;
	private PersistentFacesState persistentFacesState;
	protected static ThreadPoolExecutor longRunningTaskThreadPool = new ThreadPoolExecutor(5, 15, 30, TimeUnit.SECONDS, new LinkedBlockingQueue(20));
	private QueryExecution queryEx;	
	private ResultSet result;
	private boolean progressBarType = false;
	
	//methods for other Server Faces handlers that are used in this one
	private SemWIQHandler semwiqHandler;
	public void setSemwiqHandler(SemWIQHandler semwiqHandler) {
		this.semwiqHandler = semwiqHandler;
	}
	
	public void setRenderManager(RenderManager renderManager) {
        this.renderManager = renderManager;
        // Casting to HttpSession ruins it for portlets, in this case we only
        // need a unique reference so we use the object hash
        sessionId = FacesContext.getCurrentInstance().getExternalContext().getSession(false).toString();
        renderManager.getOnDemandRenderer(sessionId).add(this);
    }
		
	
	public SourceHandler() {
		StringBuffer sampleQuery = new StringBuffer();
		sampleQuery.append(semwiqHandler.getMediator().getConfig().getGuiConfig().getPrefixMappingString());
		sampleQuery.append("\n\nSELECT * WHERE {\n\n}");
		this.setSparqlQuery(sampleQuery.toString());
		this.setResultFormat("1");
		persistentFacesState = PersistentFacesState.getInstance();
	}
	
	
	// getters and setters
	public String getSemwiq_sourceMediatorEndpoint() {
		FacesContext context = FacesContext.getCurrentInstance();
 		ExternalContext ec = context.getExternalContext();
 		HttpServletRequest request = (HttpServletRequest)ec.getRequest();
 		String serverURI = request.getServerName();
 		int serverPort = request.getServerPort();
 		String path = request.getContextPath();
		this.semwiq_sourceMediatorEndpoint = serverURI+":"+serverPort+path;
		return semwiq_sourceMediatorEndpoint;
	}

	public void setSemwiq_sourceMediatorEndpoint(String semwiq_sourceMediatorEndpoint) {
		this.semwiq_sourceMediatorEndpoint = semwiq_sourceMediatorEndpoint;
	}

	public String getSemwiq_sourceVersion() {
//		 System.out.println(semwiqHandler.getTest());  			// only for testing purposes 
		semwiq_sourceVersion = Constants.VERSION_STRING;
 		return semwiq_sourceVersion;
	}

	public void setSemwiq_sourceVersion(String semwiq_sourceVersion) {
		this.semwiq_sourceVersion = semwiq_sourceVersion;
	}

	public String getSemwiq_sourceMonitor() {
		return semwiq_sourceMonitor;
	}

	public void setSemwiq_sourceMonitor(String semwiq_sourceMonitor) {
		this.semwiq_sourceMonitor = semwiq_sourceMonitor;
	}

	public String getSparqlQuery() {
		return sparqlQuery;
	}

	public void setSparqlQuery(String sparqlQuery) {
		this.sparqlQuery = sparqlQuery;
	}
	
	public String getResultFormat() {
		return resultFormat;
	}

	public void setResultFormat(String resultFormat) {
		this.resultFormat = resultFormat;
	}
	
	public String getExplainOutput() {
		return explainOutput;
	}

	public void setExplainOutput(String explainOutput) {
		this.explainOutput = explainOutput;
	}
	
	public boolean isShowExplainOutput() {
		return showExplainOutput;
	}

	public void setShowExplainOutput(boolean showExplainOutput) {
		this.showExplainOutput = showExplainOutput;
	}
	
	public int getPercentComplete() {
		return percentComplete;
	}

	public void setPercentComplete(int percentComplete) {
		this.percentComplete = percentComplete;
	}

	public boolean isProgressBarType() {
		return progressBarType;
	}

	public void setProgressBarType(boolean progressBarType) {
		this.progressBarType = progressBarType;
	}
	
	
	public com.icesoft.faces.context.Resource getDownload() {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		Mediator mediator = semwiqHandler.getMediator();
		if(mediator!=null && mediator.isReady()) {
			String queryString = this.getSparqlQuery();
			if (queryString==null || queryString.equalsIgnoreCase("")) {
				log.error("No query string specified.");
			}
			else {
				Query query = QueryFactory.create(queryString, Syntax.syntaxARQ);
				queryEx = mediator.createQueryExecution(query);
				result = null;
				
				int queryType = query.getQueryType();
				StringWriter outputString = new StringWriter();
				
				switch (queryType) {
					case Query.QueryTypeSelect :
						if (resultFormat!=null && !resultFormat.equalsIgnoreCase("")) {
						int format = Integer.valueOf(resultFormat).intValue();
						result = queryEx.execSelect();
							this.setShowExplainOutput(true);
							
							switch (format) {
								case 1:
									generateContent(query, result);
//									generateSampleContent();
									streamOutContent(outStream);
									break;
								case 2:
									ResultSetFormatter.output(outStream, result, ResultSetFormat.syntaxRDF_XML);
									break;
								case 3:
									ResultSetFormatter.output(outStream, result, ResultSetFormat.syntaxXML);
									break;
								case 4:
									ResultSetFormatter.output(outStream, result, ResultSetFormat.syntaxJSON);
									break;
								case 5:
									StringBuffer csvBuffer = new StringBuffer();
									List<String> resultVars = result.getResultVars();
									for (String var : resultVars)
										csvBuffer.append(var + ";");
									csvBuffer.append("\n");
									
									QuerySolution querySol;
									while(result.hasNext()) {
										querySol = result.nextSolution();
										for (String var : resultVars) {
											csvBuffer.append("\"");
											csvBuffer.append(querySol.get(var).toString());
											csvBuffer.append("\";");
										}
										csvBuffer.append("\n");
										try {
											outStream.write(csvBuffer.toString().getBytes());
										} catch (IOException e) {
											log.error(e.getMessage());
										}
									}
//									outStream.write(csvBuffer.toString());
									break;
							}
					}
					break;
						
					case Query.QueryTypeAsk :
						break;
					case Query.QueryTypeConstruct :
						Model constructResult = queryEx.execConstruct();
						constructResult.write(outStream);
						constructResult.close();
						break;
					case Query.QueryTypeDescribe :
						Model describeResult = queryEx.execDescribe();
						describeResult.write(outStream);
						describeResult.close();
						break;
				}
			}
		}
		else {
			semwiqHandler.setErrorMessage("Mediator is currently offline or not available.");
		}
		
		return new ByteArrayResource(outStream.toByteArray());
	}


	public String execute() {
		Mediator mediator = semwiqHandler.getMediator();
		if(mediator!=null && mediator.isReady()) {
			String queryString = this.getSparqlQuery();
			if (queryString==null || queryString.equalsIgnoreCase("")) {
				log.error("No query string specified.");
			}
			else {
				Query query = QueryFactory.create(queryString, Syntax.syntaxARQ);
				queryEx = mediator.createQueryExecution(query);
				result = null;
				FacesContext context = FacesContext.getCurrentInstance();
		 		ExternalContext ec = context.getExternalContext();
		 		HttpServletRequest request = (HttpServletRequest)ec.getRequest();
				int queryType = query.getQueryType();
				StringWriter outputString = new StringWriter();
				
				switch (queryType) {
					case Query.QueryTypeSelect :
						if (resultFormat!=null && !resultFormat.equalsIgnoreCase("")) {
						int format = Integer.valueOf(resultFormat).intValue();
						result = queryEx.execSelect();
						
//							try {
							OutputStream outStream = new ByteArrayOutputStream();
							this.setShowExplainOutput(true);
							
							switch (format) {
								case 1:
									System.out.println(semwiqHandler.getMediatorConfiguration().getConfigFile());
									System.out.println(1);
									generateContent(query, result);
//									generateSampleContent();
									return "navigation_result";
								case 2:
									ResultSetFormatter.output(outStream, result, ResultSetFormat.syntaxRDF_XML);
									this.setExplainOutput(outStream.toString());
									request.getSession().setAttribute("queryResult", outStream.toString());
									break;
								case 3:
//										res.setContentType("text/xml");
//										out = res.getOutputStream();
//										ResultSetFormatter.output(out, r, ResultSetFormat.syntaxXML);
//										System.out.println(3);
//										break;tS
									ResultSetFormatter.output(outStream, result, ResultSetFormat.syntaxXML);
									this.setExplainOutput(outStream.toString());
									request.getSession().setAttribute("queryResult", outStream.toString());
									break;
								case 4:
//										JSONOutput jOut = new JSONOutput() ;
//	        							jOut.format(outStream, resultSet) ; 
//										out = res.getOutputStream();
//										ResultSetFormatter.output(out, r, ResultSetFormat.syntaxJSON);
//										System.out.println(4);
//										break;
									ResultSetFormatter.output(outStream, result, ResultSetFormat.syntaxJSON);
									this.setExplainOutput(outStream.toString());
									request.getSession().setAttribute("queryResult", outStream.toString());
									break;
								case 5:
									StringBuffer csvBuffer = new StringBuffer();
									List<String> resultVars = result.getResultVars();
									for (String var : resultVars)
										csvBuffer.append(var + ";");
									csvBuffer.append("\n");
									
									QuerySolution querySol;
									while(result.hasNext()) {
										querySol = result.nextSolution();
										for (String var : resultVars) {
											csvBuffer.append("\"");
											csvBuffer.append(querySol.get(var).toString());
											csvBuffer.append("\";");
										}
										csvBuffer.append("\n");
									}
									this.setExplainOutput(csvBuffer.toString());
									request.getSession().setAttribute("queryResult", csvBuffer.toString());
									break;
							}
//						} catch (IOException e) {
//							log.error(e.getMessage());
//						}
					}
					break;
						
					case Query.QueryTypeAsk :
						this.setShowExplainOutput(true);
						String boolResult = Boolean.toString(queryEx.execAsk());
						this.setExplainOutput(boolResult);
						request.getSession().setAttribute("queryResult", boolResult);
						break;
					case Query.QueryTypeConstruct :
						this.setShowExplainOutput(true);
						Model constructResult = queryEx.execConstruct();
						constructResult.write(outputString);
						constructResult.close();
						this.setExplainOutput(outputString.toString());
						request.getSession().setAttribute("queryResult", outputString.toString());
						break;
					case Query.QueryTypeDescribe :
						this.setShowExplainOutput(true);
						Model describeResult = queryEx.execDescribe();
						describeResult.write(outputString);
						describeResult.close();
						this.setExplainOutput(outputString.toString());
						request.getSession().setAttribute("queryResult", outputString.toString());
						break;
				}
			}
		}
		else {
			semwiqHandler.setErrorMessage("Mediator is currently offline or not available.");
			return "failure";
		}
		return "success";
	}
	
	
// EXPLAIN now integrated into ARQ
	
//	public String explainPlan() {
//		this.setShowExplainOutput(true);
//		StringBuffer explainString = new StringBuffer();
//		Mediator mediator = semwiqHandler.getMediator();
//		if (!(mediator != null && mediator.isReady())) {
//			semwiqHandler.setErrorMessage("Mediator is currently offline or not available.");
//			return "failure";
//		}
//		String queryString = this.getSparqlQuery();
//		if (queryString==null || queryString.equalsIgnoreCase("")) {
//			setExplainOutput("No query String specified");
//			semwiqHandler.setErrorMessage("No query String specified");
//			return "failure";
//		}
//		try {
//			log.info("Explain plan query: "+queryString);
//			Query query = mediator.createQuery(queryString);
//			Op origOp = mediator.compile(query);
//			
//			explainString.append("----<<Query>>------------------------------------------\n\n");
//			explainString.append(query.toString()+"\n");
//			explainString.append("---<Original Query Plan>---\n");
//			explainString.append(origOp.toString(query.getPrefixMapping())+"\n");
//			
//			long startTime = System.currentTimeMillis();
//			AnnotatedOp fedOp = mediator.federate(origOp, query);
//			
//			explainString.append("---<Federated Query Plan>---\n");
//			explainString.append(fedOp.toString(query.getPrefixMapping())+"\n");
//			
//			AnnotatedOp optOp = mediator.optimize(fedOp, query);
//			long stopTime = System.currentTimeMillis();
//			
//			explainString.append("---<Optimized Query Plan>---\n");
//			explainString.append(optOp.toString(query.getPrefixMapping())+"\n");
//			
//			long avgResults = optOp.getAvgResults();
//			long minResults = optOp.getMinResults();
//			long maxResults = optOp.getMaxResults();
//			
//			explainString.append("Estimated numer of results: \t" + avgResults + " (min: " + minResults + ", max: " + maxResults + ")");
//			explainString.append("Federation & Optimization: \t" + (stopTime - startTime) + " \tms (may be much slower when loggers are set to debug levels!)");
//			setExplainOutput(explainString.toString());
//			return "success";
//		} catch(Exception e) {
//			log.error(e.getMessage());
//			return "failure";
//		}
//	}
	
	
	public String clear() {
		return "success";
	}
	
	
	
	/*
	 * next code is necessary for the results.jsp to generate the datamodel for the dynamic datatable
	 * (and also for xhtml-download function)
	 */
	private DataModel rowModel = new ArrayDataModel();
	private DataModel columnsModel = new ArrayDataModel();
	private Map entry = new HashMap();
	
	public DataModel getRowModel() {
		return rowModel;
	}

	public void setRowModel(DataModel rowModel) {
		this.rowModel = rowModel;
	}

	public DataModel getColumnsModel() {
		return columnsModel;
	}

	public void setColumnsModel(DataModel columnsModel) {
		this.columnsModel = columnsModel;
	}

	public Map getEntry() {
		return entry;
	}

	public void setEntry(Map entry) {
		this.entry = entry;
	}
	
	
	private void generateContent(Query query, ResultSet result) {
		List vars = result.getResultVars();
		int i=0;
		int col=0;
		int row=0;
		for (Iterator iter = vars.iterator(); iter.hasNext();) {
			row++;
			// put it into the hash map (position 0,1,2,3,4,5,...)
			entry.put(i, (String)iter.next());
			i++;
		}
		QuerySolution querySol;
		while (result.hasNext()) {
			row++;
			querySol = result.nextSolution();
			PrefixMapping map = query.getPrefixMapping();
			RDFNode n;
			String prefix, uri;
			for (Iterator iter = vars.iterator(); iter.hasNext();) {
				n = querySol.get((String) iter.next());
				if (n == null) {
					i++;
					continue;
				}
				else if (n instanceof Resource) {
					prefix = map.getNsURIPrefix(((Resource)n).getNameSpace());
					uri = ((Resource)n).getURI();
					if (prefix == null) {
						entry.put(i, uri);
						i++;
					}
					else {
						String entryContent = prefix +":"+((Resource)n).getLocalName();
						entry.put(i, entryContent);
						i++;
					}
				}
				else if (n instanceof Literal) {
					entry.put(i, ((Literal)n).getLexicalForm());
					i++;
				}
			}
		}
		col = i/row;
		String[] rowData = new String[row];
		String[] colData = new String[col];
		for (int j=0; j<rowData.length; j++) {
			rowData[j] = "row"+j;
		}
		for (int j=0; j<colData.length; j++) {
			colData[j] = "row"+j;
		}
		rowModel = new ArrayDataModel(rowData);
		columnsModel = new ArrayDataModel(colData);
		generateSelectResultDownload(col, row);
	}
	
	/**
	 * @param col
	 * @param row
	 */
	private void generateSelectResultDownload(int col, int row) {
		StringBuffer resultBuffer = new StringBuffer();
		// inserting HTML-Header
		resultBuffer.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\">\n");
		resultBuffer.append("<html><head/>\n<body>\n");
		
		// inserting resultSet Datatable
		resultBuffer.append("<table border=\"1\">\n");
		for (int i=0; i<row; i++) {
			resultBuffer.append("<tr>\n");
			for (int j=0; j<col; j++) {
				int position=i*col+j;
				resultBuffer.append("<td>"+entry.get(position)+"</td>\n");
			}
			resultBuffer.append("</tr>\n");
		}
		resultBuffer.append("</table>\n");
		resultBuffer.append("</body>\n");
		resultBuffer.append("</html>");
		
		FacesContext context = FacesContext.getCurrentInstance();
 		ExternalContext ec = context.getExternalContext();
 		HttpServletRequest request = (HttpServletRequest)ec.getRequest();
		request.getSession().setAttribute("queryResult", resultBuffer.toString());
	}

	public Object getEntryItem() {
		DataModel rowDataModel = getRowModel();
        if (rowDataModel.isRowAvailable())
        {
          int rowIndex = rowDataModel.getRowIndex();
          DataModel columnsDataModel = getColumnsModel();
          if (columnsDataModel.isRowAvailable())
          {
        	  int columnIndex = columnsDataModel.getRowIndex();
        	  int position=rowIndex*columnsDataModel.getRowCount()+columnIndex;
//        	  System.out.println("rowIndex: "+rowIndex+", columnIndex: "+columnIndex+", rowCount: "+rowDataModel.getRowCount()+", columnCount: "+columnsDataModel.getRowCount());
        	  return entry.get(position);
          }
        }
        return null;
	}
	
	/*
	 * streaming for the XHTML-Download option
	 */
	private void streamOutContent(ByteArrayOutputStream outStream) {
		StringBuffer temp = new StringBuffer();
		temp.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">\n");
		temp.append("<body>\n");
		temp.append("<table border=\"1\">\n");
		if (rowModel!=null && columnsModel!=null) {
			for (int i=0; i<rowModel.getRowCount(); i++) {
				temp.append("<tr>\n");
				for (int j=0; j<columnsModel.getRowCount(); j++) {
					temp.append("<td>"+(String) entry.get(i*columnsModel.getRowCount()+j)+"</td>\n");
				}
				temp.append("</tr>\n");
			}
			temp.append("</table>\n");
			temp.append("</body>");
			try {
				outStream.write(temp.toString().getBytes());
			} catch (IOException e) {
				log.error(e.getMessage());
			}
		}
	}
	
	
	// only for testing purposes
	private void generateSampleContent() {
		String[] hugo1 =  {"row1", "row2", "row3"};
		String[] hugo2 = {"col1", "col2", "col3", "col4"};
		rowModel = new ArrayDataModel(hugo1);
		columnsModel = new ArrayDataModel(hugo2);
		for (int i=0; i<12; i++) {
			entry.put(i, String.valueOf(i));
		}
		generateSelectResultDownload(hugo2.length, hugo1.length);
	}
	
	public String hideExplainResults() {
		this.setShowExplainOutput(false);
		return "success";
	}
	
	
	
	
	
	
	
	
	// ----------- currently not in use -----------
	
	// for updating the progress bar while execution
	public void startExecutionProcress(ActionEvent event) {
//        longRunningTaskThreadPool.execute(new QueryOperationRunner(persistentFacesState));
        this.execute();
    }
	
	
	// for updating the progress bar while execution
    /**
     * Utility class to represent some server process that we want to monitor
     * using ouputProgress and server push.
     */
    protected class QueryOperationRunner implements Runnable {
        PersistentFacesState state = null;
        private List resultVars;
        private int selectResults;

        public QueryOperationRunner(PersistentFacesState state) {
            this.state = state;
        }

        /**
         * Routine that takes time and updates percentage as it runs.
         */
        public void run() {
        	Mediator mediator = semwiqHandler.getMediator();
        	if(mediator!=null && mediator.isReady()) {
    			setPercentComplete(0);
    			try {

    				// counter to avoid 
    				int counter = 0;
    				while ((queryEx==null || result==null) && counter<400) {
    					Thread.sleep(30);
    				}
    				if (queryEx!=null) {
    					Long estimatedResults = (Long) queryEx.getContext().get(Constants.ESTIMATED_AVG_RESULTS);
//    					result = queryEx.execSelect();
    					resultVars = result.getResultVars();
    					
    					selectResults = 0;
    					while(result.hasNext()) {
    						if (estimatedResults > 0) {
	    						int val = (int) (100*(selectResults / (float) estimatedResults));
	    						if (val >= 0 && val <= 100) {
	    							setPercentComplete(val);
	    						}
    						}
    						else {
    							setProgressBarType(true);
    							setPercentComplete(10);
    						}
    					}
    					if (getPercentComplete()!=100) {
    						setPercentComplete(100);
    					}
    				}
    				else {
    					setPercentComplete(100);
    					log.info("QUERY wasn't successfull...");
    				}

					
//					System.out.println("QueryOperatingRunner: success ...");

    			} catch (InterruptedException e) {
    				log.error(e.getMessage(), e);
    			} catch (IllegalStateException e) {
                    log.error("Error running progress thread.", e);
                }
    			renderManager.getOnDemandRenderer(sessionId).requestRender();
    		}
        }
    }


	/* (non-Javadoc)
	 * @see com.icesoft.faces.async.render.Renderable#getState()
	 */
	public PersistentFacesState getState() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.icesoft.faces.async.render.Renderable#renderingException(com.icesoft.faces.webapp.xmlhttp.RenderingException)
	 */
	public void renderingException(RenderingException arg0) {
		// TODO Auto-generated method stub
		
	}
	
	
}