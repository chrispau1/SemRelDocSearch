package de.paul.db;

/**
 * Abstract class for a document store that is used by DocScorers.
 * 
 * @author Chris
 *
 */
public interface DocumentIndex extends DocumentSource {

	public int getDocCount();

	public String[] getDocIds();

}
