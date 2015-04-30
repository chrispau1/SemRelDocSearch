package de.paul.corpora.mohlerMihalcea;

import java.util.ArrayList;
import java.util.List;

public class Question {

	private String correctAnswer;
	private List<ScoredText> studentAnswers;

	public Question(String text) {
		setQuestion(text);
		setAnswers(new ArrayList<ScoredText>());
	}

	public Question() {

		setAnswers(new ArrayList<ScoredText>());
	}

	public String getCorrectAnswer() {
		return correctAnswer;
	}

	public void setQuestion(String question) {
		this.correctAnswer = question;
	}

	public List<ScoredText> getAnswers() {
		return studentAnswers;
	}

	public void setAnswers(List<ScoredText> answers) {
		this.studentAnswers = answers;
	}

	public class ScoredText {
		ScoredText(String studentResponse, double score2) {
			this.text = studentResponse;
			this.score = score2;
		}

		String text;
		double score;

		public String getText() {
			return text;
		}

		public double getScore() {
			return score;
		}
	}

	public void addStudentAnswer(String studentResponse, double score) {

		this.studentAnswers.add(new ScoredText(studentResponse, score));
	}
}
