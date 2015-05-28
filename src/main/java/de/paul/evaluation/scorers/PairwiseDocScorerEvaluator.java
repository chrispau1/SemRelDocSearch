package de.paul.evaluation.scorers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;

import de.paul.corpus.evaluation.EvalHandler;
import de.paul.corpus.evaluation.LeeEvalHandler;
import de.paul.db.JSONDocSourceLoader;
import de.paul.documents.AnnotatedDoc;
import de.paul.documents.impl.DocPair;
import de.paul.documents.impl.SemanticallyExpandedDoc;
import de.paul.similarity.docScorers.SemanticallyExpandedDocScorer;
import de.paul.similarity.docScorers.PairwiseDocScorer;
import de.paul.util.Paths;

public class PairwiseDocScorerEvaluator<x extends AnnotatedDoc> extends
		DocScorerEvaluator<x> {

	public static void main(String[] args) {

		SemanticallyExpandedDocScorer comboScorer = new SemanticallyExpandedDocScorer();
		PairwiseDocScorerEvaluator<SemanticallyExpandedDoc> fullEvaluator = new PairwiseDocScorerEvaluator<SemanticallyExpandedDoc>(
				comboScorer, LeeEvalHandler.getInstance(Paths.PINCOMBE_EVAL),
				Paths.COMBINATION_RANKING_SCORES);
		// JSONLoader jsonParser = new JSONLoader(Paths.MOMIJSONOUTPUTPATH);
		JSONDocSourceLoader jsonParser = new JSONDocSourceLoader(Paths.LEE_ANNOTATED_JSON);// _CORRECTED_JSON);
		List<AnnotatedDoc> docs = jsonParser.getAllDocs();
		System.out.println("Documents loaded");
		comboScorer.expandAndSetCorpus(docs);
		System.out.println("Documents expanded");
		System.out.println(fullEvaluator
				.completePairwisePearsonScore("statistics/pairs.csv"));
	}

	public PairwiseDocScorerEvaluator(PairwiseDocScorer<x> simScorer,
			EvalHandler evalHandler, String csvRankingsPath) {

		super(simScorer, evalHandler, csvRankingsPath);
	}

	public String completePairwisePearsonScore(String pairsCSVPath) {

		PairwiseDocScorer<x> castedScorer = ((PairwiseDocScorer<x>) scorer);
		ArrayList<DocPair<x>> docPairs = new ArrayList<DocPair<x>>();
		for (int i = 0; i < castedScorer.getDocCount() - 1; i++) {

			for (int j = i + 1; j < castedScorer.getDocCount(); j++) {

				try {
					if (evalHandler.getSimilarity(i, j) != null) {
						x doc1 = castedScorer.getDocumentFromIndex(Integer
								.toString(i));
						x doc2 = castedScorer.getDocumentFromIndex(Integer
								.toString(j));
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
			double algoScore = ((PairwiseDocScorer<x>) scorer).score(
					pair.getDoc1(), pair.getDoc2());
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

}
