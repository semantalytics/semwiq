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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.jku.rdfstats.RDFStatsDataset;
import at.jku.rdfstats.RDFStatsModel;
import at.jku.rdfstats.RDFStatsModelException;
import at.jku.rdfstats.hist.ComparableDomainHistogram;
import at.jku.rdfstats.html.GenerateHTML;
import at.jku.semwiq.mediator.Mediator;
import at.jku.semwiq.mediator.registry.DataSourceRegistry;
import at.jku.semwiq.mediator.registry.model.DataSource;
import at.jku.semwiq.webapp.ice.model.Datasource;
import at.jku.semwiq.webapp.ice.model.WebAppHistogram;
import at.jku.semwiq.webapp.ice.model.WebAppRessource;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;



/**
 * this class represents the handler for the jspx-document (semwiq_information.jspx) and handles 
 * the data needed for the content.
 * @author thomas
 *
 */
public class InformationHandler {
	
	private static final Logger log = LoggerFactory.getLogger(InformationHandler.class);

	private List datasourceList;
	private Datasource currentDatasource;
	private String semwiq_informationLastDownload;
	private boolean renderHistograms;
	
	//methods for other Server Faces handlers that are used in this one
	private SemWIQHandler semwiqHandler;
	public void setSemwiqHandler(SemWIQHandler semwiqHandler) {
		this.semwiqHandler = semwiqHandler;
	}
	

	//constructer
	public InformationHandler() {
		loadDatasourceList();
		this.setRenderHistograms(false);
	}
	
	// getters and setters
	public String getSemwiq_informationState() {
	 
		if (currentDatasource!=null)
			return currentDatasource.getSemwiq_informationState();
		else
			return "";
	}
	
	public void setSemwiq_informationState(String semwiq_informationState) {
		currentDatasource.setSemwiq_informationState(semwiq_informationState);
	}
	
	public String getSemwiq_informationURI() {
		if (currentDatasource!=null)
			return currentDatasource.getSemwiq_informationURI();
		else
			return "";
	}
	
	public void setSemwiq_informationURI(String semwiq_informationURI) {
		currentDatasource.setSemwiq_informationURI(semwiq_informationURI);
	}
	
	public String getSemwiq_informationURILabel() {
		if (currentDatasource!=null)
			return currentDatasource.getSemwiq_informationURILabel();
		else
			return "";
	}
	
	public void setSemwiq_informationURILabel(String semwiq_informationURILabel) {
		currentDatasource.setSemwiq_informationURILabel(semwiq_informationURILabel);
	}
	
	public String getSemwiq_informationEndpoint() {
		if (currentDatasource!=null)
			return currentDatasource.getSemwiq_informationEndpoint();
		else
			return "";
	}
	
	public void setSemwiq_informationEndpoint(String semwiq_informationEndpoint) {
		currentDatasource.setSemwiq_informationEndpoint(semwiq_informationEndpoint);
	}
	
	public String getSemwiq_informationEndpointLabel() {
		if (currentDatasource!=null)
			return currentDatasource.getSemwiq_informationEndpointLabel();
		else
			return "";
	}
	
	public void setSemwiq_informationEndpointLabel(
			String semwiq_informationEndpointLabel) {
		currentDatasource.setSemwiq_informationEndpointLabel(semwiq_informationEndpointLabel);
	}
	
	public String getSemwiq_informationProviderType() {
		if (currentDatasource!=null && currentDatasource.getSemwiq_informationProviderType()!=null)
			return currentDatasource.getSemwiq_informationProviderType().getLocalName();
		else
			return "";
	}
	
	public String getSemwiq_informationProviderTypeURI() {
		if (currentDatasource!=null && currentDatasource.getSemwiq_informationProviderType()!=null)
			return currentDatasource.getSemwiq_informationProviderType().getURI();
		else
			return "";
	}
	
	public void setSemwiq_informationProviderType(Resource type) {
		currentDatasource.setSemwiq_informationProviderType(type);
	}
	
