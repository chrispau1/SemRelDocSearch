package de.paul.evaluation.scorers;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import de.paul.documents.AnnotatedDoc;
import de.paul.evaluation.corpora.CorpusEvalHandler;
import de.paul.evaluation.corpora.RankingEvalHandler;
import de.paul.similarity.docScorers.DocumentSimilarityScorer;
import de.paul.util.TimeKeeper;
import de.paul.util.statistics.NDCGEvaluator;

public class DocScorerEvaluator<x extends AnnotatedDoc> {

	protected static final int RESULTS_TO_COMPARE_FACTOR = 2;
	protected CorpusEvalHandler evalHandler;
	protected DocumentSimilarityScorer<x> scorer;
	private String rankingsCSVPath;

	public DocScorerEvaluator(DocumentSimilarityScorer<x> scorer,
			CorpusEvalHandler evalHandler, String rankingCSVPath) {

		this.scorer = scorer;
		this.evalHandler = evalHandler;
		this.rankingsCSVPath = rankingCSVPath;
	}

	/**
	 * Compute pairwise similarities between all documents in corpus given to
	 * this object. Then rank them per document descendingly according to their
	 * scores. Calls computeRanking method for each document, which returns a
	 * CSV row. Builds all these rows into a CSV file that is saved at path
	 * given in constructor.
	 * 
	 * @return
	 */
	public String csvAllRankingsScores() {

		String meanString = "";
		FileWriter fw = null;
		int valsCount = scorer.writeCSVHeader().split(",").length - 1;
		double[] vals = new double[valsCount];
		try {
			fw = new FileWriter(rankingsCSVPath);
			fw.write(scorer.writeCSVHeader() + "\n");
			// body
			for (int i = 0; i < scorer.getDocCount(); i++) {
				String row = csvRankingScore(i);
				fw.write(row + "\n");
				/*
				 * just for experimentation
				 */
				String[] rowFields = row.split(",");
				for (int j = 1; j < rowFields.length; j++)
					vals[j - 1] += Double.parseDouble(rowFields[j]);
			}
			/*
			 * get mean
			 */
			for (double val : vals)
				meanString += "," + (val / scorer.getDocCount());

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return meanString;
	}

	/**
	 * Compute ranking of similar documents for one query document. Print return
	 * as comma-separated string, representing a row in a CSV file. Each
	 * comma-separated value is ranking quality for a ranking/similarity method.
	 */
	public String csvRankingScore(int queryDoc) {

		String scoreRow = queryDoc + ",";
		// finds most similar and writes them into result structures
		scorer.computeRankingForVariations(Integer.toString(queryDoc));
		// System.out.println("evaluating...");
		long t1 = System.currentTimeMillis();
		// add results to string for potential csv file write
		scoreRow += evaluateScores(queryDoc);
		long t2 = System.currentTimeMillis();
		TimeKeeper.getInstance().addEvalTime(t2 - t1);
		// System.out.println(TimeKeeper.getInstance().toString());
		return scoreRow;
	}

	protected List<x> getHumanRanking(int queryDocNr) {
		List<x> humRanking = new LinkedList<x>();
		try {
			if (!(evalHandler instanceof RankingEvalHandler))
				try {
					throw new Exception(
							"Cannot evaluate rankings because EvalHandler does not provide rankings.");
				} catch (Exception e) {
					e.printStackTrace();
				}
			List<Entry<Integer, Double>> rankingEntries = ((RankingEvalHandler) evalHandler)
					.getRanking(queryDocNr, 0.0);
			for (Entry<Integer, Double> entry : rankingEntries) {

				x doc = scorer.getDocumentFromIndex(Integer.toString(entry
						.getKey()));
				doc.setScore(entry.getValue());
				humRanking.add(doc);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		return humRanking;
	}

	protected String evaluateScores(int queryDoc) {
		String scoreString = "";
		/*
		 * get human ranking
		 */
		List<x> humRanking = getHumanRanking(queryDoc);
		/*
		 * get algorithmic rankings
		 */
		// evaluate
		// System.out.println("human Ranking: "
		// + humRanking.subList(0,
		// NDCGEvaluator.getRelevantElementsCount(humRanking, 3.0)
		// * RESULTS_TO_COMPARE_FACTOR));
		HashMap<Integer, List<x>> rankingPerVariation = scorer
				.getRankingsPerVariation();
		NDCGEvaluator<x> evaler = new NDCGEvaluator<x>(3.0);
		for (int i = 0; i < rankingPerVariation.size(); i++) {
			// evaluate ranked documents by current metric in comparison to
			// human evaluation
			double rankingScore = evaler.evaluateRanking(humRanking,
					rankingPerVariation.get(i), RESULTS_TO_COMPARE_FACTOR);
			// System.out.println(i + "-th AnnSim score: " + rankingScore);
			scoreString += rankingScore;
			if (i < rankingPerVariation.size() - 1)
				scoreString += ",";
		}
		return scoreString;
	}

}
