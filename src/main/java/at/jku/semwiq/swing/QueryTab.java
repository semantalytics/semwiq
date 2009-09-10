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
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableModel;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.TrackedNode;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.sparql.util.FmtUtils;

public class QueryTab extends JPanel {
	private static final long serialVersionUID = -135305652119452349L;

	public static final String DEFAULT_QUERY = "SELECT * WHERE {\n\n}\n";

	protected final SwingApp client;

	private JTextArea query;

	private JScrollPane resultsPane;
	private JTextArea resultsText;
	private JTable resultsTable;
	private DefaultTableModel resultsTableModel;
	private Object[] tableHeading; // strings

	private static int tabId = 1;

	public QueryTab(SwingApp client, String defaultQuery, String title) {
		this.client = client;
		
		if (defaultQuery == null)
			defaultQuery = DEFAULT_QUERY;

		if (title == null)
			setName("Query " + tabId++);
		else
			setName(title);
		setBorder(BorderFactory.createEmptyBorder(0, 5, 3, 5));
		Border b = BorderFactory.createBevelBorder(BevelBorder.LOWERED);
		Font f = new Font("Courier", Font.PLAIN, 12);

		query = new JTextArea(client.getConfiguration().getPrefixMappingString() + "\n" + DEFAULT_QUERY);
		query.setLineWrap(false);
		query.setBorder(b);
		query.setFont(f);
		// query.setCaretPosition(query.getText().length()-2);
		JScrollPane scrollQuery = new JScrollPane(query);
		scrollQuery.setBorder(BorderFactory.createEmptyBorder());
		scrollQuery.setPreferredSize(new Dimension(800, 250));

		resultsPane = new JScrollPane();
		resultsPane.setBorder(BorderFactory.createEmptyBorder());

		setLayout(new BorderLayout());
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true,
				scrollQuery, resultsPane);
		splitPane.setBorder(BorderFactory.createEmptyBorder());
		add(splitPane, BorderLayout.CENTER);
		setPreferredSize(new Dimension(800, 450));

		query
				.setText(client.getConfiguration().getPrefixMappingString() + "\n"
						+ defaultQuery);
	}

	public void initResultTextArea(String text) {
		Border b = BorderFactory.createBevelBorder(BevelBorder.LOWERED);
		Font f = new Font("Courier", Font.PLAIN, 12);

		resultsText = new JTextArea();
		resultsText.setLineWrap(false);
		resultsText.setBorder(b);
		resultsText.setFont(f);
		resultsText.setText(text);
		resultsPane.setViewportView(resultsText);
	}

	public String getQuery() {
		return query.getText();
	}

	public String getResults() {
		return resultsText.getText();
	}

	@SuppressWarnings("unchecked")
	public void initResultsTable(List heading) {
		tableHeading = heading.toArray();
		resultsTableModel = new DefaultTableModel();
		resultsTable = new JTable(resultsTableModel);
		resultsPane.setViewportView(resultsTable);
		resultsTableModel.setColumnIdentifiers(heading.toArray());
	}

	/**
	 * first initResultsTable must be called!
	 * 
	 * @param r
	 */
	public void appendResultRow(QuerySolution s, PrefixMapping map) {
		RDFNode n;
		Vector<String> values = new Vector<String>();
		String prefix;

		for (int i = 0; i < tableHeading.length; i++) {
			n = s.get((String) tableHeading[i]);
			if (n == null) {
				values.add(null);
				continue;
			} else if (n instanceof Resource) {
				prefix = map.getNsURIPrefix(((Resource) n).getNameSpace());
				if (prefix == null) {
					values.add("<" + ((Resource) n).getURI() + "> (" + TrackedNode.getProvenanceUri(n.asNode()) + ")");
				}
				else
					values.add(prefix + ":" + ((Resource) n).getLocalName() + " (" + TrackedNode.getProvenanceUri(n.asNode()) + ")");
			} else if (n instanceof Literal) {
				values.add(((Literal) n).getLexicalForm());
			}
		}
		resultsTableModel.addRow(values);
		resultsTable.doLayout();
	}

	/**
	 * first initResultsTextArea must be called!
	 * 
	 * @param r
	 */
	public void setResults(String r) {
		resultsText.setText(r);
	}

	/**
	 * first initResultsTextArea must be called!
	 * 
	 * @param r
	 */
	public void appendResults(String r) {
		resultsText.append(r);
	}

	public void resetTab() {
		query.setText(client.getConfiguration().getPrefixMappingString() + "\n"
				+ DEFAULT_QUERY);
		resultsPane.setViewportView(null);
	}
}
