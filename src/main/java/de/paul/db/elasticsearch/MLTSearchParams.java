package de.paul.db.elasticsearch;

import java.util.HashMap;
import java.util.Map;

public class MLTSearchParams {

	private static Map<String, Integer> docFreq = null;
	private static Map<String, Integer> termFreq = null;
	private static Map<String, Float> comboBoost = null;

	public static int docFreq(String field) {

		if (docFreq == null) {
			docFreq = new HashMap<String, Integer>();
			docFreq.put("neighbors", 2);
			docFreq.put("categories", 5);
			docFreq.put("text", 2);
		}
		return docFreq.get(field);
	}

	public static int termFreq(String field) {

		if (termFreq == null) {
			termFreq = new HashMap<String, Integer>();
			termFreq.put("neighbors", 20);
			termFreq.put("categories", 2);
			termFreq.put("text", 2);
		}
		return termFreq.get(field);
	}

	public static Float comboBoost(String field) {

		if (comboBoost == null) {
			comboBoost = new HashMap<String, Float>();
			comboBoost.put("text", 3.0f);
			comboBoost.put("neighbors", 4.0f);
			comboBoost.put("categories", 4.0f);
		}
		return comboBoost.get(field);
	}
}
