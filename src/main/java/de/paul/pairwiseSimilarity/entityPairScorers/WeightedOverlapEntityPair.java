package de.paul.pairwiseSimilarity.entityPairScorers;

import java.util.Collection;

import de.paul.annotations.Annotatable;
import de.paul.annotations.NeighborhoodAnnotation;
import de.paul.annotations.NeighborhoodAnnotation.OverlapWeightsMode;

public class WeightedOverlapEntityPair extends TransversalEntityPairScorer {

	public WeightedOverlapEntityPair(NeighborhoodAnnotation ann1,
			NeighborhoodAnnotation ann2) {
		super(ann1, ann2);
	}

	protected double score(NeighborhoodAnnotation queryAnn,
			NeighborhoodAnnotation compareAnn) {

		Collection<Annotatable> allNodes = queryAnn.getNeighbors();
		Collection<Annotatable> overlap = queryAnn.overlap(compareAnn,
				OverlapWeightsMode.MULT);
		// entity weight itself
		double ew = queryAnn.getWeight();
		// init with that weight
		double allSum = ew, overlapSum = 0;// .contains(queryAnn) ? ew : 0;
		for (Annotatable n : allNodes) {
			double w = n.getWeight();
			allSum += w;
		}
		for (Annotatable o : overlap) {
			double w = o.getWeight();
			overlapSum += w;
		}
		// prevent divide by zero
		if (allSum == 0)
			return 0;
		else
			return overlapSum / allSum;
	}
}
