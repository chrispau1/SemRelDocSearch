package de.paul.similarity.docScorers;

import java.util.List;

import de.paul.corpora.JSONLoader;
import de.paul.dbpedia.DBPediaHandler;
import de.paul.docs.AnnotatedDoc;
import de.paul.docs.impl.TransversalPerAnnotExpandedDoc;
import de.paul.pairwiseSimilarity.graphs.MWBG_Factory;
import de.paul.pairwiseSimilarity.graphs.MWBG_Factory.MWBG_mode;
import de.paul.pairwiseSimilarity.graphs.SingleEdge_MWBG;
import de.paul.pairwiseSimilarity.graphs.WeightedBipartiteGraphImpl;
import de.paul.util.Paths;
import de.paul.util.statistics.NDCGEvaluator;
import de.paul.util.statistics.StatUtil;

public class TransversalDocScorer extends
		PairwiseSimScorer<TransversalPerAnnotExpandedDoc> {

	private DBPediaHandler dbpHandler;

	private StatUtil meanMachine;

	private static final int EXPAND_RADIUS = 2; // best: 2

	public TransversalDocScorer(String rankingsCSVPath) {
		super(rankingsCSVPath);
		// objects used for new doc creation
		dbpHandler = DBPediaHandler.getInstance(Paths.TDB_DBPEDIA);
		meanMachine = new StatUtil();
	}

	// public static void main(String[] args) {
	//
	// TransversalDocScorer scorer = new TransversalDocScorer(
	// Paths.TRANSVERSAL_RANKING_SCORES);
	// System.out.println("Loading and expanding documents...");
	//
	// JSONLoader jsonParser = new JSONLoader(Paths.PINCOMBE_ANNOTATED_JSON);
	// List<AnnotatedDoc> docs = jsonParser.getAllDocs();
	// scorer.setCorpus(docs);
	// System.out.println("Computing similarity / rankings...");
	// // TransversalPerAnnotExpandedDoc doc1 = pincombeIndex.getDocument(39);
	// // TransversalPerAnnotExpandedDoc doc2 = pincombeIndex.getDocument(42);
	// // double score = scorer.score(doc1, doc2);
	// // System.out.println(score);
	// // System.out.println(scorer.csvRankingScore(39));
	// System.out.println(scorer.csvAllRankingScores());
	// }
	public static void main(String[] args) {
		TransversalDocScorer transScorer = new TransversalDocScorer(
				Paths.TRANSVERSAL_RANKING_SCORES);
		JSONLoader jsonParser = new JSONLoader(Paths.PINCOMBE_ANNOTATED_JSON);
		List<AnnotatedDoc> docs = jsonParser.getAllDocs();
		transScorer.expandAndSetCorpus(docs);
		System.out.println(transScorer
				.completePairwisePearsonScore("statistics/pairs.csv"));
		transScorer.printMeanAndVariance();
	}

	public void printMeanAndVariance() {

		double mean = meanMachine.getMean();
		System.out.println("Mean: " + mean + ", variance: "
				+ meanMachine.getVariance(mean));
	}

	public TransversalPerAnnotExpandedDoc createNewDoc(AnnotatedDoc doc) {

		return new TransversalPerAnnotExpandedDoc(doc, dbpHandler,
				EXPAND_RADIUS);
	}

	@Override
	public double score(TransversalPerAnnotExpandedDoc doc1,
			TransversalPerAnnotExpandedDoc doc2) {

		WeightedBipartiteGraphImpl bipartiteGraph = new WeightedBipartiteGraphImpl(
				doc1, doc2);
		// print matrix
		// printPairwiseEntityScoreMatrix(bipartiteGraph);
		// do scoring
		MWBG_Factory.mwbgMode = MWBG_mode.SingleEdge;
		SingleEdge_MWBG se_MWBG = bipartiteGraph.findSingleEdgeMaximumWeight();
		double result = se_MWBG.similarityScore();
		// double result = bipartiteGraph.similarityScore();
		// System.out.println(result);
		meanMachine.registerScore(result, true);
		return result;
	}

	@Override
	protected String writeCSVHeader() {

		return "id,overlap";
	}

	@Override
	protected String evaluateScores(int queryDoc) {

		String scoreString = "";
		/*
		 * get human ranking
		 */
		List<TransversalPerAnnotExpandedDoc> humRanking = getHumanRanking(queryDoc);
		/*
		 * get algorithmic rankings
		 */
		// evaluate
		// System.out.println("human Ranking: "
		// + humRanking.subList(0,
		// NDCGEvaluator.getRelevantElementsCount(humRanking, 3.0)
		// * RESULTS_TO_COMPARE_FACTOR));
		// init evaluator
		NDCGEvaluator<TransversalPerAnnotExpandedDoc> evaler = new NDCGEvaluator<TransversalPerAnnotExpandedDoc>(
				3.0);
		// evaluate ranked documents in comparison to human evaluation
		double rankingScore = evaler.evaluateRanking(humRanking, oneDocResults
				.values().iterator().next(), RESULTS_TO_COMPARE_FACTOR);
		System.out.println("Transversal ranking score: " + rankingScore);
		scoreString += rankingScore;
		return scoreString;
	}

}
