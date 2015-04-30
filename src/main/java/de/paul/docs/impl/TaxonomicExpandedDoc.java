package de.paul.docs.impl;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import de.paul.annotations.AncestorAnnotation;
import de.paul.annotations.Annotatable;
import de.paul.annotations.FullyExpandedAnnotation;
import de.paul.annotations.TopicAnnotation;
import de.paul.corpora.GlobalStatsProvider;
import de.paul.dbpedia.DBPediaHandler;
import de.paul.dbpedia.categories.HierarchyHandler;
import de.paul.docs.AnnotatedDoc;
import de.paul.util.CountMap;

public class TaxonomicExpandedDoc extends AnnotatedDoc {

	private CountMap<String> topicFreqs = null;
	private GlobalStatsProvider globalStatsProvider;

	public TaxonomicExpandedDoc(TaxonomicExpandedDoc copy) {

		super(copy.getText(), copy.getTitle(), copy.getId());
		this.globalStatsProvider = copy.globalStatsProvider;
		for (Annotatable annot : copy.getAnnotations()) {
			addAnnotation(annot);
			// init topic frequency map if necessary
			updateTopics(annot);
		}
	}

	public void addAnnotation(Annotatable annot) {
		// add annotation
		this.annotations.add(annot);
	}

	public TaxonomicExpandedDoc(AnnotatedDoc doc, DBPediaHandler dbpHandler,
			HierarchyHandler hierHandler,
			GlobalStatsProvider globalStatsProvider) {

		super(doc.getText(), doc.getTitle(), doc.getId());
		this.globalStatsProvider = globalStatsProvider;
		// get annotations without ancestors
		Collection<Annotatable> plainAnnots = unifyAnnotationsSumScores(doc
				.getAnnotations());
		// now add ancestors
		for (Annotatable a : plainAnnots) {
			String entName = a.getEntity();
			double entWeight = a.getWeight();
			AncestorAnnotation ancAnnot = new AncestorAnnotation(entName,
					entWeight, dbpHandler, hierHandler);
			addAnnotation(ancAnnot);
			// init topic frequency map if necessary
			updateTopics(ancAnnot);
		}
	}

	public TaxonomicExpandedDoc(String docId, String title, String text,
			List<Annotatable> annotations) {

		super(text, title, docId);
		for (Annotatable annot : annotations) {
			addAnnotation(annot);
		}
	}

	private void updateTopics(Annotatable ancAnnot) {
		if (topicFreqs == null)
			topicFreqs = new CountMap<String>();
		// add topics to count map
		Set<String> topics = null;
		if (ancAnnot instanceof AncestorAnnotation)
			topics = ((AncestorAnnotation) ancAnnot).getTopics();
		else if (ancAnnot instanceof FullyExpandedAnnotation)
			topics = ((FullyExpandedAnnotation) ancAnnot).getAncestors()
					.getTopics();
		for (String topic : topics) {
			topicFreqs.inc(topic);
		}
	}

	/*
	 * Topic frequency for given topics in this document.
	 */
	private double getTopicTF(Set<String> topics) {

		double freq = 0;
		for (String topic : topics) {
			freq += topicFreqs.get(topic);
		}
		return freq / topicFreqs.valueSum();
	}

	/**
	 * Expresses specificity of the annotation's topics to this document. Uses a
	 * hybrid tf-idf-inspired approach, where idf is based on global topic
	 * frequencies (NOT on the number of documents they appear in), and tf is
	 * the classical frequency of the annotation's topics in this document.
	 * 
	 * @param topics
	 * @return
	 */
	public double getTopicScore(TopicAnnotation annot) {

		Set<String> topics = annot.getTopics();
		double tf = this.getTopicTF(topics);
		double idf = globalStatsProvider.getTopicScore(topics);
		// * annot.getTopicScore();
		return tf * idf;
	}

	public Set<String> getTopics() {

		return topicFreqs.keySet();
	}

	public String toString() {
		return "(id: " + this.id + ", title: " + this.title + ", score: "
				+ this.getScore() + ")";
	}

	@Override
	public TaxonomicExpandedDoc copy() {

		return new TaxonomicExpandedDoc(this);
	}
}
