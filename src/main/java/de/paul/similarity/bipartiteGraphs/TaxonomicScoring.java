package de.paul.similarity.bipartiteGraphs;

import de.paul.similarity.entityScorers.CommonAncestor;

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
	public static double dtaxScore(CommonAncestor commAnc) {

		int d_lca_x = commAnc.getCategoryEntityDistance();
		int d_lca_y = commAnc.getCommonAncestorEntity2Distance();
		int d_root_x = commAnc.getEntityDepth();
		int d_root_y = commAnc.getEntity2Depth();
		return ((double) (d_lca_x + d_lca_y)) / (d_root_x + d_root_y);
	}

	/**
	 * Compute taxonomic distance dps
	 * 
	 * @return
	 */
	public static double dpsScore(CommonAncestor commAnc) {

		int root_lca = commAnc.getDepth();
		int d_lca_x = commAnc.getCategoryEntityDistance();
		int d_lca_y = commAnc.getCommonAncestorEntity2Distance();
		return 1.0 - ((double) root_lca) / (root_lca + d_lca_x + d_lca_y);
	}

	public static double simScore(CommonAncestor commAnc) {

		if (mode == ScoreMode.dtax)
			return 1 - dtaxScore(commAnc);
		else if (mode == ScoreMode.dps)
			return 1 - dpsScore(commAnc);
		return -1;
	}
}
