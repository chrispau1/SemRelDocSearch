package de.paul.evaluation;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.paul.util.MapUtil;

/**
 * Captures ranking of most similar documents for one document.
 * 
 * @author Chris
 *
 */
public class DocSim {

	private int id;
	private Map<Integer, Double> simDocs;
	private Map<Integer, Integer> counts;

	public DocSim(int id) {
		this.id = id;
		this.simDocs = new HashMap<Integer, Double>();
		this.counts = new HashMap<Integer, Integer>();
	}

	public int getID() {
		return id;
	}

	public void addSim(int docID, double w) {

		Double v = simDocs.get(docID);
		if (v == null)
			v = 0.0;
		v += w;
		simDocs.put(docID, v);
		Integer c = counts.get(docID);
		if (c == null)
			c = 0;
		c++;
		counts.put(docID, c);
	}

	public void computeAvg() {

		for (Entry<Integer, Double> entry : simDocs.entrySet()) {

			simDocs.put(entry.getKey(),
					entry.getValue() / counts.get(entry.getKey()));
		}
	}

	public void sort() {

		Map<Integer, Double> sortedMap = MapUtil.sortDescByValue(simDocs);
		this.simDocs = sortedMap;
	}

	/**
	 * 5 is higly related, 1 is highly unrelated
	 * 
	 * @param cutOff
	 * @return
	 */
	public List<Entry<Integer, Double>> getTopEntries(double cutOff) {

		List<Entry<Integer, Double>> res = new LinkedList<Entry<Integer, Double>>();
		for (Entry<Integer, Double> entry : simDocs.entrySet()) {
			if (entry.getValue() >= cutOff) {
				res.add(entry);
			} else
				break;
		}
		return res;
	}

	public Double getSimilarityScore(int docID) {

		return simDocs.get(docID);
	}

	public Map<Integer, Integer> getSimCounts() {

		return counts;
	}

}
