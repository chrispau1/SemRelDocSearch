package de.paul.corpora.li2006;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class LiCrawler {

	private ArrayList<ScoredDocPair> scoredDocPairs;

	public LiCrawler(String path) throws IOException {

		File f = new File(path);
		BufferedReader br = null;
		setScoredDocPairs(new ArrayList<ScoredDocPair>());
		HashSet<Integer> desiredPairs = new HashSet<Integer>(
				Arrays.asList(new Integer[] { 1, 5, 9, 13, 17, 21, 25, 29, 33,
						37, 41, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58,
						59, 60, 61, 62, 63, 64, 65 }));
		try {
			br = new BufferedReader(new FileReader(f));
			String line;
			int i = 0;
			while ((line = br.readLine()) != null) {

				if (desiredPairs.contains(i + 1)) {
					String[] fields = line.split("	");
					String t1 = fields[1];
					String t2 = fields[2];
					double score = Double.parseDouble(fields[3]);
					getScoredDocPairs().add(new ScoredDocPair(t1, t2, score));
				}
				i++;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			br.close();
		}
	}

	public ArrayList<ScoredDocPair> getScoredDocPairs() {
		return scoredDocPairs;
	}

	public void setScoredDocPairs(ArrayList<ScoredDocPair> scoredDocPairs) {
		this.scoredDocPairs = scoredDocPairs;
	}

	public class ScoredDocPair {

		public String doc1;
		public String doc2;
		public double score;

		public ScoredDocPair(String t1, String t2, double score) {
			this.doc1 = t1;
			this.doc2 = t2;
			this.score = score;
		}
	}

}
