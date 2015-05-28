package de.paul.corpora.annotators;

import java.io.IOException;

import de.paul.annotations.annotators.AnnotationException;
import de.paul.annotations.annotators.RESTAnnotator;
import de.paul.annotations.annotators.XLisaAnnotator;
import de.paul.corpora.parsers.LeeParser;
import de.paul.documents.JSONSerializableDocWrapper;
import de.paul.util.Paths;

public class LeePincombeAnnotator extends CorpusAnnotator {

	private LeeParser crawler = null;

	public static void main(String[] args) throws AnnotationException,
			IOException {

		LeePincombeAnnotator annotator = new LeePincombeAnnotator(
				Paths.LEECORPUSPATH);
		JSONSerializableDocWrapper[] docs = annotator.annotateCorpus(annotator
				.getDocuments());
		// TODO added suffix to prevent overwrite
		annotator.writeToFile(docs, Paths.LEEJSONOUTPUTPATH + "bla");

	}

	public LeePincombeAnnotator(String corpuspath) {
		try {
			crawler = new LeeParser(corpuspath);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	protected String[] getDocuments() {

		String[] docs = crawler.getDocTexts();
		return docs;
	}

	@Override
	protected RESTAnnotator getAnnotator() {

		return new XLisaAnnotator();// DBPediaSpotlightAnnotator();
	}
}
