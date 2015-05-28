package de.paul.similarity.docScorers;

import java.util.HashMap;
import java.util.List;

import de.paul.db.DocumentIndex;
import de.paul.documents.AnnotatedDoc;

public abstract class DocumentSimilarityScorer<x extends AnnotatedDoc> {

	protected DocumentIndex documentIndex;
	protected HashMap<Integer, List<x>> rankingsPerVariation;

	public int getDocCount() {
		return documentIndex.getDocCount();
	}

	public abstract x createNewDoc(AnnotatedDoc docId);

	public x getDocumentFromIndex(String docId) {
		return documentIndex.getDocument(docId);
	}

	/**
	 * Returns related documents for given document id.
	 * 
	 * @param queryDoc
	 * @return
	 */
	public abstract List<x> getRelatedDocuments(String queryDoc);

	/**
	 * For evaluation purposes. Computes rankings using different algorithmic
	 * variations and stores them internally for evaluation purposes.
	 * 
	 * @param queryDoc
	 */
	public abstract void computeRankingForVariations(String queryDoc);

	/**
	 * Return the string that makes the first row, the column description, of
	 * the CSV file containing ranking quality scores for all documents.
	 * 
	 * @return
	 */
	public abstract String writeCSVHeader();

	public HashMap<Integer, List<x>> getRankingsPerVariation() {
		return rankingsPerVariation;
	}
}
