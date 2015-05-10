package de.paul.similarity.docScorers;

import java.io.UnsupportedEncodingException;
import java.util.List;

import de.paul.annotations.annotators.AnnotationException;
import de.paul.corpora.JSONLoader;
import de.paul.docs.AnnotatedDoc;
import de.paul.pairwiseSimilarity.ESAPairwiseDocScorer;
import de.paul.util.Paths;

public class ESADocScorer extends PairwiseSimScorer<AnnotatedDoc> {

	public ESADocScorer(String rankingsCSVPath) {
		super(rankingsCSVPath);
	}

	// public static void main(String[] args) {
	//
	// ESADocScorer scorer = new ESADocScorer(Paths.ESA_RANKING_SCORES);
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
	// scorer.csvAllRankingScores();
	// }

	public static void main(String[] args) {
		ESADocScorer esaScorer = new ESADocScorer(Paths.ESA_RANKING_SCORES);
		JSONLoader jsonParser = new JSONLoader(Paths.PINCOMBE_ANNOTATED_JSON);
		List<AnnotatedDoc> docs = jsonParser.getAllDocs();
		esaScorer.expandAndSetCorpus(docs);
		System.out.println(esaScorer
				.completePairwisePearsonScore("statistics/pairs.csv"));
	}

	@Override
	public double score(AnnotatedDoc doc1, AnnotatedDoc doc2) {

		String doc1text = doc1.getText();
		String doc2text = doc2.getText();
		double score = -1;
		try {
			score = new ESAPairwiseDocScorer()
					.getSimilarity(doc1text, doc2text);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (AnnotationException e) {
			e.printStackTrace();
		}
		return score;
	}

	@Override
	public AnnotatedDoc createNewDoc(AnnotatedDoc doc) {

		return new AnnotatedDoc(doc);
	}

	@Override
	protected String writeCSVHeader() {

		return "id,esa";
	}

}
