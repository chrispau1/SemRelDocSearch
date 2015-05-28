package de.paul.similarity.entityScorers;

import de.paul.annotations.AncestorAnnotation;

public class LCAScorer extends TaxonomicEntityPairScorer {

	private CommonAncestor lca;

	public LCAScorer(AncestorAnnotation ann1, AncestorAnnotation ann2) {
		super(ann1, ann2);
		this.lca = ann1.lowestCommonAncestor(ann2);
	}

	@Override
	protected double score(AncestorAnnotation rightAnn,
			AncestorAnnotation leftAnn) {

		return lca.score();
	}

}
