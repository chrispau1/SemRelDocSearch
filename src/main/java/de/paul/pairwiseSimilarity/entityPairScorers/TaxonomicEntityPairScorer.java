package de.paul.pairwiseSimilarity.entityPairScorers;

import de.paul.annotations.AncestorAnnotation;
import de.paul.annotations.WeightedAnnotation;

public abstract class TaxonomicEntityPairScorer implements ScorableEntityPair {

	private Direction direction = Direction.undefined;
	private AncestorAnnotation leftAnnotation;
	private AncestorAnnotation rightAnnotation;

	public TaxonomicEntityPairScorer(AncestorAnnotation ann1,
			AncestorAnnotation ann2) {

		this.leftAnnotation = ann1.copy();
		this.rightAnnotation = ann2.copy();
	}

	public void setLeft() {

		this.direction = Direction.left;
	}

	public void setRight() {

		this.direction = Direction.right;
	}

	public String getEntityName() {

		return leftAnnotation.getEntity();
	}

	public String getEntity2Name() {

		return rightAnnotation.getEntity();
	}

	public double getEntityWeight() {

		return leftAnnotation.getWeight();
	}

	public double getEntity2Weight() {

		return rightAnnotation.getWeight();
	}

	public <x extends WeightedAnnotation> x getAnnotation() {

		return (x) leftAnnotation;
	}

	public <x extends WeightedAnnotation> x getAnnotation2() {

		return (x) rightAnnotation;
	}

	public double leftScore() {

		return score(leftAnnotation, rightAnnotation);
	}

	public double rightScore() {

		return score(rightAnnotation, leftAnnotation);
	}

	/*
	 * Computes the actual score. Computes the scores for the annotation put
	 * first regarding the second annotation.
	 */
	protected abstract double score(AncestorAnnotation rightAnnotation2,
			AncestorAnnotation leftAnnotation2);

	/**
	 * Implementation that relies on the direction set at an earlier point.
	 * 
	 * Direction indicates which score, left or right, shall be used. Direction
	 * needs to be set when matching is built. This allows the use of the
	 * intrinsically undirected edge as a directed edge.
	 */
	public double score() {
		if (direction == Direction.left)
			return leftScore();
		else if (direction == Direction.right)
			return rightScore();
		else
			return -17031989;
	}
}
