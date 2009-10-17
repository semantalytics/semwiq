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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.jku.semwiq.mediator.util.OntTools;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.OpVisitorBase;
import com.hp.hpl.jena.sparql.algebra.OpWalker;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * @author dorgon
 *
 */
public class TypeDetector {
	protected static final Logger log = LoggerFactory.getLogger(TypeDetector.class);

	protected static final String FAKE_NS = "http://semwiq.faw.uni-linz.ac.at/fakeVar#";
	
	/** TODO: interpret inferTypes */
	private final boolean inferTypes;
	
	private final VocabularyManager vocMgr;
	private final OntModel vocModel;
	private final OntModel infModel;
	
	public TypeDetector(boolean inferTypes, VocabularyManager vocMgr) {
		this.inferTypes = inferTypes;
		this.vocMgr = vocMgr;
		this.vocModel = vocMgr.getVocabularyModel();
		
		if (inferTypes) { // disable
			if (log.isInfoEnabled())
				log.info("Initializing inference model for automatic type detection (this may take some time depending of the size of the global vocabularies)...");
			
			infModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM); //PelletReasonerFactory.THE_SPEC);
			infModel.addSubModel(vocModel);
		} else
			infModel = null;
	}
	
	/**
	 * TODO: should also check the consistency, e.g. if A disjoint B, a query pattern { ?x a A, B } should give a warning!
	 * 
	 * @param op
	 * @return
	 */
	public SubjectTypeCache detectTypes(Op op) {
		FakeModelBuilder b = new FakeModelBuilder();
		OpWalker.walk(op, b);
		OntModel data = b.getModel();
		if (log.isDebugEnabled()) {
			try {
				data.write(new FileOutputStream("fake-graph.n3"), "N3");
			} catch (FileNotFoundException ignore) {}
		}
		
		if (infModel != null) {
			log.info("Detecting types (this may take a while)...");
			infModel.addSubModel(data, true); // TODO rebind really required?
			SubjectTypeCache types = detectTypes(op, b, data, infModel);
			infModel.removeSubModel(data);
			return types;
		} else {
			return detectTypes(op, b, data, data);  
		}
	}

	// TODO wrong behavior: union or intersection?
	private SubjectTypeCache detectTypes(Op op, FakeModelBuilder b, OntModel data, OntModel infModel) {
		// for all subjects
		Resource subject;
		ResIterator subjIter = data.listSubjects();
		SubjectTypeCache types = new SubjectTypeCache();
		
		while (subjIter.hasNext()) {
			subject = subjIter.nextResource();
			
			Set<OntClass> typeSet = new HashSet<OntClass>();
			// bootstrap with rdsf:Resource (will be removed or be left as the most general type)
			typeSet.add(vocMgr.getOntClass(RDFS.Resource.getURI()));
			
			//Set<OntClass> closure = new HashSet<OntClass>();
			OntClass newType;

			// for all types known for subject
			Set<Resource> resTypes = new HashSet<Resource>();
			try {
				// lock the vocabulary sub model
				vocModel.enterCriticalSection(Lock.READ);
				ExtendedIterator typesIter = infModel.getOntResource(subject).listRDFTypes(false);
				while (typesIter.hasNext())
					resTypes.add((Resource) typesIter.next());
			} finally {
				vocModel.leaveCriticalSection();
			}
			
			for (Resource nextType : resTypes) {
				if (!nextType.isURIResource() || nextType.getURI().startsWith(FAKE_NS))
					continue;
				
				// access vocManager so that it will load the complete vocab on demand
				newType = vocMgr.getOntClass(nextType.getURI());
				
				// case 1: newType already entailed, continue with next type
//				if (closure.contains(newType))
//					continue;
				
				// case 2: existing types entailed by newType: remove existing super types and add to closure
//				ExtendedIterator superIt = newType.listSuperClasses();
//				OntClass next;
//				while (superIt.hasNext()) {
//					next = (OntClass) superIt.next();
//					typeSet.remove(next);
//					closure.add(next);
//				}
				
//				closure.add(newType);

				typeSet.add(newType);
			}
				
			// minimize based on ontologies and add
			types.setTypeSet(b.getNodeForUri(subject.asNode()), OntTools.minimizeTypeSet(typeSet));
		}
		

		return types;
	}
	/**
	 * this builds a fake graph according to the graph pattern
	 * if type inference is disabled, this will at least include
	 * all explicitly asserted types ((?s rdf:type ??) triple patterns)
	 * 
	 * @author dorgon
	 *
	 */
	class FakeModelBuilder extends OpVisitorBase {

		/** the data model (constructed analogous to SPARQL query pattern) */
		private final OntModel model;
		
		/** cached back references */
		private final Map<String, Node> uriToSubjectMap;

		public FakeModelBuilder() {
			this.model = ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM);
			this.uriToSubjectMap = new Hashtable<String, Node>();
		}

		/**
		 * @return the model
		 */
		public OntModel getModel() {
			return model;
		}
		
		// TODO check if also works in combination with Join/Union/LeftJoin
		@Override
		public void visit(OpBGP opBGP) {
			Iterator<Triple> it = opBGP.getPattern().iterator();
			Triple t;
			Resource s;
			Property p;
			RDFNode o;
			while(it.hasNext()) {
				t = it.next();
	
				if (t.getSubject().isVariable())
					s = model.createResource(getUriForNode(t.getSubject()));
				else if (t.getSubject().isURI())
					s = model.createResource(t.getSubject().getURI());
				else
					throw new RuntimeException("Invalid node type");
				
				if (t.getPredicate().isVariable())
					p = model.createProperty(getUriForNode(t.getPredicate()));
				else if (t.getPredicate().isURI())
					p = model.createProperty(t.getPredicate().getURI());
				else
					throw new RuntimeException("Invalid node type");
	
				Node prev = t.getObject();
				if (prev.isVariable()) {
					Resource range = null;
					if (t.getPredicate().isURI()) // if p is URI, check range
						range = vocMgr.getOntProperty(t.getPredicate().getURI()).getRange();
					
					if (range != null) {
						RDFDatatype type = TypeMapper.getInstance().getTypeByName(range.getURI());
						if (range.getURI().equals(RDFS.Literal.getURI()))
							o = model.createLiteral((String) createSampleValue(null));
						else if (type == null) // also in case of intentioned plain literal, create a dummy resource
							o = model.createResource(getUriForNode(prev));
						else
							o = model.createTypedLiteral(createSampleValue(type), type);
					} else {
						o = model.createResource(getUriForNode(prev));
					}
				} else if (prev.isURI())
					o = model.createResource(prev.getURI());
				else if (prev.isLiteral())
					o = model.createLiteral(prev.getLiteralLexicalForm(), prev.getLiteralLanguage());	
				else
					throw new RuntimeException("Invalid node type");
				
				model.add(model.createStatement(s, p, o));
			}
		}
		
		public Node getNodeForUri(Node uri) {
			Node node = uriToSubjectMap.get(uri.getURI());
			if (node == null)
				node = uri;
			return node;
		}
		
		public String getUriForNode(Node node) {
			String uri = node.getName();
			if (uri.startsWith("?")) // former bnode
				uri = FAKE_NS + "bnode" + uri.substring(1, uri.length());
			else
				uri = FAKE_NS + uri;
			uriToSubjectMap.put(uri, node);
			return uri;
		}
		
		// TODO improve
		private Object createSampleValue(RDFDatatype type) {
			try {
				if (type.equals(XSDDatatype.XSDdateTime))
					return new XSDDateTime(Calendar.getInstance());
				else
					return type.getJavaClass().newInstance();
			} catch (Exception e1) {
//				log.debug(e1.getMessage(), e1);
				try {
					return type.getJavaClass().cast("");
				} catch (Exception e2) {
//					log.debug(e2.getMessage(), e2);
					return "";
				}
			}
		}
	}
}
