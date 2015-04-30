package de.paul.pairwiseSimilarity.entityPairScorers;

import java.util.Collection;

import de.paul.annotations.Annotatable;
import de.paul.annotations.NeighborhoodAnnotation;
import de.paul.annotations.NeighborhoodAnnotation.OverlapWeightsMode;

/**
 * Computes the Katz index between two entities based all paths that connect
 * them up to length 2*(entities' expansion radius).
 * 
 * Inspired by Semantic Connectivity Score (Nunes et al., 2013)
 * 
 * @author Chris
 *
 */
public class KatzEntityPairSubgraph extends TransversalEntityPairScorer {

	public KatzEntityPairSubgraph(NeighborhoodAnnotation ann1,
			NeighborhoodAnnotation ann2) {
		super(ann1, ann2);
	}

	protected double score(NeighborhoodAnnotation queryAnn,
			NeighborhoodAnnotation compareAnn) {

		// Collection<Annotatable> allNodes = queryAnn.getNeighbors();
		Collection<Annotatable> overlap = queryAnn.overlap(compareAnn,
				OverlapWeightsMode.MULT);
		double sum = 0;
		for (Annotatable ann : overlap) {
			sum += ann.getWeight();
		}
		// normalize score computing self-overlap
		double normSum = queryAnn.getWeight() * queryAnn.getWeight();
		for (Annotatable ann : queryAnn.getNeighbors())
			normSum += ann.getWeight() * ann.getWeight();
		return sum / normSum;
	}

}
