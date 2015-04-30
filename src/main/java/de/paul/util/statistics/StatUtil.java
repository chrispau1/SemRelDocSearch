package de.paul.util.statistics;

import java.util.ArrayList;
import java.util.List;

public class StatUtil {

	public static double f1Score(double prec, double rec) {

		if (Math.abs(prec + rec) <= 0.000001)
			return 0;
		else
			return 2 * prec * rec / (prec + rec);
	}

	private List<Double> scores = new ArrayList<Double>();

	public void registerScore(double score, boolean ignoreZeros) {

		if (!ignoreZeros || Math.abs(score) > 0.000001)
			this.scores.add(score);
	}

	public double getMean() {

		double sum = 0;
		for (int i = 0; i < scores.size(); i++) {

			Double val = scores.get(i);
			sum += val;
		}
		return sum / scores.size();
	}

	public double getVariance(double mean) {

		double sum = 0;
		for (int i = 0; i < scores.size(); i++) {

			Double val = scores.get(i);
			sum += Math.pow(val - mean, 2);
		}
		return sum / scores.size();
	}

}
