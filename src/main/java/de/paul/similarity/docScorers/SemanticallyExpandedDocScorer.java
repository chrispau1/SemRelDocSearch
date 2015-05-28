package de.paul.similarity.docScorers;

import de.paul.documents.AnnotatedDoc;
import de.paul.documents.impl.SemanticallyExpandedDoc;
import de.paul.kb.dbpedia.categories.WikiCatHierarchyHandler;
import de.paul.similarity.bipartiteGraphs.SingleEdge_MWBG;
import de.paul.similarity.bipartiteGraphs.WeightedBipartiteGraph;
import de.paul.similarity.bipartiteGraphs.WeightedBipartiteGraph.MWBG_mode;
import de.paul.similarity.bipartiteGraphs.WeightedBipartiteGraphImpl;
import de.paul.util.Paths;

public class SemanticallyExpandedDocScorer extends
		KnowledgeBasedDocScorers<SemanticallyExpandedDoc> {

	private static final int EXPANSION_RADIUS = 2;// best: 3
	private WikiCatHierarchyHandler hierHandler;

	public SemanticallyExpandedDocScorer() {

		/*
		 * objects used for generation of documents in create new doc method
		 */
		super();
		hierHandler = WikiCatHierarchyHandler.getInstance(Paths.TDB_DBPEDIA_HIERARCHY);
	}

	public SemanticallyExpandedDoc createNewDoc(AnnotatedDoc doc) {

		return new SemanticallyExpandedDoc(doc, EXPANSION_RADIUS, dbpHandler,
				hierHandler);
	}

	@Override
	public double score(SemanticallyExpandedDoc doc1, SemanticallyExpandedDoc doc2) {

		WeightedBipartiteGraphImpl bipartiteGraph = new WeightedBipartiteGraphImpl(
				doc1, doc2);
		// print matrix
		// if (doc1.getId().equals("8") && doc2.getId().equals("23"))
		// printPairwiseEntityScoreMatrix(bipartiteGraph);
		// do scoring
		WeightedBipartiteGraph.mwbg_mode = MWBG_mode.SingleEdge;
		SingleEdge_MWBG se_MWBG = bipartiteGraph.findSingleEdgeMaximumWeight();
		double result = se_MWBG.similarityScore();
		// System.out.println(result);
		return result;
	}

	@Override
	public String writeCSVHeader() {

		return "id,combination";
	}

}
