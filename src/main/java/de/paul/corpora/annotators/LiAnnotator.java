package de.paul.corpora.annotators;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.paul.annotations.annotators.AnnotationException;
import de.paul.annotations.annotators.RESTAnnotator;
import de.paul.annotations.annotators.XLisaAnnotator;
import de.paul.corpora.parsers.LiParser;
import de.paul.corpora.parsers.LiParser.ScoredDocPair;
import de.paul.documents.JSONSerializableDocWrapper;
import de.paul.util.Paths;

public class LiAnnotator extends CorpusAnnotator {

	private LiParser crawler = null;

	public LiAnnotator(String corpuspath) {
		try {
			crawler = new LiParser(corpuspath);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	public static void main(String[] args) throws AnnotationException,
			IOException {

		LiAnnotator annotator = new LiAnnotator(Paths.LICORPUSPATH);

		annotator.annotateSentencePairs();

	}

	private void annotateSentencePairs() throws AnnotationException {

		int id = 0;
		// result object for evaluation file (sim scores)
		Map<String, Double> simScoresMap = new HashMap<String, Double>();
		// result object for annotated corpus file
		ArrayList<JSONSerializableDocWrapper> docs = new ArrayList<JSONSerializableDocWrapper>();
		// source object
		ArrayList<ScoredDocPair> scoredSentencePairs = crawler
				.getScoredDocPairs();
		for (ScoredDocPair sentPair : scoredSentencePairs) {
			JSONSerializableDocWrapper doc1 = annotateDoc(id, sentPair.doc1);
			id++;
			JSONSerializableDocWrapper doc2 = annotateDoc(id, sentPair.doc2);
			id++;
			/*
			 * Add new docs to corpus object. Don't mind duplicates - should not
			 * matter. Ranking is not considered atm, for pairwise correlation
			 * duplicates shouldn't make a difference.
			 */
			docs.add(doc1);
			docs.add(doc2);
			/*
			 * Add score
			 */
			simScoresMap.put(
					createEvalRow(-1, doc1.getDoc().getId(), doc2.getDoc()
							.getId(), sentPair.score), sentPair.score);
		}
		/*
		 * Write corpus and eval files
		 */
		this.writeToFile(docs.toArray(new JSONSerializableDocWrapper[] {}),
				Paths.LI_JSONOUTPUTPATH);
		this.writeMapToCSVFile(simScoresMap, Paths.LI_EVALPATH_CSV);
	}

	private String createEvalRow(int subjectID, String doc1Id, String doc2Id,
			double score) {

		return subjectID + "," + doc1Id + "," + doc2Id + "," + score;
	}

	private void writeMapToCSVFile(Map<String, Double> simScoresMap,
			String momievalpathCsv) {

		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(momievalpathCsv));
			bw.write("SubjectID,Document1,Document2,Similarity\n");
			for (String row : simScoresMap.keySet()) {
				bw.write(row + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	protected RESTAnnotator getAnnotator() {

		return new XLisaAnnotator();
	}

}
