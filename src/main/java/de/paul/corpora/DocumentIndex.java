package de.paul.corpora;

import de.paul.docs.AnnotatedDoc;

/**
 * Abstract class for classes that facilitate access to a document store.
 * 
 * Also implements the stats provider interface. If a subclass is not supposed
 * to provide global statistics, let methods throw
 * {@code UnsupportedOperationException}.
 * 
 * @author Chris
 *
 */
public abstract class DocumentIndex {

	public abstract <x extends AnnotatedDoc> x getDocument(String docId);

	public abstract <x extends AnnotatedDoc> x getDocument(int docId);

	public abstract int getDocCount();

	public abstract String[] getDocIds();

}
