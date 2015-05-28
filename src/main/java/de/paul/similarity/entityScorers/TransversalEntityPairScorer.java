package de.paul.similarity.entityScorers;

import de.paul.annotations.NeighborhoodAnnotation;

public abstract class TransversalEntityPairScorer extends
		EntityPairScorer<NeighborhoodAnnotation> {

	public TransversalEntityPairScorer(NeighborhoodAnnotation ann1,
			NeighborhoodAnnotation ann2) {
		super(ann1, ann2);
	}

}
