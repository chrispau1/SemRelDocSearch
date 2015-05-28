package de.paul.similarity.entityScorers;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.paul.annotations.AncestorAnnotation;
import de.paul.annotations.Category;
import de.paul.annotations.DepthAnnotation;
import de.paul.annotations.TopicAnnotation;
import de.paul.similarity.bipartiteGraphs.TaxonomicScoring;

public class CommonAncestor extends Category {

	private DepthAnnotation annotation2;
	private Set<String> commonTopics;

	/**
	 * Simple constructor, entity information still needs to be set.
	 * 
	 * @param name
	 * @param depth
	 */
	public CommonAncestor(String name, int depth) {
		super(name, depth);
	}

	/**
	 * Full constructor with individual parameters.
	 * 
	 * @param name
	 * @param depth
	 * @param annotation1
	 * @param annotation2
	 * 
	 */
	public CommonAncestor(String name, int depth, DepthAnnotation annotation1,
			DepthAnnotation annotation2) {

		super(name, depth);
		this.setAnnotation(annotation1);
		this.setAnnotation2(annotation2);
	}

	/**
	 * Copy constructor
	 * 
	 * @param maxDepthCategory
	 */
	public CommonAncestor(CommonAncestor other) {

		super(other.getName(), other.getDepth());
		this.setAnnotation(other.getAnnotation());
		this.setAnnotation2(other.getAnnotation2());
		this.commonTopics = other.commonTopics;
	}

	private void setAnnotation2(DepthAnnotation ann) {

		this.annotation2 = ann.copy();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.paul.similarity.taxonomic.TaxonomicScorableEntityPair#getEntity2Depth
	 * ()
	 */
	public int getEntity2Depth() {
		return annotation2.getDepth();
	}

	public int getCommonAncestorEntity2Distance() {

		return annotation2.getDepth() - this.depth;
	}

	/**
	 * Compute taxonomic distance dtax
	 * 
	 * @return
	 */
	public double dtaxScore() {

		return TaxonomicScoring.dtaxScore(this);
	}

	/**
	 * Compute taxonomic distance dps
	 * 
	 * @return
	 */
	public double dpsScore() {

		return TaxonomicScoring.dpsScore(this);
	}

	/**
	 * Finds the lowest (maximum depth) node among the candidate categories that
	 * are common ancestors of two entities.
	 * 
	 * Also adds common topics ( = general categories) to lowest common
	 * ancestor.
	 * 
	 * @param candidates
	 * @return
	 */
	public static CommonAncestor lowestCommonAncestor(
			Collection<CommonAncestor> candidates) {
		// find max depth one
		int maxDepth = 0;
		CommonAncestor maxDepthCategory = null;
		Set<String> topics = new HashSet<String>();
		for (CommonAncestor cand : candidates) {

			int currDepth = cand.getDepth();
			if (currDepth > maxDepth) {

				maxDepth = currDepth;
				maxDepthCategory = cand;
			}
			if (currDepth == TopicAnnotation.TOPIC_DEPTH)
				topics.add(cand.name);
		}
		if (maxDepthCategory == null)
			return null;
		else {
			CommonAncestor ca = new CommonAncestor(maxDepthCategory);
			ca.setCommonTopics(topics);
			return ca;
		}
	}

	private void setCommonTopics(Set<String> topics) {

		this.commonTopics = topics;
	}

	public Set<String> getCommonTopics() {

		return commonTopics;
	}

	public double score() {

		if (this.getEntityName().equals(this.getEntity2Name()))
			return 1;// this.getEntityWeight() * this.getEntity2Weight();
		else
			return TaxonomicScoring.simScore(this);
	}

	public String getEntity2Name() {

		return annotation2.getEntity();
	}

	public String toString() {

		return getEntityName() + " -- (" + name + ", " + score() + ") -- "
				+ getEntity2Name();
	}

	public double getEntityWeight() {

		return getAnnotation().getWeight();
	}

	public double getEntity2Weight() {

		return annotation2.getWeight();
	}

	public static Collection<CommonAncestor> computeOverlap(
			AncestorAnnotation ann1, AncestorAnnotation ann2) {

		Map<String, CommonAncestor> commonAncestors = new HashMap<String, CommonAncestor>();
		HashSet<Category> intsct = new HashSet<Category>(ann1.getAncestors());
		intsct.retainAll(ann2.getAncestors());
		// first, build CommonAncestor objects for each intersection category
		for (Category cat : intsct) {

			// get from both ancestor sets
			CommonAncestor ca = new CommonAncestor(cat.getName(),
					cat.getDepth());
			commonAncestors.put(cat.getName(), ca);
		}
		// second, go through first set and set entity depth and name
		for (Category cat : ann1.getAncestors()) {
			CommonAncestor intsctObj = commonAncestors.get(cat.getName());
			if (intsctObj != null) {
				intsctObj.setAnnotation(new TopicAnnotation(ann1.getEntity(),
						ann1.getWeight(), cat.getEntityDepth(), ann1
								.getTopics()));
				commonAncestors.put(cat.getName(), intsctObj);
			}
		}
		// third, go through second set and set entity depth and name
		for (Category cat : ann2.getAncestors()) {
			CommonAncestor intsctObj = commonAncestors.get(cat.getName());
			if (intsctObj != null) {
				intsctObj.setAnnotation2(new TopicAnnotation(ann2.getEntity(),
						ann2.getWeight(), cat.getEntityDepth(), ann2
								.getTopics()));
				commonAncestors.put(cat.getName(), intsctObj);
			}
		}
		return commonAncestors.values();
	}

	public DepthAnnotation getAnnotation2() {

		return annotation2;
	}

	/**
	 * Symmetric scoring metric with the LCA.
	 */
	public double leftScore() {

		return score();
	}

	/**
	 * Symmetric scoring metric with the LCA.
	 */
	public double rightScore() {

		return score();
	}

	/**
	 * Symmetric scoring metric, so unnecessary method here.
	 */
	public void setLeft() {

	}

	/**
	 * Symmetric scoring metric, so unnecessary method here.
	 */
	public void setRight() {

	}

}
