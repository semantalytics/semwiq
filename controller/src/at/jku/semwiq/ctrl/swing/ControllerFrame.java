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
package at.jku.semwiq.ctrl.swing;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.jku.semwiq.ctrl.EndpointController;
import at.jku.semwiq.ctrl.SemWIQControllerException;
import at.jku.semwiq.rmi.DaemonRegistry;

/**
 * @author dorgon
 *
 */
public class ControllerFrame extends JFrame implements WindowListener {
	private static final Logger log = LoggerFactory.getLogger(ControllerFrame.class);
	
	private final ControllerToolBar toolBar;
	private final EndpointController ctrl;
	
	private final DaemonList daemons;
	private final EndpointList endpoints;
	private final JLabel statusBar;
	
	private Set<Task<?, ?>> runningTasks = new HashSet<Task<?,?>>();

	
	/**
	 * @throws SemWIQEndpointException 
	 * 
	 */
	public ControllerFrame(EndpointController ctrl) {
		this.ctrl = ctrl;
		
		setTitle("SemWIQ Endpoint Controller GUI");
		setDefaultLookAndFeelDecorated(true);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		
		daemons = new DaemonList(this, ctrl);
		endpoints = new EndpointList(this, ctrl);
		statusBar = new JLabel("(C) 2009 Andreas Langegger, al@jku.at");
		statusBar.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));
		toolBar = new ControllerToolBar(this); // endpoints and daemons need to be initialized before toolbar!
		toolBar.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));
		
		Container pane = getContentPane();
		pane.setLayout(new BorderLayout());
		pane.add(new JScrollPane(daemons), BorderLayout.WEST);
		pane.add(new JScrollPane(endpoints), BorderLayout.CENTER);
		pane.add(toolBar, BorderLayout.NORTH);
		pane.add(statusBar, BorderLayout.SOUTH);

		UpdateDaemonsTask task = new UpdateDaemonsTask(this);
		task.execute();
		
		pack();
		setSize(getWidth(), 600);
		setLocationRelativeTo(null);
		addWindowListener(this);
		setVisible(true);
	}

	/**
	 * @return the ctrl
	 */
	public EndpointController getController() {
		return ctrl;
	}
	
	/**
	 * @param e
	 */
	public void errorMessage(String message, Exception e) {
		if (e != null) {
			log.error(message, e);
			JOptionPane.showMessageDialog(this, message + " " + e.getMessage());
		} else {
			log.error(message);
			JOptionPane.showMessageDialog(this, message);
		}
	}

	/**
	 * @param e
	 */
	public void infoMessage(String message) {
		JOptionPane.showMessageDialog(this, message);
	}
	
	public ControllerToolBar getToolBar() {
		return toolBar;
	}

	public DaemonList getDaemonList() {
		return daemons;
	}
	
	public EndpointList getEndpointList() {
		return endpoints;
	}
	
	public void quit() {
		dispose();
		for (SwingWorker<?, ?> task : runningTasks)
			task.cancel(true);
	}

	public void windowClosing(WindowEvent arg0) {
		quit();
	}

	public void windowActivated(WindowEvent arg0) {}
	public void windowClosed(WindowEvent arg0) {}	
	public void windowDeactivated(WindowEvent arg0) {}
	public void windowDeiconified(WindowEvent arg0) {}
	public void windowIconified(WindowEvent arg0) {}
	public void windowOpened(WindowEvent arg0) {}

	public void addRunningTask(Task<?, ?> task) {
		runningTasks.add(task);
	}

	public void removeRunningTask(Task<?, ?> task) {
		runningTasks.remove(task);
	}

	class UpdateDaemonsTask extends Task<Void, Void> {

		public UpdateDaemonsTask(ControllerFrame app) {
			super(app);
		}
		
		@Override
		protected Void doInBackground() throws Exception {
			// invoke update of daemons list
			try {
				app.getController().init();
				for (DaemonRegistry r : app.getController().listEndpointDaemons().keySet())
					app.getDaemonList().addDaemon(r);
				if (app.getDaemonList().getSelectedRegistry() == null && app.getDaemonList().getModel().getSize() > 0)
					app.getDaemonList().setSelectedIndex(0);
			} catch (SemWIQControllerException e) {
				errorMessage(e.getMessage(), e);
			}
			
			return null;
		}
		
		@Override
		protected void taskDone() {
		}
	}

}
