package de.paul.similarity.docScorers;

import de.paul.documents.AnnotatedDoc;

public abstract class InvertedIndexDocScorer<x extends AnnotatedDoc> extends
		DocumentSimilarityScorer<x> {

	protected int resultsCount = -1;
}
