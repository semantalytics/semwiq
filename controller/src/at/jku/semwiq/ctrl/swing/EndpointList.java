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
public class EndpointList extends JList implements ListSelectionListener {
	private static final long serialVersionUID = 3283703920646693456L;

	private final ControllerFrame app;
	private DaemonRegistry activeRegistry;
	
	/**
	 * @param controllerFrame
	 * @param ctrl
	 */
	public EndpointList(ControllerFrame controllerFrame, EndpointController ctrl) {
		this.app = controllerFrame;
		this.activeRegistry = null;
		
		setModel(new DefaultListModel());
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		addListSelectionListener(this);
		
		setVisibleRowCount(-1);
		setPreferredSize(new Dimension(200, app.getHeight()));
		setMinimumSize(new Dimension(200, app.getHeight()));
		setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
	}

	public void addEndpoint(SpawnedEndpointMetadata spec) {
		if (spec.getDaemonRegistry() == activeRegistry) {
			DefaultListModel model = (DefaultListModel) getModel();
			model.addElement(spec);
			updateButtons();
		}
	}

	public void removeEndpoint(SpawnedEndpointMetadata spec) {
		if (spec.getDaemonRegistry() == activeRegistry) {
			DefaultListModel model = (DefaultListModel) getModel();
			model.removeElement(spec);
			clearSelection();
			updateButtons();
		}
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
	 */
	@Override
    public void valueChanged(ListSelectionEvent e) {
		updateButtons();
    }

	protected void updateButtons() {
		app.getToolBar().getButton(Action.KILL).setEnabled(getSelectedValue() != null);
		app.getToolBar().getButton(Action.KILL_ALL).setEnabled(getSelectedValue() != null);
	}
	
	/**
	 * @return
	 */
	private SpawnedEndpointMetadata getSelectedEndpoint() {
		if (getSelectedValue() != null)
			return (SpawnedEndpointMetadata) getSelectedValue();
		else
			return null;
	}

	/**
	 * @param selectedValue
	 */
	public void setActiveRegistry(DaemonRegistry reg) {
		this.activeRegistry = reg;
		DefaultListModel model = new DefaultListModel();
		if (reg != null) {
			try {
				SpawnedEndpointMetadata[] list = app.getController().listEndpoints(reg);
				for (SpawnedEndpointMetadata s : list)
					model.addElement(s);
			} catch (SemWIQControllerException e) {
				app.errorMessage("Failed to populate endpoint list.", e);
			}
		}
		setModel(model);
	}

}
