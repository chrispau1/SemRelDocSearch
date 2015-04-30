package de.paul.corpora;

import java.util.Set;

public interface GlobalStatsProvider {

	public double getTopicScore(Set<String> topics);
}
