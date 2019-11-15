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

package at.jku.semwiq.mediator.federator.inst;

import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.shared.Lock;

/**
 * @author dorgon
 * 
 */
public class VocabularyManagerImpl implements VocabularyManager {
	private static final Logger log = LoggerFactory.getLogger(VocabularyManagerImpl.class);

	/**
	 * Cache for global vocabularies TODO: currently grows without boundary, needs global voc mgmt
	 * different queries... explicit flushing/garbage collection necessary
	 */
	private OntModel ontModel;
	
	/** vocabularies flagged as bad */
	private final Set<String> badVocabNS;
	
	/** vocabularies successfully loaded */
	private final Set<String> goodVocabNS;
	
	public VocabularyManagerImpl() {
		this.ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_RDFS_INF);
		this.badVocabNS = new HashSet<String>();
		this.goodVocabNS = new HashSet<String>();
	}

	public void close() {
		if (ontModel != null)
			ontModel.close();
	}

	/* (non-Javadoc)
	 * @see at.faw.semwiq.mediator.registry.vocman.VocabularyManager#getVocabularyModel()
	 */
	public OntModel getVocabularyModel() {
		return ontModel;
	}
	
	public void loadVocabulary(String uri) {
		if (badVocabNS.contains(uri)) {
			log.warn("Vocabulary <" + uri + "> is marked as bad, loading skipped.");
			return;
		}
		
		ontModel.enterCriticalSection(Lock.WRITE);
		try {
			ontModel.read(uri);
			goodVocabNS.add(uri);
			if (log.isInfoEnabled())
					log.info("Vocabulary loaded for <" + uri + ">.");
		} catch (Exception e) {
			badVocabNS.add(uri);
			if (log.isDebugEnabled())
				log.warn("Error loading vocabulary for <" + uri + ">.", e);
			else
				log.warn("Error loading vocabulary for <" + uri + ">.");
		} finally {
			ontModel.leaveCriticalSection();
		}
	}

	public OntClass getOntClass(String uri) {
		OntClass c = getClassFromModel(uri);
		if (c != null)
			return c;

		// try once to read from the Web by base URI if not already successfully loaded
		String vocabUri = detectBaseUri(uri);
		if (!goodVocabNS.contains(vocabUri)) {
			log.debug("Trying to read vocabulary <" + uri + "> from the Web...");
			loadVocabulary(vocabUri);
			
			c = getClassFromModel(uri);
			if (c != null)
				return c;
		}
		
		// still not found - if vocab loaded this may indicate wrong class in data source
		if (goodVocabNS.contains(vocabUri)) {
			log.warn("*** WARNING! *** Class  <" + uri + "> not found in definitions loaded from <" + vocabUri + ">. The mediator may produce unexpected query results!");
		} else {
			log.info("Creating dummy class representation for  <" + uri + ">. Note that missing vocabulary definitions may lead to unexpected query results!");				
		}
		return createDummyClass(uri);
	}

	public OntProperty getOntProperty(String uri) {
		OntProperty p = getPropertyFromModel(uri);
		if (p != null)
			return p;

		// try to read from the Web by base URI if not already successfully loaded...
		String vocabUri = detectBaseUri(uri);
		if (!goodVocabNS.contains(vocabUri)) {
			log.debug("Trying to read vocabulary <" + uri + "> from the Web...");
			loadVocabulary(detectBaseUri(uri));
			
			p = getPropertyFromModel(uri);
			if (p != null)
				return p;
		}
	
		// still not found - if vocab loaded this may indicate wrong class in data source
		if (goodVocabNS.contains(vocabUri)) {
			log.warn("*** WARNING! *** Property <" + uri + "> not found in definitions loaded from <" + vocabUri + ">. The mediator may produce unexpected query results!");
		} else {
			log.info("Creating dummy property representation for  <" + uri + ">. Note that missing vocabulary definitions may lead to unexpected query results!");				
		}

		return createDummyProperty(uri);
	}

	private OntClass getClassFromModel(String uri) {
		ontModel.enterCriticalSection(Lock.READ);
		try {
			return ontModel.getOntClass(uri);
		} finally {
			ontModel.leaveCriticalSection();
		}
	}
	
	private OntClass createDummyClass(String uri) {
		ontModel.enterCriticalSection(Lock.WRITE);
		try {
			return ontModel.createClass(uri);
		} finally {
			ontModel.leaveCriticalSection();
		}
	}

	private OntProperty getPropertyFromModel(String uri) {
		ontModel.enterCriticalSection(Lock.READ);
		try {
			return ontModel.getOntProperty(uri);
		} finally {
			ontModel.leaveCriticalSection();
		}
	}
	
	private OntProperty createDummyProperty(String uri) {
		ontModel.enterCriticalSection(Lock.WRITE);
		try {
			return ontModel.createOntProperty(uri);
		} finally {
			ontModel.leaveCriticalSection();
		}
	}

	private String detectBaseUri(String conceptUri) {
		int anchor = conceptUri.indexOf('#');
		if (anchor > 0)
			return conceptUri.substring(0, anchor);
		
		int slash = conceptUri.lastIndexOf('/');
		if (slash > 0)
			return conceptUri.substring(0, slash + 1);

		return conceptUri;
	}
	
	public void write(OutputStream outputStream) {
		ontModel.enterCriticalSection(Lock.READ);
		try {
			ontModel.write(outputStream);
		} finally {
			ontModel.leaveCriticalSection();
		}
	}
}
