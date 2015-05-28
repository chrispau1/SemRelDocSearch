package de.paul.similarity.docScorers;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.commons.httpclient.methods.GetMethod;

import de.paul.annotations.annotators.AnnotationException;
import de.paul.documents.AnnotatedDoc;
import de.paul.util.RESTHandler;

public class ESADocScorer extends PairwiseDocScorer<AnnotatedDoc> {

	@Override
	public double score(AnnotatedDoc doc1, AnnotatedDoc doc2) {

		String doc1text = doc1.getText();
		String doc2text = doc2.getText();
		double score = -1;
		try {
			score = new ESASimilarityViaREST()
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
	public String writeCSVHeader() {

		return "id,esa";
	}

	class ESASimilarityViaREST extends RESTHandler {

		public double getSimilarity(String doc1Text, String doc2Text)
				throws UnsupportedEncodingException, AnnotationException {

			String url = "http://vmdeb20.deri.ie:8890/esaservice?task=esa&term1="
					+ URLEncoder.encode(doc1Text, "utf-8")
					+ "&term2="
					+ URLEncoder.encode(doc2Text, "utf-8");
			String s = request(new GetMethod(url));
			double score = -1;
			try {
				score = Double.parseDouble(s.substring(1, s.length() - 1));
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
			return score;
		}

	}

}
