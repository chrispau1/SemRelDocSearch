package de.paul.relDocSearch;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import de.paul.db.BulkDocSourceHandler;
import de.paul.db.JSONDocSourceLoader;
import de.paul.db.SimpleDocumentIndex;
import de.paul.db.elasticsearch.ESHandler;
import de.paul.db.elasticsearch.ESImporter;
import de.paul.db.triplestore.TDBHandler;
import de.paul.documents.AnnotatedDoc;
import de.paul.documents.impl.SemanticallyExpandedDoc;
import de.paul.kb.dbpedia.DBPediaHandler;
import de.paul.kb.dbpedia.categories.WikiCatHierarchyHandler;
import de.paul.similarity.docScorers.ElasticSearchScorer;
import de.paul.similarity.docScorers.SemanticallyExpandedDocScorer;
import de.paul.util.CombineMode;
import de.paul.util.Directionality;
import de.paul.util.Paths;

/**
 * Main class for related document search using semantically expanded documents.
 * 
 * @author Chris
 *
 */
public class SemExpRelDocSearch extends RelDocSearch {

	private static final String DELIMITER = "	";
	private static final int EXPANSION_RADIUS = 2;
	private static final String OUTPUTPATH = "statistics/PRE_FULL_Rankings.csv";

	// private SimpleDocumentIndex documentIndex;
	private WikiCatHierarchyHandler hierHandler;
	private DBPediaHandler dbpHandler;
	private String tdbPath;
	private String defaultESDocType;
	private String elasticSearchIndexName;
	private int expansionRadius;
	private int defaultCandidateSetSize;
	private Directionality edgeDirMode = Directionality.OUTGOING;
	private CombineMode combineMode = CombineMode.PLUS;

	public SemExpRelDocSearch(String tdbPath, String idxName, String docType,
			int expRadius, int defaultCandidateSetSize) {

		this.tdbPath = tdbPath;
		this.elasticSearchIndexName = idxName;
		this.defaultESDocType = docType;
		this.expansionRadius = expRadius;
		this.defaultCandidateSetSize = defaultCandidateSetSize;
	}

	public static void main(String[] args) throws NumberFormatException,
			IOException {
		// SemExpRelDocSearch main = new SemExpRelDocSearch(Paths.LEE_TDB,
		// "pincombenew", "news", EXPANSION_RADIUS, 20); // for
		// Lee50
		// corpus
		SemExpRelDocSearch main = new SemExpRelDocSearch(Paths.EXP_DOCS_TDB_R2,
				"es_xlime", "news", EXPANSION_RADIUS, 20); // for xLiMe corpus
		// main.loadLeeDocuments(2);
		// main.loadLeePincombeCorpusIntoES();

		// XLimeQuadStoreHelper quadHelper = XLimeQuadStoreHelper
		// .getInstance(Paths.XLIME_NEWS_PATH);
		// main.bulkExpandAndAddDocuments(quadHelper, 500, 500, 2, "es_xlime",
		// "news");
		// main.prepareDocStore(null);
		// int[] sizes = new int[] { 35 };// , 10, 15, 20, 35, 50 };
		// for (int candSetSize : sizes) {
		// System.out.println("Cand set size " + candSetSize + ": ");
		// String avgNdcg = main.computeAllRankingsScore("pincombenew",
		// "news", candSetSize);
		// System.out.println("(" + candSetSize + "," + avgNdcg + ")");
		// }
		List<SemanticallyExpandedDoc> relatedDocuments = main
				.getRelatedDocuments("http://ijs.si/enryched/265215908");
		System.out.println(relatedDocuments);
	}

	public List<SemanticallyExpandedDoc> getRelatedDocuments(String docType,
			String id, int candidateSetSize) throws FileNotFoundException {

		SemanticallyExpandedDocScorer comboScorer = new SemanticallyExpandedDocScorer(
				EXPANSION_RADIUS, null, null);

		if (docType == null)
			docType = defaultESDocType;

		ElasticSearchScorer esScorer = new ElasticSearchScorer(
				elasticSearchIndexName, docType, candidateSetSize);

		List<SemanticallyExpandedDoc> list = getRelatedDocuments(
				elasticSearchIndexName, docType, id, candidateSetSize,
				comboScorer, esScorer.getRelatedDocuments(id));
		esScorer.close();
		return list;
	}

