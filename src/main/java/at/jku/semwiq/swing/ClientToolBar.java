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

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.FileOutputStream;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import javax.swing.JToolBar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.jku.semwiq.mediator.registry.RegistryException;
import at.jku.semwiq.mediator.registry.model.MonitoringProfile;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.util.FileUtils;

public class ClientToolBar extends JToolBar implements ActionListener {
	private static final Logger log = LoggerFactory.getLogger(ClientToolBar.class);
	
	private static final long serialVersionUID = -2976414295783438880L;
	
	/** the available actions */
	public enum Action {
		NEW("New Query", "Create a new query tab."),
		CLOSE("Close Tab", "Close the active query tab."),
		CLEAR("Clear", "Clears the query and results pane."),
		
		RUN("Run Query", "Runs the query and displays results below."),
		CANCEL("Cancel", 	"Cancel query processing."),

		STATUPD("Update Statistics", "Update statistics."),
		REGISTER("Register", "Register multiple data source endpoints from a file-based catalog model."),
		REGISTERURI("Register by URI", "Register a single data source endpoint by its endpoint URI."),
		UNREGISTERURI("Unregister by URI", "Unregister a single data source endpoint by its endpoint URI."),
		EXPORT("Export Store", "Export global RDF store into a file."),
//		EXPORTVOCAB("Export Vocabularies", "Export globally cached vocabulary."),
		EXPORTSTATS("Export Statistics", "Export statistics into RDF file."),
		QUIT("Quit", "Quit TestClient application.");

		private String title;
		private String tip;

		Action(String title, String tip) {
			this.title = title;
			this.tip = tip;
		}

		public String getTip() { return tip; }
		public String getTitle() { return title; }
	}

	protected final SwingApp client;

	/** background tasks */
	protected QueryProcessingTask queryProcTask = null;

	/** all the buttons for each action */
	protected Map<Action, JButton> buttons = new Hashtable<Action, JButton>();
	
	/**
	 * constructor
	 * @param client reference
	 */
	public ClientToolBar(SwingApp client) {
		this.client = client;

		createButton(Action.NEW);
		createButton(Action.CLOSE);
		createButton(Action.CLEAR);
		add(new JSeparator(JSeparator.VERTICAL));
		
		createButton(Action.RUN);
		createButton(Action.CANCEL);
		add(new JSeparator(JSeparator.VERTICAL));		
		
		createButton(Action.STATUPD);
		createButton(Action.REGISTER);
		createButton(Action.REGISTERURI);
		createButton(Action.UNREGISTERURI);
		createButton(Action.EXPORT);
//		createButton(Action.EXPORTVOCAB);
		createButton(Action.EXPORTSTATS);
		createButton(Action.QUIT);
	}

	/**
	 * @param action
	 * @return corresponding JButton
	 */
	public JButton getButton(Action act) {
		return buttons.get(act);
	}
	
