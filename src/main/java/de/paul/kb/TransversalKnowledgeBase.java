package de.paul.kb;

import java.util.Collection;
import java.util.Set;

import de.paul.annotations.Annotatable;

public interface TransversalKnowledgeBase {

	/**
	 * Expands the set along outgoing edges.
	 * 
	 * @param outEntities
	 * @return
	 */
	public Set<Annotatable> getNeighborsOutEdges(
			Collection<? extends Annotatable> outEntities);

	/**
	 * Expands the set reversely along incoming edges.
	 * 
	 * @param inEntities
	 * @return
	 */
	public Set<Annotatable> getNeighborsInEdges(
			Collection<? extends Annotatable> inEntities);

	/**
	 * Gets traversal neighbors of given entities using both in- and out-going
	 * edges.
	 * 
	 * @param entities
	 * @return
	 */
	public Set<Annotatable> getBirectionalTransversalNeighbors(
			Collection<? extends Annotatable> entities);

}