	public String getSemwiq_informationProviderName() {
		if (currentDatasource!=null)
			return currentDatasource.getSemwiq_informationProviderName();
		else
			return "";
	}
	
	public void setSemwiq_informationProviderName(String semwiq_informationProviderName) {
		currentDatasource.setSemwiq_informationProviderName(semwiq_informationProviderName);
	}
	
	public String getSemwiq_informationMaintainer() {
		if (currentDatasource!=null)
			return currentDatasource.getSemwiq_informationMaintainer();
		else
			return "";
	}
	
	public void setSemwiq_informationMaintainer(String semwiq_informationMaintainer) {
		currentDatasource.setSemwiq_informationMaintainer(semwiq_informationMaintainer);
	}
	
	public String getSemwiq_informationStatDate() {
		if (currentDatasource!=null)
			return currentDatasource.getSemwiq_informationStatDate();
		else
			return "";
	}
	
	public void setSemwiq_informationStatDate(String semwiq_informationStatDate) {
		currentDatasource.setSemwiq_informationStatDate(semwiq_informationStatDate);
	}
	
	public String getSemwiq_informationMonitoringProfile() {
		if (currentDatasource!=null)
			return currentDatasource.getSemwiq_informationMonitoringProfile();
		else
			return "";
	}
	
	public void setSemwiq_informationMonitoringProfile(String semwiq_informationMonitoringProfile) {
		currentDatasource.setSemwiq_informationMonitoringProfile(semwiq_informationMonitoringProfile);
	}
	
	public String getSemwiq_informationMaintainerSameAs() {
		if (currentDatasource!=null)
			return currentDatasource.getSemwiq_informationMaintainerSameAs();
		else
			return "";
	}
	
	public void setSemwiq_informationMaintainerSameAs(String semwiq_informationMaintainerSameAs) {
		currentDatasource.setSemwiq_informationMaintainerSameAs(semwiq_informationMaintainerSameAs);
	}
	
	public String getSemwiq_informationLastDownload() {
		return semwiq_informationLastDownload;
	}

	public void setSemwiq_informationLastDownload(
			String semwiq_informationLastDownload) {
		this.semwiq_informationLastDownload = semwiq_informationLastDownload;
	}


	public Datasource getCurrentDatasource() {
		return currentDatasource;
	}

	public void setCurrentDatasource(Datasource currentDatasource) {
		this.currentDatasource = currentDatasource;
	}
	
	public List getDatasourceList() {
		return datasourceList;
	}
	
	public boolean isRenderHistograms() {
		return renderHistograms;
	}

	public void setRenderHistograms(boolean renderHistograms) {
		this.renderHistograms = renderHistograms;
	}


	public List getDatasourceListItems() {
		List datasourceListItems = new ArrayList();
		for (Iterator iter=datasourceList.iterator(); iter.hasNext();) {
			Datasource ds = (Datasource) iter.next();
			DataSourceRegistry reg = semwiqHandler.getMediator().getDataSourceRegistry();
			try {
				DataSource semwiqDs = reg.getDataSourceByEndpointUri(ds.getSemwiq_informationEndpoint());				
				datasourceListItems.add(new SelectItem(ds, semwiqDs.getSPARQLEndpointURL()));
			} catch (Exception e) {
				log.error("Failed to get data source for endpoint <" + ds.getSemwiq_informationEndpoint());
			}
		}
		return datasourceListItems;
	}
	
	public List getDatasourceWebappList() {
		if (currentDatasource!=null)
			return currentDatasource.getResource();
		else
			return new ArrayList();
	}
	
