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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * @author dorgon
 * 
 */
public class LogDialog extends JDialog implements ActionListener {
	private static final long serialVersionUID = -7350805751327398302L;

	protected final JTextArea logWin;
	protected final JButton clearButton;
	protected final JScrollPane scrollLog;

	public LogDialog(JFrame owner) {
		super(owner, "Log Window");

		logWin = new JTextArea();
		logWin.setFont(new Font("Courier", Font.PLAIN, 10));
		logWin.setEditable(false);
		clearButton = new JButton("Clear Log");
		clearButton.setActionCommand("CLEAR");
		clearButton.addActionListener(this);
		clearButton.setToolTipText("Clears the log window.");

		setLayout(new BorderLayout());
		scrollLog = new JScrollPane(logWin);
		scrollLog.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		scrollLog.setPreferredSize(new Dimension(450, 700));
		getContentPane().add(scrollLog, BorderLayout.CENTER);
		getContentPane().add(clearButton, BorderLayout.PAGE_END);
		pack();
	}

	public void msg(String s) {
		logWin.append(s);
		logWin.setCaretPosition(logWin.getText().length() - 1);
	}

	public void clearLog() {
		logWin.setText("");
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("CLEAR"))
			clearLog();
	}
}
