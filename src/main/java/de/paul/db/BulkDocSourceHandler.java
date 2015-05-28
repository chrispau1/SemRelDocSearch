package de.paul.db;

import java.util.List;

/**
 * Facilitates access to sources holding annotated documents.
 * 
 * @author Chris
 *
 */
public interface BulkDocSourceHandler extends DocumentSource {

	/**
	 * Returns one bulk of documents of size count at position offset.
	 * 
	 * @param offset
	 * @param count
	 * @return
	 */
	public abstract List<String> getBulkDocs(int offset, int count);
}
