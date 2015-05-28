package de.paul.corpus.evaluation;

import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;

import org.json.JSONException;
import org.json.JSONObject;

import de.paul.util.Paths;

public class LeeEvalHandler extends RankingEvalHandler {

	private static final int DOCCOUNT = 50;
	private static LeeEvalHandler instance;

	private LeeEvalHandler(String string) {

		this.path = string;
	}

	public static LeeEvalHandler getInstance(String path) {

		if (instance == null)
			instance = new LeeEvalHandler(path);
		return instance;
	}

	public static void main(String[] args) throws IOException {

		LeeEvalHandler p = new LeeEvalHandler(Paths.LEE_EVAL_DATA);
		p.loadRows();
		p.sortAndWriteRankings("text_output_data/TopSimDocs.json");
	}

	public DocSim[] getDocSims() {
		return this.docSims;
	}

	// TODO: doesn't write atm!
	public void sortAndWriteRankings(String outPath) throws JSONException,
			IOException {

		// init top JSON array
		// JSONArray topArr = new JSONArray();
		for (int i = 0; i < docSims.length; i++) {

			// init json object for document
			JSONObject docObj = new JSONObject();
			// assign id
			docObj.put("id", i);

			// init JSON array
			// JSONArray arr = new JSONArray();
			// //get the top entries for this document
			// List<Entry<Integer, Double>> entries = docSim.getTopEntries(2);
			// for (Entry<Integer, Double> e:entries) {
			// JSONObject obj = new JSONObject();
			// obj.put("id", e.getKey().toString());
			// obj.put("score", e.getValue());
			// arr.put(obj);
			// }
			// docObj.put("simDocs", arr);
			// topArr.put(docObj);
		}
		// FileWriter fw = null;
		// try {
		// fw = new FileWriter(outPath);
		// fw.write(topArr.toString(2));
		// } finally {
		// fw.close();
		// }
	}

	public Double getSimilarity(int doc1, int doc2) throws IOException {

		if (docSims == null) {
			this.loadRows();
		}
		return docSims[doc1].getSimilarityScore(doc2);
	}

	public List<Entry<Integer, Double>> getRanking(int docId, double cutOff)
			throws IOException {

		if (docSims == null) {
			this.loadRows();
		}
		List<Entry<Integer, Double>> humRanking = docSims[docId]
				.getTopEntries(cutOff);
		// for (Entry<Integer, Double> entry : humRanking) {
		// x doc = (x) callingObject.createScoredDoc(
		// Integer.toString(entry.getKey()), entry.getValue());
		// res.add(doc);
		// }
		return humRanking;
	}

	protected int getDocCount() {

		return DOCCOUNT;
	}

	@Override
	protected int adjustID(int i) {
		return i - 1;
	}
}
