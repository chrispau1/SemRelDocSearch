package de.paul.annotations;

import org.json.JSONObject;

import de.paul.similarity.entityScorers.ScorableEntityPair;

/**
 * Encapsulates annotation name and annotation weight as a JSONObject for simple
 * JSON parsing
 * 
 * @author Chris
 *
 */
public class JSONAnnotation extends JSONObject implements Annotatable {

	private String entity;
	private double weight;

	public JSONAnnotation(String ent, double weight) {

		this.setEntity(ent);
		this.setWeight(weight);
	}

	public String getEntity() {
		return entity;
	}

	public void setEntity(String entity) {
		if (entity == null)
			entity = "";
		this.entity = entity;
		this.put("entity", entity);
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
		this.put("weight", weight);
	}

	public boolean equals(Object other) {

		if (other == null)
			return false;
		return this.getEntity().equals(((Annotatable) other).getEntity());
	}

	public int hashCode() {
		return this.getEntity().hashCode();
	}

	public Annotatable copy() {

		return new JSONAnnotation(entity, weight);
	}

	public ScorableEntityPair createEdge(Annotatable other) {

		throw new UnsupportedOperationException();
	}
}
