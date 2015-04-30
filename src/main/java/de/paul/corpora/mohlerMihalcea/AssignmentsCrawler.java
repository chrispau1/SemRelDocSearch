package de.paul.corpora.mohlerMihalcea;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class AssignmentsCrawler {

	public static void main(String[] args) {

	}

	private ArrayList<Question> questions;

	public AssignmentsCrawler(String path) throws IOException {

		File f = new File(path);
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(f));
			String line;
			this.setQuestions(new ArrayList<Question>());
			int i = 0;
			Question currQuestion = null;
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
					currQuestion = new Question(corrAnswer);
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

	public ArrayList<Question> getQuestions() {
		return questions;
	}

	public void setQuestions(ArrayList<Question> questions) {
		this.questions = questions;
	}

}
