package de.paul.similarity.docScorers;

import de.paul.documents.AnnotatedDoc;
import de.paul.documents.impl.TransversalExpandedDoc;
import de.paul.similarity.bipartiteGraphs.SingleEdge_MaxGraph;
import de.paul.similarity.bipartiteGraphs.WeightedBipartiteGraph;
import de.paul.similarity.bipartiteGraphs.WeightedBipartiteGraph.MaxGraph_mode;
import de.paul.similarity.bipartiteGraphs.WeightedBipartiteGraphImpl;
import de.paul.util.Directionality;
import de.paul.util.statistics.StatUtil;

public class TransversalDocScorer extends
		KnowledgeBasedDocScorers<TransversalExpandedDoc> {

	private StatUtil meanMachine;

	private Directionality edgeDirMode = Directionality.OUTGOING;

	private int expansionRadius = 2; // best: 2

	public TransversalDocScorer(Directionality edgeMode, int expRadius) {
		// objects used for new doc creation
		super();
		if (expRadius >= 0 && expRadius < 5)
			this.expansionRadius = expRadius;
		else
			System.err.println("Invalid expansion radius " + expRadius
					+ " provided. Must be between 0 and 4");
		if (edgeMode != null)
			this.edgeDirMode = edgeMode;
		meanMachine = new StatUtil();
	}

	public void printMeanAndVariance() {

		double mean = meanMachine.getMean();
		System.out.println("Mean: " + mean + ", variance: "
				+ meanMachine.getVariance(mean));
	}

	public TransversalExpandedDoc createNewDoc(AnnotatedDoc doc) {

		return new TransversalExpandedDoc(doc, dbpHandler, expansionRadius,
				edgeDirMode);
	}

	@Override
	public double score(TransversalExpandedDoc doc1, TransversalExpandedDoc doc2) {

		WeightedBipartiteGraphImpl bipartiteGraph = new WeightedBipartiteGraphImpl(
				doc1, doc2);
		// print matrix
		// printPairwiseEntityScoreMatrix(bipartiteGraph);
		// do scoring
		WeightedBipartiteGraph.mwbg_mode = MaxGraph_mode.SingleEdge;
		SingleEdge_MaxGraph se_MWBG = bipartiteGraph.computeMaxGraph();
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
