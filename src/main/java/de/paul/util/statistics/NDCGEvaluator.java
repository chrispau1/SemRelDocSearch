package de.paul.util.statistics;

import java.util.ArrayList;
import java.util.List;

import de.paul.docs.RankableDoc;

public class NDCGEvaluator<x extends RankableDoc> implements
		RankingEvaluator<x> {

	private double cutOff;
	private double ndcgRating;

	public NDCGEvaluator(double cutOff) {

		this.cutOff = cutOff;
	}

	public double evaluateRanking(List<x> humRanking, List<x> algoRanking,
			int countFactor) {

		int relCount = getRelevantElementsCount(humRanking, cutOff);
		// returned lists from ElasticSearch methods can be very short, so take
		// min of desired results and list size
		List<x> subAlgoList = algoRanking.subList(
				0,
				Math.min(relCount * countFactor,
						Math.max(algoRanking.size(), 0)));
		// System.out.println("algo Ranking: " + subAlgoList);
		return evaluateRanking(humRanking, subAlgoList);
	}

	public double evaluateRanking(List<x> humRanking, List<x> algoRanking) {

		int relCount = getRelevantElementsCount(humRanking, cutOff);
		if (relCount == 0 && algoRanking.size() == 0)
			ndcgRating = 1.0;
		else if (algoRanking.size() == 0 && relCount > 0)
			ndcgRating = 0.0;
		else
			ndcgRating = ndcg(humRanking, algoRanking);
		return getNdcgRating();
	}

	public static <x extends RankableDoc> int getRelevantElementsCount(
			List<x> humRanking, double cutOff) {

		int res = 0;
		for (x el : humRanking) {
			if (el.getScore() >= cutOff)
				res++;
		}
		return res;
	}

	private double ndcg(List<x> humRanking, List<x> algoRanking) {

		// get the human evaluation for the algo ranking
		Double[] rel = humanRelevanceForAlgoRanking(humRanking, algoRanking);
		// numerator
		double dcg = dcg(rel);
		// denominator
		double idcg = idcg(humRanking, algoRanking.size());
		// if it is not zero (double style)
		if (Math.abs(idcg) >= 0.0001)
			return dcg / idcg;
		else
			return 0;
	}

	private double idcg(List<x> humRanking, int firstN) {

		Double[] rel_hum = filterList(humRanking, firstN);// filterList(humRanking,
															// cutOff);
		double dcg_sorted = dcg(rel_hum);
		return dcg_sorted;
	}

	/*
	 * filters out ranking after the first <parameter> places
	 */
	private Double[] filterList(List<x> humRanking, int count) {

		ArrayList<Double> rel2 = new ArrayList<Double>();
		int i = 0;
		for (RankableDoc el : humRanking) {

			// OR if there are more relevant documents than were found!
			if (i < count || el.getScore() >= cutOff)
				rel2.add(el.getScore());
			else
				break;
			i++;
		}
		return rel2.toArray(new Double[] {});
	}

	private Double[] humanRelevanceForAlgoRanking(List<x> humRanking,
			List<x> algoRanking) {

		Double[] rel = new Double[algoRanking.size()];
		for (int i = 0; i < algoRanking.size(); i++) {
			x a_i = algoRanking.get(i);
			int pos;
			if ((pos = humRanking.indexOf(a_i)) != -1) {

				rel[i] = humRanking.get(pos).getScore();
			} else {
				rel[i] = 0.0;
				System.out
						.println("Algo returned document that wasn't rated by humans");
			}
		}
		return rel;
	}

	private double dcg(Double[] rel) {

		double score = 0;
		for (int i = 0; i < rel.length; i++) {
			double discounter = Math.log(i + 2) / Math.log(2);
			double denom;
			denom = Math.pow(2, rel[i]) - 1;
			score += denom / discounter;
		}
		return score;
	}

	public String writeEvaluationRow() {

		return String.valueOf(ndcgRating);
	}

	public double getNdcgRating() {
		return ndcgRating;
	}

}
