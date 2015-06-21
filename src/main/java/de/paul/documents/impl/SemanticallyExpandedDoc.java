package de.paul.documents.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.paul.annotations.AncestorAnnotation;
import de.paul.annotations.Annotatable;
import de.paul.annotations.Category;
import de.paul.annotations.NeighborhoodAnnotation;
import de.paul.annotations.SemanticallyExpandedAnnotation;
import de.paul.documents.AnnotatedDoc;
import de.paul.documents.ElasticSearchSerializableDoc;
import de.paul.kb.dbpedia.DBPediaHandler;
import de.paul.kb.dbpedia.categories.WikiCatHierarchyHandler;
import de.paul.util.CombineMode;
import de.paul.util.Directionality;
import de.paul.util.MapUtil;
import de.paul.util.SumMap;

public class SemanticallyExpandedDoc extends TaxonomicExpandedDoc implements
		ElasticSearchSerializableDoc {

	// Category hierarchy has depth 15
	private static final int MAX_HIERARCHY_DEPTH = 15;
	private HashMap<String, String> fieldMap = null;
	private CombineMode combineMode = CombineMode.PLUS;

	private Directionality edgeDirMode = Directionality.OUTGOING;

	public SemanticallyExpandedDoc(SemanticallyExpandedDoc copy) {

		super(copy);
		this.combineMode = copy.combineMode;
		this.edgeDirMode = copy.edgeDirMode;
	}

	// public SemanticallyExpandedDoc(AnnotatedDoc doc, int expansion_radius,
	// DBPediaHandler dbpHandler, WikiCatHierarchyHandler hierHandler) {
	// // Directionality edgeDirMode) {
	//
	// // super constructor creates taxonomical annotations and adds them.
	// // adding is overwritten though (addAnnotation) such that a fully
	// // expanded
	// // annotation is added, with the transversal part set to NULL
	// super(doc, dbpHandler, hierHandler);
	// this.edgeDirMode = edgeDirMode;
	// addTransversal(expansion_radius, dbpHandler);
	// }

	private void addTransversal(int expansion_radius, DBPediaHandler dbpHandler) {
		// add transversal part
		for (Annotatable ann : annotations) {
			String ent = ann.getEntity();
			double weight = ann.getWeight();
			NeighborhoodAnnotation neighAnn = new NeighborhoodAnnotation(ent,
					weight, dbpHandler, expansion_radius, edgeDirMode);
			// set neighborhood annotation
			((SemanticallyExpandedAnnotation) ann).setCombineMode(combineMode);
			((SemanticallyExpandedAnnotation) ann).setNeighbors(neighAnn);
		}
	}

	public SemanticallyExpandedDoc(AnnotatedDoc doc, int expansion_radius,
			DBPediaHandler dbpHandler, WikiCatHierarchyHandler hierHandler,
			CombineMode combineMode, Directionality edgeMode) {

		super(doc, dbpHandler, hierHandler);
		if (combineMode != null)
			this.combineMode = combineMode;
		if (edgeMode != null)
			this.edgeDirMode = edgeMode;
		addTransversal(expansion_radius, dbpHandler);
	}

	public SemanticallyExpandedDoc(String docId, String title, String text,
			List<Annotatable> annotations) {

		super(docId, title, text, annotations);
	}

	public void addAnnotation(Annotatable ann) {

		if (ann instanceof AncestorAnnotation) {
			SemanticallyExpandedAnnotation fullAnnot = null;
			try {
				fullAnnot = new SemanticallyExpandedAnnotation(
						(AncestorAnnotation) ann, null, combineMode);
			} catch (Exception e) {
				// can't be thrown here.
				e.printStackTrace();
			}
			annotations.add(fullAnnot);
		} else if (ann instanceof SemanticallyExpandedAnnotation)
			annotations.add(((SemanticallyExpandedAnnotation) ann).copy());
	}

	@Override
	public SemanticallyExpandedDoc copy() {

		return new SemanticallyExpandedDoc(this);
	}

	public Map<String, String> getElasticSearchFields() {

		if (fieldMap == null) {
			fieldMap = new HashMap<String, String>();
			fieldMap.put("title", this.title);
			fieldMap.put("text", this.text);
			// fieldMap.put("annotations",
			// WeightedAnnotation.produceString(this.annotations));
			fieldMap.put("categories", MapUtil.stringifyMap(
					this.getAllCategories(), ES_WORD_STRETCH_FACTOR));
			fieldMap.put("neighbors", MapUtil.stringifyMap(
					this.getAllNeighors(), ES_WORD_STRETCH_FACTOR));
		}
		return fieldMap;
	}

	/*
	 * Collect all annotations' neighboring entities in one map, from name to
	 * weight.
	 */
	private Map<String, Double> getAllNeighors() {

		SumMap<String> neiMap = new SumMap<String>();
		// for each annotation
		for (Annotatable ann : annotations) {

			NeighborhoodAnnotation neighbor = ((SemanticallyExpandedAnnotation) ann)
					.getNeighbors();
			// add annotation itself to map
			neiMap.inc(neighbor.getEntity(), neighbor.getWeight());
			// add annotations' semantically related entities to map
			for (Annotatable n : neighbor.getNeighbors()) {

				neiMap.inc(n.getEntity(), n.getWeight());
			}
		}
		return neiMap;
	}

	/*
	 * Combine all annotations' categories in one map, from name to weight.
	 * 
	 * Weight can be assigned judging by category's depth and number of
	 * occurrences.
	 */
	private Map<String, Double> getAllCategories() {

		SumMap<String> catMap = new SumMap<String>();
		// for each annotation
		for (Annotatable ann : annotations) {

			AncestorAnnotation ancestor = ((SemanticallyExpandedAnnotation) ann)
					.getAncestors();
			// add all ancestors to map
			for (Category cat : ancestor.getAncestors()) {

				double catWeight = cat.getDepth() / (MAX_HIERARCHY_DEPTH + 1.0);
				catMap.inc(cat.getName(), catWeight);
			}
		}
		return catMap;
	}

	public String getElasticSearchField(String key) {

		return getElasticSearchFields().get(key);
	}
}
