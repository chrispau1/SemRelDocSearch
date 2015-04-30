package de.paul.docs.impl;

import java.util.Collection;
import java.util.List;

import de.paul.annotations.Annotatable;
import de.paul.annotations.NeighborhoodAnnotation;
import de.paul.dbpedia.DBPediaHandler;
import de.paul.docs.AnnotatedDoc;

public class TransversalPerAnnotExpandedDoc extends AnnotatedDoc {

	public TransversalPerAnnotExpandedDoc(
			TransversalPerAnnotExpandedDoc toCopyFrom) {

		super(toCopyFrom.getText(), toCopyFrom.getTitle(), toCopyFrom.getId());
		List<Annotatable> annots = toCopyFrom.annotations;
		for (Annotatable ann : annots)
			this.annotations.add(ann.copy());
	}

	public TransversalPerAnnotExpandedDoc(AnnotatedDoc doc,
			DBPediaHandler dbpHandler, int expansionRadius) {

		super(doc.getText(), doc.getTitle(), doc.getId());
		Collection<Annotatable> plainAnnots = unifyAnnotationsSumScores(doc
				.getAnnotations());
		for (Annotatable ann : plainAnnots) {
			String ent = ann.getEntity();
			double weight = ann.getWeight();
			NeighborhoodAnnotation neighAnn = new NeighborhoodAnnotation(ent,
					weight, dbpHandler, expansionRadius);
			this.annotations.add(neighAnn);
		}
	}

	@Override
	public AnnotatedDoc copy() {

		return new TransversalPerAnnotExpandedDoc(this);
	}

	public String toString() {
		return "(id: " + this.id + ", title: " + this.title + ", score: "
				+ this.getScore() + ")";
	}

}
