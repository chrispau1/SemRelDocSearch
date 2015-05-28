package de.paul.kb.dbpedia;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.tdb.TDBFactory;

public class PageRankHandler {

	private static final String HAS_RANK1 = "http://purl.org/voc/vrank#hasRank";
	private static final String HAS_RANK2 = "http://purl.org/voc/vrank#rankValue";

	// public static void main(String[] args) throws IOException {
	// PageRankHandler handler = new PageRankHandler();
	//
	// }

	private Dataset ds;

	public PageRankHandler() {
		// create the dataset
		this.ds = TDBFactory.createDataset("C:/dbpedia_pagerank/tdb");
	}

	public double getPageRank(String entity) {

		double pageRank = -1;
		String queryString = "select ?pr where { <" + entity + "> <"
				+ HAS_RANK1 + ">/<" + HAS_RANK2 + "> + ?pr}";
		/*
		 * Run query
		 */
		Query query = QueryFactory.create(queryString);
		QueryExecution qexec = QueryExecutionFactory.create(query, ds);
		try {
			ResultSet results = qexec.execSelect();
			if (results != null) {
				QuerySolution soln = null;
				if (results.hasNext()) {
					soln = results.next();
					if (soln != null) {
						pageRank = soln.get("pr").asLiteral().getFloat();
					}
				}
			}
		} finally {
			qexec.close();
		}
		return pageRank;
	}
}
