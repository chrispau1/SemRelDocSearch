package de.paul.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

public class FeatureExtractor {

	public static Logger logger = Logger.getLogger(FeatureExtractor.class);

	HashMap<String, String> input;
	private LinkedList<String> labels;
	private LinkedList<String> words;
	private HashMap<String, HashMap<String, Double>> tf;
	private HashMap<String, Integer> df;
	private HashMap<String, HashMap<String, Double>> tfidf;
	private boolean tfTransform;
	private boolean idfTransform;
	private boolean lengthNorm;

	public FeatureExtractor(HashMap<String, String> input, boolean tfTransform,
			boolean idfTransform, boolean lengthNorm) {
		this.input = input;
		this.labels = new LinkedList<String>();
		this.words = new LinkedList<String>();
		this.tf = new HashMap<String, HashMap<String, Double>>();
		this.df = new HashMap<String, Integer>();
		this.tfidf = new HashMap<String, HashMap<String, Double>>();
		this.tfTransform = tfTransform;
		this.idfTransform = idfTransform;
		this.lengthNorm = lengthNorm;

		this.extract();
	}

	public void extract() {

		if (this.input == null)
			return;

		String property;
		int numberOfdocuments = 0;

		HashSet<String> labelSet = new HashSet<String>();
		HashSet<String> wordSet = new HashSet<String>();

		for (Entry<String, String> entry : input.entrySet()) {

			property = entry.getKey();
			String value = entry.getValue();

			labelSet.add(property);

			if (!tf.containsKey(property)) {
				HashMap<String, Double> tokens = new HashMap<String, Double>();
				tf.put(property, tokens);
				numberOfdocuments++;
			}

			try {
				for (String token : LuceneUtils.getTokens(value)) {

					wordSet.add(token);

					if (tf.get(property).containsKey(token))
						tf.get(property).put(token,
								tf.get(property).get(token) + 1);
					else {
						tf.get(property).put(token, 1.0);
						if (!df.containsKey(token))
							df.put(token, 1);
						else
							df.put(token, df.get(token) + 1);
					}
				}
			} catch (IOException e) {
				logger.error("Error in tokenizing the string \"" + value + "\"");
				e.printStackTrace();
				continue;
			}
		}

		labels.addAll(labelSet);
		words.addAll(wordSet);

		if (this.tfTransform) {
			for (HashMap<String, Double> tfValues : tf.values()) {
				for (Entry<String, Double> entry : tfValues.entrySet()) {
					// System.out.printf("%f %s %f \n", entry.getValue(), ",  ",
					// Math.log(entry.getValue() + 1));
					tfValues.put(entry.getKey(), Math.log(entry.getValue() + 1));
				}
			}
		}

		double tfidfvalue = 0.0;
		if (idfTransform) {
			for (String prop : tf.keySet()) {
				HashMap<String, Double> tokenTfIdf = new HashMap<String, Double>();
				HashMap<String, Double> tfp = tf.get(prop);
				for (String token : tfp.keySet()) {
					// tfidfvalue = tfp.get(token) * Math.log(numberOfdocuments
					// / (1.0 + df.get(token)));
					tfidfvalue = tfp.get(token)
							* Math.log((double) numberOfdocuments
									/ (double) df.get(token));
					tokenTfIdf.put(token, tfidfvalue);
				}
				tfidf.put(prop, tokenTfIdf);
			}
		}
	}

	public LinkedList<String> getLabels() {
		return labels;
	}

	public LinkedList<String> getWords() {
		return words;
	}

	public HashMap<String, HashMap<String, Double>> getFeatures() {

		HashMap<String, HashMap<String, Double>> features = new HashMap<String, HashMap<String, Double>>();

		if (idfTransform)
			features = this.tfidf;
		else
			features = this.tf;

		if (lengthNorm) {
			double sum;
			for (String label : labels) {
				sum = 0.0;
				for (Entry<String, Double> entry : features.get(label)
						.entrySet()) {
					sum += entry.getValue() * entry.getValue();
				}
				for (Entry<String, Double> entry : features.get(label)
						.entrySet()) {
					features.get(label).put(entry.getKey(),
							entry.getValue() / Math.sqrt(sum));
				}
			}
		}

		return features;
	}

	public HashMap<String, HashMap<String, Double>> getTfidf() {
		return tfidf;
	}

	public int numFeatures() {
		return this.words.size();
	}
}
