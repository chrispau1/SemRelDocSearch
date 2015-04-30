package de.paul.docs;

import java.util.LinkedList;
import java.util.List;

import org.json.JSONObject;

import de.paul.annotations.Annotatable;
import de.paul.annotations.JSONAnnotation;
import de.paul.docs.impl.JSONTextDoc;

/**
 * Encapsulates a text document for JSON writing Needs wrapper or double
 * inheritance instead
 * 
 * @author Chris
 *
 */
public class JSONSerializableDocWrapper extends JSONObject {

	private JSONTextDoc doc;

	public JSONSerializableDocWrapper(JSONTextDoc doc) {

		this.setDoc(doc.copy());
		this.put("text", doc.getText());
		List<JSONAnnotation> serializableAnnots = new LinkedList<JSONAnnotation>();
		for (Annotatable a : doc.getAnnotations()) {
			serializableAnnots.add((JSONAnnotation) a.copy());
		}
		this.put("annotations", serializableAnnots);
	}

	public JSONTextDoc getDoc() {
		return doc;
	}

	public void setDoc(JSONTextDoc doc) {
		this.doc = doc;
	}
}
