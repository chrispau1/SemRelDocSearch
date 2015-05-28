package de.paul.similarity.entityScorers;

import de.paul.annotations.WeightedAnnotation;

public interface ScorableEntityPair<x extends WeightedAnnotation> {

	enum Direction {
		undefined, left, right
	};

	/**
	 * Set direction of edge that is to be used for scoring in this instance to
	 * "left"
	 */
	public void setLeft();

	/**
	 * Set direction of edge that is to be used for scoring in this instance to
	 * "right"
	 */
	public void setRight();

	/**
	 * Return name of first entity
	 */
	public String getEntityName();

	/**
	 * Return name of second entity
	 */
	public String getEntity2Name();

	/**
	 * Return weight of first entity
	 */
	public double getEntityWeight();

	/**
	 * Return weight of second entity
	 */
	public double getEntity2Weight();

	/**
	 * Return entity
	 */
	public x getAnnotation();

	/**
	 * Return entity 2
	 */
	public x getAnnotation2();

	/**
	 * Returns a SIMILARITY score for the left entity towards the right entity,
	 * between 0 and 1.
	 * 
	 * @return
	 */
	public double leftScore();

	/**
	 * Returns a SIMILARITY score for the right entity towards the left entity,
	 * between 0 and 1.
	 * 
	 * @return
	 */
	public double rightScore();

	/**
	 * Returns the score for the edge's direction that was selected.
	 * 
	 * @return
	 */
	public double score();

}
