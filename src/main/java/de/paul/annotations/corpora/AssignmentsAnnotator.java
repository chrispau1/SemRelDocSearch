package de.paul.annotations.corpora;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.paul.annotations.annotators.AnnotationException;
import de.paul.annotations.annotators.RESTAnnotator;
import de.paul.annotations.annotators.XLisaAnnotator;
import de.paul.corpora.mohlerMihalcea.AssignmentsCrawler;
import de.paul.corpora.mohlerMihalcea.Question;
import de.paul.corpora.mohlerMihalcea.Question.ScoredText;
import de.paul.docs.JSONSerializableDocWrapper;
import de.paul.util.MapUtil;
import de.paul.util.Paths;

public class AssignmentsAnnotator extends CorpusAnnotator {

	private AssignmentsCrawler crawler;

	public static void main(String[] args) throws AnnotationException {

		AssignmentsAnnotator assAnn = new AssignmentsAnnotator(
				Paths.MOMICORPUSPATH);
		assAnn.annotateAssignments();
	}

	private void annotateAssignments() throws AnnotationException {

		int id = 0;
		// result object for evaluation file (sim scores)
		Map<String, Double> simScoresMap = new HashMap<String, Double>();
		// result object for annotated corpus file
		ArrayList<JSONSerializableDocWrapper> docs = new ArrayList<JSONSerializableDocWrapper>();
		// source object
		ArrayList<Question> questions = crawler.getQuestions();
		for (Question q : questions) {
			String correctAnswer = q.getCorrectAnswer();
			JSONSerializableDocWrapper corrAnsDoc = annotateDoc(id,
					correctAnswer);
			/*
			 * Add correct answer to corpus object
			 */
			docs.add(corrAnsDoc);
			List<ScoredText> answers = q.getAnswers();
			id++;
			for (ScoredText a : answers) {
				JSONSerializableDocWrapper studAnsDoc = annotateDoc(id,
						a.getText());
				/*
				 * Add student answer as a document to list of data to be
				 * written to corpus file
				 */
				docs.add(studAnsDoc);
				/*
				 * add value to filterMap that will be used to discard the 230
				 * highest ranked student answers (like in Li et al. 2006 and
				 * SSA), and then to write remaining sim scores to file.
				 */
				simScoresMap.put(
						createEvalRow(-1, corrAnsDoc.getDoc().getId(),
								studAnsDoc.getDoc().getId(), a.getScore()), a
								.getScore());
				id++;
			}
		}
		/*
		 * Write corpus and eval files
		 */
		this.writeToFile(docs.toArray(new JSONSerializableDocWrapper[] {}),
				Paths.MOMIJSONOUTPUTPATH);
		this.writeMapToCSVFile(simScoresMap, Paths.MOMIEVALPATH_CSV);
	}

	private void writeMapToCSVFile(Map<String, Double> simScoresMap,
			String momievalpathCsv) {

		/*
		 * Filter out 230 most highly ranked answers
		 */
		ArrayList<String> simScores = filterOutNHighest(simScoresMap);
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(momievalpathCsv));
			bw.write("SubjectID,Document1,Document2,Similarity\n");
			for (String row : simScores) {
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

	private ArrayList<String> filterOutNHighest(Map<String, Double> simScoresMap) {

		Map<String, Double> sortedMap = MapUtil.sortDescByValue(simScoresMap);
		int i = 0;
		ArrayList<String> res = new ArrayList<String>();
		for (Entry<String, Double> entry : sortedMap.entrySet()) {
			if (i >= 230) {
				res.add(entry.getKey());
			}
			i++;
		}
		return res;
	}

	private String createEvalRow(int subjectID, String doc1Id, String doc2Id,
			double score) {

		return subjectID + "," + doc1Id + "," + doc2Id + "," + score;
	}

	public AssignmentsAnnotator(String corpuspath) {
		try {
			crawler = new AssignmentsCrawler(corpuspath);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	@Override
	protected RESTAnnotator getAnnotator() {

		return new XLisaAnnotator();
	}

}
