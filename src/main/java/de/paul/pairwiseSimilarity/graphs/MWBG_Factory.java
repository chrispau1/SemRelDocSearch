package de.paul.pairwiseSimilarity.graphs;

import de.paul.docs.AnnotatedDoc;

public class MWBG_Factory {

	public enum MWBG_mode {
		SingleEdge, WeightedSingleEdge
	};

	public static MWBG_mode mwbgMode = MWBG_mode.SingleEdge;

	public static SingleEdge_MWBG produceSE_MWBG(AnnotatedDoc doc1,
			AnnotatedDoc doc2) {

		if (mwbgMode == MWBG_mode.SingleEdge)
			// return new SingleEdge_MWBG(doc1.getAnnotations().size(), doc2
			// .getAnnotations().size());
			return new SingleEdge_MWBG(doc1, doc2);
		else if (mwbgMode == MWBG_mode.WeightedSingleEdge)
			return new WeightedSingleEdge_MWBG(doc1, doc2);
		return null;
	}
}