	@SuppressWarnings("unused")
	private void prepareDocStore(
			List<SemanticallyExpandedDoc> expandedCandidates) {
		dbpHandler = DBPediaHandler.getInstance(Paths.TDB_DBPEDIA);
		hierHandler = WikiCatHierarchyHandler
				.getInstance(Paths.TDB_DBPEDIA_HIERARCHY);
		this.documentIndex = new SimpleDocumentIndex();
		if (expandedCandidates == null) {
			// expand all docs and add to store for easier lookup
			JSONDocSourceLoader jsonParser = new JSONDocSourceLoader(
					Paths.LEE_ANNOTATED_JSON);
			List<AnnotatedDoc> docs = jsonParser.getAllDocs();
			long t1 = System.currentTimeMillis();
			for (AnnotatedDoc simpleDoc : docs) {
				SemanticallyExpandedDoc expDoc = new SemanticallyExpandedDoc(
						simpleDoc, EXPANSION_RADIUS, dbpHandler, hierHandler,
						combineMode, edgeDirMode);
				((SimpleDocumentIndex) this.documentIndex).addDocument(expDoc);
			}
			long t2 = System.currentTimeMillis();
			System.out.println("Average time expanding documents: " + (t2 - t1)
					/ 50.0);
		} else {
			for (SemanticallyExpandedDoc doc : expandedCandidates) {
				((SimpleDocumentIndex) this.documentIndex).addDocument(doc);
			}
		}

	}

	@SuppressWarnings("unused")
	private void loadLeeCorpusIntoES() {

		dbpHandler = DBPediaHandler.getInstance(Paths.TDB_DBPEDIA);
		hierHandler = WikiCatHierarchyHandler
				.getInstance(Paths.TDB_DBPEDIA_HIERARCHY);
		JSONDocSourceLoader leeJSONLoader = new JSONDocSourceLoader(
				Paths.LEE_ANNOTATED_JSON);
		ESImporter importer = ESImporter.getInstance("pincombenew", "news");
		try {
			for (int i = 0; i < leeJSONLoader.getDocCount(); i++) {

				// load and expand doc
				SemanticallyExpandedDoc doc = new SemanticallyExpandedDoc(
						leeJSONLoader.getDocument(Integer.toString(i)),
						EXPANSION_RADIUS, dbpHandler, hierHandler, combineMode,
						edgeDirMode);
				System.out.println("Doc " + i + " loaded");
				// import into ES index
				importer.addExpandedDocument(doc);
			}
		} finally {
			importer.shutdown();
		}
	}

