package de.paul.util;

import java.util.HashMap;

/**
 * Util map that makes counting in a map easier.
 * 
 * @author Chris
 * @param <k>
 */
public class CountMap<k> extends HashMap<k, Integer> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 603966112752030226L;

	public void inc(k key) {

		Integer val = this.get(key);
		if (val == null)
			val = 0;
		this.put(key, val + 1);
	}

	public int valueSum() {

		int sum = 0;
		for (Integer val : this.values()) {
			sum += val;
		}
		return sum;
	}

}