	/**
	 * create a new JButton, add to buttons and the container
	 * @param action
	 */
	private void createButton(Action act) {
		JButton b = new JButton(act.getTitle());
		b.setToolTipText(act.getTip());
		b.setActionCommand(act.toString());
		b.addActionListener(this);
		buttons.put(act, b);
		add(b);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		
		if (cmd.equals(Action.NEW.toString())) {
			client.newTab(null);
			
		} else if (cmd.equals(Action.CLOSE.toString())) {
			client.closeTab(null);

		} else if (cmd.equals(Action.CLEAR.toString())) {
			client.getTab().resetTab();
			
		} else if (cmd.equals(Action.RUN.toString())) {
			doQuery(client.getTab().getQuery());
			
		} else if (cmd.equals(Action.CANCEL.toString())) {
			if (queryProcTask != null && !queryProcTask.isDone())
				queryProcTask.cancel(false);
			
		} else if (cmd.equals(Action.REGISTER.toString())) {
			JFileChooser fj = new JFileChooser();
			fj.setName("Choose a file with one or more data source descriptions");
			int returnVal = fj.showOpenDialog(this);
			if(returnVal == JFileChooser.APPROVE_OPTION) {
				String fileName = null;
				try {
					fileName = fj.getSelectedFile().getCanonicalPath();
					Model m = FileManager.get().loadModel(fileName);
					client.getMediator().getDataSourceRegistry().getManager().register(m);
				} catch (Exception ex) {
					log.error("Error loading data source descriptions from '" + fileName + "'.", ex);
				}
			}
		
		} else if (cmd.equals(Action.REGISTERURI.toString())) {
//			String uri = JOptionPane.showInputDialog(this, "Register Endpoint by URI", "Please enter a SPARQL endpoint URI:", JOptionPane.QUESTION_MESSAGE);
			List<MonitoringProfile> profiles;
			try {
				profiles = client.getMediator().getDataSourceRegistry().getAvailableMonitoringProfiles();
				RegisterDialog reg = new RegisterDialog(client, profiles);
				boolean ok = reg.showDialog();
				if (ok) {
					String uri = reg.getSparqlEndpoint();
					MonitoringProfile profile = reg.getMonitoringProfile();
					
					if  (uri != null && uri.startsWith("http://"))
						try {
							client.getMediator().getDataSourceRegistry().getManager().register(uri, profile);
						} catch (RegistryException e1) {
							log.error("Error registering endpoint <" + uri + ">.", e1);
						}
						else if (uri != null && uri.trim().length() > 0)
							log.error("The URI is invald: " + uri + ".");
				}
			} catch (RegistryException e2) {
				log.error("Failed to get monitoring profiles for dialog.", e2);
			}
			
		} else if (cmd.equals(Action.UNREGISTERURI.toString())) {
			String uri = JOptionPane.showInputDialog(this, "Unregister Endpoint by URI", "Please enter a SPARQL endpoint URI:", JOptionPane.QUESTION_MESSAGE);			
			if  (uri != null && uri.startsWith("http://"))
				try {
					client.getMediator().getDataSourceRegistry().getManager().unregister(uri);
				} catch (RegistryException e1) {
					log.error("Error unregistering endpoint <" + uri + ">.", e1);
				}
			else if (uri != null && uri.trim().length() > 0)
				log.error("The URI is invald: " + uri + ".");
			
		} else if (cmd.equals(Action.EXPORT.toString())) {
			JFileChooser export = new JFileChooser();
			export.setName("Choose a target file for the RDF store export");
			int returnVal2 = export.showSaveDialog(this);
			if(returnVal2 == JFileChooser.APPROVE_OPTION) {
				String fileName = null;
				try {
					fileName = export.getSelectedFile().getCanonicalPath();
		            String syntax = FileUtils.guessLang(fileName);
			            if (syntax == null || syntax.equals(""))
			                syntax = FileUtils.langXML ;
					Model m = client.getMediator().getGlobalStore();
					m.enterCriticalSection(Lock.READ);
					try {
						m.write(new FileOutputStream(fileName), syntax);
						log.info("Global RDF store exported into '" + fileName + "'.");
					} finally {
						m.leaveCriticalSection();
					}
				} catch (Exception ex) {
					log.error("Error exporting global RDF store into file '" + fileName + "'.", ex);
				}
			}

//		} else if (cmd.equals(Action.EXPORTVOCAB.toString())) {
//			JFileChooser export = new JFileChooser();
//			export.setName("Choose a target file for the global vocabularies export");
//			int returnVal2 = export.showSaveDialog(this);
//			if(returnVal2 == JFileChooser.APPROVE_OPTION) {
//				String fileName = null;
//				try {
//					fileName = export.getSelectedFile().getCanonicalPath();
//		            String syntax = FileUtils.guessLang(fileName);
//			            if (syntax == null || syntax.equals(""))
//			                syntax = FileUtils.langXML ;
//					OntModel vocModel = client.getMediator().getDataSourceRegistry().getVocabularyManager().getVocabularyModel();
//					vocModel.enterCriticalSection(Lock.READ);
//					try {
//						vocModel.write(new FileOutputStream(fileName), syntax);
//						log.info("Globally cached vocabularies exported into '" + fileName + "'.");
//					} finally {
//						vocModel.leaveCriticalSection();
//					}
//				} catch (Exception ex) {
//					log.error("Error exporting globally cached vocabularies into file '" + fileName + "'.", ex);
//				}
//			}

		} else if (cmd.equals(Action.EXPORTSTATS.toString())) {
			JFileChooser export = new JFileChooser();
			export.setName("Choose a target file for the statistics export");
			int returnVal2 = export.showSaveDialog(this);
			if(returnVal2 == JFileChooser.APPROVE_OPTION) {
				String fileName = null;
				try {
					fileName = export.getSelectedFile().getCanonicalPath();
		            String syntax = FileUtils.guessLang(fileName);
			            if (syntax == null || syntax.equals(""))
			                syntax = FileUtils.langXML ;
					Model statsModel = client.getMediator().getDataSourceRegistry().getRDFStatsModel().getWrappedModel();
					statsModel.enterCriticalSection(Lock.READ);
					try {
						statsModel.write(new FileOutputStream(fileName), syntax);
						log.info("Statistics exported into '" + fileName + "'.");
					} finally {
						statsModel.leaveCriticalSection();
					}
				} catch (Exception ex) {
					log.error("Error exporting statistics into file '" + fileName + "'.", ex);
				}
			}

		} else if (cmd.equals(Action.STATUPD.toString())) {
			try {
				client.getMediator().getDataSourceRegistry().getMonitor().triggerCompleteUpdate();
			} catch (RegistryException e1) {
				log.error("Failed to trigger complete update.", e1);
			}
			
		} else if (cmd.equals(Action.QUIT.toString())) {
			client.quit();
		}
	}

	/**
	 * @param query
	 */
	private void doQuery(String query) {
		queryProcTask = new QueryProcessingTask(client, query);
		client.getProgressBar().setString(null);
		client.getProgressBar().setValue(0);
		client.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		getButton(Action.RUN).setEnabled(false);
        
		queryProcTask.addPropertyChangeListener(
			     new PropertyChangeListener() {
			         public  void propertyChange(PropertyChangeEvent evt) {
			        	 // update progress bar
			             if ("progress".equals(evt.getPropertyName())) {
			            	 int value = (Integer) evt.getNewValue();
			            		 client.getProgressBar().setValue(value);
			             }
			         }
			     });

		queryProcTask.execute();
	}

	/**
	 * 
	 */
	public void deactivate() {
		for (JButton b : buttons.values())
			b.setEnabled(false);
	}
	
	/**
	 * 
	 */
	public void activate() {
		for (JButton b : buttons.values())
			b.setEnabled(true);
	}

}
