package de.paul.pairwiseSimilarity.graphs;

import de.paul.pairwiseSimilarity.entityPairScorers.TaxonomicScorableEntityPair;

public class TaxonomicScoring {

	public enum ScoreMode {
		dtax, dps
	};

	public static ScoreMode mode = ScoreMode.dps;

	/**
	 * Compute taxonomic distance dtax
	 * 
	 * @return
	 */
	public static double dtaxScore(TaxonomicScorableEntityPair tsep) {

		int d_lca_x = tsep.getCategoryEntityDistance();
		int d_lca_y = tsep.getCommonAncestorEntity2Distance();
		int d_root_x = tsep.getEntityDepth();
		int d_root_y = tsep.getEntity2Depth();
		return ((double) (d_lca_x + d_lca_y)) / (d_root_x + d_root_y);
	}

	/**
	 * Compute taxonomic distance dps
	 * 
	 * @return
	 */
	public static double dpsScore(TaxonomicScorableEntityPair tsep) {

		int root_lca = tsep.getDepth();
		int d_lca_x = tsep.getCategoryEntityDistance();
		int d_lca_y = tsep.getCommonAncestorEntity2Distance();
		return 1.0 - ((double) root_lca) / (root_lca + d_lca_x + d_lca_y);
	}

	public static double simScore(TaxonomicScorableEntityPair tsep) {

		if (mode == ScoreMode.dtax)
			return 1 - dtaxScore(tsep);
		else if (mode == ScoreMode.dps)
			return 1 - dpsScore(tsep);
		return -1;
	}
}
