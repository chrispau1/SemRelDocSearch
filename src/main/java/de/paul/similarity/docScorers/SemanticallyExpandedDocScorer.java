package de.paul.similarity.docScorers;

import de.paul.documents.AnnotatedDoc;
import de.paul.documents.impl.SemanticallyExpandedDoc;
import de.paul.kb.dbpedia.categories.WikiCatHierarchyHandler;
import de.paul.similarity.bipartiteGraphs.SingleEdge_MaxGraph;
import de.paul.similarity.bipartiteGraphs.WeightedBipartiteGraph;
import de.paul.similarity.bipartiteGraphs.WeightedBipartiteGraph.MaxGraph_mode;
import de.paul.similarity.bipartiteGraphs.WeightedBipartiteGraphImpl;
import de.paul.util.CombineMode;
import de.paul.util.Directionality;
import de.paul.util.Paths;

public class SemanticallyExpandedDocScorer extends
		KnowledgeBasedDocScorers<SemanticallyExpandedDoc> {

	private int expansionRadius = 2;// best: 3
	private WikiCatHierarchyHandler hierHandler;

	private Directionality edgeDirMode = Directionality.OUTGOING;
	private CombineMode combineMode = CombineMode.PLUS;

	public SemanticallyExpandedDocScorer(int expRadius,
			Directionality edgeMode, CombineMode combineMode) {

		super();
		hierHandler = WikiCatHierarchyHandler
				.getInstance(Paths.TDB_DBPEDIA_HIERARCHY);
		if (expRadius >= 0 && expRadius < 5)
			this.expansionRadius = expRadius;
		else
			System.err.println("Invalid expansion radius " + expRadius
					+ " provided. Must be between 0 and 4");
		if (edgeMode != null)
			this.edgeDirMode = edgeMode;
		if (combineMode != null)
			this.combineMode = combineMode;
	}

	public void setEdgeDirMode(Directionality mode) {
		this.edgeDirMode = mode;
	}

	public void setCombineMode(CombineMode combineMode) {
		this.combineMode = combineMode;
	}

	public SemanticallyExpandedDoc createNewDoc(AnnotatedDoc doc) {

		return new SemanticallyExpandedDoc(doc, expansionRadius, dbpHandler,
				hierHandler, combineMode, edgeDirMode);
	}

	@Override
	public double score(SemanticallyExpandedDoc doc1,
			SemanticallyExpandedDoc doc2) {

		WeightedBipartiteGraphImpl bipartiteGraph = new WeightedBipartiteGraphImpl(
				doc1, doc2);
		// print matrix
		// if (doc1.getId().equals("8") && doc2.getId().equals("23"))
		// printPairwiseEntityScoreMatrix(bipartiteGraph);
		// do scoring
		WeightedBipartiteGraph.mwbg_mode = MaxGraph_mode.SingleEdge;
		SingleEdge_MaxGraph se_MWBG = bipartiteGraph.computeMaxGraph();
		double result = se_MWBG.similarityScore();
		// System.out.println(result);
		return result;
	}

	@Override
	public String writeCSVHeader() {

		return "id,combination";
	}

}
