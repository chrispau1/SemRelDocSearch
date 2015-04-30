package de.paul.pairwiseSimilarity.entityPairScorers;

import java.util.Collection;

import de.paul.annotations.Annotatable;
import de.paul.annotations.NeighborhoodAnnotation;
import de.paul.annotations.NeighborhoodAnnotation.OverlapWeightsMode;

/**
 * Simple, unweighted implementation of transversal similarity. Simply counts
 * the overlapping elements and divides by all elements (of the respective
 * entity).
 * 
 * @author Chris
 *
 */
public class SimpleEntityPairSubgraph extends TransversalEntityPairScorer {

	public SimpleEntityPairSubgraph(NeighborhoodAnnotation ann1,
			NeighborhoodAnnotation ann2) {
		super(ann1, ann2);
	}

	protected double score(NeighborhoodAnnotation queryAnn,
			NeighborhoodAnnotation compareAnn) {

		Collection<Annotatable> allNodes = queryAnn.getNeighbors();
		Collection<Annotatable> overlap = queryAnn.overlap(compareAnn,
				OverlapWeightsMode.NONE);
		// prevent divbyzero
		return ((double) overlap.size()) / Math.max(1, allNodes.size());
	}

}
