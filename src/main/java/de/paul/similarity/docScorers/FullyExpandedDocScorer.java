package de.paul.similarity.docScorers;

import java.util.List;

import de.paul.corpora.JSONLoader;
import de.paul.dbpedia.DBPediaHandler;
import de.paul.dbpedia.categories.HierarchyHandler;
import de.paul.docs.AnnotatedDoc;
import de.paul.docs.impl.FullyExpandedDoc;
import de.paul.pairwiseSimilarity.graphs.MWBG_Factory;
import de.paul.pairwiseSimilarity.graphs.MWBG_Factory.MWBG_mode;
import de.paul.pairwiseSimilarity.graphs.SingleEdge_MWBG;
import de.paul.pairwiseSimilarity.graphs.WeightedBipartiteGraphImpl;
import de.paul.util.Paths;
import de.paul.util.statistics.NDCGEvaluator;

public class FullyExpandedDocScorer extends PairwiseSimScorer<FullyExpandedDoc> {

	private static final int EXPANSION_RADIUS = 2;// best: 3
	private DBPediaHandler dbpHandler;
	private HierarchyHandler hierHandler;

	// public static void main(String[] args) {
	// /*
	// * initialize combination scorer
	// */
	// FullyExpandedDocScorer comboScorer = new FullyExpandedDocScorer(
	// Paths.COMBINATION_RANKING_SCORES);
	// JSONLoader jsonParser = new JSONLoader(
	// Paths.PINCOMBE_ANNOTATED_CORRECTED_JSON);
	// // JSONLoader jsonParser = new JSONLoader(
	// // Paths.PINCOMBE_ANNOTATED_CORRECTED_JSON);
	// List<AnnotatedDoc> docs = jsonParser.getAllDocs();
	// System.out.println("Docs loaded");
	// comboScorer.expandAndSetCorpus(docs);
	// System.out.println("Docs expanded");
	//
	// System.out.println("Computing similarity / rankings...");
	// System.out.println(comboScorer.csvAllRankingScores());
	// }

	public static void main(String[] args) {
		FullyExpandedDocScorer comboScorer = new FullyExpandedDocScorer(
				Paths.COMBINATION_RANKING_SCORES);
		// JSONLoader jsonParser = new JSONLoader(Paths.MOMIJSONOUTPUTPATH);
		JSONLoader jsonParser = new JSONLoader(Paths.PINCOMBE_ANNOTATED_JSON);// _CORRECTED_JSON);
		List<AnnotatedDoc> docs = jsonParser.getAllDocs();
		System.out.println("Documents loaded");
		comboScorer.expandAndSetCorpus(docs);
		System.out.println("Documents expanded");
		System.out.println(comboScorer
				.completePairwisePearsonScore("statistics/pairs.csv"));
	}

	// public static void main(String[] args) {
	// FullyExpandedDocScorer comboScorer = new FullyExpandedDocScorer(
	// Paths.COMBINATION_RANKING_SCORES);
	// // JSONLoader jsonParser = new JSONLoader(Paths.MOMIJSONOUTPUTPATH);
	// ArrayList<AnnotatedDoc> docs = new ArrayList<AnnotatedDoc>();
	// ArrayList<Annotatable> annots0 = new ArrayList<Annotatable>();
	// annots0.add(new WeightedAnnotation(
	// "http://dbpedia.org/resource/Gregg_Popovich", 1.0));
	// annots0.add(new WeightedAnnotation(
	// "http://dbpedia.org/resource/Tony_Parker", 1.0));
	// annots0.add(new WeightedAnnotation(
	// "http://dbpedia.org/resource/San_Antonio", 1.0));
	// docs.add(new QuadDoc("0", "doc0Text", "doc0Title", annots0));
	// ArrayList<Annotatable> annots1 = new ArrayList<Annotatable>();
	// annots1.add(new WeightedAnnotation(
	// "http://dbpedia.org/resource/Dirk_Nowitzki", 1.0));
	// annots1.add(new WeightedAnnotation("http://dbpedia.org/resource/Texas",
	// 1.0));
	// annots1.add(new WeightedAnnotation(
	// "http://dbpedia.org/resource/Dallas", 1.0));
	// docs.add(new QuadDoc("1", "doc1Text", "doc1Title", annots1));
	// comboScorer.expandAndSetCorpus(docs);
	// System.out.println(comboScorer
	// .completePairwisePearsonScore("statistics/pairs.csv"));
	//
	// }

	public FullyExpandedDocScorer(String rankingsCSVPath) {

		super(rankingsCSVPath);
		/*
		 * objects used for generation of documents in create new doc method
		 */
		dbpHandler = DBPediaHandler.getInstance(Paths.TDB_DBPEDIA);
		hierHandler = HierarchyHandler.getInstance(Paths.TDB_DBPEDIA_HIERARCHY);
	}

	public FullyExpandedDoc createNewDoc(AnnotatedDoc doc) {

		return new FullyExpandedDoc(doc, EXPANSION_RADIUS, dbpHandler,
				hierHandler, documentIndex);
	}

	@Override
	public double score(FullyExpandedDoc doc1, FullyExpandedDoc doc2) {

		WeightedBipartiteGraphImpl bipartiteGraph = new WeightedBipartiteGraphImpl(
				doc1, doc2);
		// print matrix
		// if (doc1.getId().equals("8") && doc2.getId().equals("23"))
		// printPairwiseEntityScoreMatrix(bipartiteGraph);
		// do scoring
		MWBG_Factory.mwbgMode = MWBG_mode.SingleEdge;
		SingleEdge_MWBG se_MWBG = bipartiteGraph.findSingleEdgeMaximumWeight();
		double result = se_MWBG.similarityScore();
		// System.out.println(result);
		return result;
	}

	@Override
	protected String evaluateScores(int queryDoc) {

		String scoreString = "";
		/*
		 * get human ranking
		 */
		List<FullyExpandedDoc> humRanking = getHumanRanking(queryDoc);
		/*
		 * get algorithmic rankings
		 */
		// evaluate
		// System.out.println("human Ranking: "
		// + humRanking.subList(0,
		// NDCGEvaluator.getRelevantElementsCount(humRanking, 3.0)
		// * RESULTS_TO_COMPARE_FACTOR));

		// init evaluator
		NDCGEvaluator<FullyExpandedDoc> evaler = new NDCGEvaluator<FullyExpandedDoc>(
				3.0);
		// evaluate ranked documents in comparison to human evaluation
		// use first element of doc results map cause there should only be one
		// entry
		double rankingScore = evaler.evaluateRanking(humRanking, oneDocResults
				.values().iterator().next(), RESULTS_TO_COMPARE_FACTOR);
		// System.out.println("Combo ranking score: " + rankingScore);
		scoreString += rankingScore;
		return scoreString;
	}

	@Override
	protected String writeCSVHeader() {

		return "id,combination";
	}

}
