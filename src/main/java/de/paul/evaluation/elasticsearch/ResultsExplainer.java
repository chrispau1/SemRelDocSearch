package de.paul.evaluation.elasticsearch;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import de.paul.util.MapUtil;

public class ResultsExplainer {

	// public static void main(String[] args) {
	//
	// ESHandler sh = null;
	// try{
	// sh = ESHandler.getInstance("es_xlime", "news");
	// System.out.println("retrieving...");
	// ExpESDoc doc1 = sh.getDocument("AUo8E_YOUnzTGUtNPbwH");
	// ExpESDoc doc2 = sh.getDocument("AUo8E_YPUnzTGUtNPbwL");
	// System.out.println("Done");
	// ResultsExplainer explainer = new ResultsExplainer();
	// // String res = explainer.explainTraverse(doc1, doc2);
	// String res = explainer.getOverlap(doc1.getEntities(),
	// doc2.getEntities());
	// System.out.println(res);
	// }finally{
	// sh.shutdown();
	// }
	// }

	public String getOverlap(String t1, String t2) {

		// System.out.println("Calc overlap..");
		String[] s1 = t1.split(" ");
		String[] s2 = t2.split(" ");
		HashMap<String, Integer> m1 = new HashMap<String, Integer>();
		HashMap<String, Integer> m2 = new HashMap<String, Integer>();
		HashMap<String, Integer> res = new HashMap<String, Integer>();
		for (int i = 0; i < s1.length; i++) {
			Integer v = m1.get(s1[i]);
			if (v == null)
				v = 0;
			m1.put(s1[i], v + 1);
		}
		for (int i = 0; i < s2.length; i++) {
			Integer v = m2.get(s2[i]);
			if (v == null)
				v = 0;
			m2.put(s2[i], v + 1);
		}
		for (Entry<String, Integer> e : m1.entrySet()) {
			Integer otherVal = m2.get(e.getKey());
			if (otherVal != null)
				res.put(e.getKey(),
						(int) Math.round(Math.min(e.getValue(), otherVal)));
		}
		Map<String, Integer> sortedRes = MapUtil.sortDescByValue(res);
		String resString = "";
		int ctr = 0;
		for (Entry<String, Integer> e : sortedRes.entrySet()) {

			resString += "(" + e.getKey() + ", " + e.getValue() + "), ";
			if (ctr > 9)
				break;
			ctr++;
		}
		// System.out.println("Done");
		return resString;
	}

}
