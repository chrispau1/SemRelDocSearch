package de.paul.similarity.docScorers;

import de.paul.documents.AnnotatedDoc;
import de.paul.kb.dbpedia.DBPediaHandler;
import de.paul.util.Paths;

public abstract class KnowledgeBasedDocScorers<x extends AnnotatedDoc> extends
		PairwiseDocScorer<x> {

	protected DBPediaHandler dbpHandler;

	public KnowledgeBasedDocScorers() {

		dbpHandler = DBPediaHandler.getInstance(Paths.TDB_DBPEDIA);
	}

}
