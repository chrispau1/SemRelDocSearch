package de.paul.similarity.entityScorers;

import de.paul.annotations.AncestorAnnotation;

public abstract class TaxonomicEntityPairScorer extends
		EntityPairScorer<AncestorAnnotation> {

	public TaxonomicEntityPairScorer(AncestorAnnotation ann1,
			AncestorAnnotation ann2) {
		super(ann1, ann2);
	}
}
