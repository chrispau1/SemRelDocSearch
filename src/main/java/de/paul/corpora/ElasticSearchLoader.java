package de.paul.corpora;

import java.util.List;

import de.paul.corpora.elasticsearch.ESImporter;
import de.paul.docs.AnnotatedDoc;
import de.paul.docs.ElasticSearchSerializableDoc;

/**
 * General methods and templates for (bulk-)loading documents into a
 * ElasticSearch index.
 * 
 * @author Chris
 *
 */
public abstract class ElasticSearchLoader {

	protected ESImporter ih;

	protected abstract List<AnnotatedDoc> getThisBulksDocs(int offset, int count);

	public ElasticSearchLoader(String idxName, String docType) {

		ih = ESImporter.getInstance(idxName, docType);
	}

	public ElasticSearchLoader() {

	}

	public void loadInBulks(int initOffset, int bulksize, int iterations) {

		int offset = initOffset;
		int c = 0;
		try {
			ih.setBulkSize(bulksize);
			while (c < iterations && bulkAddDocuments(offset, bulksize)) {

				c++;
				offset += bulksize;
				System.out.println("Added " + (offset - initOffset) + " docs");
			}
			// commit remaining elements
			ih.bulkCommitNow();
		} finally {
			ih.shutdown();
		}
	}

	protected boolean bulkAddDocuments(int offset, int count) {

		// get bulk of docs uris
		List<AnnotatedDoc> bulkDocs = getThisBulksDocs(offset, count);
		// iterate over them, generate docs and add
		for (AnnotatedDoc esDoc : bulkDocs) {

			// if doc is null, it means that it has no annotations.
			if (esDoc != null) {
				if (esDoc instanceof ElasticSearchSerializableDoc)
					bulkAddDocument((ElasticSearchSerializableDoc) esDoc);
				// System.out.println("Doc added");
			}
		}
		// force commit for the remaining documents
		// ih.bulkCommitNow();
		return bulkDocs.size() == count;
	}

	private void bulkAddDocument(ElasticSearchSerializableDoc esDoc) {

		ih.addDocToBulk(esDoc);
	}

}
