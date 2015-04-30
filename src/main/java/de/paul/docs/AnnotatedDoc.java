package de.paul.docs;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.elasticsearch.common.Strings;

import de.paul.annotations.Annotatable;

/**
 * Plain document for this context.
 * 
 * Specifies title, text and annotation fields.
 * 
 * @author Chris
 *
 */
public class AnnotatedDoc implements RankableDoc, Iterable<Annotatable> {

	protected String id = null;
	protected String text;
	protected List<Annotatable> annotations;
	protected String title;
	private double simScore = -17031989;

	public AnnotatedDoc(AnnotatedDoc copy) {
		this.text = copy.getText();
		this.title = copy.getTitle();
		this.id = copy.getId();
		this.annotations = new LinkedList<Annotatable>();
		for (Annotatable ann : copy.annotations) {
			this.annotations.add(ann.copy());
		}
	}

	public AnnotatedDoc(String text, String title, String id) {
		this.text = text;
		this.title = title;
		this.id = id;
		this.annotations = new LinkedList<Annotatable>();
	}

	public String[] getAnnotationsAsStringArray() {

		String[] res = new String[annotations.size()];
		int i = 0;
		for (Annotatable an : annotations) {
			res[i] = an.getEntity();
			i++;
		}

		return res;
	}

	public String getAnnotationsAsString() {

		return Strings.arrayToDelimitedString(
				this.getAnnotationsAsStringArray(), " ");
	}

	public double[] getAnnotationWeightsAsDoubleArray() {

		double[] res = new double[annotations.size()];
		int i = 0;
		for (Annotatable an : annotations) {
			res[i] = an.getWeight();
			i++;
		}

		return res;
	}

	/*
	 * does not create deep copies! Use only for simple objects.
	 */
	public static Collection<Annotatable> unifyAnnotationsSumScores(
			List<Annotatable> annotations) {

		Map<String, Annotatable> uniqueAnnotsSummedScores = new HashMap<String, Annotatable>();
		for (Annotatable annot : annotations) {

			Annotatable ent = uniqueAnnotsSummedScores.get(annot.getEntity());
			if (ent != null) {
				ent.setWeight(ent.getWeight() + annot.getWeight());
				uniqueAnnotsSummedScores.put(annot.getEntity(), ent);
			} else
				uniqueAnnotsSummedScores.put(annot.getEntity(), annot.copy());
		}
		return uniqueAnnotsSummedScores.values();
	}

	public String getText() {
		return text;
	}

	public List<Annotatable> getAnnotations() {
		return annotations;
	}

	public String getTitle() {

		return title;
	}

	public String printAnnotations() {

		String s = "{\n";
		for (Annotatable an : annotations) {
			s += " e: " + an.getEntity() + " , w: " + an.getWeight() + "\n";
		}
		return s + "}";
	}

	public Iterator<Annotatable> iterator() {
		return annotations.iterator();
	}

	public String getId() {

		return id;
	}

	/**
	 * Should be overwritten by implementing classes !!!
	 * 
	 * @return
	 */
	public AnnotatedDoc copy() {
		// return new AnnotatedDoc(text, title, id);
		return new AnnotatedDoc(this);
	}

	public Set<String> getTopics() {

		throw new UnsupportedOperationException();
	}

	public double getScore() {
		return simScore;
	}

	public void setScore(double score) {
		this.simScore = score;
	}

	public boolean equals(Object other) {

		return this.id.equals(((AnnotatedDoc) other).getId());
	}

	public String toString() {
		return "(id: " + this.id + ", title: " + this.title + ", score: "
				+ this.getScore() + ")";
	}

	public int compareTo(RankableDoc arg0) {

		return (int) Math.signum(this.getScore() - arg0.getScore());
	}
}
