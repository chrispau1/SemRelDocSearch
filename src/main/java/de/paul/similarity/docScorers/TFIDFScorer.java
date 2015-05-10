package de.paul.similarity.docScorers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.paul.corpora.JSONLoader;
import de.paul.docs.AnnotatedDoc;
import de.paul.util.FeatureExtractor;
import de.paul.util.Paths;
import de.paul.util.statistics.CosineSimilarity;

public class TFIDFScorer extends PairwiseSimScorer<AnnotatedDoc> {

	private Map<String, HashMap<String, Double>> docVecMap = new HashMap<String, HashMap<String, Double>>();
	private FeatureExtractor featureExtractor;

	public static void main(String[] args) {
		TFIDFScorer scorer = new TFIDFScorer();
		JSONLoader jsonParser = new JSONLoader(Paths.PINCOMBE_ANNOTATED_JSON);
		List<AnnotatedDoc> docs = jsonParser.getAllDocs();
		scorer.expandAndSetCorpus(docs);
		scorer.createFeatureExtractor();
		System.out.println(scorer
				.completePairwisePearsonScore("statistics/pairs.csv"));
	}

	private void createFeatureExtractor() {

		HashMap<String, String> input = new HashMap<String, String>();
		for (int i = 0; i < documentIndex.getDocCount(); i++)
			input.put(Integer.toString(i),
					documentIndex.getDocument(Integer.toString(i)).getText());
		featureExtractor = new FeatureExtractor(input, true, true, true);
		docVecMap = featureExtractor.getTfidf();
	}

	public TFIDFScorer() {
		super(Paths.TFIDF_RANKING_SCORES);
	}

	@Override
	public double score(AnnotatedDoc doc1, AnnotatedDoc doc2) {

		HashMap<String, Double> docVec1 = docVecMap.get(doc1.getId());
		HashMap<String, Double> docVec2 = docVecMap.get(doc2.getId());
		return CosineSimilarity.score(docVec1, docVec2);
	}

	@Override
	public AnnotatedDoc createNewDoc(AnnotatedDoc doc) {

		return new AnnotatedDoc(doc.getText(), doc.getTitle(), doc.getId());
	}

	@Override
	protected String writeCSVHeader() {
		return "id,tfidf";
	}

}
