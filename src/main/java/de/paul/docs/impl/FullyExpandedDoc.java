package de.paul.docs.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.paul.annotations.AncestorAnnotation;
import de.paul.annotations.Annotatable;
import de.paul.annotations.FullyExpandedAnnotation;
import de.paul.annotations.NeighborhoodAnnotation;
import de.paul.corpora.GlobalStatsProvider;
import de.paul.dbpedia.DBPediaHandler;
import de.paul.dbpedia.categories.HierarchyHandler;
import de.paul.docs.AnnotatedDoc;
import de.paul.docs.ElasticSearchSerializableDoc;
import de.paul.similarity.taxonomic.Category;
import de.paul.util.MapUtil;
import de.paul.util.SumMap;

public class FullyExpandedDoc extends TaxonomicExpandedDoc implements
		ElasticSearchSerializableDoc {

	private static final int ES_WORD_STRETCH_FACTOR = 50;
	// Category hierarchy has depth 15
	private static final int MAX_HIERARCHY_DEPTH = 15;
	private HashMap<String, String> fieldMap = null;

	public FullyExpandedDoc(FullyExpandedDoc copy) {

		super(copy);
	}

	public FullyExpandedDoc(AnnotatedDoc doc, int expansion_radius,
			DBPediaHandler dbpHandler, HierarchyHandler hierHandler,
			GlobalStatsProvider globalStatsProvider) {

		// super constructor creates taxonomical annotations and adds them.
		// adding is overwritten though (addAnnotation) such that a fully
		// expanded
		// annotation is added, with the transversal part set to NULL
		super(doc, dbpHandler, hierHandler, globalStatsProvider);
		// add transversal part
		for (Annotatable ann : annotations) {
			String ent = ann.getEntity();
			double weight = ann.getWeight();
			long t1 = System.currentTimeMillis();
			NeighborhoodAnnotation neighAnn = new NeighborhoodAnnotation(ent,
					weight, dbpHandler, expansion_radius);

			long t2 = System.currentTimeMillis();
			// System.out.println((t2 - t1));
			// set neighborhood annotation
			((FullyExpandedAnnotation) ann).setNeighbors(neighAnn);
		}
	}

	public FullyExpandedDoc(String docId, String title, String text,
			List<Annotatable> annotations) {

		super(docId, title, text, annotations);
	}

	public void addAnnotation(Annotatable ann) {

		if (ann instanceof AncestorAnnotation) {
			FullyExpandedAnnotation fullAnnot = null;
			try {
				fullAnnot = new FullyExpandedAnnotation(
						(AncestorAnnotation) ann, null);
			} catch (Exception e) {
				// can't be thrown here.
				e.printStackTrace();
			}
			annotations.add(fullAnnot);
		} else if (ann instanceof FullyExpandedAnnotation)
			annotations.add(((FullyExpandedAnnotation) ann).copy());
	}

	@Override
	public FullyExpandedDoc copy() {

		return new FullyExpandedDoc(this);
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

			NeighborhoodAnnotation neighbor = ((FullyExpandedAnnotation) ann)
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

			AncestorAnnotation ancestor = ((FullyExpandedAnnotation) ann)
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
