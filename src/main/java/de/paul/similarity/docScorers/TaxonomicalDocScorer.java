package de.paul.similarity.docScorers;

import java.util.HashMap;
import java.util.List;

import de.paul.corpora.JSONLoader;
import de.paul.dbpedia.DBPediaHandler;
import de.paul.dbpedia.categories.HierarchyHandler;
import de.paul.docs.AnnotatedDoc;
import de.paul.docs.impl.TaxonomicExpandedDoc;
import de.paul.pairwiseSimilarity.graphs.MWBG_Factory;
import de.paul.pairwiseSimilarity.graphs.MWBG_Factory.MWBG_mode;
import de.paul.pairwiseSimilarity.graphs.SingleEdge_MWBG;
import de.paul.pairwiseSimilarity.graphs.TaxonomicScoring;
import de.paul.pairwiseSimilarity.graphs.TaxonomicScoring.ScoreMode;
import de.paul.pairwiseSimilarity.graphs.WeightedBipartiteGraphImpl;
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
		PairwiseSimScorer<TaxonomicExpandedDoc> {

	private DBPediaHandler dbpHandler;
	private HierarchyHandler hierHandler;
	private StatUtil meanMachine;

	public TaxonomicalDocScorer(String rankingsCSVPath) {

		super(rankingsCSVPath);
		/*
		 * objects used for generation of documents in create new doc method
		 */
		dbpHandler = DBPediaHandler.getInstance(Paths.TDB_DBPEDIA);
		hierHandler = HierarchyHandler.getInstance(Paths.TDB_DBPEDIA_HIERARCHY);
		/*
		 * to calc mean and variance
		 */
		meanMachine = new StatUtil();
	}

	// public static void main(String[] args) {
	//
	// TaxonomicalDocScorer annSimScorer = null;
	// try {
	//
	// annSimScorer = new TaxonomicalDocScorer(Paths.ANNSIM_RANKING_SCORES);
	// /*
	// * import documents in memory-based index
	// */
	//
	// JSONLoader jsonParser = new JSONLoader(
	// Paths.PINCOMBE_ANNOTATED_JSON);
	// List<AnnotatedDoc> docs = jsonParser.getAllDocs();
	// annSimScorer.setCorpus(docs);
	// MWBG_Factory.mwbgMode = MWBG_mode.SingleEdge;
	// TaxonomicScoring.mode = ScoreMode.dps;
	// // annSimScorer.score(doc1, doc3);
	// // annSimScorer.computeSimilarities(doc1, doc4);
	// annSimScorer.csvAllRankingScores();
	// } catch (Exception e) {
	// e.printStackTrace();
	// } finally {
	// annSimScorer.closeHelpers();
	// }
	// }
	public static void main(String[] args) {
		TaxonomicalDocScorer comboScorer = new TaxonomicalDocScorer(
				Paths.ANNSIM_RANKING_SCORES + "3");
		JSONLoader jsonParser = new JSONLoader(Paths.PINCOMBE_ANNOTATED_JSON);
		// JSONLoader jsonParser = new JSONLoader(Paths.MOMIJSONOUTPUTPATH);
		// JSONLoader jsonParser = new JSONLoader(Paths.LI_JSONOUTPUTPATH);
		List<AnnotatedDoc> docs = jsonParser.getAllDocs();
		comboScorer.expandAndSetCorpus(docs);
		MWBG_Factory.mwbgMode = MWBG_mode.SingleEdge;
		TaxonomicScoring.mode = ScoreMode.dtax;
		System.out.println(comboScorer
				.completePairwisePearsonScore("statistics/pairs.csv"));
		// comboScorer.printMeanAndVariance();
	}

	public void printMeanAndVariance() {

		double mean = meanMachine.getMean();
		System.out.println("Mean: " + mean + ", variance: "
				+ meanMachine.getVariance(mean));
	}

	protected String writeCSVHeader() {

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

	private void closeHelpers() {

		dbpHandler.close();
		hierHandler.close();
	}

	@Override
	protected void computeRanking(String queryDoc) {

		MWBG_Factory.mwbgMode = MWBG_mode.SingleEdge;
		TaxonomicScoring.mode = ScoreMode.dtax;
		List<TaxonomicExpandedDoc> results0 = getRelatedDocuments(queryDoc);
		oneDocResults = new HashMap<Integer, List<TaxonomicExpandedDoc>>();
		MWBG_Factory.mwbgMode = MWBG_mode.SingleEdge;
		TaxonomicScoring.mode = ScoreMode.dps;
		List<TaxonomicExpandedDoc> results1 = getRelatedDocuments(queryDoc);
		oneDocResults = new HashMap<Integer, List<TaxonomicExpandedDoc>>();
		MWBG_Factory.mwbgMode = MWBG_mode.WeightedSingleEdge;
		TaxonomicScoring.mode = ScoreMode.dtax;
		List<TaxonomicExpandedDoc> results2 = getRelatedDocuments(queryDoc);
		oneDocResults = new HashMap<Integer, List<TaxonomicExpandedDoc>>();
		MWBG_Factory.mwbgMode = MWBG_mode.WeightedSingleEdge;
		TaxonomicScoring.mode = ScoreMode.dps;
		List<TaxonomicExpandedDoc> results3 = getRelatedDocuments(queryDoc);
		oneDocResults = new HashMap<Integer, List<TaxonomicExpandedDoc>>();
		oneDocResults.put(0, results0);
		oneDocResults.put(1, results1);
		oneDocResults.put(2, results2);
		oneDocResults.put(3, results3);
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
