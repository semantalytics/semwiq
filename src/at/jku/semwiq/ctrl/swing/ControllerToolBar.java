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

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import javax.swing.filechooser.FileFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.jku.semwiq.ctrl.SemWIQControllerException;
import at.jku.semwiq.rmi.CommonConstants;
import at.jku.semwiq.rmi.EndpointDaemon;
import at.jku.semwiq.rmi.EndpointMetadata;
import at.jku.semwiq.rmi.InterfaceUtils;
import at.jku.semwiq.rmi.RemoteEndpointDaemon;
import at.jku.semwiq.rmi.DaemonRegistry;
import at.jku.semwiq.rmi.SpawnedEndpointMetadata;

import com.healthmarketscience.rmiio.SimpleRemoteInputStream;
import com.hp.hpl.jena.util.FileUtils;

/**
 * @author dorgon
 *
 */
public class ControllerToolBar extends JToolBar implements ActionListener {
	private static final Logger log = LoggerFactory.getLogger(ControllerToolBar.class);
	
	public enum Action {
		ADD_DAEMON("Add Daemon", "Add remote endpoint daemon", true),
		REMOVE_DAEMON("Remove Daemon", "Remove selected endpoint daemon", false),
		REFRESH_DAEMONS("Refresh Daemons", "Refresh and remove invalid endpoint daemons", false),
		SHUTDOWN_DAEMON("Shutdown Daemon", "Shutdown selected endpoint daemon", false),
		SHUTDOWN_ALL("Shutdown All", "Shutdown all endpoint daemons", false),
		
		REFRESH_ENDPOINTS("Refresh Endpoints", "Refresh list of endpoints for selected daemon", false),
		SPAWN("Spawn Endpoint", "Spawn new endpoint with selected daemon", false),
		KILL("Kill Endpoint", "Kill selected endpoint", false),
		KILL_ALL("Kill All", "Kill all endpoints of selected daemon", false),

		QUIT("Quit", "Quit application", true);

		String title;
		String tip;
		boolean defaultEnabled;

		Action(String title, String tip, boolean enabled) {
			this.title = title;
			this.tip = tip;
			this.defaultEnabled = enabled;
		}
	}

	private final ControllerFrame app;
	
	private final Map<Action, JButton> buttons = new Hashtable<Action, JButton>();
	

