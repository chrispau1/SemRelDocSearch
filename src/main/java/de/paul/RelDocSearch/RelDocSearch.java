package de.paul.relDocSearch;

import java.io.FileNotFoundException;
import java.util.List;

import de.paul.db.BulkDocSourceHandler;
import de.paul.documents.AnnotatedDoc;
import de.paul.documents.impl.SemanticallyExpandedDoc;
import de.paul.similarity.docScorers.DocumentSimilarityScorer;

/**
 * Abstract class for related document search classes operating in a pre-search
 * / full search approach using a candidate set.
 * 
 * @author Chris
 *
 */
public abstract class RelDocSearch extends DocumentSimilarityScorer<SemanticallyExpandedDoc> {

	/**
	 * Returns most related documents for a document already existing in corpus
	 * with specified document type and specified id.
	 * 
	 * If docType is null, the default document type will be used.
	 * 
	 * @param docType
	 * @param id
	 * @param candidateSetSize
	 * @return
	 * @throws FileNotFoundException
	 */
	public abstract List<SemanticallyExpandedDoc> getRelatedDocuments(String docType,
			String id, int candidateSetSize) throws FileNotFoundException;

	/**
	 * Returns most related documents for a new annotated document. Method calls
	 * expandAndAddDocument method, then searches related documents based on the
	 * added document.
	 * 
	 * @param newDoc
	 * @param docType
	 * @param candidateSetSize
	 * @return
	 * @throws FileNotFoundException
	 */
	public abstract List<SemanticallyExpandedDoc> getRelatedDocuments(
			AnnotatedDoc newDoc, String docType, int candidateSetSize)
			throws FileNotFoundException;

	/**
	 * Expand an annotated document and add it to the corpus. Will be added both
	 * to pre-search and full search data structures.
	 * 
	 * @param newDoc
	 * @param docType
	 * @return
	 */
	public abstract String addDocument(AnnotatedDoc newDoc,
			String docType);

	public abstract void bulkAddDocuments(BulkDocSourceHandler docSource,
			int offset, int count, String docType);

}
