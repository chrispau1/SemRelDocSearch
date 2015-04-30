package de.paul.pairwiseSimilarity.entityPairScorers;

import java.util.Collection;

import de.paul.annotations.Annotatable;
import de.paul.annotations.NeighborhoodAnnotation;
import de.paul.annotations.NeighborhoodAnnotation.OverlapWeightsMode;

public class HeteSimEntityPairSubgraph extends TransversalEntityPairScorer {

	public HeteSimEntityPairSubgraph(NeighborhoodAnnotation ann1,
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
