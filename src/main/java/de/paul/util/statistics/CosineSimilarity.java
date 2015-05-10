package de.paul.util.statistics;

import java.util.Map;
import java.util.Map.Entry;

public class CosineSimilarity {

	public static double score(Map<String, Double> docVec1,
			Map<String, Double> docVec2) {
		double dotProduct = 0.0;
		double magnitude1 = 0.0;
		double magnitude2 = 0.0;

		for (Entry<String, Double> entry : docVec1.entrySet()) // docVector1 and
																// docVector2
		// must be of same length
		{
			Double val1 = entry.getValue();
			Double val2 = docVec2.get(entry.getKey());
			if (val2 == null)
				val2 = 0.0;
			dotProduct += val1 * val2; // a.b
			magnitude1 += Math.pow(val1, 2); // (a^2)
			magnitude2 += Math.pow(val2, 2); // (b^2)
		}

		magnitude1 = Math.sqrt(magnitude1);// sqrt(a^2)
		magnitude2 = Math.sqrt(magnitude2);// sqrt(b^2)

		if (magnitude1 != 0.0 && magnitude2 != 0.0) {
			return dotProduct / (magnitude1 * magnitude2);
		} else {
			return 0.0;
		}
	}

}
