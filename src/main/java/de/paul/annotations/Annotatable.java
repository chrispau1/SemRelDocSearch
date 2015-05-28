package de.paul.annotations;

import de.paul.similarity.entityScorers.ScorableEntityPair;

public interface Annotatable {

	public String getEntity();

	public double getWeight();

	public void setWeight(double weight);

	public <x extends Annotatable> x copy();

	/**
	 * Produce an undirected edge (an entity pair) between this and another
	 * entity. An edge is represented by an object of type ScorableEntityPair or
	 * a subclass. These classes define scoring functions for entity similarity
	 * between this and the other given entity.
	 * 
	 * @param <x>
	 * 
	 */
	public ScorableEntityPair createEdge(Annotatable other);

	public void setEntity(String entName);

}
