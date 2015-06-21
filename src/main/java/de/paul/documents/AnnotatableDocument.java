package de.paul.documents;

/**
 * Interface that all document classes in this context ought to implement.
 * 
 * Defines basic functions for title, text, annotations.
 * 
 * @author Chris
 *
 */
public interface AnnotatableDocument {

	public String getId();

	public String getTitle();

	public String getText();
}
