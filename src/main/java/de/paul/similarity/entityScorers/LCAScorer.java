package de.paul.similarity.entityScorers;

import de.paul.annotations.AncestorAnnotation;

public class LCAScorer extends TaxonomicEntityPairScorer {

	private CommonAncestor lca;

	public LCAScorer(AncestorAnnotation ann1, AncestorAnnotation ann2) {
		super(ann1, ann2);
		// sets null if overlap is only root
		this.lca = ann1.lowestCommonAncestor(ann2);
	}

	@Override
	protected double score(AncestorAnnotation rightAnn,
			AncestorAnnotation leftAnn) {
		if (lca == null)
			return 0;
		else
			return lca.score();
	}

}
