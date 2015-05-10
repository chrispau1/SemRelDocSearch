package de.paul.docs.impl;

import java.util.Collection;
import java.util.List;

import de.paul.annotations.AncestorAnnotation;
import de.paul.annotations.Annotatable;
import de.paul.dbpedia.DBPediaHandler;
import de.paul.dbpedia.categories.HierarchyHandler;
import de.paul.docs.AnnotatedDoc;

public class TaxonomicExpandedDoc extends AnnotatedDoc {

	public TaxonomicExpandedDoc(TaxonomicExpandedDoc copy) {

		super(copy.getText(), copy.getTitle(), copy.getId());
		for (Annotatable annot : copy.getAnnotations()) {
			addAnnotation(annot);
		}
	}

	public void addAnnotation(Annotatable annot) {
		// add annotation
		this.annotations.add(annot);
	}

	public TaxonomicExpandedDoc(AnnotatedDoc doc, DBPediaHandler dbpHandler,
			HierarchyHandler hierHandler) {

		super(doc.getText(), doc.getTitle(), doc.getId());
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
		}
	}

	public TaxonomicExpandedDoc(String docId, String title, String text,
			List<Annotatable> annotations) {

		super(text, title, docId);
		for (Annotatable annot : annotations) {
			addAnnotation(annot);
		}
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
