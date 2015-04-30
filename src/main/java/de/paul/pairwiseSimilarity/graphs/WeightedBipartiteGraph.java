package de.paul.pairwiseSimilarity.graphs;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import de.paul.annotations.Annotatable;
import de.paul.docs.AnnotatedDoc;
import de.paul.pairwiseSimilarity.entityPairScorers.ScorableEntityPair;

public abstract class WeightedBipartiteGraph {

	protected List<ScorableEntityPair> matchings;
	protected int annotCount1;
	protected int annotCount2;

	protected AnnotatedDoc doc2;
	protected AnnotatedDoc doc1;

	public void addMatching(ScorableEntityPair matching) {

		if (matchings == null) {
			matchings = new LinkedList<ScorableEntityPair>();
		}
		matchings.add(matching);
	}

	public List<ScorableEntityPair> getMatchings() {
		return matchings;
	}

	public void setMatchings(List<ScorableEntityPair> matchings) {
		this.matchings = matchings;
	}

	public abstract double similarityScore()
			throws UnsupportedOperationException;

	/**
	 * Returns a CSV string representing the right-side scores of all matchings
	 * as a matrix. So these numbers show the similarity between the second
	 * document and the first (asymmetric!).
	 * 
	 * @return
	 */
	public String printPairwiseRightScoreMatrix() {

		return printPairwiseScoreMatrix(false);
	}

	/**
	 * Returns a CSV string representing the scores of all matchings as a
	 * matrix. So these numbers show the similarity between the first document
	 * and the second (asymmetric!).
	 * 
	 * @return
	 */
	public String printPairwiseLeftScoreMatrix() {

		return printPairwiseScoreMatrix(true);
	}

	private String printPairwiseScoreMatrix(boolean left) {

		// Map annotations to integers for use with array-based matrix
		List<Annotatable> ans1 = doc1.getAnnotations();
		List<Annotatable> ans2 = doc2.getAnnotations();
		HashMap<String, Integer> map1 = mapEntsToNumbers(ans1);
		HashMap<String, Integer> map2 = mapEntsToNumbers(ans2);
		// fill array with scores
		double[][] scores = new double[ans1.size()][ans2.size()];
		for (ScorableEntityPair m : matchings) {
			String e1 = m.getAnnotation().getEntity();
			String e2 = m.getAnnotation2().getEntity();
			Integer i1 = map1.get(e1);
			Integer i2 = map2.get(e2);
			if (left) {
				m.setLeft();
				scores[i1][i2] = m.score();
			} else {
				m.setRight();
				scores[i1][i2] = m.score();
			}
		}
		// print header
		String separator = ";";
		StringBuilder sb = new StringBuilder(separator);
		// second document's annotations are printed horizontal, thus first
		Iterator<Annotatable> it2 = ans2.iterator();
		while (it2.hasNext()) {
			Annotatable a = it2.next();
			sb.append(a.getEntity().split("http://dbpedia.org/resource/")[1]);
			if (it2.hasNext())
				sb.append(separator);
		}
		sb.append("\n");
		// print matrix, including doc1 annotations in first column
		Iterator<Annotatable> it1 = ans1.iterator();
		for (int i = 0; i < scores.length; i++) {
			for (int j = 0; j < scores[i].length; j++) {

				if (j == 0)
					sb.append(it1.next().getEntity()
							.split("http://dbpedia.org/resource/")[1]
							+ separator);

				sb.append(scores[i][j]);
				if (j < scores[i].length - 1)
					sb.append(separator);
			}
			sb.append("\n");
		}
		return sb.toString();
	}

	private HashMap<String, Integer> mapEntsToNumbers(List<Annotatable> ans) {

		int ctr = 0;
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		for (Annotatable a : ans) {
			if (map.get(a.getEntity()) != null)
				System.out.println("Multiple occurrences of same entity: "
						+ a.getEntity());
			else {
				map.put(a.getEntity(), ctr);
				ctr++;
			}
		}
		return map;
	}
}
