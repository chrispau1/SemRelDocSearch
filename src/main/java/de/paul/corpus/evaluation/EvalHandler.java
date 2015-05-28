package de.paul.corpus.evaluation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public abstract class EvalHandler {

	protected String path;
	protected DocSim[] docSims = null;

	public void loadRows() throws IOException {

		docSims = new DocSim[getDocCount()];
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(path));
			String line;
			// skip first line (col names)
			br.readLine();
			while ((line = br.readLine()) != null) {

				String[] row = line.split(",");
				int doc1 = Integer.parseInt(row[1]);
				int doc2 = Integer.parseInt(row[2]);
				double sim = Double.parseDouble(row[3]);
				int doc1Adj = adjustID(doc1);
				int doc2Adj = adjustID(doc2);
				if (docSims[doc1Adj] == null)
					docSims[doc1Adj] = new DocSim(doc1Adj);
				if (docSims[doc2Adj] == null)
					docSims[doc2Adj] = new DocSim(doc2Adj);
				docSims[doc1Adj].addSim(doc2Adj, sim);
				docSims[doc2Adj].addSim(doc1Adj, sim);
			}
		} finally {
			br.close();
		}
		// compute average and sort descendingly!
		for (int i = 0; i < docSims.length; i++) {

			DocSim docSim = docSims[i];
			if (docSim != null) {
				docSim.computeAvg();
				docSim.sort();
			}
		}
	}

	protected abstract int adjustID(int i);

	protected abstract int getDocCount();

	public abstract Double getSimilarity(int doc1, int doc2) throws IOException;

}
