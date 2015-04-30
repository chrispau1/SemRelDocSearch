package de.paul.docs.impl;

import java.util.LinkedList;
import java.util.List;

import de.paul.annotations.Annotatable;
import de.paul.docs.AnnotatedDoc;

public class QuadDoc extends AnnotatedDoc {

	public QuadDoc(String docId, String text, String title,
			List<Annotatable> annotations) {
		super(text, title, docId);
		this.annotations = new LinkedList<Annotatable>();
		for (Annotatable ann : annotations) {
			this.annotations.add(ann.copy());
		}
	}

	@Override
	public AnnotatedDoc copy() {

		return new QuadDoc(id, text, title, annotations);
	}

}
