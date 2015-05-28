package de.paul.corpora.parsers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Parser for Mohler / Mihalcea Assignment corpus.
 * 
 * @author Chris
 *
 */
public class AssignmentsParser {

	private ArrayList<AssignmentQuestion> questions;

	public AssignmentsParser(String path) throws IOException {

		File f = new File(path);
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(f));
			String line;
			this.setQuestions(new ArrayList<AssignmentQuestion>());
			int i = 0;
			AssignmentQuestion currQuestion = null;
			while ((line = br.readLine()) != null) {

				// end question
				if (line.equals("")) {
					// add previous question
					if (currQuestion != null) {
						this.getQuestions().add(currQuestion);
					}
					// new question
				} else if (line.startsWith("###")) {

					// skip question string itself
					line = br.readLine();
					// get correct answer text
					line = br.readLine();
					String corrAnswer = line.split("Answer:")[1];
					currQuestion = new AssignmentQuestion(corrAnswer);
					// skip empty line
					line = br.readLine();
				} else {
					// split student response string
					String[] fields = line.split("\\s+", 3);
					double score = Double.parseDouble(fields[0]);
					// ignore student id
					String studentResponse = fields[2];
					// maybe process text? <br>s and more?
					currQuestion.addStudentAnswer(studentResponse, score);
				}
				i++;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			br.close();
		}
	}

	public ArrayList<AssignmentQuestion> getQuestions() {
		return questions;
	}

	public void setQuestions(ArrayList<AssignmentQuestion> questions) {
		this.questions = questions;
	}

}
