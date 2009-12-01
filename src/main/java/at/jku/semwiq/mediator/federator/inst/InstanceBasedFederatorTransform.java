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

import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.jku.rdfstats.RDFStatsDataset;
import at.jku.rdfstats.RDFStatsModel;
import at.jku.rdfstats.RDFStatsModelException;
import at.jku.semwiq.mediator.federator.FederatorException;
import at.jku.semwiq.mediator.registry.model.DataSource;
import at.jku.semwiq.mediator.util.OntTools;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_URI;
import com.hp.hpl.jena.graph.Node_Variable;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.OpVars;
import com.hp.hpl.jena.sparql.algebra.TransformCopy;
import com.hp.hpl.jena.sparql.algebra.Transformer;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP;
import com.hp.hpl.jena.sparql.algebra.op.OpDistinct;
import com.hp.hpl.jena.sparql.algebra.op.OpJoin;
import com.hp.hpl.jena.sparql.algebra.op.OpNull;
import com.hp.hpl.jena.sparql.algebra.op.OpService;
import com.hp.hpl.jena.sparql.algebra.op.OpUnion;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * @author dorgon
 *
 * Step 1: splitting BGPs by subject groups into BGP-fragments
 *   where each BGP-fragment is formed based on the original BGP but contains only triples with equal subject
 *
 * Step 2: depending on type of subject: generate union sequence of multiple
 *   a) Service(BGP-fragment) - in case subject is an URI for each describing data source, or
 *   b) Service(more complex sub plan for BGP-fragment) - in case subject is Var or bnode
 *
 */
public class InstanceBasedFederatorTransform extends TransformCopy {
	private static final Logger log = LoggerFactory.getLogger(InstanceBasedFederatorTransform.class);

	public static final boolean DISCARD_SUBPLAN_DUPLICATES = true;
	public static final boolean EXCLUDE_EMPTY_PLANS = false;

	private final InstanceBasedFederator federator;
	private final SubjectTypeCache typeCache;
	private final RDFStatsModel stats;
	
	private final BNodeReplacer bnodeReplacer;
	
//	private OpProject project;
	
	private Hashtable<Node, Set<Triple>> subjGroups;

	/**
	 * @param fed
	 * @param types
	 * @param root
	 * @return transformed root
	 */
	public static Op apply(InstanceBasedFederator fed, SubjectTypeCache types, Op root) {
		return Transformer.transform(new InstanceBasedFederatorTransform(fed, types, root), root);
	}
	
	/**
	 * @param fed
	 * @param types
	 * @param root operator
	 */
	private InstanceBasedFederatorTransform(InstanceBasedFederator fed, SubjectTypeCache types, Op root) {
		this.federator = fed;
		this.typeCache = types;

		this.bnodeReplacer = new BNodeReplacer(OpVars.allVars(root));
		this.stats = federator.getDataSourceRegistry().getRDFStatsModel();
	}

//	public Op finalize() {
//		// don't copy, just set transformed subOp
//		rootDummy.setSubOp(subOp);
//		((AnnotatedOpBase) subOp).setParent(rootDummy);
//		
//		// post processing, project out bnode variables if required
//		bnodeReplacer.finish();
//		if (bnodeReplacer.allSize() > 0) {
//			Collection<Var> excludes = bnodeReplacer.getAllReplacementVars();
//
//			// create a project op with complement of replaced bnode vars
//			if (project == null) {
//				project = (AnnotatedOpProject) AnnotatedOpBuilder.create(new OpProject(null, new ArrayList<Var>()), rootDummy);
//
//				// add all, but projection vars
//				Collection<Var> globalVars = rootDummy.getKnownVariables();
//				for (Var v : globalVars)
//					if (!excludes.contains(v)) project.addProjectVar(v);
//
//				// now place into plan, check for possible distinct and place beyond in this case
//				AnnotatedOp possibleDistinct = rootDummy.getSubOp();
//				// TODO test
//				AnnotatedOp1 op1;
//				if (possibleDistinct instanceof AnnotatedOpDistinct) {
//					op1 = (AnnotatedOpDistinct) possibleDistinct;
//				} else {
//					op1 = rootDummy;
//				}
//
//				AnnotatedOpBase finalSub = (AnnotatedOpBase) op1.getSubOp();
//				project.setSubOp(finalSub);
//				project.setParent(op1);
//				op1.setSubOp(project);
//				finalSub.setParent(project);
//
//			} else {
//				// ensure there is not some of the "hidden" vars in the project (may only occur upon misspelled queries, but would lead to unexpected results)
//				for (Var v : excludes)
//					project.removeProjectVar(v);
//			}
//		}
//		
//		return rootDummy;
//	}

//	/* (non-Javadoc)
//	 * @see at.faw.semwiq.mediator.optimizer.transform.AnnotatedTransformCopy#transform(at.faw.semwiq.mediator.optimizer.op.AnnotatedOpProject, at.faw.semwiq.mediator.optimizer.op.AnnotatedOp)
//	 */
//	@Override
//	public Op transform(OpProject opProject, Op subOp) {
//		project = opProject; // remember already existing global projection
//		return super.transform(opProject, subOp);
//	}
	
