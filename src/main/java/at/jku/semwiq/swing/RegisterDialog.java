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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.jku.semwiq.mediator.registry.model.MonitoringProfile;

/**
 * @author dorgon, Andreas Langegger, al@jku.at
 *
 * register data source by URI and MonitoringProfile
 */
public class RegisterDialog extends JComponent implements ActionListener {
	private static final Logger log = LoggerFactory.getLogger(RegisterDialog.class);
	private static final String CANCEL = "cancel";
	private static final String REGISTER = "register";
	
	private final SwingApp app;
	private final List<MonitoringProfile> profList;
	
	private JTextField urlBox;
	private JList profiles;
	private JDialog dialog;
	private JButton ok, cancel;
	
	/** return values */
	private boolean returnValue = false;
	
	public RegisterDialog(SwingApp app, List<MonitoringProfile> profList) {
		this.app = app;
		this.profList = profList;
	}

	public MonitoringProfile getMonitoringProfile() {
		return (MonitoringProfile) profiles.getSelectedValue();
	}
	
	public String getSparqlEndpoint() {
		return urlBox.getText();
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
//		if (cmd.equals(CHOOSE)) {
//			JFileChooser fj = new JFileChooser();
//			fj.setName("Please select a file containing RDF data");
//			fj.setFileFilter(new FileFilter() {
//				@Override
//				public boolean accept(File f) {
//					String name = f.getName().toLowerCase();
//					return (name.endsWith(".n3") || name.endsWith(".ttl") || name.endsWith(".rdf") || name.endsWith(".xml"));
//				}
//
//				@Override
//				public String getDescription() {
//					return "RDF files (.n3 .ttl .rdf .xml)";
//				}
//			});
//			
//			int returnVal = fj.showOpenDialog(this);
//			if (returnVal == JFileChooser.APPROVE_OPTION) {
//				String fileName = null;
//				try {
//					selectedFile = fj.getSelectedFile();
//					file.setText(selectedFile.getName());
//				} catch (Exception ex) {
//					log.error("Error loading data source descriptions from '" + fileName + "'.", ex);
//				}
//			}
		if (cmd.equals(REGISTER)) {
			try {
				if (urlBox.getText() == null || urlBox.getText().length() == 0)
					JOptionPane.showMessageDialog(dialog, "Please sepecify a SPARQL endpoint URI.");
				else if (profiles.getSelectedValue() == null) {
					JOptionPane.showMessageDialog(dialog, "Please select a monitoring profile.");
				} else {
					returnValue = true;
					dialog.dispose();
				}
			} catch (Exception other) {
				JOptionPane.showMessageDialog(dialog, other.getMessage());
			}
		} else if (cmd.equals(CANCEL)) {
			returnValue = false;
			dialog.dispose();
		}
	}
	
	/**
	 * 
	 */
	public boolean showDialog() {
		if (SwingUtilities.isEventDispatchThread()) {
			return privateShowDialog();
		}
		
		final boolean[] returnValue = new boolean[1];
		
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					returnValue[0] = privateShowDialog();
				}
			});
		} catch (java.lang.reflect.InvocationTargetException ex) {
		} catch (java.lang.InterruptedException ex) {
		}
		
		return returnValue[0];
	}

	/**
	 * @return
	 */
	private boolean privateShowDialog() {
		urlBox = new JTextField();
		Container top = new Container();
		top.setLayout(new BoxLayout(top, BoxLayout.LINE_AXIS));
		top.add(new JLabel("Sparql Endpoint URI:"));
		top.add(urlBox, BorderLayout.LINE_END);
		
		profiles = new JList();
		profiles.setListData(profList.toArray());		
		JScrollPane profilesScroller = new JScrollPane(profiles);

		ok = new JButton("Register");
		ok.setActionCommand(REGISTER);
		ok.addActionListener(this);
		cancel = new JButton("Cancel");
		cancel.setActionCommand(CANCEL);
		cancel.addActionListener(this);
		Container buttons = new Container();
		buttons.setLayout(new BoxLayout(buttons, BoxLayout.LINE_AXIS));
		buttons.add(ok);
		buttons.add(cancel);

		dialog = new JDialog(app, "Register Data Source", true);	
        Container contentPane = dialog.getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(top, BorderLayout.PAGE_START);
        contentPane.add(profilesScroller, BorderLayout.CENTER);
        contentPane.add(buttons, BorderLayout.PAGE_END);
        
        dialog.pack();
        dialog.setLocationRelativeTo(app);
    	dialog.addWindowListener(new WindowAdapter() {
    	    public void windowClosing(WindowEvent e) {
    	    	returnValue = false;
    	    }
    	});

    	dialog.setVisible(true);
    	dialog.removeAll();
    	dialog.dispose();
    	dialog = null;
    	return returnValue;
	}

	
}