	/**
	 * Expands documents from specified document quadstore
	 * 
	 * @param offset
	 * @param count
	 * @param expRadius
	 * @param idxName
	 * @param docType
	 */
	public void bulkAddDocuments(BulkDocSourceHandler docSource, int offset,
			int count, String docType) {

		dbpHandler = DBPediaHandler.getInstance(Paths.TDB_DBPEDIA);
		hierHandler = WikiCatHierarchyHandler
				.getInstance(Paths.TDB_DBPEDIA_HIERARCHY);
		ESImporter esHandler = ESImporter.getInstance(elasticSearchIndexName,
				docType);
		esHandler.setBulkSize(20);
		TDBHandler docsTDB = TDBHandler.getInstance(Paths.EXP_DOCS_TDB_R
				+ this.expansionRadius);
		try {
			System.setErr(new PrintStream(new BufferedOutputStream(
					new FileOutputStream("sysout.txt"))));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		try {
			// get document ids
			List<String> docIds = docSource.getBulkDocs(offset, count);
			for (String id : docIds) {
				// create expanded doc
				AnnotatedDoc plainDoc = docSource.getDocument(id);
				// expand
				SemanticallyExpandedDoc expDoc = new SemanticallyExpandedDoc(
						plainDoc, this.expansionRadius, dbpHandler,
						hierHandler, combineMode, edgeDirMode);
				/*
				 * Import into ES
				 */
				esHandler.addDocToBulk(expDoc);
				System.out.print("Doc added to ES...");
				/*
				 * Import into Docs TDB
				 */
				docsTDB.addExpandedDocument(expDoc);
				System.out.print("and tdb.\n");
			}
			// commit remaining if applies
			esHandler.bulkCommitNow();
		} finally {
			esHandler.shutdown();
			dbpHandler.close();
			hierHandler.close();
		}
	}

	@SuppressWarnings("unused")
	private void loadLeeCorpusIntoTDB(int expRadius) {

		JSONDocSourceLoader jsonParser = new JSONDocSourceLoader(
				Paths.LEE_ANNOTATED_JSON);
		dbpHandler = DBPediaHandler.getInstance(Paths.TDB_DBPEDIA);
		hierHandler = WikiCatHierarchyHandler
				.getInstance(Paths.TDB_DBPEDIA_HIERARCHY);
		TDBHandler docsTDB = TDBHandler.getInstance(tdbPath);
		try {
			System.setErr(new PrintStream(new BufferedOutputStream(
					new FileOutputStream("sysout.txt"))));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		try {
			// get document ids
			List<AnnotatedDoc> docs = jsonParser.getAllDocs();
			for (AnnotatedDoc plainDoc : docs) {
				// create expanded doc
				SemanticallyExpandedDoc expDoc = new SemanticallyExpandedDoc(
						plainDoc, expRadius, dbpHandler, hierHandler,
						combineMode, edgeDirMode);

				/*
				 * Import into Docs TDB
				 */
				docsTDB.addExpandedDocument(expDoc);
				System.out.print("Doc added to tdb.\n");
			}
		} finally {
			dbpHandler.close();
			hierHandler.close();
		}
	}

	/**
	 * New document gets added to corpus, most similar documents are output.
	 *
	 * @param newDoc
	 * @return
	 * @throws FileNotFoundException
	 */
	public List<SemanticallyExpandedDoc> getRelatedDocuments(
			AnnotatedDoc newDoc, String docType, int candidateSetSize)
			throws FileNotFoundException {

		String id = this.addDocument(newDoc, docType);
		return this.getRelatedDocuments(docType, id, candidateSetSize);
	}

	public String addDocument(AnnotatedDoc newDoc, String docType) {

		String id = null;
		dbpHandler = DBPediaHandler.getInstance(Paths.TDB_DBPEDIA);
		hierHandler = WikiCatHierarchyHandler
				.getInstance(Paths.TDB_DBPEDIA_HIERARCHY);
		ESImporter esHandler = ESImporter.getInstance(
				this.elasticSearchIndexName, docType);
		esHandler.setBulkSize(20);
		TDBHandler docsTDB = TDBHandler.getInstance(tdbPath);
		try {
			// expand
			SemanticallyExpandedDoc expDoc = new SemanticallyExpandedDoc(
					newDoc, this.expansionRadius, dbpHandler, hierHandler,
					combineMode, edgeDirMode);
			/*
			 * Import into ES
			 */
			esHandler.addExpandedDocument(expDoc);
			System.out.print("Doc added to ES...");
			/*
			 * Import into Docs TDB
			 */
			docsTDB.addExpandedDocument(expDoc);
			// if everything worked, set id to be returned
			id = expDoc.getId();
			System.out.print("and tdb.\n");
		} finally {
			esHandler.shutdown();
			dbpHandler.close();
			hierHandler.close();
		}
		return id;
	}

	/*
	 * Document gets pulled from corpus. Most similar documents are returned.
	 */
	private List<SemanticallyExpandedDoc> getRelatedDocuments(String idxName,
			String docType, String id, int candidateSetSize,
			SemanticallyExpandedDocScorer comboScorer,
			List<AnnotatedDoc> candidateSet) throws FileNotFoundException {

		// get candidate set by running ES search
		// int timeOneIter = 0;
		// long t2 = System.currentTimeMillis();
		// System.out.println("Generate candidate set: " + (t2 - t1));
		// timeOneIter += (t2 - t1);
		// candSetGenTime += (t2 - t1);
		// System.out.println("Candidate set generated.");
		List<SemanticallyExpandedDoc> finalResults = null;
		List<SemanticallyExpandedDoc> expandedCandidates = new LinkedList<SemanticallyExpandedDoc>();

		// for all candidates, get expanded versions
		if (candidateSet != null) {
			// if (candidateSet.size() < candidateSetSize)
			// System.out.println("returned only " + candidateSet.size()
			// + " instead of " + candidateSetSize);
			// if there is a Triplestore with expanded documents, use it
			if (tdbPath != null) {
				TDBHandler docsTDB = TDBHandler.getInstance(tdbPath);
				// long t1b = System.currentTimeMillis();
				for (AnnotatedDoc cand : candidateSet) {

					expandedCandidates.add(getDocumentFromTDB(docsTDB,
							cand.getId()));
				}
				// add expanded query document
				expandedCandidates.add(getDocumentFromTDB(docsTDB, id));
				// long t2b = System.currentTimeMillis();
				// System.out.println("Retrieve expanded candidate documents: "
				// + (t2b - t1b));
				// timeOneIter += (t2b - t1b);
				// timeSpentOnDocRetrieval += t2b - t1b;
				// docsRetrieved += expandedCandidates.size();
			} else if (this.documentIndex != null) {
				// if no triplestore was given, check for this class' doc store.
				for (AnnotatedDoc cand : candidateSet) {
					expandedCandidates
							.add((SemanticallyExpandedDoc) this.documentIndex
									.getDocument(cand.getId()));
				}
				// add query doc
				expandedCandidates
						.add((SemanticallyExpandedDoc) this.documentIndex
								.getDocument(id));
			} else {
				// expand docs
				System.out
						.println("No triplestore available for expanded doc retrieval. "
								+ "Docs will be expanded.");
				dbpHandler = DBPediaHandler.getInstance(Paths.TDB_DBPEDIA);
				hierHandler = WikiCatHierarchyHandler
						.getInstance(Paths.TDB_DBPEDIA_HIERARCHY);
				for (AnnotatedDoc cand : candidateSet) {

					SemanticallyExpandedDoc expDoc = new SemanticallyExpandedDoc(
							cand, EXPANSION_RADIUS, dbpHandler, hierHandler,
							combineMode, edgeDirMode);
					expandedCandidates.add(expDoc);
				}
				// expand query doc and add
				SemanticallyExpandedDoc expQueryDoc = new SemanticallyExpandedDoc(
						this.getDocumentFromES(idxName, docType, id),
						EXPANSION_RADIUS, dbpHandler, hierHandler, combineMode,
						edgeDirMode);
				expandedCandidates.add(expQueryDoc);
			}

			/*
			 * just for performance eval
			 */
			// expandAndAnalyzeDocuments(expandedCandidates);
			/*
			 * 
			 */
			// System.out.println("Query document added.");
			// init pairwise scorer
			// System.out.println("Scorer initialized.");
			comboScorer.setCorpus(expandedCandidates);
			// System.out.println("Corpus generated.");
			// long t3 = System.currentTimeMillis();
			finalResults = comboScorer.getRelatedDocuments(id);
			// long t4 = System.currentTimeMillis();
			// System.out.println("Compute pairwise sims: " + (t4 - t3));
			// timeOneIter += (t4 - t3);
			// pairwiseScoreTime += (t4 - t3);
			// System.out.println("Ranking produced.");
			outputRankings(candidateSet, finalResults, OUTPUTPATH);
		} else
			System.out.println("no candidates...?");
		// timeAllIter += timeOneIter;
		return finalResults;
	}

	// private void expandAndAnalyzeDocuments(
	// List<FullyExpandedDoc> expandedCandidates)
	// throws FileNotFoundException {
	//
	// dbpHandler = DBPediaHandler.getInstance(Paths.TDB_DBPEDIA);
	// hierHandler = HierarchyHandler.getInstance(Paths.TDB_DBPEDIA_HIERARCHY);
	// int timeSum = 0;
	// int annotSum = 0;
	// PrintStream bla = System.out;
	// File file = new File("transStats.txt");
	// FileOutputStream fos = new FileOutputStream(file);
	// PrintStream ps = new PrintStream(fos);
	// System.setOut(ps);
	// StringBuilder sb = new StringBuilder();
	// for (FullyExpandedDoc fullCand : expandedCandidates) {
	// QuadDoc cand = new QuadDoc(fullCand.getId(), fullCand.getText(),
	// fullCand.getTitle(), fullCand.getAnnotations());
	// long t1 = System.currentTimeMillis();
	// FullyExpandedDoc doc = new FullyExpandedDoc(cand, EXPANSION_RADIUS,
	// dbpHandler, hierHandler, null);
	// long t2 = System.currentTimeMillis();
	// int annotCount = doc.getAnnotations().size();
	// long time = t2 - t1;
	// sb.append(annotCount + "," + time + "\n");
	// timeSum += time;
	// annotSum += annotCount;
	// }
	// System.setOut(bla);
	// // System.out.println(sb.toString());
	// // System.out.println("Total time: " + timeSum + ", total annots: "
	// // + annotSum);
	// }

	private SemanticallyExpandedDoc getDocumentFromTDB(TDBHandler docsTDB,
			String id) {

		return docsTDB.getDocument(id);
	}

	private AnnotatedDoc getDocumentFromES(String idxName, String docType,
			String id) {
		ESHandler handler = null;
		/*
		 * get query doc itself !
		 */
		AnnotatedDoc doc = null;
		try {
			handler = ESHandler.getInstance(idxName, docType);
			doc = handler.getDocument(id);
		} finally {
			handler.shutdown();
		}
		return doc;
	}

	private void outputRankings(List<AnnotatedDoc> candidateSet,
			List<SemanticallyExpandedDoc> finalResults, String outputpath) {

		int pos = 0;
		HashMap<String, Integer> idAtPos = new HashMap<String, Integer>();
		for (AnnotatedDoc doc : candidateSet) {

			idAtPos.put(doc.getId(), pos);
			pos++;
		}
		pos = 0;
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(OUTPUTPATH));
			bw.write("docID" + DELIMITER + "docTitle" + DELIMITER + "finalPos"
					+ DELIMITER + "candidatePos\n");
			for (SemanticallyExpandedDoc doc : finalResults) {

				Integer candPos = idAtPos.get(doc.getId());
				String row = doc.getId() + DELIMITER + doc.getTitle()
						+ DELIMITER + pos + DELIMITER + candPos + "\n";
				bw.write(row);
				pos++;
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
	public SemanticallyExpandedDoc createNewDoc(AnnotatedDoc docId) {

		return new SemanticallyExpandedDoc(docId, EXPANSION_RADIUS, dbpHandler,
				hierHandler, combineMode, edgeDirMode);
	}

	@Override
	public List<SemanticallyExpandedDoc> getRelatedDocuments(String queryDoc) {

		try {
			return getRelatedDocuments(defaultESDocType, queryDoc,
					defaultCandidateSetSize);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void computeRankingForVariations(String queryDoc) {

		SemanticallyExpandedDocScorer comboScorer = new SemanticallyExpandedDocScorer(
				EXPANSION_RADIUS, null, null);

		ElasticSearchScorer esScorer = new ElasticSearchScorer(
				elasticSearchIndexName, defaultESDocType,
				defaultCandidateSetSize);

		List<AnnotatedDoc> candSet = esScorer.getRelatedDocuments(queryDoc);
		List<SemanticallyExpandedDoc> fullSet = new LinkedList<SemanticallyExpandedDoc>();
		for (AnnotatedDoc elem : candSet) {
			SemanticallyExpandedDoc fd = new SemanticallyExpandedDoc(
					elem.getId(), elem.getTitle(), elem.getText(), null);
			fullSet.add(fd);
		}
		rankingsPerVariation = new HashMap<Integer, List<SemanticallyExpandedDoc>>();
		rankingsPerVariation.put(0, fullSet);
		List<SemanticallyExpandedDoc> list = null;
		try {
			list = getRelatedDocuments(elasticSearchIndexName,
					defaultESDocType, queryDoc, defaultCandidateSetSize,
					comboScorer, candSet);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		esScorer.close();
		rankingsPerVariation.put(1, list);
	}

	@Override
	public String writeCSVHeader() {

		return "pre-search,full search";
	}
}
