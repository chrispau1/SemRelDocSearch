package de.paul.util;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.ngram.NGramTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;

public class LuceneUtils {

	public static Set<String> getTokens(String value) throws IOException {
		Set<String> result;
		// if (Conf.gramSize > 0) {
		// result = LuceneUtils.tokenize(value, Conf.gramSize);
		// } else {
		// if (Conf.discardSpecialChars)
		// // Removing any non-alphanumeric character from values
		// result = LuceneUtils.analyze(
		// value.replaceAll("[\\W]|_|" + Conf.SEPARATOR, " ")
		// .trim(), Conf.analyzer);
		// else
		// Removing non-ASCII characters because they cause headache in
		// read/write on HDFS
		result = LuceneUtils.analyze(value// value.replaceAll(",|\"|[^\\x00-\\x7F]|""#",
											// "")
				, new EnglishAnalyzer());// WhitespaceAnalyzer(Version.LUCENE_44));
		// }
		return result;
	}

	public static Set<String> analyze(String text, Analyzer analyzer)
			throws IOException {
		Set<String> set = new HashSet<String>();

		TokenStream ts = analyzer.tokenStream(null, new StringReader(text));
		CharTermAttribute charAtt = ts.addAttribute(CharTermAttribute.class);

		try {
			ts.reset(); // this is required in 4.0. Without this, nothing works
						// :)
			while (ts.incrementToken()) {
				set.add(charAtt.toString().toLowerCase());
			}
			ts.end();
		} finally {
			ts.close();
		}
		return set;
	}

	public static Set<String> tokenize(String text, int ngram)
			throws IOException {
		Set<String> set = new HashSet<String>();

		TokenStream ts = new NGramTokenizer(Version.LUCENE_44,
				new StringReader(text), ngram, ngram);
		CharTermAttribute charAtt = ts.addAttribute(CharTermAttribute.class);

		try {
			ts.reset(); // this is required in 4.0. Without this, nothing works
						// :)
			while (ts.incrementToken()) {
				set.add(charAtt.toString());
			}
			ts.end();
		} finally {
			ts.close();
		}
		return set;
	}

	public static Set<String> wordTokenize(String text) throws IOException {

		Analyzer analyzer = new WhitespaceAnalyzer(Version.LUCENE_44);

		Set<String> set = new HashSet<String>();

		TokenStream ts = analyzer.tokenStream(null, new StringReader(text));
		CharTermAttribute charAtt = ts.addAttribute(CharTermAttribute.class);

		try {
			ts.reset(); // this is required in 4.0. Without this, nothing works
						// :)
			while (ts.incrementToken()) {
				set.add(charAtt.toString().toLowerCase());
			}
			ts.end();
		} finally {
			ts.close();
		}
		analyzer.close();
		return set;
	}

	public static void main(String[] args) throws Exception {
		Analyzer analyzer = new SimpleAnalyzer(Version.LUCENE_44);
		Set<String> out1 = LuceneUtils.analyze("hello there", analyzer); // or
																			// any
																			// other
																			// analyzer
		Set<String> out2 = LuceneUtils.wordTokenize("i hate lucene");
		System.out.println(out1.toString());
		System.out.println(out2);
		analyzer.close();
	}
}
