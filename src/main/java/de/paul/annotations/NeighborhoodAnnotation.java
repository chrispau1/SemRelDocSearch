package de.paul.annotations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.paul.dbpedia.DBPediaHandler;
import de.paul.docs.AnnotatedDoc;
import de.paul.pairwiseSimilarity.entityPairScorers.ScorableEntityPair;
import de.paul.pairwiseSimilarity.entityPairScorers.WeightedOverlapEntityPair;

public class NeighborhoodAnnotation extends WeightedAnnotation {

	public enum OverlapWeightsMode {
		MULT, NONE
	}

	private List<Set<Annotatable>> outNeighborsAtDistance;
	private Collection<Annotatable> neighbors;
	private ArrayList<Set<Annotatable>> inNeighborsAtDistance;

	/**
	 * Create annotation and expand neighborhood up to specified radius
	 * 
	 * @param ent
	 * @param weight
	 * @param dbpHandler
	 * @param expansionRadius
	 */
	public NeighborhoodAnnotation(String ent, double weight,
			DBPediaHandler dbpHandler, int expansionRadius) {
		super(ent, weight);
		// setWeight(1.0);
		expandAnnotation(dbpHandler, expansionRadius);
	}

	public NeighborhoodAnnotation(String ent, double weight,
			Set<Annotatable> neighbors) {
		super(ent, weight);
		// setWeight(1.0);
		this.setNeighbors(neighbors);
	}

	public NeighborhoodAnnotation(NeighborhoodAnnotation other) {

		super(other.getEntity(), other.getWeight());
		// setWeight(1.0);
		LinkedList<Annotatable> copies = new LinkedList<Annotatable>();
		Collection<Annotatable> othersNeighbors = other.getNeighbors();
		if (othersNeighbors != null) {
			for (Annotatable n : othersNeighbors) {
				copies.add(n.copy());
			}
		}
		this.setNeighbors(copies);
	}

	public void expandAnnotation(DBPediaHandler dbpHandler, int expansionRadius) {

		List<Annotatable> neighborList = new LinkedList<Annotatable>();
		// get neighbors for i hops away
		outNeighborsAtDistance = new ArrayList<Set<Annotatable>>(
				expansionRadius);
		inNeighborsAtDistance = new ArrayList<Set<Annotatable>>(expansionRadius);
		for (int i = 0; i < expansionRadius; i++) {
			// get query entities, starting off with annotation itself
			Set<Annotatable> outEntitySet, inEntitySet;
			if (i == 0) {
				outEntitySet = new HashSet<Annotatable>();
				outEntitySet.add(this.copy());
				inEntitySet = new HashSet<Annotatable>();
				inEntitySet.add(this.copy());
			} else {
				// continuing with entities found in last iteration
				outEntitySet = outNeighborsAtDistance.get(i - 1);
				inEntitySet = inNeighborsAtDistance.get(i - 1);
			}
			// get neighboring entities
			Set<Annotatable> oneHopOutNeighbors = dbpHandler
					.getNeighborsOutEdges(outEntitySet);

			Set<Annotatable> oneHopInNeighbors;
			// don't do that right now
			if (i == -1)
				oneHopInNeighbors = dbpHandler.getNeighborsInEdges(inEntitySet);
			else
				oneHopInNeighbors = new HashSet<Annotatable>();
			// post-process
			Set<Annotatable> postProcessedOneHopOutNeighbors = postProcessLayer(
					i, oneHopOutNeighbors);
			Set<Annotatable> postProcessedOneHopInNeighbors = postProcessLayer(
					i, oneHopInNeighbors);
			outNeighborsAtDistance.add(postProcessedOneHopOutNeighbors);
			inNeighborsAtDistance.add(postProcessedOneHopInNeighbors);
			neighborList.addAll(postProcessedOneHopOutNeighbors);
			neighborList.addAll(postProcessedOneHopInNeighbors);
		}
		// now unify the elements of the neighbor list so that each node only
		// appears once with added weights
		Collection<Annotatable> unifiedNeighbors = AnnotatedDoc
				.unifyAnnotationsSumScores(neighborList);
		this.neighbors = unifiedNeighbors;
	}

