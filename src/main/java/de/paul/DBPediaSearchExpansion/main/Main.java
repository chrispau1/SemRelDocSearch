package de.paul.DBPediaSearchExpansion.main;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import de.paul.corpora.JSONLoader;
import de.paul.corpora.SimpleDocumentIndex;
import de.paul.corpora.elasticsearch.ESHandler;
import de.paul.corpora.elasticsearch.ESImporter;
import de.paul.corpora.triplestore.TDBHandler;
import de.paul.corpora.xLiMe.XLimeQuadStoreHelper;
import de.paul.dbpedia.DBPediaHandler;
import de.paul.dbpedia.categories.HierarchyHandler;
import de.paul.docs.AnnotatedDoc;
import de.paul.docs.RankableDoc;
import de.paul.docs.impl.FullyExpandedDoc;
import de.paul.docs.impl.QuadDoc;
import de.paul.evaluation.PinCombeEvalHandler;
import de.paul.similarity.docScorers.ElasticSearchScorer;
import de.paul.similarity.docScorers.FullyExpandedDocScorer;
import de.paul.util.Paths;
import de.paul.util.statistics.NDCGEvaluator;

public class Main {

	private static final String DELIMITER = "	";
	private static final int EXPANSION_RADIUS = 2;
	private static final String OUTPUTPATH = "statistics/PRE_FULL_Rankings.csv";
	private static final String LEE_TDB = "tdbStore/lee";

	public static void main(String[] args) throws NumberFormatException,
			IOException {

		Main main = new Main();
		// main.loadLeeDocuments(2);
		// main.loadLeePincombeCorpusIntoES();
		// main.loadDocuments(500, 500, 2, "es_xlime", "news");
		main.prepareDocStore(null);
		int[] sizes = new int[] { 35 };// , 10, 15, 20, 35, 50 };
		for (int candSetSize : sizes) {
			System.out.println("Cand set size " + candSetSize + ": ");
			String avgNdcg = main.computeAllRankingsScore("pincombenew",
					"news", candSetSize);
			System.out.println("(" + candSetSize + "," + avgNdcg + ")");
		}
		// best yet: "http://ijs.si/enryched/265215060" baseball. .... 265220666
		// = milch...265211349=syrien....265217455=tikrit ,,best 265269257
		// main.getRelatedContent("es_xlime", "news",
		// "http://ijs.si/enryched/265215908", 100, Paths.EXP_DOCS_TDB_R2);
	}

	private List<FullyExpandedDoc> getRelatedContent(String idxName,
			String docType, String id, int candidateSetSize, String tdbPath)
			throws FileNotFoundException {

		FullyExpandedDocScorer comboScorer = new FullyExpandedDocScorer(
				Paths.COMBINATION_RANKING_SCORES);

		ElasticSearchScorer esScorer = new ElasticSearchScorer(
				Paths.ES_RANKING_SCORES, idxName, docType, candidateSetSize);

		List<FullyExpandedDoc> list = getRelatedContent(idxName, docType, id,
				candidateSetSize, tdbPath, comboScorer, esScorer);
		esScorer.close();
		return list;
	}

	private void prepareDocStore(List<FullyExpandedDoc> expandedCandidates) {
		dbpHandler = DBPediaHandler.getInstance(Paths.TDB_DBPEDIA);
		hierHandler = HierarchyHandler.getInstance(Paths.TDB_DBPEDIA_HIERARCHY);
		this.docStore = new SimpleDocumentIndex();
		if (expandedCandidates == null) {
			// expand all docs and add to store for easier lookup
			JSONLoader jsonParser = new JSONLoader(
					Paths.PINCOMBE_ANNOTATED_JSON);
			List<AnnotatedDoc> docs = jsonParser.getAllDocs();
			long t1 = System.currentTimeMillis();
			for (AnnotatedDoc simpleDoc : docs) {
				FullyExpandedDoc expDoc = new FullyExpandedDoc(simpleDoc,
						EXPANSION_RADIUS, dbpHandler, hierHandler, null);
				this.docStore.addDocument(expDoc);
			}
			long t2 = System.currentTimeMillis();
			System.out.println("Average time expanding documents: " + (t2 - t1)
					/ 50.0);
		} else {
			for (FullyExpandedDoc doc : expandedCandidates) {
				this.docStore.addDocument(doc);
			}
		}

	}

