package de.paul.similarity.bipartiteGraphs;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import de.paul.annotations.Annotatable;
import de.paul.documents.AnnotatedDoc;
import de.paul.similarity.entityScorers.ScorableEntityPair;

/**
 * Weighted Bipartite Graph implementation.
 * 
 * @author Chris
 *
 */
public class WeightedBipartiteGraphImpl extends WeightedBipartiteGraph {

	/**
	 * Builds full bipartite graph between the two documents.
	 * 
	 * @param doc1
	 * @param doc2
	 */
	public WeightedBipartiteGraphImpl(AnnotatedDoc doc1, AnnotatedDoc doc2) {

		this.doc1 = doc1;
		this.doc2 = doc2;
		this.annotCount1 = doc1.getAnnotations().size();
		this.annotCount2 = doc2.getAnnotations().size();
		// iterate through pairs and compute taxonomical distances
		Iterator<Annotatable> it1 = doc1.iterator();
		while (it1.hasNext()) {
			Annotatable annot1 = it1.next();
			Iterator<Annotatable> it2 = doc2.iterator();
			while (it2.hasNext()) {
				Annotatable annot2 = it2.next();
				// produce edge, e.g. in case of taxonomic metric: lca
				ScorableEntityPair edge = annot1.createEdge(annot2);
				// add to edges of bipartite graph
				if (edge != null)
					this.addMatching(edge);
			}
		}
	}

	/**
	 * Create a "Single Edge" MWBG. Hereby defined as the subgraph of a
	 * bipartite graph that for each node of each partition contains the maximum
	 * weight edge connecting it with the other partition.
	 * 
	 * @return
	 */
	public SingleEdge_MWBG findSingleEdgeMaximumWeight() {

		SingleEdge_MWBG subgraph = WeightedBipartiteGraph.produce_MWBG(
				this.doc1, this.doc2);
		// objects to keep track of maximums
		Map<String, Double> maxima1 = new HashMap<String, Double>();
		Map<String, ScorableEntityPair> maxMatchings1 = new HashMap<String, ScorableEntityPair>();
		Map<String, Double> maxima2 = new HashMap<String, Double>();
		Map<String, ScorableEntityPair> maxMatchings2 = new HashMap<String, ScorableEntityPair>();
		/*
		 * find maximum outgoing edges for each node
		 */
		if (matchings != null) {
			for (ScorableEntityPair m : matchings) {

				String ent1Name = m.getEntityName();
				String ent2Name = m.getEntity2Name();
				double leftScore = m.leftScore();
				// previous max
				Double max1 = maxima1.get(ent1Name);
				// if no max yet or found new max
				if (max1 == null || leftScore > max1) {
					maxima1.put(ent1Name, leftScore);
					maxMatchings1.put(ent1Name, m);
				}
				// previous max
				double rightScore = m.rightScore();
				Double max2 = maxima2.get(ent2Name);
				// if no max yet or found new max
				if (max2 == null || rightScore > max2) {
					maxima2.put(ent2Name, rightScore);
					maxMatchings2.put(ent2Name, m);
				}
			}
			/*
			 * Go through max maps and add matchings to subgraph
			 */
			for (ScorableEntityPair m : maxMatchings1.values()) {
				m.setLeft();
				subgraph.addMatching(m);
				// System.out.println("1 : " + m);
			}
			for (ScorableEntityPair m : maxMatchings2.values()) {
				m.setRight();
				subgraph.addMatching(m);
				// System.out.println("2 : " + m);
			}
			// System.out.println("");
		}
		return subgraph;
	}

	/**
	 * Computes the 1-1 (a.k.a. Independent Edge) Maximum Weight Bipartite Graph
	 * for this bipartite graph.
	 * 
	 * @return
	 */
	public IndependentEdge_MWBG findIndependentEdgeMaximumWeightBG() {

		return null;
	}

	/**
	 * This class does not provide an implementation due to the large and messy
	 * graph it potentially represents.
	 */
	@Override
	public double similarityScore() throws UnsupportedOperationException {

		throw new UnsupportedOperationException();
	}

}
