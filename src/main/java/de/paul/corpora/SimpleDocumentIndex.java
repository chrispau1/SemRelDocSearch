package de.paul.corpora;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import de.paul.docs.AnnotatedDoc;
import de.paul.util.CountMap;

public class SimpleDocumentIndex extends DocumentIndex {

	private Map<String, AnnotatedDoc> docs;
	private CountMap<String> globalTopicFreqs = new CountMap<String>();

	public SimpleDocumentIndex() {
		docs = new HashMap<String, AnnotatedDoc>();
	}

	public <x extends AnnotatedDoc> x getDocument(int docId) {

		return getDocument(Integer.toString(docId));
	}

	public <x extends AnnotatedDoc> x getDocument(String docId) {

		return (x) docs.get(docId).copy();
	}

	// public void createAndAddDocument(AnnotatedDoc doc, boolean
	// collectTopicStats) {
	//
	// docs.put(doc.getId(), doc);
	// if (collectTopicStats) {
	//
	// Set<String> docTopics = null;
	// try {
	// docTopics = doc.getTopics();
	// } catch (UnsupportedOperationException e) {
	// // System.out.println("Document type does not support topics");
	// }
	// if (docTopics != null)
	// for (String t : docTopics)
	// globalTopicFreqs.inc(t);
	// }
	// }

	public void addDocument(AnnotatedDoc doc, boolean collectTopicStats) {

		docs.put(doc.getId(), doc);
		if (collectTopicStats) {
			Set<String> docTopics = null;
			try {
				docTopics = doc.getTopics();
			} catch (UnsupportedOperationException e) {
				// System.out.println("Document type does not support topics");
			}
			if (docTopics != null)
				for (String t : docTopics)
					globalTopicFreqs.inc(t);
		}
	}

	public void addDocument(AnnotatedDoc doc) {
		addDocument(doc, false);
	}

	public double getTopicScore(Set<String> topics) {

		double freq = 0;
		// sum up scores of this entity's topics
		for (String topic : topics)
			freq += globalTopicFreqs.get(topic);

		if (Math.abs(freq) > 0.0001)
			// log idf - ratio shows how specific this entity's topics are
			return Math.log(globalTopicFreqs.valueSum() / freq);
		else
			return 1;
	}

	// public void createAndAddDocument(AnnotatedDoc doc) {
	//
	// createAndAddDocument(doc, true);
	// }

	@Override
	public int getDocCount() {

		return this.docs.size();
	}

	@Override
	public String[] getDocIds() {

		return this.docs.keySet().toArray(new String[] {});
	}
}
