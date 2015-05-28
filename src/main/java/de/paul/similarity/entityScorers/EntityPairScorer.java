package de.paul.similarity.entityScorers;

import de.paul.annotations.WeightedAnnotation;

public abstract class EntityPairScorer<x extends WeightedAnnotation> implements
		ScorableEntityPair<x> {

	protected x leftAnnotation;
	protected x rightAnnotation;

	protected Direction direction = Direction.undefined;

	public EntityPairScorer(x ann1, x ann2) {
		this.setLeftAnnotation(ann1);
		this.setRightAnnotation(ann2);
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

	public void setLeftAnnotation(x leftAnnotation) {
		this.leftAnnotation = leftAnnotation;
	}

	public void setRightAnnotation(x rightAnnotation) {
		this.rightAnnotation = rightAnnotation;
	}

	public x getAnnotation() {

		return leftAnnotation;
	}

	public x getAnnotation2() {

		return rightAnnotation;
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
	protected abstract double score(x rightAnn, x leftAnn);

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
