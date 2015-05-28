package de.paul.db;

import de.paul.documents.AnnotatedDoc;

public interface DocumentSource {

	public <x extends AnnotatedDoc> x getDocument(String docId);

}