	public ControllerToolBar(ControllerFrame app) {
		this.app = app;

		for (Action a : Action.values())
			createButton(a);

		if (app.getController().listEndpointDaemons().size() > 0)
			app.getDaemonList().setSelectedIndex(0);
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
		JButton b = new JButton(act.title);
		b.setToolTipText(act.tip);
		b.setActionCommand(act.toString());
		b.addActionListener(this);
		b.setEnabled(act.defaultEnabled);
		buttons.put(act, b);
		add(b);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		
		if (cmd.equals(Action.ADD_DAEMON.toString())) {
			getButton(Action.ADD_DAEMON).setEnabled(false);
			app.setCursor(Cursor.WAIT_CURSOR);

			AddDaemonTask add = new AddDaemonTask(app);
			add.execute();
			
		} else if (cmd.equals(Action.REMOVE_DAEMON.toString())) {
			DaemonRegistry reg = (DaemonRegistry) app.getDaemonList().getSelectedValue();
			app.getController().removeEndpointDaemon(reg);
			app.getDaemonList().removeDaemon(reg);
			
		} else if (cmd.equals(Action.SHUTDOWN_DAEMON.toString())) {
			getButton(Action.SHUTDOWN_DAEMON).setEnabled(false);
			app.setCursor(Cursor.WAIT_CURSOR);

			DaemonRegistry reg = (DaemonRegistry) app.getDaemonList().getSelectedValue();
			ShutdownDaemonTask shutdown = new ShutdownDaemonTask(app, reg);
			shutdown.execute();
			
		} else if (cmd.equals(Action.SHUTDOWN_ALL.toString())) {
			getButton(Action.SHUTDOWN_ALL).setEnabled(false);
			app.setCursor(Cursor.WAIT_CURSOR);

			ShutdownAllDaemonsTask shutdown = new ShutdownAllDaemonsTask(app);
			shutdown.execute();
			
		} else if (cmd.equals(Action.SPAWN.toString())) {
			try {
				DaemonRegistry reg = (DaemonRegistry) app.getDaemonList().getSelectedValue();
				EndpointDaemon daemon = app.getController().getEndpointDaemon(reg);
				
				JFileChooser fj = new JFileChooser();
				fj.setName("Please select a file containing RDF data");
				fj.setFileFilter(new FileFilter() {
					@Override
					public boolean accept(File f) {
						String name = f.getName().toLowerCase();
						return (name.endsWith(".n3") || name.endsWith(".ttl") || name.endsWith(".rdf") || name.endsWith(".xml"));
					}

					@Override
					public String getDescription() {
						return "RDF files (.n3 .ttl .rdf .xml)";
					}
				});
				
				int returnVal = fj.showOpenDialog(this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					try {
						String file = fj.getSelectedFile().getAbsolutePath();
						InputStream data = new BufferedInputStream(new FileInputStream(fj.getSelectedFile()));
						getButton(Action.SPAWN).setEnabled(false);
						getButton(Action.SHUTDOWN_DAEMON).setEnabled(false);
						getButton(Action.SHUTDOWN_ALL).setEnabled(false);
						app.setCursor(Cursor.WAIT_CURSOR);		
					
						SpawnEndpointTask spawn = new SpawnEndpointTask(app, reg, data, file, FileUtils.guessLang(file));
						spawn.execute();
					} catch (Exception ex) {
						log.error("Error loading RDF data from '" + fj.getSelectedFile() + "'.", ex);
					}
				}
				
			} catch (Exception ex) {
				app.errorMessage(ex.getMessage(), ex);
			}
		
		} else if (cmd.equals(Action.KILL.toString())) {
			getButton(Action.KILL).setEnabled(false);
			app.setCursor(Cursor.WAIT_CURSOR);
	
			SpawnedEndpointMetadata spec = (SpawnedEndpointMetadata) app.getEndpointList().getSelectedValue();
			KillEndpointTask kill = new KillEndpointTask(app, spec);
			kill.execute();
		
		} else if (cmd.equals(Action.REFRESH_DAEMONS.toString())) {
			getButton(Action.REFRESH_DAEMONS).setEnabled(false);
			app.setCursor(Cursor.WAIT_CURSOR);
			
			try {
				app.getController().init();
			} catch (SemWIQControllerException e1) {
				app.errorMessage("Failed to refresh factaories.", e1);
			} finally {
				app.setCursor(Cursor.DEFAULT_CURSOR);
				getButton(Action.REFRESH_DAEMONS).setEnabled(true);
			}
			
		} else if (cmd.equals(Action.REFRESH_ENDPOINTS.toString())) {
			getButton(Action.REFRESH_ENDPOINTS).setEnabled(false);
			app.setCursor(Cursor.WAIT_CURSOR);
			
			try {
				app.getEndpointList().setActiveRegistry(app.getDaemonList().getSelectedRegistry());
			} finally {
				app.setCursor(Cursor.DEFAULT_CURSOR);
				getButton(Action.REFRESH_ENDPOINTS).setEnabled(true);
			}
			
		} else if (cmd.equals(Action.QUIT.toString()))
			app.quit();
	}
	

// action tasks
	
	public class ShutdownAllDaemonsTask extends Task<Void, Void> {
		
		public ShutdownAllDaemonsTask(ControllerFrame app) {
			super(app);
		}

		@Override
		protected Void doInBackground() throws Exception {
			for (RemoteEndpointDaemon f : app.getController().listEndpointDaemons().values())
				f.shutdown();
			return null;
		}
		
		@Override
		protected void taskDone() {
			try {
				get();
				app.getDaemonList().removeAllDaemons();
			} catch (Exception e) {
				app.errorMessage(e.getMessage(), e);
			} finally {
				app.setCursor(Cursor.DEFAULT_CURSOR);
			}
		}
	}
	
	public class ShutdownDaemonTask extends Task<Void, Void> {
		private final DaemonRegistry reg;
		