	@Override
	public Op transform(OpBGP opBGP) throws RuntimeException {
		try {
			// set this.subjGroups
			createSubjectGroups(opBGP);
	
			Op prevGroupOp = null;
			
			// iterate over subject groups
			Iterator<Node> i = subjGroups.keySet().iterator();
			Node subj;
			Set<OntClass> typeSet;
			List<DataSource> dataSources;
			OpService newDS;
			Op prevUnionOp;
			
			while (i.hasNext()) {
				subj = i.next();

				prevUnionOp = null;
				typeSet = typeCache.getTypeSet(subj);
				
				// if subject is a URI node
				if (subj instanceof Node_URI) {
					dataSources = federator.getDataSourceRegistry().getAvailableDescribingDataSources(subj.getURI());
					log.debug("data source(s) describing <" + subj + ">: " + dataSources);
					
					bnodeReplacer.startNewLogicalBGP();
					OpBGP newBGP = createBGP(toBasicPattern(subjGroups.get(subj)), typeSet, false);
					
					// include unions over all sites describing the instance regardless of its typeSet:
					// union over (serviceA(BGP), serviceB(BGP), ...)
					for (DataSource ds : dataSources) {
						newDS = new OpService(Node.createURI(ds.getSPARQLEndpointURL()), newBGP.copy());
						prevUnionOp = (prevUnionOp == null) ? newDS : new OpUnion(prevUnionOp, newDS);												
					}
					
				} else if (subj instanceof Node_Variable) {
					log.debug("type set of subject " + subj + ": " + typeSet + "\n");
					
					// look for sites storing instances of determined types, create union over all sites
					Op subPlan;
					for (DataSource ds : federator.getDataSourceRegistry().getAvailableDataSources()) {
						subPlan = createSubPlan(ds, subj, typeSet);

						// if valid sub plan
						if (subPlan != null) {
							newDS = new OpService(Node.createURI(ds.getSPARQLEndpointURL()), subPlan); // in the simplest case this returns just an OpBGP

//							if (!EXCLUDE_EMPTY_PLANS || subPlan.getMaxResults() > 0) {
								// if it will produce any results at all, include into global plan
								if (prevUnionOp == null)
									prevUnionOp = newDS;
								else {
									OpUnion union = new OpUnion(prevUnionOp, newDS);
									prevUnionOp = union; //TODO improve code
								}
//							}
						}
					}
				}

				// already break and return OpNull if one of the join plans has no results
				if (prevUnionOp == null)
					return OpNull.create();
				
				// join over subject groups
				if (prevGroupOp == null)
					prevGroupOp = prevUnionOp;
				else
					prevGroupOp = OpJoin.create(prevGroupOp, prevUnionOp);
			}

			return prevGroupOp;

		} catch (Exception e) {
			throw new RuntimeException("Failed to federate: "
					+ e.getMessage(), e);
		}
	}

	/**
	 * create a new BGP with bnode labels that are unique within
	 * this BGP and cannot occur twice in other BGPs in the plan
	 * 
	 * @param basicPattern
	 * @return
	 */
	private OpBGP createBGP(BasicPattern basicPattern, Set<OntClass> typeSet, boolean replaceBnodeVars) {
		BasicPattern newPattern = new BasicPattern();
		Iterator<Triple> it = basicPattern.iterator();
		Triple t;
		Node s, p, o;
		
		if (replaceBnodeVars) 
			bnodeReplacer.startNewBGP();
		
		while (it.hasNext()) {
			t = it.next();
			s = t.getSubject();
			p = t.getPredicate();
			o = t.getObject();
			
			// continue with next if property is rdf:type and object is not a variable and object is not in typeSet
			// (means it's already subsumed by another type in typeSet)
			if (p.isURI() && p.getURI() == RDF.type.getURI() && (!o.isVariable() && !typeSet.contains(federator.getVocabularyManager().getOntClass(o.getURI()))))
				continue;
			
			// usually generateVars in SPARQLParser's LabelToNodeMap is set to true and the SPARQL parser generates variables
			// with double quotes for bnodes like "??0" - getName() strips one "?" so, a former bnode name still starts with "?"
			
			if (replaceBnodeVars) {
				if (s.isVariable() && s.getName().startsWith("?")) {
					s = bnodeReplacer.replaceSubjectVar((Var) s);
				} if (p.isVariable() && p.getName().startsWith("?"))
					p = bnodeReplacer.replaceVar((Var) p); // p is actually never blank
				if (o.isVariable() && o.getName().startsWith("?"))
					o = bnodeReplacer.replaceVar((Var) o);
			}
			
			newPattern.add(new Triple(s, p, o)); // replace triple
		}
		
		// TODO check if BGP has typing triples
		OpBGP bgp = new OpBGP(newPattern);
		return bgp;
	}

