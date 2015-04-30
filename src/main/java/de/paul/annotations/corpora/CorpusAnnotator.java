package de.paul.annotations.corpora;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.json.JSONArray;

import de.paul.annotations.JSONAnnotation;
import de.paul.annotations.annotators.AnnotationException;
import de.paul.annotations.annotators.RESTAnnotator;
import de.paul.docs.JSONSerializableDocWrapper;
import de.paul.docs.impl.JSONTextDoc;

/**
 * Abstract class for corpus document annotation.
 * 
 * Abstract method getAnnotator() allows setting the specific annotator in sub
 * classes.
 * 
 * @author Chris
 *
 */
public abstract class CorpusAnnotator {

	/*
	 * calls specified annotator, prints output to console and file
	 */
	public JSONSerializableDocWrapper[] annotateCorpus(String[] docs)
			throws AnnotationException, IOException {

		int docCount = docs.length;
		JSONSerializableDocWrapper[] jsonDocs = new JSONSerializableDocWrapper[docCount];
		for (int i = 0; i < Math.round(Math.min(docCount, docs.length)); i++) {
			String doc = docs[i];
			jsonDocs[i] = annotateDoc(i, doc);
		}
		return jsonDocs;
	}

	protected JSONSerializableDocWrapper annotateDoc(int id, String doc)
			throws AnnotationException {

		RESTAnnotator annotator = getAnnotator();
		// String s = "Doc " + id + ": \n - text: " + doc + "\n - annots: ";
		List<JSONAnnotation> annots = annotator.annotate(doc);
		// for (JSONAnnotation a : annots)
		// s += "( " + a.getEntity() + ", " + a.getWeight() + "), ";
		// System.out.println(s);
		// set title end position in string
		int endOfTitle = doc.indexOf(' ', 50);
		if (endOfTitle == -1)
			endOfTitle = doc.length();
		return new JSONSerializableDocWrapper(
				new JSONTextDoc(Integer.toString(id), doc, doc.substring(0,
						endOfTitle), annots));
	}

	public void writeToFile(JSONSerializableDocWrapper[] jsonDocs,
			String jsonoutputpath) {

		FileWriter fw = null;
		try {
			fw = new FileWriter(jsonoutputpath);
			JSONArray arr = new JSONArray(jsonDocs);
			String s = arr.toString(2);
			fw.write(s + "\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/*
	 * Set annotator that will be used in corpus annotation method.
	 */
	protected abstract RESTAnnotator getAnnotator();
}
