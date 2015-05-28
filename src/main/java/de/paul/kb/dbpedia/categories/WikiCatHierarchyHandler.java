package de.paul.kb.dbpedia.categories;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.tdb.TDBFactory;

import de.paul.annotations.Annotatable;
import de.paul.annotations.Category;
import de.paul.annotations.DepthAnnotation;
import de.paul.annotations.TopicAnnotation;
import de.paul.kb.HierarchicalKnowledgeBase;

public class WikiCatHierarchyHandler implements HierarchicalKnowledgeBase {

	private static Map<String, WikiCatHierarchyHandler> instances = new HashMap<String, WikiCatHierarchyHandler>();
	private Dataset ds;

	private WikiCatHierarchyHandler(String path) {

		// create the dataset
		this.ds = TDBFactory.createDataset(path);
	}

	/**
	 * Get ancestors for all categories in Array.
	 * 
	 * @param categories
	 * @return
	 */
	public Set<Category> getAncestors(Annotatable ann, Set<String> categories) {

		return getAncestors(ann, categories, true);
	}

	/**
	 * Get ancestors for all categories in Array.
	 * 
	 * @param categories
	 * @param collectTopicStats
	 * @return
	 */
	public Set<Category> getAncestors(Annotatable ann, Set<String> categories,
			boolean collectTopicStats) {

		Set<Category> ancestors = new HashSet<Category>();
		if (categories == null || categories.size() == 0)
			return ancestors;
		/*
		 * find maximum depth category for entity and set this as entity's depth
		 */
		int maxCatDepth = getMaxCategoryDepth(categories);
		/*
		 * Run main ancestor query
		 */
		String queryString = buildAncestorQuery(categories);
		Query query = QueryFactory.create(queryString);
		QueryExecution qexec = QueryExecutionFactory.create(query, ds);
		try {
			ResultSet results = qexec.execSelect();
			if (results != null) {
				QuerySolution soln = null;
				String cat = null;
				int depth = -1;
				while (results.hasNext()) {

					soln = results.next();
					if (soln.get("ancestor") != null)
						cat = soln.get("ancestor").asNode().getURI();
					if (soln.get("depth") != null)
						depth = soln.get("depth").asLiteral().getInt();

					if (cat != null && depth != -1) {
						Category category = new Category(cat, depth,
								new DepthAnnotation(ann.getEntity(),
										ann.getWeight(), maxCatDepth));
						ancestors.add(category);
						/*
						 * Register topic memberships of entity
						 */
						if (collectTopicStats) {
							if (depth == TopicAnnotation.TOPIC_DEPTH) {
								TopicAnnotation.updateGlobalStats(cat);
							}
						}
					}
				}
			} else
				System.out.println("No results found. Query: " + queryString);
		} finally {
			qexec.close();
		}
		return ancestors;
	}

	private int getMaxCategoryDepth(Set<String> categories) {

		int maxDepth = -1;
		String queryString = "SELECT (MAX(?depth) AS ?maxdepth) WHERE { {";
		int ctr = 0;
		for (String cat : categories) {

			// lowercase it etc
			cat = formatCategories(cat);
			// include depth of category aka direct parent of node of interest
			queryString += tripleString(cat,
					WikiCategoryHierarchyConverter.DEPTH, "?depth");
			if (ctr < categories.size() - 1)
				queryString += "} UNION {";
			ctr++;
		}
		queryString += "}}";
		/*
		 * Run query
		 */
		Query query = QueryFactory.create(queryString);
		QueryExecution qexec = QueryExecutionFactory.create(query, ds);
		try {
			ResultSet results = qexec.execSelect();
			if (results != null) {
				QuerySolution soln = null;
				soln = results.next();
				if (soln.get("maxdepth") != null)
					maxDepth = soln.get("maxdepth").asLiteral().getInt();
			} else
				System.out.println("No results found. Query: " + queryString);
		} finally {
			qexec.close();
		}
		return maxDepth;
	}

	/**
	 * Get the depth of a category in the Wikipedia Category Hierarchy
	 * 
	 * @param category
	 * @return
	 */
	public int getDepth(String category) {

		category = formatCategories(category);
		int depth = -1;
		String queryString = "SELECT ?depth WHERE {"
				+ tripleString(category, WikiCategoryHierarchyConverter.DEPTH,
						"?depth") + "}";
		Query query = QueryFactory.create(queryString);
		QueryExecution qexec = QueryExecutionFactory.create(query, ds);
		try {
			ResultSet results = qexec.execSelect();
			if (results != null) {
				QuerySolution soln = null;
				while (results.hasNext()) {
					soln = results.next();
					depth = soln.get("depth").asLiteral().getInt();
					break;
				}
			}
		} finally {
			qexec.close();
		}
		return depth;
	}

	/*
	 * Builds SPARQL query string
	 */
	private String buildAncestorQuery(Set<String> categories) {
		/*
		 * Build query as UNION of category clauses second brace because of
		 * union'ed queries
		 */
		String queryString = "SELECT ?ancestor ?depth WHERE {{";
		int ctr = 0;
		for (String cat : categories) {

			// lowercase it etc
			cat = formatCategories(cat);
			/*
			 * create nested select queries for ancestors to help SPARQL engine
			 * build efficient queries
			 */
			queryString += "{SELECT ?ancestor WHERE {"
					+ tripleString(cat,
							WikiCategoryHierarchyConverter.ANCESTOR,
							"?ancestor") + "}" + "}";
			// union with other categories
			if (ctr < categories.size() - 1)
				queryString += "} UNION {";
			ctr++;
		}
		// add point after last UNION statement to complete it.
		queryString += "}."
		// for efficient query processing, add depth query at the end.
				+ tripleString("?ancestor",
						WikiCategoryHierarchyConverter.DEPTH, "?depth")
				// round up query.
				+ "}";
		return queryString;
	}

	/*
	 * Turns s p o into a triple string representation. Adds < > if necessary,
	 * white spaces and dot at the end.
	 */
	private String tripleString(String subj, String pred, String obj) {

		if (!subj.startsWith("?") && !subj.startsWith("<"))
			subj = "<" + subj + ">";
		if (!pred.startsWith("?") && !pred.startsWith("<"))
			pred = "<" + pred + ">";
		if (!obj.startsWith("?") && !obj.startsWith("<"))
			obj = "<" + pred + ">";
		return subj + " " + pred + " " + obj + ".";
	}

	/*
	 * lowercase input categories for querying
	 */
	private String formatCategories(String cat) {

		return cat.toLowerCase();
	}

	public void close() {

		this.ds.close();
	}

	public static WikiCatHierarchyHandler getInstance(String path) {

		if (instances.get(path) == null) {
			instances.put(path, new WikiCatHierarchyHandler(path));
		}
		return instances.get(path);
	}

}
