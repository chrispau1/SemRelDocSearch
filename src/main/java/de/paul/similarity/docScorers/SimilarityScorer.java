package de.paul.similarity.docScorers;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import de.paul.corpora.DocumentIndex;
import de.paul.docs.AnnotatedDoc;
import de.paul.evaluation.PinCombeEvalHandler;
import de.paul.util.Paths;
import de.paul.util.TimeKeeper;

public abstract class SimilarityScorer<x extends AnnotatedDoc> {

	protected static final int RESULTS_TO_COMPARE_FACTOR = 2;
	private String rankingsCSVPath;
	protected DocumentIndex documentIndex;
	protected HashMap<Integer, List<x>> oneDocResults;

	public SimilarityScorer(String rankingsCSVPath) {

		this.rankingsCSVPath = rankingsCSVPath;
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
	public String csvAllRankingScores() {

		String meanString = "";
		FileWriter fw = null;
		int valsCount = writeCSVHeader().split(",").length - 1;
		double[] vals = new double[valsCount];
		try {
			fw = new FileWriter(rankingsCSVPath);
			fw.write(writeCSVHeader() + "\n");
			// body
			for (int i = 0; i < getDocCount(); i++) {
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
				meanString += "," + (val / getDocCount());

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

	protected abstract int getDocCount();

	/**
	 * Compute ranking of similar documents for one query document. Print return
	 * as comma-separated string, representing a row in a CSV file. Each
	 * comma-separated value is ranking quality for a ranking/similarity method.
	 */
	public String csvRankingScore(int queryDoc) {

		String scoreRow = queryDoc + ",";
		// finds most similar and writes them into result structures
		computeRanking(Integer.toString(queryDoc));
		// System.out.println("evaluating...");
		long t1 = System.currentTimeMillis();
		// add results to string for potential csv file write
		scoreRow += evaluateScores(queryDoc);
		long t2 = System.currentTimeMillis();
		TimeKeeper.getInstance().addEvalTime(t2 - t1);
		// System.out.println(TimeKeeper.getInstance().toString());
		return scoreRow;
	}

	public abstract x createNewDoc(AnnotatedDoc docId);

	protected List<x> getHumanRanking(int queryDocNr) {
		long t1 = System.currentTimeMillis();
		PinCombeEvalHandler evalHandler = PinCombeEvalHandler
				.getInstance(Paths.PINCOMBE_EVAL);
		long t2 = System.currentTimeMillis();
		TimeKeeper.getInstance().addToTimeMap("PinCombeEvalHandlerInit",
				t2 - t1);
		List<x> humRanking = new LinkedList<x>();
		try {
			List<Entry<Integer, Double>> rankingEntries = evalHandler
					.getRanking(queryDocNr, 0.0);
			for (Entry<Integer, Double> entry : rankingEntries) {

				x doc = documentIndex.getDocument(entry.getKey());
				doc.setScore(entry.getValue());
				humRanking.add(doc);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		t1 = System.currentTimeMillis();
		TimeKeeper.getInstance().addToTimeMap("getRanking", t1 - t2);
		return humRanking;
	}

	protected abstract String evaluateScores(int queryDoc);

	public abstract List<x> getRelatedDocuments(String queryDoc);

	protected abstract void computeRanking(String queryDoc);

	/**
	 * Return the string that makes the first row, the column description, of
	 * the CSV file containing ranking quality scores for all documents.
	 * 
	 * @return
	 */
	protected abstract String writeCSVHeader();
}
