package de.paul.pairwiseSimilarity.entityPairScorers;

import de.paul.annotations.NeighborhoodAnnotation;
import de.paul.annotations.WeightedAnnotation;

public abstract class TransversalEntityPairScorer implements ScorableEntityPair {

	private NeighborhoodAnnotation leftAnnotation;
	private NeighborhoodAnnotation rightAnnotation;

	private Direction direction = Direction.undefined;

	public TransversalEntityPairScorer(NeighborhoodAnnotation ann1,
			NeighborhoodAnnotation ann2) {
		this.setLeftAnnotation(ann1);
		this.setRightAnnotation(ann2);
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
	protected abstract double score(NeighborhoodAnnotation queryAnn,
			NeighborhoodAnnotation compareAnn);

	public void setLeftAnnotation(NeighborhoodAnnotation leftAnnotation) {
		this.leftAnnotation = leftAnnotation;
	}

	public void setRightAnnotation(NeighborhoodAnnotation rightAnnotation) {
		this.rightAnnotation = rightAnnotation;
	}

	public void setLeft() {
		this.direction = Direction.left;
	}

	public void setRight() {
		this.direction = Direction.right;
	}

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
