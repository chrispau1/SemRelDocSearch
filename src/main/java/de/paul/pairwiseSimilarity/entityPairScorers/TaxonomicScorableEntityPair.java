package de.paul.pairwiseSimilarity.entityPairScorers;

import java.util.Set;

public interface TaxonomicScorableEntityPair extends ScorableEntityPair {

	public int getDepth();

	public int getEntityDepth();

	public int getEntity2Depth();

	public int getCategoryEntityDistance();

	public int getCommonAncestorEntity2Distance();

	public Set<String> getCommonTopics();

}