package de.paul.similarity.docScorers;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import de.paul.db.SimpleDocumentIndex;
import de.paul.documents.AnnotatedDoc;
import de.paul.similarity.bipartiteGraphs.WeightedBipartiteGraphImpl;
import de.paul.util.Paths;

/**
 * Abstract, generic class for pairwise document scoring. Defines methods for
 * common use cases, like the actual similarity computation of two documents or
 * the ranking of one or many documents of a corpus.
 * 
 * @author Chris
 *
 * @param <x>
 */
public abstract class PairwiseDocScorer<x extends AnnotatedDoc> extends
		DocumentSimilarityScorer<x> {

	public abstract double score(x doc1, x doc2);

	public x createScoredDoc(String id, double score) {

		x doc = getDocumentFromIndex(id);
		doc.setScore(score);
		return doc;
	}

	protected void printPairwiseEntityScoreMatrix(
			WeightedBipartiteGraphImpl bipartiteGraph) {

		FileWriter fw = null;
		try {
			fw = new FileWriter(Paths.SCOREMATRIX_FILEPATH + "_left");
			String text = bipartiteGraph.printPairwiseLeftScoreMatrix();
			fw.write(text);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			fw = new FileWriter(Paths.SCOREMATRIX_FILEPATH + "_right");
			String text = bipartiteGraph.printPairwiseRightScoreMatrix();
			System.out.println(text);
			fw.write(text);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void expandAndSetCorpus(List<AnnotatedDoc> docs) {

		documentIndex = new SimpleDocumentIndex();
		for (AnnotatedDoc plainDoc : docs) {

			x doc = this.createNewDoc(plainDoc);
			((SimpleDocumentIndex) documentIndex).addDocument(doc);
		}
	}

	public void setCorpus(List<x> docs) {

		documentIndex = new SimpleDocumentIndex();
		for (AnnotatedDoc doc : docs) {
			((SimpleDocumentIndex) documentIndex).addDocument(doc.copy());
		}
	}

	@Override
	public List<x> getRelatedDocuments(String queryDoc) {

		// System.out.println("Loading query doc...");
		x doc1 = getDocumentFromIndex(queryDoc);
		// System.out.println("Loaded query doc");
		// int docCount = this.getDocCount();
		List<x> results = new ArrayList<x>();
		// simScores = new HashMap<Integer, Double>();
		for (String id : documentIndex.getDocIds()) {
			// only compute for different docs
			if (!id.equals(queryDoc)) {
				x doc2 = getDocumentFromIndex(id);
				// call scoring function for each pair
				// simScores.put(i, score(doc1, doc2));
				doc2.setScore(score(doc1, doc2));
				results.add(doc2);
			}
		}
		Collections.sort(results, Collections.reverseOrder());
		return results;
	}

	@Override
	public void computeRankingForVariations(String queryDoc) {

		List<x> results = getRelatedDocuments(queryDoc);
		rankingsPerVariation = new HashMap<Integer, List<x>>();
		rankingsPerVariation.put(0, results);
		// System.out.println("Ranking computed.");
	}

}
