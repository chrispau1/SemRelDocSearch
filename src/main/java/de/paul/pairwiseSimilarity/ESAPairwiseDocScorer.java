package de.paul.pairwiseSimilarity;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.commons.httpclient.methods.GetMethod;

import de.paul.annotations.annotators.AnnotationException;
import de.paul.util.RESTHandler;

public class ESAPairwiseDocScorer extends RESTHandler {

	public double getSimilarity(String doc1Text, String doc2Text)
			throws UnsupportedEncodingException, AnnotationException {

		String url = "http://vmdeb20.deri.ie:8890/esaservice?task=esa&term1="
				+ URLEncoder.encode(doc1Text, "utf-8") + "&term2="
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
