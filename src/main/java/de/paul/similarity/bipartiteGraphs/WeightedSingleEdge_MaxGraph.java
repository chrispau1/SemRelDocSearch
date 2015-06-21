package de.paul.similarity.bipartiteGraphs;

import de.paul.annotations.WeightedAnnotation;
import de.paul.documents.AnnotatedDoc;
import de.paul.similarity.entityScorers.ScorableEntityPair;

public class WeightedSingleEdge_MaxGraph extends SingleEdge_MaxGraph {

	// public WeightedSingleEdge_MWBG(int annotCount1, int annotCount2) {
	// super(annotCount1, annotCount2);
	// // }
	//
	// private AnnotatedDoc doc1;
	// private AnnotatedDoc doc2;

	public WeightedSingleEdge_MaxGraph(AnnotatedDoc doc1, AnnotatedDoc doc2) {
		super(doc1.getAnnotations().size(), doc2.getAnnotations().size());
		this.doc1 = doc1;
		this.doc2 = doc2;
	}

	@Override
	public double similarityScore() throws UnsupportedOperationException {

		return weightedEntityAltAnnSim();
	}

	/*
	 * Considers both entities' weights connected by an edge.
	 */
	private double weightedEntityAltAnnSim() {

		// sum over all edges
		double numerator = 0;
		double denominator = 0;
		for (ScorableEntityPair edge : edges) {

			// get entity topic score
			WeightedAnnotation ent1 = edge.getAnnotation();
			WeightedAnnotation ent2 = edge.getAnnotation2();
			// double topicScore1 = 1, topicScore2 = 1, topicTopicScore = 1;
			// Set<String> commonTopics = edge instanceof
			// TaxonomicEntityPairScorer ? ((TaxonomicEntityPairScorer) edge)
			// .getCommonTopics() : null;
			// if (commonTopics != null && !commonTopics.isEmpty())
			// topicTopicScore = TopicAnnotation.getTopicScore(commonTopics);
			// if (ent1 instanceof TopicAnnotation)
			// topicScore1 = ((TaxonomicExpandedDoc) doc1)
			// .getTopicScore((TopicAnnotation) ent1);
			// if (ent2 instanceof TopicAnnotation)
			// topicScore2 = ((TaxonomicExpandedDoc) doc2)
			// .getTopicScore((TopicAnnotation) ent2);
			// add this edge's contribution to score
			double entity1Weight = 1;// edge.getEntityWeight();
			double entity2Weight = 1;// edge.getEntity2Weight();
			numerator += /* topicScore1 * */entity1Weight * edge.score()
					* entity2Weight;/* * topicScore2 */// * topicTopicScore;
			denominator += /* topicScore1 * */entity1Weight * entity2Weight;
			// * topicTopicScore;
			// * topicScore2;
		}
		double res = 0;
		if (denominator != 0)
			res = numerator / denominator;
		return res;
	}

}