	private SimpleDocumentIndex docStore;
	private HierarchyHandler hierHandler;
	private DBPediaHandler dbpHandler;
	private int timeAllIter = 0;
	private long timeSpentOnDocRetrieval = 0;
	private long docsRetrieved = 0;
	private long candSetGenTime = 0;
	private long pairwiseScoreTime = 0;

	public String computeAllRankingsScore(String idxName, String docType,
			int candidateSetSize) throws NumberFormatException, IOException {

		// run full search process with current candidate set size
		double fullScores = 0;
		double preScores = 0;
		// init scorer
		FullyExpandedDocScorer comboScorer = new FullyExpandedDocScorer(
				Paths.COMBINATION_RANKING_SCORES);

		ElasticSearchScorer esScorer = new ElasticSearchScorer(
				Paths.ES_RANKING_SCORES, idxName, docType, candidateSetSize);
		try {
			for (int i = 0; i < 50; i++) {
				String id = Integer.toString(i);
				String scoreString = this.getRelatedContentAndEvaluate(idxName,
						docType, id, candidateSetSize, LEE_TDB, comboScorer,
						esScorer);
				// System.out.println("Docid " + id + "," + score);
				String[] score = scoreString.split(",");
				preScores += Double.parseDouble(score[0]);
				fullScores += Double.parseDouble(score[1]);
			}
			// System.out.println("Total time: " + timeAllIter);
			System.out.println("Per document: " + (timeAllIter / 50.0));
			// System.out.println("Total time doc retrieval: "
			// + timeSpentOnDocRetrieval);
			System.out.println("Average time cand set gen: " + candSetGenTime
					/ 50.0);
			System.out.println("Average time doc retrieval: "
					+ timeSpentOnDocRetrieval / 50.0);
			System.out.println("Average time pairwise scoring: "
					+ pairwiseScoreTime / 50.0);
		} finally {
			esScorer.close();
		}

		return (preScores / 50.0) + "," + (fullScores / 50.0);
	}

	private String getRelatedContentAndEvaluate(String idxName, String docType,
			String id, int candidateSetSize, String tdbPath,
			FullyExpandedDocScorer comboScorer, ElasticSearchScorer esScorer)
			throws NumberFormatException, IOException {

		// get machine ranking
		List<FullyExpandedDoc> machineRanking = this.getRelatedContent(idxName,
				docType, id, candidateSetSize, tdbPath, comboScorer, esScorer);
		// get pre-search ranking
		List<AnnotatedDoc> preRanking = esScorer.getRelatedDocuments(id);
		List<FullyExpandedDoc> comparablePreRanking = new ArrayList<FullyExpandedDoc>();
		for (AnnotatedDoc doc : preRanking) {
			comparablePreRanking.add((FullyExpandedDoc) docStore
					.getDocument(doc.getId()));
		}
		// get human ranking
		PinCombeEvalHandler evalHandler = PinCombeEvalHandler
				.getInstance(Paths.PINCOMBE_EVAL);
		List<FullyExpandedDoc> humRanking = new LinkedList<FullyExpandedDoc>();
		List<Entry<Integer, Double>> rankingEntries = evalHandler.getRanking(
				Integer.parseInt(id), 0.0);
		for (Entry<Integer, Double> entry : rankingEntries) {

			FullyExpandedDoc doc = docStore.getDocument(entry.getKey());
			doc.setScore(entry.getValue());
			humRanking.add(doc);
		}
		// compute score
		NDCGEvaluator<FullyExpandedDoc> evaler = new NDCGEvaluator<FullyExpandedDoc>(
				3.0);
		double rankingScore = evaler.evaluateRanking(humRanking,
				machineRanking, 2);
		double preRankingScore = evaler.evaluateRanking(humRanking,
				comparablePreRanking, 2);
		// print pre-score, full score
		return preRankingScore + "," + rankingScore;
	}

