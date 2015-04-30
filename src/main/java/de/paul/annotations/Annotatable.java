package de.paul.annotations;

import de.paul.pairwiseSimilarity.entityPairScorers.ScorableEntityPair;

public interface Annotatable {

	public String getEntity();

	public double getWeight();

	public void setWeight(double weight);

	public <x extends Annotatable> x copy();

	/**
	 * Produce an undirected edge (an entity pair) between this and another
	 * entity.
	 * 
	 * @param <x>
	 * 
	 */
	public ScorableEntityPair createEdge(Annotatable other);

	public void setEntity(String entName);

}
