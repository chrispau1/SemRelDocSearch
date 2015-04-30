package de.paul.annotations;

import java.util.Set;

import de.paul.util.CountMap;

public class TopicAnnotation extends DepthAnnotation {

	public static final int TOPIC_DEPTH = 1;

	private Set<String> topics;
	protected static int allTopicsSum;
	protected static boolean topicFreqChanged = true;
	protected static CountMap<String> topicFreq = null;

	public TopicAnnotation(String ent, double weight, int depth,
			Set<String> topics) {

		super(ent, weight, depth);
		this.setTopics(topics);
	}

	public Set<String> getTopics() {
		return topics;
	}

	public void setTopics(Set<String> topics) {
		this.topics = topics;
	}

	public static void updateGlobalStats(String topic) {

		if (topicFreq == null)
			topicFreq = new CountMap<String>();
		topicFreq.inc(topic);
		topicFreqChanged = true;
	}

	public double getTopicScore() {

		return getTopicScore(topics);
	}

	public static double getTopicScore(Set<String> topics) {
		int sum = getAllTopicsSum();
		double freq = 0;
		// sum up scores of this entity's topics
		for (String topic : topics)
			freq += topicFreq.get(topic);

		if (Math.abs(freq) > 0.0001)
			// log idf - ratio shows how specific this entity's topics are
			return Math.log(sum / freq);
		else
			return 1;
	}

	private static int getAllTopicsSum() {

		if (topicFreqChanged) {
			allTopicsSum = topicFreq.valueSum();
			topicFreqChanged = false;
		}
		return allTopicsSum;
	}

	public TopicAnnotation copy() {
		return new TopicAnnotation(getEntity(), getWeight(), getDepth(),
				this.topics);
	}
}
