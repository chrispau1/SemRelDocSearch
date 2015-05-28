package de.paul.documents;

/**
 * Interface for document classes that can be ranked. A score can be assigned
 * and queried from them.
 * 
 * @author Chris
 *
 */
public interface RankableDoc extends AnnotatableDocument,
		Comparable<RankableDoc> {

	public double getScore();

	public void setScore(double score);

}
