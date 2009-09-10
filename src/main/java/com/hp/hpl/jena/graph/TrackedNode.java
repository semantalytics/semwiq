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
package com.hp.hpl.jena.graph;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeVisitor;
import com.hp.hpl.jena.graph.impl.LiteralLabel;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.PrefixMapping;

/**
 * @author dorgon, Andreas Langegger, al@jku.at
 *
 */
public class TrackedNode extends Node {
	private final Node wrapped;
	private final String source;
	
	/**
	 * provenance tracking of nodes
	 */
	public TrackedNode(Node n, String source) {
		super(n.label);
		this.wrapped = n;
		this.source = source;
	}

	public String getProvenanceUri() {
		return source;
	}
	
	public static String getProvenanceUri(Node n) {
		if (n instanceof TrackedNode)
			return ((TrackedNode) n).getProvenanceUri();
		else return null;
	}
	
	/**
	 * @param o
	 * @return
	 * @see com.hp.hpl.jena.graph.Node#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		return wrapped.equals(o);
	}

	/**
	 * @return
	 * @see com.hp.hpl.jena.graph.Node#getBlankNodeId()
	 */
	public AnonId getBlankNodeId() {
		return wrapped.getBlankNodeId();
	}

	/**
	 * @return
	 * @see com.hp.hpl.jena.graph.Node#getBlankNodeLabel()
	 */
	public String getBlankNodeLabel() {
		return wrapped.getBlankNodeLabel();
	}

	/**
	 * @return
	 * @see com.hp.hpl.jena.graph.Node#getIndexingValue()
	 */
	public Object getIndexingValue() {
		return wrapped.getIndexingValue();
	}

	/**
	 * @return
	 * @see com.hp.hpl.jena.graph.Node#getLiteral()
	 */
	public LiteralLabel getLiteral() {
		return wrapped.getLiteral();
	}

	/**
	 * @return
	 * @see com.hp.hpl.jena.graph.Node#getLiteralDatatype()
	 */
	public RDFDatatype getLiteralDatatype() {
		return wrapped.getLiteralDatatype();
	}

	/**
	 * @return
	 * @see com.hp.hpl.jena.graph.Node#getLiteralDatatypeURI()
	 */
	public String getLiteralDatatypeURI() {
		return wrapped.getLiteralDatatypeURI();
	}

	/**
	 * @return
	 * @see com.hp.hpl.jena.graph.Node#getLiteralIsXML()
	 */
	public boolean getLiteralIsXML() {
		return wrapped.getLiteralIsXML();
	}

	/**
	 * @return
	 * @see com.hp.hpl.jena.graph.Node#getLiteralLanguage()
	 */
	public String getLiteralLanguage() {
		return wrapped.getLiteralLanguage();
	}

	/**
	 * @return
	 * @see com.hp.hpl.jena.graph.Node#getLiteralLexicalForm()
	 */
	public String getLiteralLexicalForm() {
		return wrapped.getLiteralLexicalForm();
	}

	/**
	 * @return
	 * @see com.hp.hpl.jena.graph.Node#getLiteralValue()
	 */
	public Object getLiteralValue() {
		return wrapped.getLiteralValue();
	}

	/**
	 * @return
	 * @see com.hp.hpl.jena.graph.Node#getLocalName()
	 */
	public String getLocalName() {
		return wrapped.getLocalName();
	}

	/**
	 * @return
	 * @see com.hp.hpl.jena.graph.Node#getName()
	 */
	public String getName() {
		return wrapped.getName();
	}

	/**
	 * @return
	 * @see com.hp.hpl.jena.graph.Node#getNameSpace()
	 */
	public String getNameSpace() {
		return wrapped.getNameSpace();
	}

	/**
	 * @return
	 * @see com.hp.hpl.jena.graph.Node#getURI()
	 */
	public String getURI() {
		return wrapped.getURI();
	}

	/**
	 * @return
	 * @see com.hp.hpl.jena.graph.Node#hashCode()
	 */
	public int hashCode() {
		return wrapped.hashCode();
	}

	/**
	 * @param uri
	 * @return
	 * @see com.hp.hpl.jena.graph.Node#hasURI(java.lang.String)
	 */
	public boolean hasURI(String uri) {
		return wrapped.hasURI(uri);
	}

	/**
	 * @return
	 * @see com.hp.hpl.jena.graph.Node#isBlank()
	 */
	public boolean isBlank() {
		return wrapped.isBlank();
	}

	/**
	 * @return
	 * @see com.hp.hpl.jena.graph.Node#isConcrete()
	 */
	public boolean isConcrete() {
		return wrapped.isConcrete();
	}

	/**
	 * @return
	 * @see com.hp.hpl.jena.graph.Node#isLiteral()
	 */
	public boolean isLiteral() {
		return wrapped.isLiteral();
	}

	/**
	 * @return
	 * @see com.hp.hpl.jena.graph.Node#isURI()
	 */
	public boolean isURI() {
		return wrapped.isURI();
	}

	/**
	 * @return
	 * @see com.hp.hpl.jena.graph.Node#isVariable()
	 */
	public boolean isVariable() {
		return wrapped.isVariable();
	}

	/**
	 * @param other
	 * @return
	 * @see com.hp.hpl.jena.graph.Node#matches(com.hp.hpl.jena.graph.Node)
	 */
	public boolean matches(Node other) {
		return wrapped.matches(other);
	}

	/**
	 * @param o
	 * @return
	 * @see com.hp.hpl.jena.graph.Node#sameValueAs(java.lang.Object)
	 */
	public boolean sameValueAs(Object o) {
		return wrapped.sameValueAs(o);
	}

	/**
	 * @return
	 * @see com.hp.hpl.jena.graph.Node#toString()
	 */
	public String toString() {
		return wrapped.toString();
	}

	/**
	 * @param quoting
	 * @return
	 * @see com.hp.hpl.jena.graph.Node#toString(boolean)
	 */
	public String toString(boolean quoting) {
		return wrapped.toString(quoting);
	}

	/**
	 * @param pm
	 * @param quoting
	 * @return
	 * @see com.hp.hpl.jena.graph.Node#toString(com.hp.hpl.jena.shared.PrefixMapping, boolean)
	 */
	public String toString(PrefixMapping pm, boolean quoting) {
		return wrapped.toString(pm, quoting);
	}

	/**
	 * @param pm
	 * @return
	 * @see com.hp.hpl.jena.graph.Node#toString(com.hp.hpl.jena.shared.PrefixMapping)
	 */
	public String toString(PrefixMapping pm) {
		return wrapped.toString(pm);
	}

	/**
	 * @param v
	 * @return
	 * @see com.hp.hpl.jena.graph.Node#visitWith(com.hp.hpl.jena.graph.NodeVisitor)
	 */
	public Object visitWith(NodeVisitor v) {
		return wrapped.visitWith(v);
	}
	
}
