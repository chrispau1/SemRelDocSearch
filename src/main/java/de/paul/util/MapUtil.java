package de.paul.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class MapUtil {

	private static final String DELIMITER = " ";

	public static <K, V extends Comparable<? super V>> Map<K, V> sortAscByValue(
			Map<K, V> map) {
		List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(
				map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
				return (o1.getValue()).compareTo(o2.getValue());
			}
		});

		Map<K, V> result = new LinkedHashMap<K, V>();
		for (Map.Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}

	public static <K, V extends Comparable<? super V>> Map<K, V> sortDescByValue(
			Map<K, V> map) {

		List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(
				map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
				return (o2.getValue()).compareTo(o1.getValue());
			}
		});

		Map<K, V> result = new LinkedHashMap<K, V>();
		for (Map.Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}

	/**
	 * Creates a string out of a map by printing each word (key) as many times
	 * as the (rounded) weight (value) says.
	 * 
	 * @param map
	 * @return
	 */
	public static String stringifyMap(Map<String, Double> map) {

		String s = "";
		for (Entry<String, Double> entry : map.entrySet()) {
			int freq = (int) Math.round(entry.getValue());
			String uri = entry.getKey();
			s += convertToWeightedString(uri, freq);
		}
		return s;
	}

	private static String convertToWeightedString(String uri, int freq) {

		String s = "";
		for (int i = 0; i < freq; i++)
			s += uri + DELIMITER;
		return s;
	}

	/**
	 * Weight factor is multiplied with size of map, such that resulting string
	 * takes length of document into account and scales word frequency somewhat
	 * accordingly, while keeping the word count across documents of different
	 * sizes comparable.
	 * 
	 * @param map
	 * @param weightFactor
	 * @return
	 */
	public static String stringifyMap(Map<String, Double> map, int weightFactor) {

		String s = "";

		if (map != null && map.size() != 0) {
			// get value sum
			double valSum = 0;
			for (Entry<String, Double> entry : map.entrySet()) {
				valSum += entry.getValue();
			}
			// desired overall number of words
			double normSum = map.size() * weightFactor;
			// iterate over entries
			for (Entry<String, Double> entry : map.entrySet()) {
				// get plain weight
				Double weight = entry.getValue();
				// its contribution to weight sum used to normalize with desired
				// sum
				double adjustedWeight = normSum * weight / valSum;
				s += convertToWeightedString(entry.getKey(),
						(int) Math.round(adjustedWeight));
			}
		}
		return s;
	}
}