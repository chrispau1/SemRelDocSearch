package de.paul.similarity.entityScorers;

import java.util.Collection;
import java.util.Set;

import de.paul.annotations.AncestorAnnotation;
import de.paul.annotations.Category;

public class WeightedAncestorOverlapScorer extends
		TaxonomicEntityPairScorer {

	public WeightedAncestorOverlapScorer(AncestorAnnotation ann1,
			AncestorAnnotation ann2) {
		super(ann1, ann2);
	}

	@Override
	protected double score(AncestorAnnotation queryAnn,
			AncestorAnnotation compareAnn) {

		double ancScore;
		if (queryAnn.getEntity().equals(compareAnn.getEntity())) {
			ancScore = 1;
		} else {
			Set<Category> thisAncestors = queryAnn.getAncestors();
			Collection<CommonAncestor> overlap = queryAnn
					.ancestorOverlap(compareAnn);

			double numerator = 0;
			double denominator = 0;
			for (Category ann : thisAncestors) {
				denominator += ann.getDepth();
			}
			for (CommonAncestor o : overlap) {
				numerator += o.getDepth();
			}
			if (denominator == 0)
				ancScore = 0;
			else
				ancScore = numerator / denominator;
		}
		// return queryAnn.getWeight() * ancScore * compareAnn.getWeight();
		return ancScore;
	}

}
