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
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import ch.qos.logback.classic.spi.LoggingEvent;

import at.jku.semwiq.log.LogBuffer;
import at.jku.semwiq.log.LogBufferDispatcher;

/**
 * @author dorgon
 * 
 */
public class LogDialog extends JFrame implements ActionListener {
	private static final long serialVersionUID = -7350805751327398302L;

	protected final JTextArea logWin;
	protected final JButton clearButton;
	protected final JScrollPane scrollLog;
	protected final LogBuffer buf;
	protected final Thread receiver;

	public LogDialog() {
		super("Log Window");
		
		setDefaultLookAndFeelDecorated(true);

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
		scrollLog.setPreferredSize(new Dimension(800, 400));
		getContentPane().add(scrollLog, BorderLayout.CENTER);
		getContentPane().add(clearButton, BorderLayout.PAGE_END);

		buf = LogBufferDispatcher.createLogBuffer();
		receiver = new Thread() {
			@Override
			public void run() {
				while (true) {
					LoggingEvent event;
					try {
						event = buf.nextEvent();
						msg(event.getMessage());
					} catch (InterruptedException e) {
						break;
					}
				}
			}
		};
		receiver.start();
		
		pack();
		setVisible(true);
	}

	private void msg(String msg) {
		logWin.append(msg + "\n");
		logWin.setCaretPosition(logWin.getText().length() - msg.length());
	}
	
	public void clearLog() {
		logWin.setText("");
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("CLEAR"))
			clearLog();
	}

	public void close() {
		receiver.interrupt();
		dispose();
	}

}
