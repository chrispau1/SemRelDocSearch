package de.paul.similarity.docScorers;

import java.util.HashMap;
import java.util.List;

import de.paul.db.elasticsearch.ESHandler;
import de.paul.documents.AnnotatedDoc;
import de.paul.documents.RankableDoc;
import de.paul.documents.impl.SemanticallyExpandedDoc;

public class ElasticSearchScorer extends InvertedIndexDocScorer<AnnotatedDoc> {

	private static final int DOC_COUNT = 50;

	public ElasticSearchScorer(String idxName, String docType) {

		documentIndex = ESHandler.getInstance(idxName, docType);
	}

	public ElasticSearchScorer(String idxName, String docType,
			int fixedNumberOfResults) {

		documentIndex = ESHandler.getInstance(idxName, docType);
		this.resultsCount = fixedNumberOfResults;
	}

	public static void main(String[] args) {

		String idxName = "pincombe";
		String docType = "news";
		// String idxName = "es_xlime";
		// String docType = "semExp";
		ElasticSearchScorer scorer = new ElasticSearchScorer(idxName, docType);
		try {
			System.out.println("transverse, hierarchical, text, combo");
			// System.out.println(scorer.csvAllRankingScores());
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

	public SemanticallyExpandedDoc createNewDoc(AnnotatedDoc doc) {

		throw new UnsupportedOperationException();
	}

	@Override
	public void computeRankingForVariations(String queryDoc) {

		int resultsToBeReturned = determineResultsCount(queryDoc);
		List<AnnotatedDoc> traverse = ((ESHandler) documentIndex)
				.traverseSearch(queryDoc, resultsToBeReturned);
		List<AnnotatedDoc> hierarchical = ((ESHandler) documentIndex)
				.hierarchicalSearch(queryDoc, resultsToBeReturned);
		List<AnnotatedDoc> text = ((ESHandler) documentIndex).textSearch(
				queryDoc, resultsToBeReturned);
		List<AnnotatedDoc> combo = ((ESHandler) documentIndex)
				.combinationSearch(queryDoc, resultsToBeReturned);
		rankingsPerVariation = new HashMap<Integer, List<AnnotatedDoc>>();
		getRankingsPerVariation().put(0, traverse);
		getRankingsPerVariation().put(1, hierarchical);
		getRankingsPerVariation().put(2, text);
		getRankingsPerVariation().put(3, combo);
	}

	/*
	 * If a fixed result count has been set via constructor, use it. If not,
	 * return results in a number relative to the number of relevant results
	 */
	private int determineResultsCount(String queryDoc) {
		int resultsToBeReturned;

		if (resultsCount == -1) {
			// simplified
			resultsToBeReturned = getDocCount();
			// List<AnnotatedDoc> humRanks = getHumanRanking(Integer
			// .parseInt(queryDoc));
			// /*
			// * For now, added factor such that more (i.e., at least enough)
			// * elements get returned than get compared in the end
			// */
			// resultsToBeReturned = RESULTS_TO_COMPARE_FACTOR
			// * NDCGEvaluator.getRelevantElementsCount(humRanks, 3.0) * 2;
		} else
			resultsToBeReturned = this.resultsCount;
		return resultsToBeReturned;
	}

	@Override
	public String writeCSVHeader() {

		return "id,traversal,hierarchical,text,evenCombo\n";
	}

	@Override
	public int getDocCount() {

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
