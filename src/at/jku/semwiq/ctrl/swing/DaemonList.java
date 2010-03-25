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

import java.awt.Dimension;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.border.BevelBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import at.jku.semwiq.ctrl.EndpointController;
import at.jku.semwiq.ctrl.SemWIQControllerException;
import at.jku.semwiq.ctrl.swing.ControllerToolBar.Action;
import at.jku.semwiq.rmi.DaemonRegistry;
import at.jku.semwiq.rmi.SpawnedEndpointMetadata;

/**
 * @author dorgon
 *
 */
public class DaemonList extends JList implements ListSelectionListener {
	private static final long serialVersionUID = -3260611842832708349L;

	private final ControllerFrame app;
	
	public DaemonList(ControllerFrame frame, EndpointController ctrl) {
		this.app = frame;
		
		setModel(new DefaultListModel());		
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		addListSelectionListener(this);
		
		setVisibleRowCount(-1);
		setPreferredSize(new Dimension(200, app.getHeight()));
		setMinimumSize(new Dimension(200, app.getHeight()));
		setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
	}

	public DaemonRegistry getSelectedRegistry() {
		if (getSelectedValue() != null)
			return (DaemonRegistry) getSelectedValue();
		else
			return null;
	}
	
	public void addDaemon(DaemonRegistry registry) {
		DefaultListModel model = (DefaultListModel) getModel();
		model.addElement(registry);
		updateButtons();
		app.getEndpointList().setActiveRegistry(getSelectedRegistry());
	}

	public void removeDaemon(DaemonRegistry registry) {
		DefaultListModel model = (DefaultListModel) getModel();
		model.removeElement(registry);
		clearSelection();
		updateButtons();
		app.getEndpointList().setActiveRegistry(getSelectedRegistry());
	}

	public void removeAllDaemons() {
		DefaultListModel model = (DefaultListModel) getModel();
		model.removeAllElements();
		clearSelection();
		updateButtons();
		app.getEndpointList().setActiveRegistry(null);
	}
	
	@Override
    public void valueChanged(ListSelectionEvent e) {
		updateButtons();
		app.getEndpointList().setActiveRegistry(getSelectedRegistry());
    }

	protected void updateButtons() {
		boolean enabled = getSelectedValue() != null;
		app.getToolBar().getButton(Action.REMOVE_DAEMON).setEnabled(enabled);
		app.getToolBar().getButton(Action.REFRESH_DAEMONS).setEnabled(enabled);
		app.getToolBar().getButton(Action.SPAWN).setEnabled(enabled);
		app.getToolBar().getButton(Action.SHUTDOWN_DAEMON).setEnabled(enabled);
		app.getToolBar().getButton(Action.SHUTDOWN_ALL).setEnabled(enabled);
		app.getToolBar().getButton(Action.REFRESH_ENDPOINTS).setEnabled(enabled);
	}
}
