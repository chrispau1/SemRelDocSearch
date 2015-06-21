package de.paul.annotations;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import de.paul.kb.dbpedia.DBPediaHandler;
import de.paul.kb.dbpedia.categories.WikiCatHierarchyHandler;
import de.paul.similarity.entityScorers.CommonAncestor;
import de.paul.similarity.entityScorers.LCAScorer;
import de.paul.similarity.entityScorers.TaxonomicEntityPairScorer;

public class AncestorAnnotation extends WeightedAnnotation {

	/*
	 * Need to be ALL ancestors, beginning with the immediate categories of the
	 * entity.
	 */
	private Set<Category> ancestors;

	public AncestorAnnotation(String ent, double weight) {
		super(ent, weight);
	}

	public AncestorAnnotation(String ent, double weight, Set<Category> ancestors) {
		super(ent, weight);
		this.ancestors = ancestors;
	}

	public AncestorAnnotation(String entName, double entWeight,
			DBPediaHandler dbpHandler, WikiCatHierarchyHandler hierHandler) {

		super(entName, entWeight);
		// get categories
		setAncestors(expandHierarchically(dbpHandler, hierHandler, entName));
	}

	private Set<Category> expandHierarchically(DBPediaHandler dbpHandler,
			WikiCatHierarchyHandler hierHandler, String entName) {

		// long t1 = System.currentTimeMillis();
		Set<String> cats = dbpHandler.getCategories(entName);
		// long t2 = System.currentTimeMillis();
		// TimeKeeper.getInstance().addGetCategoryTime(t2 - t1);
		// get ancestors
		// t1 = System.currentTimeMillis();
		Set<Category> ancestors = hierHandler.getAncestors(this, cats);
		// t2 = System.currentTimeMillis();
		// TimeKeeper.getInstance().addGetAncestorsTime(t2 - t1);
		return ancestors;
	}

	public Set<Category> getAncestors() {
		return ancestors;
	}

	public void setAncestors(Set<Category> ancestors) {
		this.ancestors = ancestors;
	}

	public void addAncestor(Category anc) {

		if (ancestors == null)
			ancestors = new HashSet<Category>();
		ancestors.add(anc);
	}

	/**
	 * Determines overlapping categorical ancestors of this and another
	 * annotation object.
	 * 
	 * @param other
	 * @return
	 */
	public Collection<CommonAncestor> ancestorOverlap(AncestorAnnotation other) {

		if (this.ancestors != null && other.getAncestors() != null)
			return CommonAncestor.computeOverlap(this, other);
		else
			return null;
	}

	/**
	 * Returns the LCA between this and another annotation object.
	 * 
	 * @param other
	 * @return
	 */
	public CommonAncestor lowestCommonAncestor(AncestorAnnotation other) {

		Collection<CommonAncestor> overlap = this.ancestorOverlap(other);
		if (overlap != null && overlap.size() != 0)
			return CommonAncestor.lowestCommonAncestor(overlap);
		else
			return null;
	}

	public AncestorAnnotation copy() {

		return new AncestorAnnotation(getEntity(), getWeight(), getAncestors());
	}

	public String toString() {

		return "Entity: " + this.getEntity() + ", Categories: "
				+ this.ancestors;
	}

	public Set<String> getTopics() {

		Set<String> res = new HashSet<String>();
		for (Category anc : ancestors) {
			if (anc.getDepth() == TopicAnnotation.TOPIC_DEPTH)
				res.add(anc.getName());
		}
		return res;
	}

	public TaxonomicEntityPairScorer createEdge(Annotatable other) {

		// return this.lowestCommonAncestor((AncestorAnnotation) other);
		return new LCAScorer(this, (AncestorAnnotation) other);
	}
}