	public void loadLeePincombeCorpusIntoES() {

		dbpHandler = DBPediaHandler.getInstance(Paths.TDB_DBPEDIA);
		hierHandler = HierarchyHandler.getInstance(Paths.TDB_DBPEDIA_HIERARCHY);
		JSONLoader leePincombeLoader = new JSONLoader(
				Paths.PINCOMBE_ANNOTATED_JSON);
		ESImporter importer = ESImporter.getInstance("pincombenew", "news");
		try {
			for (int i = 0; i < leePincombeLoader.getDocCount(); i++) {

				// load and expand doc
				FullyExpandedDoc doc = new FullyExpandedDoc(
						leePincombeLoader.getDoc(Integer.toString(i)),
						EXPANSION_RADIUS, dbpHandler, hierHandler, null);
				System.out.println("Doc " + i + " loaded");
				// import into ES index
				importer.addExpandedDocument(doc);
			}
		} finally {
			importer.shutdown();
		}
	}

	public void loadDocuments(int offset, int count, int expRadius,
			String idxName, String docType) {

		XLimeQuadStoreHelper quadHelper = XLimeQuadStoreHelper
				.getInstance(Paths.XLIME_NEWS_PATH);
		dbpHandler = DBPediaHandler.getInstance(Paths.TDB_DBPEDIA);
		hierHandler = HierarchyHandler.getInstance(Paths.TDB_DBPEDIA_HIERARCHY);
		ESImporter esHandler = ESImporter.getInstance(idxName, docType);
		esHandler.setBulkSize(20);
		TDBHandler docsTDB = TDBHandler.getInstance(Paths.EXP_DOCS_TDB_R
				+ expRadius);
		try {
			System.setErr(new PrintStream(new BufferedOutputStream(
					new FileOutputStream("sysout.txt"))));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		try {
			// get document ids
			List<String> docIds = quadHelper.getBulkDocs(offset, count);
			for (String id : docIds) {
				// create expanded doc
				AnnotatedDoc plainDoc = quadHelper.getDocument(id);
				// expand
				FullyExpandedDoc expDoc = new FullyExpandedDoc(plainDoc,
						expRadius, dbpHandler, hierHandler, null);
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

	public void loadLeeDocuments(int expRadius) {

		JSONLoader jsonParser = new JSONLoader(Paths.PINCOMBE_ANNOTATED_JSON);
		dbpHandler = DBPediaHandler.getInstance(Paths.TDB_DBPEDIA);
		hierHandler = HierarchyHandler.getInstance(Paths.TDB_DBPEDIA_HIERARCHY);
		TDBHandler docsTDB = TDBHandler.getInstance(LEE_TDB);
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
				FullyExpandedDoc expDoc = new FullyExpandedDoc(plainDoc,
						expRadius, dbpHandler, hierHandler, null);

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
	 */
	public List<RankableDoc> getRelatedContent(AnnotatedDoc newDoc) {

		return null;
	}

	/**
	 * Document gets pulled from corpus. Most similar documents are returned.
	 *
	 * @param id
	 * @param tdbPath
	 * @param comboScorer
	 * @param esScorer
	 * @return
	 * @throws FileNotFoundException
	 */
	public List<FullyExpandedDoc> getRelatedContent(String idxName,
			String docType, String id, int candidateSetSize, String tdbPath,
			FullyExpandedDocScorer comboScorer, ElasticSearchScorer esScorer)
			throws FileNotFoundException {

		// get candidate set by running ES search
		int timeOneIter = 0;
		List<AnnotatedDoc> candidateSet = null;
		long t1 = System.currentTimeMillis();
		candidateSet = esScorer.getRelatedDocuments(id);
		long t2 = System.currentTimeMillis();
		// System.out.println("Generate candidate set: " + (t2 - t1));
		timeOneIter += (t2 - t1);
		candSetGenTime += (t2 - t1);
		// System.out.println("Candidate set generated.");
		List<FullyExpandedDoc> finalResults = null;
		List<FullyExpandedDoc> expandedCandidates = new LinkedList<FullyExpandedDoc>();

		// for all candidates, get expanded versions
		if (candidateSet != null) {
			// if (candidateSet.size() < candidateSetSize)
			// System.out.println("returned only " + candidateSet.size()
			// + " instead of " + candidateSetSize);
			// if there is a Triplestore with expanded documents, use it
			if (tdbPath != null) {
				TDBHandler docsTDB = TDBHandler.getInstance(tdbPath);
				long t1b = System.currentTimeMillis();
				for (AnnotatedDoc cand : candidateSet) {

					expandedCandidates.add(getDocumentFromTDB(docsTDB,
							cand.getId()));
				}
				// add expanded query document
				expandedCandidates.add(getDocumentFromTDB(docsTDB, id));
				long t2b = System.currentTimeMillis();
				// System.out.println("Retrieve expanded candidate documents: "
				// + (t2b - t1b));
				timeOneIter += (t2b - t1b);
				timeSpentOnDocRetrieval += t2b - t1b;
				docsRetrieved += expandedCandidates.size();
			} else if (this.docStore != null) {
				// if no triplestore was given, check for this class' doc store.
				for (AnnotatedDoc cand : candidateSet) {
					expandedCandidates.add((FullyExpandedDoc) this.docStore
							.getDocument(cand.getId()));
				}
				// add query doc
				expandedCandidates.add((FullyExpandedDoc) this.docStore
						.getDocument(id));
			} else {
				// expand docs
				System.out
						.println("No triplestore available for expanded doc retrieval. "
								+ "Docs will be expanded.");
				dbpHandler = DBPediaHandler.getInstance(Paths.TDB_DBPEDIA);
				hierHandler = HierarchyHandler
						.getInstance(Paths.TDB_DBPEDIA_HIERARCHY);
				for (AnnotatedDoc cand : candidateSet) {

					FullyExpandedDoc expDoc = new FullyExpandedDoc(cand,
							EXPANSION_RADIUS, dbpHandler, hierHandler, null);
					expandedCandidates.add(expDoc);
				}
				// expand query doc and add
				FullyExpandedDoc expQueryDoc = new FullyExpandedDoc(
						this.getDocumentFromES(idxName, docType, id),
						EXPANSION_RADIUS, dbpHandler, hierHandler, null);
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
			long t3 = System.currentTimeMillis();
			finalResults = comboScorer.getRelatedDocuments(id);
			long t4 = System.currentTimeMillis();
			// System.out.println("Compute pairwise sims: " + (t4 - t3));
			timeOneIter += (t4 - t3);
			pairwiseScoreTime += (t4 - t3);
			// System.out.println("Ranking produced.");
			outputRankings(candidateSet, finalResults, OUTPUTPATH);
		} else
			System.out.println("no candidates...?");
		timeAllIter += timeOneIter;
		return finalResults;
	}

	private void expandAndAnalyzeDocuments(
			List<FullyExpandedDoc> expandedCandidates)
			throws FileNotFoundException {

		dbpHandler = DBPediaHandler.getInstance(Paths.TDB_DBPEDIA);
		hierHandler = HierarchyHandler.getInstance(Paths.TDB_DBPEDIA_HIERARCHY);
		int timeSum = 0;
		int annotSum = 0;
		PrintStream bla = System.out;
		File file = new File("transStats.txt");
		FileOutputStream fos = new FileOutputStream(file);
		PrintStream ps = new PrintStream(fos);
		System.setOut(ps);
		StringBuilder sb = new StringBuilder();
		for (FullyExpandedDoc fullCand : expandedCandidates) {
			QuadDoc cand = new QuadDoc(fullCand.getId(), fullCand.getText(),
					fullCand.getTitle(), fullCand.getAnnotations());
			long t1 = System.currentTimeMillis();
			FullyExpandedDoc doc = new FullyExpandedDoc(cand, EXPANSION_RADIUS,
					dbpHandler, hierHandler, null);
			long t2 = System.currentTimeMillis();
			int annotCount = doc.getAnnotations().size();
			long time = t2 - t1;
			sb.append(annotCount + "," + time + "\n");
			timeSum += time;
			annotSum += annotCount;
		}
		System.setOut(bla);
		// System.out.println(sb.toString());
		// System.out.println("Total time: " + timeSum + ", total annots: "
		// + annotSum);
	}

	private FullyExpandedDoc getDocumentFromTDB(TDBHandler docsTDB, String id) {

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
			List<FullyExpandedDoc> finalResults, String outputpath) {

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
			for (FullyExpandedDoc doc : finalResults) {

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
}