	/**
	 * @param set
	 * @return
	 */
	private BasicPattern toBasicPattern(Set<Triple> set) {
		BasicPattern p = new BasicPattern();
		for (Triple t : set)
			p.add(t);
		return p;
	}

	/**
	 * Generates a sub-plan for data source ds, if it stores instances matching all types in typeSet.
	 * i.e. a data source is only interesting, if it has at least one instance of at least one sub-class of each type in typeSet
	 * 
	 * If a data source is capable of subsumption reasoning natively, we have to look also for instances of the sub-class hierarchy for each type.
	 * Similarly, if we want to "simulate" subsumption reasoning based on distincted union sequence bindings, we have to look at sub-class hierarchies.
	 * 
	 * @param ds the data source
	 * @param subj current subject of the BGP which will be obtained by subjGroups.get(subj)
	 * @param typeSet the set of must-match types A, B, C, ... for the current subject
	 * @return OpBGP or a more complex sub plan
	 * @throws FederatorException 
	 */
	private Op createSubPlan(DataSource ds, Node subj, Set<OntClass> typeSet) throws FederatorException {
		try {		
			// subsumption result? (either by ds itself or union-plan) -> query RDFStats for sub-class hierarchies for each type cl in typeSet
			if (federator.isSubsumption() || ds.hasSubsumption()) {
	
				Map<OntClass, Set<OntClass>> subTypes = new Hashtable<OntClass, Set<OntClass>>();
				Set<OntClass> sub;
				boolean exactMatch = true;
				
				for (OntClass cl : typeSet) {
					sub = new HashSet<OntClass>();
					collectInterestingSubTypes(ds, cl, sub);
					
					// already break if one class-set of typeSet is missing at all to include this data source
					if (sub.size() == 0)
						return null;
					else
						subTypes.put(cl, sub);
		
					if (!sub.contains(cl))
						exactMatch = false; // no exact match, will have to split BGP and combine
				}
	
				// if data source does subsumption itself or if we have an exact match for typeSet, create a simple BGP
				if (ds.hasSubsumption() || exactMatch)
					return createSimplePlan(subj, typeSet);
				
				// create distincted union sequence bindings otherwise
				else
					return createSubsumptionPlan(ds, subj, subTypes);
				
			// no subsumption enabled in config && data source does no subsumption itself, only check RDFStats for top classes cl in typeSet
			} else {
					for (OntClass cl : typeSet) {
						RDFStatsDataset statsDs = stats.getDataset(ds.getSPARQLEndpointURL());
						if (statsDs == null)
							return null;
						else if (statsDs.triplesForPattern(null, RDF.type.asNode(), cl.asNode()) <= 0)
							return null;
					}
					return createSimplePlan(subj, typeSet);
			}
		} catch (RDFStatsModelException e) {
			throw new FederatorException("Failed to create a sub plan for " + ds.toString() + ", subject '" + subj + "'.", e);
		}
	}

	private Op createSimplePlan(Node subj, Set<OntClass> typeSet) {
		Set<Triple> p = subjGroups.get(subj);
		// update subjGroups and ensure BGP contains current typeSet (required for leftjoin over detached BGP parts, etc.)
		for (OntClass cl : typeSet)
			p.add(new Triple(subj, RDF.type.asNode(), cl.asNode()));
		
		bnodeReplacer.startNewLogicalBGP();
		return createBGP(toBasicPattern(p), typeSet, false);
	}
	
