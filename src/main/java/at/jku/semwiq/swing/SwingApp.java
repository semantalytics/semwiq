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

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.jku.semwiq.mediator.Constants;
import at.jku.semwiq.mediator.Mediator;
import at.jku.semwiq.mediator.MediatorImpl;
import at.jku.semwiq.mediator.conf.GUIConfig;

/**
 * @author dorgon
 * 
 */
public class SwingApp extends JFrame implements WindowListener {
	
	private static final long serialVersionUID = -6999546579045604640L;
	private static final Logger log = LoggerFactory.getLogger(SwingApp.class);

	public static final String TITLE = "SemWIQ Standalone Client";
	
	public static String CAT_OUTPUT_FILE = "exported-catalog.owl";

	private final List<QueryTab> qTabs = new ArrayList<QueryTab>();
	private final ClientToolBar toolBar;
	private final JTabbedPane tabPane;
	private final JProgressBar progressBar;
	
	private static Mediator mediator;
	private static GUIConfig config;
	
	private static Thread shutdownHook = new Thread() {
		public void run() {
			mediator.shutdown();
		}
	};

	public static void main(String[] args) {
		Runtime.getRuntime().addShutdownHook(shutdownHook);
		
		try {
			// start the GUI event dispatch thread
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					new SwingApp();
				}
			});

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return;
		}		
	}

	public SwingApp() {
		setTitle(TITLE + " - Loading...");

		setDefaultLookAndFeelDecorated(true);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		
		Container pane = getContentPane();
		pane.setLayout(new BorderLayout());

		toolBar = new ClientToolBar(this);
		pane.add(toolBar, BorderLayout.PAGE_START);

		tabPane = new JTabbedPane();
		pane.add(tabPane, BorderLayout.CENTER);

		progressBar = new JProgressBar();
		progressBar.setValue(0);
		progressBar.setStringPainted(true);
		pane.add(progressBar, BorderLayout.PAGE_END);
		
		pack();
		setSize(getWidth(), 600);
		setLocationRelativeTo(null);
		addWindowListener(this);
		setVisible(true);
		
		deactivateGui();
		new SwingWorker<Void, Void>() {
			private boolean failed = false;
			
			protected Void doInBackground() {
				// start a new mediator instance
				try {
					mediator = new MediatorImpl();
				} catch (Throwable e) {
					failed = true;
					log.error(e.getMessage(), e);
				}
				return null;
			}
			
			/* (non-Javadoc)
			 * @see javax.swing.SwingWorker#done()
			 */
			@Override
			protected void done() {
				// set title to version, show sample queries
				if (failed || mediator == null || !mediator.isReady())
					quit();
				else {
					config = mediator.getConfig().getGuiConfig();
					setTitle(TITLE + " - " + Constants.VERSION_STRING);
	
					Map<String, String> queries = config.getSampleQueries();
					for (String name : queries.keySet())
						newTab(queries.get(name), name);

					activateGui();
				}
			}

		}.execute();
	}

	private void activateGui() {
		toolBar.activate();
	}

	/**
	 * 
	 */
	private void deactivateGui() {
		toolBar.deactivate();
	}

	public ClientToolBar getToolBar() {
		return toolBar;
	}

	public JProgressBar getProgressBar() {
		return progressBar;
	}
	
	public QueryTab getTab() {
		return (QueryTab) tabPane.getSelectedComponent();
	}

	public void newTab(String defaultQuery) {
		newTab(defaultQuery, null);
	}

	public final void newTab(String defaultQuery, String title) {
		QueryTab q = new QueryTab(this, defaultQuery, title);
		qTabs.add(q);
		tabPane.add(q);
		tabPane.setSelectedComponent(q);
		tabPane.revalidate();
		tabPane.repaint();
	}

	/**
	 * Close tab q or the selected tab if q == null.
	 * 
	 * @param q
	 */
	public void closeTab(QueryTab q) {
		if (tabPane.getTabCount() == 0)
			return;
		if (q == null)
			q = (QueryTab) tabPane.getSelectedComponent();
		qTabs.remove(q);
		tabPane.remove(q);
		tabPane.revalidate();
		tabPane.repaint();
	}

	public Mediator getMediator() {
		return mediator;
	}
	
	public void windowActivated(WindowEvent arg0) {}

	public void windowClosed(WindowEvent arg0) {}

	public void windowClosing(WindowEvent arg0) {
		quit();
	}

	public void windowDeactivated(WindowEvent arg0) {}

	public void windowDeiconified(WindowEvent arg0) {}

	public void windowIconified(WindowEvent arg0) {}

	public void windowOpened(WindowEvent arg0) {}

	public GUIConfig getConfiguration() {
		return config;
	}
	
	public void quit() {
		if (mediator != null)
			mediator.shutdown();
		dispose();
	}

}
