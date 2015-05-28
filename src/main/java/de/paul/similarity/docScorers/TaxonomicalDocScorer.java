package de.paul.similarity.docScorers;

import java.util.HashMap;
import java.util.List;

import de.paul.documents.AnnotatedDoc;
import de.paul.documents.impl.TaxonomicExpandedDoc;
import de.paul.kb.dbpedia.categories.WikiCatHierarchyHandler;
import de.paul.similarity.bipartiteGraphs.SingleEdge_MWBG;
import de.paul.similarity.bipartiteGraphs.TaxonomicScoring;
import de.paul.similarity.bipartiteGraphs.TaxonomicScoring.ScoreMode;
import de.paul.similarity.bipartiteGraphs.WeightedBipartiteGraph;
import de.paul.similarity.bipartiteGraphs.WeightedBipartiteGraph.MWBG_mode;
import de.paul.similarity.bipartiteGraphs.WeightedBipartiteGraphImpl;
import de.paul.util.Paths;
import de.paul.util.statistics.StatUtil;

/**
 * Applies AnnSim-like algorithms and their evaluation for document similarity
 * ranking and provides printouts and statistics about the results.
 * 
 * @author Chris
 *
 */
public class TaxonomicalDocScorer extends
		KnowledgeBasedDocScorers<TaxonomicExpandedDoc> {

	private WikiCatHierarchyHandler hierHandler;
	private StatUtil meanMachine;

	public TaxonomicalDocScorer() {

		/*
		 * objects used for generation of documents in create new doc method
		 */
		super();
		hierHandler = WikiCatHierarchyHandler.getInstance(Paths.TDB_DBPEDIA_HIERARCHY);
		/*
		 * to calc mean and variance
		 */
		meanMachine = new StatUtil();
	}

	public void printMeanAndVariance() {

		double mean = meanMachine.getMean();
		System.out.println("Mean: " + mean + ", variance: "
				+ meanMachine.getVariance(mean));
	}

	public String writeCSVHeader() {

		return "id,se_dtax,se_dps,wse_dtax,wse_dps";
	}

	/*
	 * Runs the pairwise similarity calculations between the query document and
	 * each other document in the corpus.
	 * 
	 * @param queryDoc
	 */
	// protected void computeRanking(int queryDoc) {
	//
	// System.out.println("Loading query doc...");
	// TaxonomicExpandedDoc doc1 = getDocumentFromIndex(queryDoc);
	// System.out.println("Loaded query doc");
	// allScores.resetScores();
	// for (int i = 0; i < documentIndex.getDocCount(); i++) {
	// if (i != queryDoc) {
	// TaxonomicExpandedDoc doc2 = getDocumentFromIndex(i);
	// GraphImplScores scores = computeSimilarities(doc1, doc2);
	// setScores(i, scores);
	// }
	// // System.out.println("Ranking doc " + i + " of 49");
	// }
	// System.out.println("Documents loaded.");
	// }

	public void closeHelpers() {

		dbpHandler.close();
		hierHandler.close();
	}

	@Override
	public void computeRankingForVariations(String queryDoc) {

		WeightedBipartiteGraph.mwbg_mode = MWBG_mode.SingleEdge;
		TaxonomicScoring.mode = ScoreMode.dtax;
		List<TaxonomicExpandedDoc> results0 = getRelatedDocuments(queryDoc);
		WeightedBipartiteGraph.mwbg_mode = MWBG_mode.SingleEdge;
		TaxonomicScoring.mode = ScoreMode.dps;
		List<TaxonomicExpandedDoc> results1 = getRelatedDocuments(queryDoc);
		WeightedBipartiteGraph.mwbg_mode = MWBG_mode.WeightedSingleEdge;
		TaxonomicScoring.mode = ScoreMode.dtax;
		List<TaxonomicExpandedDoc> results2 = getRelatedDocuments(queryDoc);
		WeightedBipartiteGraph.mwbg_mode = MWBG_mode.WeightedSingleEdge;
		TaxonomicScoring.mode = ScoreMode.dps;
		List<TaxonomicExpandedDoc> results3 = getRelatedDocuments(queryDoc);
		rankingsPerVariation = new HashMap<Integer, List<TaxonomicExpandedDoc>>();
		rankingsPerVariation.put(0, results0);
		rankingsPerVariation.put(1, results1);
		rankingsPerVariation.put(2, results2);
		rankingsPerVariation.put(3, results3);
		System.out.println("Ranking computed.");
	}

	public TaxonomicExpandedDoc createNewDoc(AnnotatedDoc doc) {

		return new TaxonomicExpandedDoc(doc, dbpHandler, hierHandler);
	}

	@Override
	public double score(TaxonomicExpandedDoc doc1, TaxonomicExpandedDoc doc2) {

		WeightedBipartiteGraphImpl bipartiteGraph = new WeightedBipartiteGraphImpl(
				doc1, doc2);
		SingleEdge_MWBG se_MWBG = bipartiteGraph.findSingleEdgeMaximumWeight();
		// print matrix
		if (doc1.getId().equals("8") && doc2.getId().equals("46"))
			printPairwiseEntityScoreMatrix(bipartiteGraph);
		double score = se_MWBG.similarityScore();
		meanMachine.registerScore(score, true);
		return score;
	}
}
