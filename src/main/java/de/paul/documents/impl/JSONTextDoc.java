package de.paul.documents.impl;

import java.util.List;

import de.paul.annotations.Annotatable;
import de.paul.annotations.JSONAnnotation;
import de.paul.documents.AnnotatedDoc;

public class JSONTextDoc extends AnnotatedDoc {

	public JSONTextDoc(String id, String doc, String title,
			List<JSONAnnotation> annots) {

		super(doc, title, id);
		for (JSONAnnotation a : annots)
			this.addAnnotation(a);
	}

	public JSONTextDoc(String id, String doc, String title) {

		super(doc, title, id);
	}

	public void addAnnotation(Annotatable annot) {

		this.annotations.add(annot);
	}

	public String toString() {

		return " - text: " + text + "\n - annotations: "
				+ getAnnotationsAsString();
	}

	@Override
	public JSONTextDoc copy() {

		JSONTextDoc copy = new JSONTextDoc(id, text, title);
		for (Annotatable annot : this.annotations) {
			copy.addAnnotation(annot);
		}
		return copy;
	}

}