		public ShutdownDaemonTask(ControllerFrame app, DaemonRegistry reg) {
			super(app);
			this.reg = reg;
		}

		@Override
		protected Void doInBackground() throws Exception {
			app.getController().shutdownEndpointDaemon(reg);
			return null;
		}
		
		@Override
		protected void taskDone() {
			try {
				get();
				app.getDaemonList().removeDaemon(reg);
			} catch (Exception e) {
				app.errorMessage(e.getMessage(), e);
			} finally {
				app.setCursor(Cursor.DEFAULT_CURSOR);
			}
		}
	}
	
	public class KillEndpointTask extends Task<Void, Void> {
		private final SpawnedEndpointMetadata spec;
		
		public KillEndpointTask(ControllerFrame app, SpawnedEndpointMetadata spec) {
			super(app);
			this.spec = spec;
		}
		
		/* (non-Javadoc)
		 * @see javax.swing.SwingWorker#doInBackground()
		 */
		@Override
		protected Void doInBackground() throws Exception {
			app.getController().killEndpoint(spec);
			return null;
		}
		
		@Override
		protected void taskDone() {
			try {
				get();
				app.getEndpointList().removeEndpoint(spec);
			} catch (Exception e) {
				app.errorMessage(e.getMessage(), e);
			} finally {
				app.setCursor(Cursor.DEFAULT_CURSOR);
			}
		}
	}
	
	class SpawnEndpointTask extends Task<SpawnedEndpointMetadata, Void> {
		private final DaemonRegistry reg;
		private final InputStream stream;
		private final String dataFormat;
		private final String baseUri;

		public SpawnEndpointTask(ControllerFrame app, DaemonRegistry reg, InputStream stream, String baseUri, String dataFormat) {
			super(app);
			this.reg = reg;
			this.stream = stream;
			this.baseUri = baseUri;
			this.dataFormat = dataFormat;
		}
		
		/* (non-Javadoc)
		 * @see javax.swing.SwingWorker#doInBackground()
		 */
		@Override
		protected SpawnedEndpointMetadata doInBackground() throws Exception {
			EndpointMetadata meta = new EndpointMetadata();
			return app.getController().spawnEndpoint(reg, meta, new SimpleRemoteInputStream(stream).export(), dataFormat, baseUri);
		}
		
		@Override
		protected void taskDone() {
			try {
				SpawnedEndpointMetadata spawned = get();
				app.getEndpointList().addEndpoint(spawned);
			} catch (Exception e) {
				app.errorMessage(e.getMessage(), e);
			} finally {
				app.getDaemonList().updateButtons();
				app.setCursor(Cursor.DEFAULT_CURSOR);
			}
		}
	}
	
	class AddDaemonTask extends Task<DaemonRegistry, Void> {

		public AddDaemonTask(ControllerFrame app) {
			super(app);
		}
		
		/* (non-Javadoc)
		 * @see javax.swing.SwingWorker#doInBackground()
		 */
		@Override
		protected DaemonRegistry doInBackground() throws Exception {
			String uri = JOptionPane.showInputDialog(app, "Add remote endpoint daemon", "Please enter hostname and port (default port is " + CommonConstants.RMI_REGISTRY_PORT + "):", JOptionPane.QUESTION_MESSAGE);			
			if  (uri != null) {
				DaemonRegistry reg = InterfaceUtils.getDaemonRegistry(uri);
				app.getController().getEndpointDaemon(reg);
				return reg;
			} else if (uri != null && uri.trim().length() > 0)
				app.errorMessage("Invalid endpoint: " + uri + ", enter hostname or hostname and port like 'yourdomain.com:1099'.", null);
			
			return null;
		}

		@Override
		protected void taskDone() {
			try {
				DaemonRegistry reg = get();
				if (reg != null)
					app.getDaemonList().addDaemon(reg);
			} catch (Exception e) {
				app.errorMessage(e.getMessage(), e);
			} finally {
				getButton(Action.ADD_DAEMON).setEnabled(true);
				app.setCursor(Cursor.DEFAULT_CURSOR);
			}
		}
	}
}
