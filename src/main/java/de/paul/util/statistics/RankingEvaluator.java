package de.paul.util.statistics;

import java.util.List;

import de.paul.docs.RankableDoc;

public interface RankingEvaluator<x extends RankableDoc> {

	public double evaluateRanking(List<x> humRanking, List<x> algoRanking);

	public String writeEvaluationRow();
}
