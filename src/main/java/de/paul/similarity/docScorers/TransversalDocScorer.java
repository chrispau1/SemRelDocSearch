package de.paul.similarity.docScorers;

import de.paul.documents.AnnotatedDoc;
import de.paul.documents.impl.TransversalExpandedDoc;
import de.paul.similarity.bipartiteGraphs.SingleEdge_MWBG;
import de.paul.similarity.bipartiteGraphs.WeightedBipartiteGraph;
import de.paul.similarity.bipartiteGraphs.WeightedBipartiteGraph.MWBG_mode;
import de.paul.similarity.bipartiteGraphs.WeightedBipartiteGraphImpl;
import de.paul.util.statistics.StatUtil;

public class TransversalDocScorer extends
		KnowledgeBasedDocScorers<TransversalExpandedDoc> {

	private StatUtil meanMachine;

	private static final int EXPAND_RADIUS = 2; // best: 2

	public TransversalDocScorer() {
		// objects used for new doc creation
		super();
		meanMachine = new StatUtil();
	}

	public void printMeanAndVariance() {

		double mean = meanMachine.getMean();
		System.out.println("Mean: " + mean + ", variance: "
				+ meanMachine.getVariance(mean));
	}

	public TransversalExpandedDoc createNewDoc(AnnotatedDoc doc) {

		return new TransversalExpandedDoc(doc, dbpHandler, EXPAND_RADIUS);
	}

	@Override
	public double score(TransversalExpandedDoc doc1, TransversalExpandedDoc doc2) {

		WeightedBipartiteGraphImpl bipartiteGraph = new WeightedBipartiteGraphImpl(
				doc1, doc2);
		// print matrix
		// printPairwiseEntityScoreMatrix(bipartiteGraph);
		// do scoring
		WeightedBipartiteGraph.mwbg_mode = MWBG_mode.SingleEdge;
		SingleEdge_MWBG se_MWBG = bipartiteGraph.findSingleEdgeMaximumWeight();
		double result = se_MWBG.similarityScore();
		// double result = bipartiteGraph.similarityScore();
		// System.out.println(result);
		meanMachine.registerScore(result, true);
		return result;
	}

	@Override
	public String writeCSVHeader() {

		return "id,overlap";
	}

}