	public void loadDatasourceList() {
//		System.out.println("loading datasource list ...");
		datasourceList = new ArrayList();
		Mediator mediator = semwiqHandler.getMediator();
		if (mediator==null || !mediator.isReady()) {
			semwiqHandler.setErrorMessage("Mediator is currently offline or not available.");
		}
		else {
			RDFStatsModel rdfStatsModel = mediator.getDataSourceRegistry().getRDFStatsModel();
			RDFStatsDataset dataset;
			
			List<DataSource> regDatasourceList;
			try {
				regDatasourceList = mediator.getDataSourceRegistry().getRegisteredDataSources();				
			} catch (Exception e) {
				log.error("Failed to get list of data sources.", e);
				return; //TODO check with Thomas
			}
			
			String dsStatus;
			Resource providerType = null;
			for (Iterator iter = regDatasourceList.iterator(); iter.hasNext();) {
				DataSource regDs = (DataSource)iter.next();
				Datasource webDs = new Datasource();
				
				if(!regDs.isEnabled()) {
					dsStatus = "DISABLED";
				}
				else if (!regDs.isAvailable()) {
					dsStatus = "OFFLINE";
				}
				else {
					dsStatus = "ONLINE";
				}
				webDs.setSemwiq_informationState(dsStatus);
				webDs.setSemwiq_informationEndpoint(regDs.getSPARQLEndpointURL());
				webDs.setSemwiq_informationEndpointLabel(regDs.getSPARQLEndpointURL());
//				webDs.setSemwiq_informationMaintainer(regDs.get().getName());
//				webDs.setSemwiq_informationProviderName(regDs.getProvider().getName());
//				for (Resource res : regDs.getProvider().listRDFTypes()) {
//					if (res.getURI().equalsIgnoreCase(FOAF.Person.getURI())) {
//						providerType = FOAF.Person;
//					}
//					else if (res.getURI().equalsIgnoreCase(FOAF.Organization.getURI())) {
//						providerType = FOAF.Organization;
//					}
//					else if (res.getURI().equalsIgnoreCase(FOAF.Group.getURI())) {
//						providerType = FOAF.Group;
//					}
//				}
//				webDs.setSemwiq_informationProviderType(providerType);
				webDs.setSemwiq_informationURI(regDs.getUri());
				webDs.setSemwiq_informationURILabel(regDs.getUri());
				
				try {
					dataset = rdfStatsModel.getDataset(regDs.getSPARQLEndpointURL());
//					RDFStatsDatasetExt ext = new RDFStatsDatasetExt(dataset);
//					ext.getLastDownload();
					if (dataset != null) {
						webDs.setSemwiq_informationStatDate(dataset.getDate().toString());

						
						// TODO statistics
//						String regUri = dataset.getSourceUrl();
//						List<Resource> regResList = rdfStatsModel.hasInstanceCountsFor(regUri);
//						List<WebAppRessource> webResList = new ArrayList();
//						
//						for (Iterator iter2 = regResList.iterator(); iter2.hasNext();) {
//							Resource regRes = (Resource)iter2.next();
//							String regResUri = regRes.getURI();
//							long regResCount = rdfStatsModel.instancesTotal(regUri, regRes);
//							String histogramLink = "";
//							if (rdfStatsModel.hasHistogramsFor(regUri, regRes).size() > 0) {
//								histogramLink = "123test";
//							}
//							WebAppRessource webRes = new WebAppRessource(regResUri, regResUri, (int)regResCount, histogramLink);
//							webResList.add(webRes);
//						}
//						webDs.setResource(webResList);
					}
				} catch (RDFStatsModelException e) {
					log.error(e.getMessage(), e);
				}
				datasourceList.add(webDs);
			}
		}
	}
	
	/**
	 * adds a datasource (given from the DatasourcePopup) to the list
	 * @param datasource
	 */
	public void addDatasource(Datasource datasource) {
		datasourceList.add(datasource);
//		System.out.println(datasource.getSemwiq_informationEndpoint());
	}

	
	/**
	 *  delestes a datasource from the list (catalog jspx) when clicked on th "-"-button
	 * @return
	 */
	public String deleteDatasource() {
		if (currentDatasource != null) {
			datasourceList.remove(currentDatasource);
			return "ds deleted";
		}
		else {
			return "no ds";
		}
	}
	
	public void effectChangeListener(ValueChangeEvent event){
	}
	
	
	
