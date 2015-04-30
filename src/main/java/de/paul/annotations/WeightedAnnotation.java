package de.paul.annotations;

import java.util.LinkedList;
import java.util.List;

import de.paul.pairwiseSimilarity.entityPairScorers.ScorableEntityPair;

public class WeightedAnnotation implements Annotatable {

	private static final String annotationDelimiter = ";;;";
	private static final String entWeightDelimiter = ",,,";
	private String entity;
	private double weight;

	public WeightedAnnotation(String ent, double weight) {

		this.setEntity(ent);
		this.setWeight(weight);
	}

	public String getEntity() {
		return entity;
	}

	public void setEntity(String entity) {
		this.entity = entity;
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	public boolean equals(Object other) {

		return this.getEntity().equals(((Annotatable) other).getEntity());
	}

	public int hashCode() {
		return this.getEntity().hashCode();
	}

	public Annotatable copy() {

		return new WeightedAnnotation(entity, weight);
	}

	public ScorableEntityPair createEdge(Annotatable other) {

		throw new UnsupportedOperationException();
	}

	public String toString() {
		return "(" + entity + ", " + weight + ")";
	}

	public static List<Annotatable> parseString(String annotString) {

		List<Annotatable> annotations = new LinkedList<Annotatable>();

		String[] weightedAnnotStrings = annotString.split(annotationDelimiter);
		for (String s : weightedAnnotStrings) {
			String[] parts = s.split(entWeightDelimiter);
			String ent = parts[0];
			double weight = Double.parseDouble(parts[1]);
			annotations.add(new WeightedAnnotation(ent, weight));
		}
		return annotations;
	}

	public static String produceString(List<Annotatable> annotations) {

		String s = "";
		for (Annotatable ann : annotations) {

			if (s.length() != 0)
				s += annotationDelimiter;
			s += ann.getEntity() + entWeightDelimiter + ann.getWeight();
		}
		return s;
	}

}
