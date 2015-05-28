package de.paul.annotations;


/**
 * Represents a category from the DBPedia Category Hierarchy.
 * 
 * Captures name and depth of the category, plus information about the DBPedia
 * entity it is associated with in this context. This is necessary to maintain
 * the knowledge of which entity and category belong together and to compute
 * distances between categories and entities.
 * 
 * @author Chris
 *
 */
public class Category {

	protected String name;
	protected int depth;
	private DepthAnnotation annotation;

	public Category(String name, int depth) {

		this.setName(name);
		this.setDepth(depth);
	}

	public Category(String name, int depth, DepthAnnotation annotation) {

		this.setName(name);
		this.setDepth(depth);
		this.setAnnotation(annotation);
	}

	public int getDepth() {
		return depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getEntityDepth() {
		return annotation.getDepth();
	}

	public String getEntityName() {

		return getAnnotation().getEntity();
	}

	public double getEntityWeight() {

		return getAnnotation().getWeight();
	}

	public int getCategoryEntityDistance() {

		return annotation.getDepth() - this.depth;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof Category
				&& this.name.equals(((Category) obj).getName());
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	public String toString() {
		return "(name: " + this.name + ", depth: " + this.depth + ")";
	}

	public DepthAnnotation getAnnotation() {
		return annotation;
	}

	public void setAnnotation(DepthAnnotation a) {
		this.annotation = a.copy();
	}

	// public void setAnnotation(String entName, double entWeight, int depth) {
	// this.annotation = new DepthAnnotation(entName, entWeight, depth);
	// }
}
