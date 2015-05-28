package de.paul.corpus.evaluation;

import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;

public abstract class RankingEvalHandler extends EvalHandler {

	public abstract List<Entry<Integer, Double>> getRanking(int docId,
			double cutOff) throws IOException;

}
