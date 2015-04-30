package de.paul.similarity.docScorers;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;

import de.paul.corpora.SimpleDocumentIndex;
import de.paul.docs.AnnotatedDoc;
import de.paul.docs.impl.DocPair;
import de.paul.evaluation.EvalHandler;
import de.paul.evaluation.PinCombeEvalHandler;
import de.paul.pairwiseSimilarity.graphs.WeightedBipartiteGraphImpl;
import de.paul.util.Paths;

/**
 * Abstract, generic class for pairwise document scoring. Defines methods for
 * common use cases, like the actual similarity computation of two documents or
 * the ranking of one or many documents of a corpus.
 * 
 * @author Chris
 *
 * @param <x>
 */
public abstract class PairwiseSimScorer<x extends AnnotatedDoc> extends
		SimilarityScorer<x> {

	private EvalHandler evalHandler;

	public PairwiseSimScorer(String rankingsCSVPath) {

		super(rankingsCSVPath);
		evalHandler = PinCombeEvalHandler.getInstance(Paths.PINCOMBE_EVAL);
		// evalHandler = Li2006EvalHandler.getInstance(Paths.LI_EVALPATH_CSV);//
		// MOMIEVALPATH_CSV);
	}

	public String completePairwisePearsonScore(String pairsCSVPath) {

		ArrayList<DocPair<x>> docPairs = new ArrayList<DocPair<x>>();
		for (int i = 0; i < getDocCount() - 1; i++) {

			for (int j = i + 1; j < getDocCount(); j++) {

				try {
					if (evalHandler.getSimilarity(i, j) != null) {
						x doc1 = getDocumentFromIndex(Integer.toString(i));
						x doc2 = getDocumentFromIndex(Integer.toString(j));
						docPairs.add(new DocPair<x>(doc1, doc2));
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		String pears = pearsonScorePairs(docPairs.toArray(new DocPair[] {}),
				pairsCSVPath);
		return pears;
	}

	public String pearsonScorePairs(DocPair<x>[] pairs, String pairsCSVPath) {

		ArrayList<Double> algoScores = new ArrayList<Double>();
		ArrayList<Double> humanScores = new ArrayList<Double>();
		for (int i = 0; i < pairs.length; i++) {
			DocPair<x> pair = pairs[i];
			/*
			 * TODO: testing on all the ones with annotations right now
			 */
			// List<Annotatable> annot1 = pair.getDoc1().getAnnotations();
			// List<Annotatable> annot2 = pair.getDoc2().getAnnotations();
			// if (annot1.size() > 0 && annot2.size() > 0) {
			double algoScore = score(pair.getDoc1(), pair.getDoc2());
			algoScores.add(algoScore);
			int id1 = Integer.parseInt(pair.getDoc1().getId());
			int id2 = Integer.parseInt(pair.getDoc2().getId());
			double humanScore = 0;
			try {
				humanScore = evalHandler.getSimilarity(id1, id2);
			} catch (IOException e) {
				e.printStackTrace();
			}
			humanScores.add(humanScore);
			// }
		}
		// compute correlation metrics
		System.out.println("pairs with annotations each: " + algoScores.size());
		double[] algoScoresArray = new double[algoScores.size()];
		double[] humanScoresArray = new double[humanScores.size()];
		for (int i = 0; i < algoScores.size(); i++) {
			algoScoresArray[i] = algoScores.get(i);
			humanScoresArray[i] = humanScores.get(i);
		}
		double pearsScore = new PearsonsCorrelation().correlation(
				algoScoresArray, humanScoresArray);
		double spearmansScore = new SpearmansCorrelation().correlation(
				algoScoresArray, humanScoresArray);
		double harmMean = 2.0 / (1.0 / pearsScore + 1.0 / spearmansScore);
		return pearsScore + "," + spearmansScore + "," + harmMean;
	}

	public void printMeanAndVariance() {

		throw new UnsupportedOperationException();
	}

	public abstract double score(x doc1, x doc2);

	protected x getDocumentFromIndex(String docId) {
		return documentIndex.getDocument(docId);
	}

	public x createScoredDoc(String id, double score) {

		x doc = getDocumentFromIndex(id);
		doc.setScore(score);
		return doc;
	}

	protected void printPairwiseEntityScoreMatrix(
			WeightedBipartiteGraphImpl bipartiteGraph) {

		FileWriter fw = null;
		try {
			fw = new FileWriter(Paths.SCOREMATRIX_FILEPATH + "_left");
			String text = bipartiteGraph.printPairwiseLeftScoreMatrix();
			fw.write(text);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			fw = new FileWriter(Paths.SCOREMATRIX_FILEPATH + "_right");
			String text = bipartiteGraph.printPairwiseRightScoreMatrix();
			System.out.println(text);
			fw.write(text);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void expandAndSetCorpus(List<AnnotatedDoc> docs) {

		documentIndex = new SimpleDocumentIndex();
		for (AnnotatedDoc plainDoc : docs) {

			x doc = this.createNewDoc(plainDoc);
			((SimpleDocumentIndex) documentIndex).addDocument(doc);
		}
	}

	public void setCorpus(List<x> docs) {

		documentIndex = new SimpleDocumentIndex();
		for (AnnotatedDoc doc : docs) {
			((SimpleDocumentIndex) documentIndex).addDocument(doc.copy());
		}
	}

	protected int getDocCount() {
		return documentIndex.getDocCount();
	}

	@Override
	public List<x> getRelatedDocuments(String queryDoc) {

		// System.out.println("Loading query doc...");
		x doc1 = getDocumentFromIndex(queryDoc);
		// System.out.println("Loaded query doc");
		int docCount = this.getDocCount();
		List<x> results = new ArrayList<x>();
		// simScores = new HashMap<Integer, Double>();
		for (String id : documentIndex.getDocIds()) {
			// only compute for different docs
			if (!id.equals(queryDoc)) {
				x doc2 = getDocumentFromIndex(id);
				// call scoring function for each pair
				// simScores.put(i, score(doc1, doc2));
				doc2.setScore(score(doc1, doc2));
				results.add(doc2);
			}
		}
		Collections.sort(results, Collections.reverseOrder());
		return results;
	}

	@Override
	protected void computeRanking(String queryDoc) {

		List<x> results = getRelatedDocuments(queryDoc);
		oneDocResults = new HashMap<Integer, List<x>>();
		oneDocResults.put(0, results);
		// System.out.println("Ranking computed.");
	}

}
