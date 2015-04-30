package de.paul.similarity.docScorers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import cc.mallet.pipe.CharSequence2TokenSequence;
import cc.mallet.pipe.CharSequenceLowercase;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.pipe.TokenSequenceRemoveStopwords;
import cc.mallet.pipe.iterator.CsvIterator;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.types.InstanceList;
import cc.mallet.util.Maths;
import de.paul.corpora.JSONLoader;
import de.paul.docs.AnnotatedDoc;
import de.paul.util.Paths;
import de.paul.util.statistics.NDCGEvaluator;

public class LDA_KL_Scorer extends PairwiseSimScorer<AnnotatedDoc> {

	private ParallelTopicModel model;

	public LDA_KL_Scorer(int numTopics, InstanceList instances,
			int numIterations) {

		super(Paths.LDA_KL_RANKING_SCORES);
		// double alpha = 1.0;
		double alpha = 1.0;// 50.0 / numTopics;
		double beta = 0.1;
		model = new ParallelTopicModel(numTopics, alpha, beta);

		model.addInstances(instances);

		// Use two parallel samplers, which each look at one half the corpus and
		// combine
		// statistics after every iteration.
		model.setNumThreads(2);

		// Run the model for 50 iterations and stop (this is for testing only,
		// for real applications, use 1000 to 2000 iterations)
		model.setNumIterations(numIterations);
		try {
			model.estimate();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws UnsupportedEncodingException,
			FileNotFoundException {

		// Begin by importing documents from text to feature sequences
		ArrayList<Pipe> pipeList = new ArrayList<Pipe>();

		// Pipes: lowercase, tokenize, remove stopwords, map to features
		pipeList.add(new CharSequenceLowercase());
		pipeList.add(new CharSequence2TokenSequence(Pattern
				.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")));
		pipeList.add(new TokenSequenceRemoveStopwords(new File(
				"text_output_data/stoplist_en.txt"), "UTF-8", false, false,
				false));
		pipeList.add(new TokenSequence2FeatureSequence());

		InstanceList instances = new InstanceList(new SerialPipes(pipeList));

		Reader fileReader = new InputStreamReader(new FileInputStream(new File(
				"text_output_data/MalletCorpus.txt")), "UTF-8");
		instances.addThruPipe(new CsvIterator(fileReader, Pattern
				.compile("^(\\S*)[\\s,]*(\\S*)[\\s,]*(.*)$"), 3, 2, 1)); // data,
																			// label,
																			// name
																			// fields

		// Create a model with 100 topics, alpha_t = 0.01, beta_w = 0.01
		// Note that the first parameter is passed as the sum over topics, while
		// the second is numTopics: 30 50 100 200
		LDA_KL_Scorer ldaScorer = null;

		StringBuilder sb = new StringBuilder();

		int[] numTopics = new int[] { 50 };
		int[] numIterations = new int[] { 1000 };
		// int[] numTopics = new int[] { 30, 50, 100, 200 };
		// int[] numIterations = new int[] { 50, 100, 500, 1000 };
		for (int i = 0; i < numTopics.length; i++) {
			for (int j = 0; j < numIterations.length; j++) {
				ldaScorer = new LDA_KL_Scorer(numTopics[i], instances,
						numIterations[j]);
				/*
				 * import documents in memory-based index
				 */

				JSONLoader jsonParser = new JSONLoader(
						Paths.PINCOMBE_ANNOTATED_JSON);
				List<AnnotatedDoc> docs = jsonParser.getAllDocs();
				ldaScorer.expandAndSetCorpus(docs);
				// sb.append(numTopics[i] + ", " + numIterations[j] + ": "
				// + ldaScorer.csvAllRankingScores() + "\n");

				sb.append(numTopics[i]
						+ ", "
						+ numIterations[j]
						+ ": "
						+ ldaScorer
								.completePairwisePearsonScore("statistics/pairs.csv")
						+ "\n");
			}
		}
		System.out.println(sb.toString());
	}

	@Override
	public double score(AnnotatedDoc doc1, AnnotatedDoc doc2) {
		double[] topicDistribution0 = model.getTopicProbabilities(Integer
				.parseInt(doc1.getId()));
		double[] topicDistribution1 = model.getTopicProbabilities(Integer
				.parseInt(doc2.getId()));
		return -Maths.klDivergence(topicDistribution0, topicDistribution1);
	}

	@Override
	public AnnotatedDoc createNewDoc(AnnotatedDoc doc) {
		return new AnnotatedDoc(doc.getText(), doc.getTitle(), doc.getId());
	}

	@Override
	protected String evaluateScores(int queryDoc) {
		String scoreString = "";
		/*
		 * get human ranking
		 */
		List<AnnotatedDoc> humRanking = getHumanRanking(queryDoc);
		/*
		 * get algorithmic rankings
		 */
		// evaluate
		// System.out.println("human Ranking: "
		// + humRanking.subList(0,
		// NDCGEvaluator.getRelevantElementsCount(humRanking, 3.0)
		// * RESULTS_TO_COMPARE_FACTOR));
		NDCGEvaluator<AnnotatedDoc> evaler = new NDCGEvaluator<AnnotatedDoc>(
				3.0);
		for (int i = 0; i < oneDocResults.size(); i++) {
			// evaluate ranked documents by current metric in comparison to
			// human evaluation
			double rankingScore = evaler.evaluateRanking(humRanking,
					oneDocResults.get(i), RESULTS_TO_COMPARE_FACTOR);
			// System.out.println(i + "-th AnnSim score: " + rankingScore);
			scoreString += rankingScore;
			if (i < oneDocResults.size() - 1)
				scoreString += ",";
		}
		return scoreString;
	}

	@Override
	protected String writeCSVHeader() {
		return "id,lda_kl";
	}

}