	/*
	 * Standard implementation: do nothing.
	 */
	protected Set<Annotatable> postProcessLayer(int dist,
			Set<Annotatable> oneHopNeighbors) {

		return oneHopNeighbors;
	}

	/**
	 * Find paths between this and another NeighborhoodAnnotation. Paths are
	 * represented through overlapping entities between the two annotations.
	 * Entities were found by expanding the annotation in DBPedia towards
	 * neighboring entities. Overlapping entities are returned.
	 * 
	 * Annotation itself, a.k.a. the DBPedia entity discovered in the document
	 * text, is also added into overlap computation. This is essential to the
	 * approach, as annotation expansion is unidirectional at the moment.
	 * 
	 * @param other
	 * @param combineWeights
	 * @return
	 */
	public Set<Annotatable> overlap(NeighborhoodAnnotation other,
			OverlapWeightsMode combineWeights) {
		/*
		 * this
		 */
		Map<String, Annotatable> thisEntities = this.getNeighborsCopy();
		// add annotation itself to find paths directly to it
		WeightedAnnotation originalAnnotation = new WeightedAnnotation(
				this.getEntity(), this.getWeight());
		// see if entity is also included in neighborhood
		Annotatable entInNeighborhood;
		if ((entInNeighborhood = thisEntities.get(this.getEntity())) != null)
			originalAnnotation.setWeight(originalAnnotation.getWeight()
					+ entInNeighborhood.getWeight());
		thisEntities.put(this.getEntity(), originalAnnotation);
		/*
		 * other
		 */
		Map<String, Annotatable> otherEntities = other.getNeighborsCopy();
		// add annotation itself to find paths directly to it
		originalAnnotation = new WeightedAnnotation(other.getEntity(),
				other.getWeight());
		// see if entity is also included in neighborhood
		if ((entInNeighborhood = otherEntities.get(other.getEntity())) != null)
			originalAnnotation.setWeight(originalAnnotation.getWeight()
					+ entInNeighborhood.getWeight());
		otherEntities.put(other.getEntity(), originalAnnotation);
		return overlap(thisEntities.values(), otherEntities.values(),
				combineWeights);
	}

	private Set<Annotatable> overlap(Collection<Annotatable> entities,
			Collection<Annotatable> otherEntities,
			OverlapWeightsMode combineWeights) {

		if (entities != null && otherEntities != null) {
			Set<Annotatable> intsct = new HashSet<Annotatable>(entities);
			// intersect neighbor sets.
			intsct.retainAll(otherEntities);
			// perform additional work if more elaborate weight computation are
			// desired
			if (combineWeights == OverlapWeightsMode.MULT) {
				// build access structure to get to values
				Map<Annotatable, Double> otherWeights = new HashMap<Annotatable, Double>();
				for (Annotatable on : otherEntities) {
					otherWeights.put(on, on.getWeight());
				}
				// now multiply this entity's weight for overlapping elements
				// with other entity's assigned weight

				for (Annotatable elem : intsct) {
					Double otherWeight = otherWeights.get(elem);
					elem.setWeight(otherWeight * elem.getWeight());
				}
			}
			return intsct;
		} else
			return null;
	}

	public Collection<Annotatable> getNeighbors() {
		return neighbors;
	}

	public Map<String, Annotatable> getNeighborsCopy() {

		Map<String, Annotatable> copy = new HashMap<String, Annotatable>();
		for (Annotatable n : neighbors) {
			copy.put(n.getEntity(), n.copy());
		}
		return copy;
	}

	public void setNeighbors(Collection<Annotatable> collection) {
		this.neighbors = collection;
	}

	public String toString() {

		return "Entity: " + this.getEntity() + ", Neighbors: " + this.neighbors;
	}

	public NeighborhoodAnnotation copy() {

		return new NeighborhoodAnnotation(this);
	}

	public ScorableEntityPair createEdge(Annotatable other) {

		// return new KatzEntityPairSubgraph(this, (NeighborhoodAnnotation)
		// other);
		return new WeightedOverlapEntityPair(this,
				(NeighborhoodAnnotation) other);
		// return new HeteSimEntityPairSubgraph(this,
		// (NeighborhoodAnnotation) other);
	}
}
