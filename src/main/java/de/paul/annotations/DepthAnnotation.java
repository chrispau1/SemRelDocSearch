package de.paul.annotations;

/**
 * Weighted annotation that also specifies a depth parameter. Is used for
 * taxonomical depth.
 * 
 * @author Chris
 *
 */
public class DepthAnnotation extends WeightedAnnotation {

	private int depth;

	public DepthAnnotation(String ent, double weight, int depth) {
		super(ent, weight);
		this.setDepth(depth);
	}

	public DepthAnnotation(WeightedAnnotation annot, int depth) {
		super(annot.getEntity(), annot.getWeight());
		this.setDepth(depth);
	}

	public int getDepth() {
		return depth;
	}

	/**
	 * Puts the node depth at the provided depth plus 1 - due to the node being
	 * one level under its category
	 */
	public void setDepth(int catDepth) {
		this.depth = catDepth + 1;
	}

	public DepthAnnotation copy() {
		return new DepthAnnotation(getEntity(), getWeight(), getDepth());
	}

}