	public List getHistograms() {
		
		List<WebAppHistogram> waHistograms = new ArrayList<WebAppHistogram>();
		
		FacesContext context = FacesContext.getCurrentInstance();
        Map map = context.getExternalContext().getRequestParameterMap();
        
        String classURI = (String) map.get("classuri");
        String endpointURI = getSemwiq_informationEndpoint();
//        System.out.println*gg("classURI: "+classURI);
//        System.out.println("endpointURI: "+endpointURI);
        Model tmp = ModelFactory.createDefaultModel();
        Resource cl = tmp.createResource(classURI);
        
// TODO statistics:
//        RDFStatsModel rdfStatsModel = semwiqHandler.getMediator().getRegistry().getRDFStatsModel();
//        List<Property> hisProps = rdfStatsModel.hasHistogramsFor(endpointURI, cl);
//        for (Property p : hisProps) {
//        	List<String> ranges = rdfStatsModel.hasHistogramsFor(endpointURI, cl, p);
//        	
//        	for (String range : ranges) {
//        		try {
//					Histogram h = rdfStatsModel.getHistogram(endpointURI, cl, p, range);
//					long[] bins = h.getBinData();
//					String localName;
//					if (p.getLocalName().equalsIgnoreCase("")) {
//						localName = p.getURI();
//					}
//					else {
//						localName = p.getLocalName();
//					}
//					WebAppHistogram waHis;
//					if (h instanceof ComparableDomainHistogram) {
//						waHis = new WebAppHistogram(bins, h, localName, p.getURI(), true);
//					}
//					else {
//						waHis = new WebAppHistogram(bins, h, localName, p.getURI(), false);
//					}
//					
//					waHistograms.add(waHis);
//				} catch (RDFStatsModelException e) {
//					log.error(e.getMessage(), e);
//				}
//        	}
//        }
        
        
        
//        int n = 1;
//        for (Iterator i=waHistograms.iterator(); i.hasNext();) {
//        	WebAppHistogram temp = (WebAppHistogram)i.next();
//        	System.out.println(n+": "+temp.getPropertyName());
//        	n++;
//        }
        return waHistograms;
	}
	
	public String updateDatasource() {
		if (currentDatasource!=null) {
			try {
				DataSourceRegistry thisReg = semwiqHandler.getMediator().getDataSourceRegistry();
				DataSource tmp = thisReg.getDataSourceByEndpointUri(currentDatasource.getSemwiq_informationEndpoint());
				thisReg.getMonitor().triggerUpdate(tmp);
				return "success";
			} catch (Exception e) {
				log.error(e.getMessage());
				return "failed";
			}
		}
		else {
			return "failed";
		}
	}
	
	// only for test purposes
	private int percentComplete;

	public int getPercentComplete() {
		return percentComplete;
	}

	public void setPercentComplete(int percentComplete) {
		this.percentComplete = percentComplete;
	}
	
	public void run() {
	    for (int i = 0; i <= 100; i += 10) {
	        // pause the thread
	        try {
				Thread.sleep(300);
				setPercentComplete(i);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	}
	
	public String navigationInformation() {
		Mediator mediator = semwiqHandler.getMediator();
		if(mediator==null || !mediator.isReady()) {
			semwiqHandler.setErrorMessage("Mediator is currently offline or not available.");
			return "failure";
		}
		else {
			return "navigation_admin_catalog";
		}
	}
	
	public String renderHist() {
		this.setRenderHistograms(true);
		return "success";
	}

//	private String histogram = "";
	
	public String getHistogram() {
		if (currentDatasource == null)
			return "";
		
		RDFStatsModel model = semwiqHandler.getMediator().getDataSourceRegistry().getRDFStatsModel();
		try {
			if (model.getDataset(currentDatasource.getSemwiq_informationEndpoint()) == null)
				return ""; // not yet existing
			return GenerateHTML.generateHTML(model, currentDatasource.getSemwiq_informationEndpoint(), true);
		} catch (RDFStatsModelException e) {
			log.error("Failed to generate histogram for view.", e);
			return e.getMessage();
		}
	}
	
//	public void setHistogram(String foo) {
//		// TODO?
//	}
}