	/** create a union sequence for each sub-types combination
	 * 
	 * @param ds //TODO not needed
	 * @param subj
	 * @param subTypes
	 * @return
	 */
	private Op createSubsumptionPlan(DataSource ds, Node subj, Map<OntClass, Set<OntClass>> subTypes) {
		// first create a plain BGP pattern without any typing triples (will be inserted accordingly for each generated BGP)
		BasicPattern withoutTypes = new BasicPattern();
		Iterator<Triple> it = subjGroups.get(subj).iterator();
		Triple t;
		while (it.hasNext()) {
			t = it.next();
			if (!(t.getPredicate().isURI() && t.getPredicate().getURI() == RDF.type.getURI() && !t.getObject().isVariable()))
				withoutTypes.add(t);
		}

		List<Set<OntClass>> combinations = OntTools.generateTypeCombinations(subTypes);
		
		Op prevUnion = null;
		BasicPattern p;
		
		bnodeReplacer.startNewLogicalBGP();
		for (Set<OntClass> subSet : combinations) {
			p = new BasicPattern();
			for (OntClass cl : subSet)
				p.add(new Triple(subj, RDF.type.asNode(), cl.asNode()));
			p.addAll(withoutTypes);
			
			Op newBGP = createBGP(p, subSet, true);
			if (prevUnion == null)
				prevUnion = newBGP;
			else
				prevUnion = new OpUnion(prevUnion, newBGP);
		}
		
		if (prevUnion == null)
			return null;
		
		if (combinations.size() > 1 && DISCARD_SUBPLAN_DUPLICATES) {
			return new OpDistinct(prevUnion);
		} else
			return prevUnion;
	}
	
	/** returns the set of interesting sub classes for cl of data source ds
	 * 
	 * @param ds
	 * @param cl some class
	 * @param accu accumulating list of classes
	 * 
	 * TODO: detect cycles
	 * @throws RDFStatsModelException 
	 */
	private void collectInterestingSubTypes(DataSource ds, OntClass cl, Set<OntClass> accu) throws RDFStatsModelException {
		RDFStatsDataset statsDs = stats.getDataset(ds.getSPARQLEndpointURL());
		if (statsDs != null && statsDs.triplesForPattern(null, RDF.type.asNode(), cl.asNode()) > 0) {
			accu.add(cl);
			
		} else { // decend further
			ExtendedIterator nextLevel = cl.listSubClasses(true);
			while (nextLevel.hasNext())
				collectInterestingSubTypes(ds, (OntClass) nextLevel.next(), accu);			
		}
	}
	
	/** re-group BGP's triple patterns according to equal subject nodes (may be variable or also URI/bnode) */
	private void createSubjectGroups(OpBGP opBGP) {
		subjGroups = new Hashtable<Node, Set<Triple>>();
		Node subj;
		Triple t;
		
		// re-group triples according to equal subjects
		Iterator<Triple> it = opBGP.getPattern().iterator();
		while (it.hasNext()) {
			t = it.next();
			subj = t.getSubject();

			// get triples for subj, create BasicPattern for subj if not exists yet
			Set<Triple> triples = subjGroups.get(subj);
			if (triples == null) {
				triples = new HashSet<Triple>();
				subjGroups.put(subj, triples);
			}

			// add triple to list
			triples.add(t);
		}
	}

	public class BNodeReplacer {
		
		private Set<Var> replacementVars = new HashSet<Var>();
		private Map<Var, Var> bnodeToVarSubject = new Hashtable<Var, Var>();
		private Map<Var, Var> bnodeToVarBGP = new Hashtable<Var, Var>();

		/** set of query variables */
		private final Set<Var> globalVars;
		
		/** sequence */
		private int sequence = 0;
		private int subjectSeq = 0;
		
		public BNodeReplacer(Set<Var> globalVars) {
			this.globalVars = globalVars;
		}
		
		public void finish() {
			// add the rest of BGP vars to replacementVars
			startNewLogicalBGP();
		}
		
		public int allSize() {
			return replacementVars.size();
		}
		
		public void startNewBGP() {
			replacementVars.addAll(bnodeToVarBGP.values());
			bnodeToVarBGP = new Hashtable<Var, Var>();
		}
		
		public void startNewLogicalBGP() {
			startNewBGP();
			replacementVars.addAll(bnodeToVarSubject.values());
			bnodeToVarSubject = new Hashtable<Var, Var>();
		}
		
		/**
		 * @return the collection of replacement vars
		 */
		public Collection<Var> getAllReplacementVars() {
			return replacementVars;
		}
		
		public Var replaceSubjectVar(Var bnodeVar) {
			Var exists = bnodeToVarSubject.get(bnodeVar);
			if (exists != null)
				return exists;
			
			Var replacement;
			// assure replacement name is not already used
			do replacement = Var.alloc("sb" + subjectSeq++);
			while (globalVars.contains(replacement));

			bnodeToVarSubject.put(bnodeVar, replacement);
			return replacement;
		}
		
		/** get replacement Var for bnode Var for current BGP
		 * 
		 * @param bnodeVar variable of a former bnode
		 * @return replacement real var
		 */
		public Var replaceVar(Var bnodeVar) {
			Var exists = bnodeToVarBGP.get(bnodeVar);
			if (exists != null)
				return exists;
			
			Var replacement;
			// assure replacement name is not already used
			do replacement = Var.alloc("b" + sequence++);
			while (globalVars.contains(replacement));

			bnodeToVarBGP.put(bnodeVar, replacement);
			return replacement;
		}
	}
	
}