package de.paul.util;

import java.util.HashMap;
import java.util.Map;

public class TimeKeeper {

	private static TimeKeeper instance = null;

	private Map<String, Integer> times = new HashMap<String, Integer>();
	private Map<String, Integer> counts = new HashMap<String, Integer>();

	private TimeKeeper() {

	}

	public static TimeKeeper getInstance() {
		if (instance == null)
			instance = new TimeKeeper();
		return instance;
	}

	public void addGetCategoryTime(long l) {

		addToTimeMap("getCategoryTime", l);
	}

	public void addToTimeMap(String name, long timeAdded) {
		Integer val = times.get(name);
		if (val == null)
			val = 0;
		times.put(name, (int) (val + timeAdded));
		val = counts.get(name);
		if (val == null)
			val = 0;
		counts.put(name, val + 1);
	}

	public void addGetAncestorsTime(long l) {

		addToTimeMap("getAncestorsTime", l);
	}

	public void addSETime(long l) {

		addToTimeMap("SETime", l);
	}

	public void addWSETime(long l) {

		addToTimeMap("WSETime", l);
	}

	public void addEvalTime(long l) {

		addToTimeMap("evalTime", l);
	}

	public String toString() {

		StringBuilder sb = new StringBuilder("Timekeeper printout: \n");
		for (String key : times.keySet()) {
			sb.append(key + " total: " + times.get(key) + ", count: "
					+ counts.get(key) + ", avg: " + ((double) times.get(key))
					/ counts.get(key) + "\n");
		}
		return sb.toString();
	}

	public void addLoadDocsTime(long l) {

		this.addToTimeMap("loadDocsTime", l);
	}
}
