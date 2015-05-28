package de.paul.similarity.bipartiteGraphs;

import de.paul.similarity.entityScorers.ScorableEntityPair;

/**
 * 1-1 Maximum Weight Bipartite Graph between two sets of annotations.
 * 
 * @author Chris
 *
 */
public class IndependentEdge_MWBG extends WeightedBipartiteGraph {

	public IndependentEdge_MWBG(int annotCount1, int annotCount2) {

		this.annotCount1 = annotCount1;
		this.annotCount2 = annotCount2;
	}

	/*
	 * Annotation Similarity formula taken from Vidal's paper
	 */
	private double annSim() {

		// 2* sum over all edges
		double numerator = 0;
		for (ScorableEntityPair edge : matchings) {
			numerator += 2 * edge.score();
		}
		double denominator = annotCount1 + annotCount2;
		double res = 0;
		if (denominator != 0)
			res = numerator / denominator;
		return res;
	}

	@Override
	public double similarityScore() throws UnsupportedOperationException {

		return annSim();
	}
}
