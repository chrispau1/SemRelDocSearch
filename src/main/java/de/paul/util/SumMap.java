package de.paul.util;

import java.util.HashMap;

public class SumMap<k> extends HashMap<k, Double> {

	private static final long serialVersionUID = 283926214882466765L;

	public void inc(k key, double addVal) {

		Double val = this.get(key);
		if (val == null)
			val = 0.0;
		this.put(key, val + addVal);
	}

	public double valueSum() {

		double sum = 0;
		for (Double val : this.values()) {
			sum += val;
		}
		return sum;
	}

}
