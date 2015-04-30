package de.paul.pairwiseSimilarity.graphs;

import de.paul.docs.AnnotatedDoc;
import de.paul.pairwiseSimilarity.entityPairScorers.ScorableEntityPair;

public class SingleEdge_MWBG extends WeightedBipartiteGraph {

	public SingleEdge_MWBG(int annotCount1, int annotCount2) {

		this.annotCount1 = annotCount1;
		this.annotCount2 = annotCount2;
	}

	public SingleEdge_MWBG(AnnotatedDoc doc1, AnnotatedDoc doc2) {
		this.doc1 = doc1;
		this.doc2 = doc2;
		this.annotCount1 = doc1.getAnnotations().size();
		this.annotCount2 = doc2.getAnnotations().size();
	}

	/*
	 * Annotation Similarity formula similar to one from Vidal's paper
	 */
	private double altAnnSim() {

		// sum over all edges
		double numerator = 0;
		if (matchings != null) {
			for (ScorableEntityPair edge : matchings) {
				numerator += edge.score();
			}
			double denominator = annotCount1 + annotCount2;
			double res = 0;
			if (denominator != 0)
				res = numerator / denominator;
			return res;
		} else
			return 0;
	}

	@Override
	public double similarityScore() throws UnsupportedOperationException {

		return altAnnSim();
	}

}
