package de.paul.similarity.docScorers;

import java.util.HashMap;
import java.util.List;

import de.paul.corpora.elasticsearch.ESHandler;
import de.paul.docs.AnnotatedDoc;
import de.paul.docs.RankableDoc;
import de.paul.docs.impl.FullyExpandedDoc;
import de.paul.util.Paths;
import de.paul.util.statistics.NDCGEvaluator;

public class ElasticSearchScorer extends SimilarityScorer<AnnotatedDoc> {

	private static final int DOC_COUNT = 50;
	private int resultsCount = -1;

	public ElasticSearchScorer(String rankingCSVPath, String idxName,
			String docType) {

		super(rankingCSVPath);
		documentIndex = ESHandler.getInstance(idxName, docType);
	}

	public ElasticSearchScorer(String rankingCSVPath, String idxName,
			String docType, int fixedNumberOfResults) {

		super(rankingCSVPath);
		documentIndex = ESHandler.getInstance(idxName, docType);
		this.resultsCount = fixedNumberOfResults;
	}

	public static void main(String[] args) {

		String idxName = "pincombe";
		String docType = "news";
		// String idxName = "es_xlime";
		// String docType = "semExp";
		ElasticSearchScorer scorer = new ElasticSearchScorer(
				Paths.ES_RANKING_SCORES, idxName, docType);
		try {
			System.out.println("transverse, hierarchical, text, combo");
			System.out.println(scorer.csvAllRankingScores());
		} finally {
			scorer.close();
		}
	}

	public void close() {

		((ESHandler) documentIndex).shutdown();
	}

	public RankableDoc createScoredDoc(String id, double score) {

		AnnotatedDoc document = documentIndex.getDocument(id);
		document.setScore(score);
		return document;
	}

	public FullyExpandedDoc createNewDoc(AnnotatedDoc doc) {

		throw new UnsupportedOperationException();
	}

	@Override
	protected String evaluateScores(int queryDoc) {

		String scoreString = "";
		/*
		 * get human ranking
		 */
		List<AnnotatedDoc> humRanking = getHumanRanking(queryDoc);
		/*
		 * get algorithmic rankings
		 */
		// evaluate
		// System.out.println("human Ranking: "
		// + humRanking.subList(0,
		// NDCGEvaluator.getRelevantElementsCount(humRanking, 3.0)
		// * RESULTS_TO_COMPARE_FACTOR));
		// init evaluator
		NDCGEvaluator<AnnotatedDoc> evaler = new NDCGEvaluator<AnnotatedDoc>(
				3.0);
		// iterate over different scoring metrics
		for (int i = 0; i < oneDocResults.size(); i++) {
			// evaluate ranked documents by current metric in comparison to
			// human evaluation
			double rankingScore = evaler.evaluateRanking(humRanking,
					oneDocResults.get(i), RESULTS_TO_COMPARE_FACTOR);
			// System.out.println("SingleEdge dps ranking score: " +
			// rankingScore);
			scoreString += rankingScore;
			if (i < oneDocResults.size() - 1)
				scoreString += ",";
		}
		return scoreString;
	}

	@Override
	protected void computeRanking(String queryDoc) {

		int resultsToBeReturned = determineResultsCount(queryDoc);
		List<AnnotatedDoc> traverse = ((ESHandler) documentIndex)
				.traverseSearch(queryDoc, resultsToBeReturned);
		List<AnnotatedDoc> hierarchical = ((ESHandler) documentIndex)
				.hierarchicalSearch(queryDoc, resultsToBeReturned);
		List<AnnotatedDoc> text = ((ESHandler) documentIndex).textSearch(
				queryDoc, resultsToBeReturned);
		List<AnnotatedDoc> combo = ((ESHandler) documentIndex)
				.combinationSearch(queryDoc, resultsToBeReturned);
		oneDocResults = new HashMap<Integer, List<AnnotatedDoc>>();
		oneDocResults.put(0, traverse);
		oneDocResults.put(1, hierarchical);
		oneDocResults.put(2, text);
		oneDocResults.put(3, combo);
	}

	/*
	 * If a fixed result count has been set via constructor, use it. If not,
	 * return results in a number relative to the number of relevant results
	 */
	private int determineResultsCount(String queryDoc) {
		int resultsToBeReturned;

		if (resultsCount == -1) {
			List<AnnotatedDoc> humRanks = getHumanRanking(Integer
					.parseInt(queryDoc));
			/*
			 * For now, added factor such that more (i.e., at least enough)
			 * elements get returned than get compared in the end
			 */
			resultsToBeReturned = RESULTS_TO_COMPARE_FACTOR
					* NDCGEvaluator.getRelevantElementsCount(humRanks, 3.0) * 2;
		} else
			resultsToBeReturned = this.resultsCount;
		return resultsToBeReturned;
	}

	@Override
	protected String writeCSVHeader() {

		return "id,traversal,hierarchical,text,evenCombo\n";
	}

	@Override
	protected int getDocCount() {

		return DOC_COUNT;
	}

	@Override
	public List<AnnotatedDoc> getRelatedDocuments(String queryDoc) {

		int resultsToBeReturned = determineResultsCount(queryDoc);
		/*
		 * Use results from field-combined MLT search
		 */
		List<AnnotatedDoc> results = ((ESHandler) documentIndex)
				.combinationSearch(queryDoc, resultsToBeReturned);
		return results;
	}

}
