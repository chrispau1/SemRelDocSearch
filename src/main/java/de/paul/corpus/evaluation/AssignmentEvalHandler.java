package de.paul.corpus.evaluation;

import java.io.IOException;

import de.paul.util.Paths;

public class AssignmentEvalHandler extends EvalHandler {

	protected static AssignmentEvalHandler instance;

	public static void main(String[] args) throws IOException {

		AssignmentEvalHandler p = new AssignmentEvalHandler(
				Paths.MOMIEVALPATH_CSV);
		p.loadRows();
		System.out.println("adad");
	}

	private AssignmentEvalHandler(String string) {

		this.path = string;
	}

	public static AssignmentEvalHandler getInstance(String path) {

		if (instance == null)
			instance = new AssignmentEvalHandler(path);
		return instance;
	}

	@Override
	public Double getSimilarity(int doc1, int doc2) throws IOException {
		if (docSims == null) {
			this.loadRows();
		}
		DocSim scores = docSims[doc1];
		if (scores != null)
			return scores.getSimilarityScore(doc2);
		else
			return null;
	}

	@Override
	protected int getDocCount() {

		return 651;
	}

	@Override
	protected int adjustID(int i) {
		return i;
	}
}
