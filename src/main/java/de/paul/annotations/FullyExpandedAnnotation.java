package de.paul.annotations;

import java.util.HashSet;

import de.paul.pairwiseSimilarity.entityPairScorers.CombinedEntityPairScorer;
import de.paul.pairwiseSimilarity.entityPairScorers.CombinedEntityPairScorer.CombineMode;
import de.paul.pairwiseSimilarity.entityPairScorers.ScorableEntityPair;

public class FullyExpandedAnnotation extends WeightedAnnotation {

	private AncestorAnnotation ancestors;
	private NeighborhoodAnnotation neighbors;

	public FullyExpandedAnnotation(AncestorAnnotation ancAnn,
			NeighborhoodAnnotation neiAnn) throws Exception {
		super(ancAnn.getEntity(), ancAnn.getWeight());
		if (neiAnn != null) {
			if (!ancAnn.getEntity().equals(neiAnn.getEntity()))
				throw new Exception(
						"Annotation cannot be created as entity names of child annotations"
								+ " carry different names");
			this.neighbors = neiAnn.copy();
		} else
			this.neighbors = new NeighborhoodAnnotation(ancAnn.getEntity(),
					ancAnn.getWeight(), new HashSet<Annotatable>());
		this.ancestors = ancAnn.copy();
	}

	public FullyExpandedAnnotation(FullyExpandedAnnotation other) {

		super(other.getEntity(), other.getWeight());
		this.ancestors = other.ancestors.copy();
		this.neighbors = other.neighbors.copy();
	}

	public ScorableEntityPair createEdge(Annotatable other) {

		return new CombinedEntityPairScorer(this,
				(FullyExpandedAnnotation) other, CombineMode.PLUS);
	}

	public NeighborhoodAnnotation getNeighbors() {

		return neighbors;
	}

	public AncestorAnnotation getAncestors() {
		return ancestors;
	}

	public void setNeighbors(NeighborhoodAnnotation neighAnn) {

		this.neighbors = neighAnn.copy();
	}

	public FullyExpandedAnnotation copy() {
		return new FullyExpandedAnnotation(this);
	}

	public String toString() {

		String s = "(" + super.toString();
		return s + ", categories: " + ancestors.toString() + ", neighbors: "
				+ neighbors.toString() + ")";
	}
}
