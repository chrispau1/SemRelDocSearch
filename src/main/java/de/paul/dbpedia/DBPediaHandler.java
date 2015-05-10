package de.paul.dbpedia;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.tdb.TDBFactory;

import de.paul.annotations.AncestorAnnotation;
import de.paul.annotations.Annotatable;
import de.paul.annotations.FullyExpandedAnnotation;
import de.paul.annotations.NeighborhoodAnnotation;
import de.paul.annotations.NeighborhoodAnnotation.OverlapWeightsMode;
import de.paul.annotations.WeightedAnnotation;
import de.paul.dbpedia.categories.HierarchyHandler;
import de.paul.dbpedia.expansion.StopURIHandler;
import de.paul.pairwiseSimilarity.entityPairScorers.CombinedEntityPairScorer;
import de.paul.pairwiseSimilarity.entityPairScorers.CombinedEntityPairScorer.CombineMode;
import de.paul.util.Paths;
import de.paul.util.SumMap;

/**
 * Provides methods to access DBPedia entity information regarding Wikipedia
 * Category affiliation and entity neighborhood.
 * 
 * @author Chris
 *
 */
public class DBPediaHandler {

	// predicate that links entities to their category in DBPedia
	private static final String DCTERMS_SUBJECT = "<http://purl.org/dc/terms/subject>";
	// redirect predicate
	private static final String WIKI_REDIRECT = "<http://dbpedia.org/ontology/wikiPageRedirects>";
	protected static final double BETA = 0.5;
	private static final int PAGERANK_MAX = 50;
	// used to distinguish between hierarchical predicates and transversal ones
	// within DBPedia
	protected final HashSet<String> HIERARCH_PREDS = new HashSet<String>(
			Arrays.asList("http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
					"http://purl.org/dc/terms/subject",
					"http://www.w3.org/2004/02/skos/core#broader",
					"http://www.w3.org/2000/01/rdf-schema#subClassOf"));

	public static void main(String[] args) {

		DBPediaHandler handler = new DBPediaHandler(Paths.TDB_DBPEDIA);
		HierarchyHandler hierHandler = HierarchyHandler
				.getInstance(Paths.TDB_DBPEDIA_HIERARCHY);
		String entity = "http://dbpedia.org/resource/Dirk_Nowitzki";
		/*
		 * Similarity
		 */
		String entity2 = "http://dbpedia.org/resource/Gregg_Popovich";
		NeighborhoodAnnotation e1a = new NeighborhoodAnnotation(entity, 1.0,
				handler, 2);
		NeighborhoodAnnotation e2a = new NeighborhoodAnnotation(entity2, 1.0,
				handler, 2);
		Set<Annotatable> res = e1a.overlap(e2a, OverlapWeightsMode.MULT);
		for (Annotatable ann2 : res) {
			System.out.println(ann2.getEntity());
		}
		// KatzEntityPairSubgraph entPairScorer = new
		// KatzEntityPairSubgraph(e1,
		// e2);
		AncestorAnnotation e1b = new AncestorAnnotation(entity, 1.0, handler,
				hierHandler);
		AncestorAnnotation e2b = new AncestorAnnotation(entity2, 1.0, handler,
				hierHandler);
		// System.out.println(e1b.ancestorOverlap(e2b));
		System.out.println(e1b.lowestCommonAncestor(e2b));

		FullyExpandedAnnotation e1 = null;
		FullyExpandedAnnotation e2 = null;
		try {
			e1 = new FullyExpandedAnnotation(e1b, e1a, CombineMode.PLUS);
			e2 = new FullyExpandedAnnotation(e2b, e2a, CombineMode.PLUS);
		} catch (Exception e) {
			e.printStackTrace();
		}
		CombinedEntityPairScorer entPairScorer = new CombinedEntityPairScorer(
				e1, e2, CombineMode.PLUS);
		entPairScorer.setLeft();
		System.out.println(entPairScorer.score());
		entPairScorer.setRight();
		System.out.println(entPairScorer.score());
		/*
		 * Neighbors
		 */
		// Set<Annotatable> oneStep = handler
		// .getNeighborsOutEdges(Arrays
		// .asList(new Annotatable[] { new WeightedAnnotation(
		// entity2, 1.0) }));
		// Set<Annotatable> oneStep = handler
		// .getNeighborsOutEdges(Arrays
		// .asList(new Annotatable[] {
		// new WeightedAnnotation(
		// "http://dbpedia.org/resource/San_Antonio_Spurs",
		// 1.0),
		// new WeightedAnnotation(
		// "http://dbpedia.org/resource/National_Basketball_Association",
		// 1.0) }));
		// for (Annotatable ann : oneStep) {
		// System.out.println(entity2 + " -> " + ann.getEntity());
		// Set<Annotatable> twoStep = handler.getNeighborsOutEdges(Arrays
		// .asList(new Annotatable[] { ann }));
		// for (Annotatable ann2 : twoStep)
		// System.out.println(ann.getEntity() + " -> " + ann2.getEntity());
		// }
		/*
		 * Ancestors
		 */
		// Set<String> cats = new HashSet<String>(
		// Arrays.asList(new String[] {
		// "http://dbpedia.org/resource/Category:Dallas_Mavericks_players"
		// }));// handler.getCategories(entity);
		// Set<String> cats2 = new HashSet<String>(
		// Arrays.asList(new String[] {
		// "http://dbpedia.org/resource/Category:Sports_in_the_United_States_by_city"
		// }));// handler.getCategories(entity);
		// for (String cat : cats)
		// System.out.println(cat + " -> " + entity);
		// handler.getCategories(entity2);
		// for (String cat : cats2)
		// System.out.println(cat + " -> " + entity2);
		// HashSet<String> copyCats = new HashSet<String>(cats);
		// copyCats.retainAll(cats2);
		// System.out.println("Overlap:");
		// for (String cat : copyCats)
		// System.out.println(cat + " -> " + entity);
		// Set<Category> ancs = HierarchyHandler.getInstance(
		// Paths.TDB_DBPEDIA_HIERARCHY).getAncestors(
		// new WeightedAnnotation(entity, 1.0), cats, false);
		// Set<Category> ancs2 = HierarchyHandler.getInstance(
		// Paths.TDB_DBPEDIA_HIERARCHY).getAncestors(
		// new WeightedAnnotation(entity2, 1.0), cats2, false);
		// ancs2.retainAll(ancs);
		// for (Category anc : ancs2) {
		// System.out.println(anc.getName() + " -> " + anc.getEntityName()
		// + "  (" + anc.getDepth() + ")");
		// }
	}

	public Set<String> getCategories(String entity) {

		Set<String> cats = new HashSet<String>();
		// resolve redirects
		entity = redirectsTo(entity);
		// build query
		String queryString = "SELECT ?cat WHERE {" + "<" + entity + "> "
				+ DCTERMS_SUBJECT + " ?cat ." + "}";
		/*
		 * Run query
		 */
		Query query = QueryFactory.create(queryString);
		QueryExecution qexec = QueryExecutionFactory.create(query, ds);
		try {
			ResultSet results = qexec.execSelect();
			if (results != null) {
				QuerySolution soln = null;
				while (results.hasNext()) {
					soln = results.next();
					cats.add(soln.get("cat").asNode().getURI());
				}
			}
		} finally {
			qexec.close();
		}
		return cats;
	}

	public String redirectsTo(String entity) {
		// remove "'" if occurs
		entity = entity.replaceAll("\'", "%27");
		// build redirect query
		String queryString = "SELECT ?redir WHERE {" + "<" + entity + "> "
				+ WIKI_REDIRECT + " ?redir ." + "}";
		/*
		 * Run query
		 */
		Query query = QueryFactory.create(queryString);
		QueryExecution qexec = QueryExecutionFactory.create(query, ds);
		String redir = entity;
		try {
			ResultSet results = qexec.execSelect();
			if (results != null) {
				QuerySolution soln = null;
				if (results.hasNext()) {
					soln = results.next();
					redir = soln.get("redir").asNode().getURI();
				}
			}
		} finally {
			qexec.close();
		}
		return redir;
	}

	/**
	 * Gets traversal neighbors of given entities using both in- and out-going
	 * edges.
	 * 
	 * @param entities
	 * @return
	 */
	public Set<Annotatable> getBirectionalTransversalNeighbors(
			Collection<? extends Annotatable> entities) {

		/*
		 * Build and run query
		 */
		String queryString = buildTransversalQuery(entities, true);
		return getTransversalNeighbors(entities, queryString);
	}

	/*
	 * Helper method that just puts entities for fast lookup into a map.
	 */
	private HashMap<String, Annotatable> constructQueryEntityMap(
			Collection<? extends Annotatable> entities) {
		HashMap<String, Annotatable> queryEntityMap = new HashMap<String, Annotatable>();
		for (Annotatable ent : entities) {
			// resolve redirects
			String queryEntName = ent.getEntity();
			// set as entity name
			ent.setEntity(queryEntName);
			// check if copy is needed here
			queryEntityMap.put(queryEntName, ent);
		}
		return queryEntityMap;
	}

	/*
	 * Performs the real expansion work.
	 * 
	 * Collects entities that are one step away via transversal semantic links
	 * from the query entities.
	 * 
	 * Assigns weights to the newly discovered entities by combining entity
	 * weights of those query entities that a new one is reachable from in one
	 * hop. Discounts weights to account for distance from the original
	 * annotation set.
	 */
	private Set<Annotatable> getTransversalNeighbors(
			Collection<? extends Annotatable> entities, String queryString) {

		// to later assign weights for newly found entities more easily, put
		// query entities into map
		HashMap<String, Annotatable> queryEntityMap = constructQueryEntityMap(entities);
		SumMap<String> newEntityMap = new SumMap<String>();
		/*
		 * Build and run query
		 */
		Query query = QueryFactory.create(queryString);
		QueryExecution qexec = QueryExecutionFactory.create(query, ds);
		try {
			ResultSet results = qexec.execSelect();
			if (results != null) {
				QuerySolution soln = null;
				// predicate type might be used eventually for weighting
				// purposes
				String subj = "", obj = "", pred = "";
				double weight = -1;
				while (results.hasNext()) {

					soln = results.next();
					// check if null. Shouldn't happen but did occasionally
					if (soln.get("s") != null && soln.get("p") != null
							&& soln.get("o") != null) {
						subj = soln.get("s").asLiteral().getString();
						obj = soln.get("o").asNode().getURI();
						pred = soln.get("p").asNode().getURI();
						// filter out hierarchical results
						if (!HIERARCH_PREDS.contains(pred)
								&& !blacklistedEnts.contains(subj)
								&& !blacklistedEnts.contains(obj)) {
							// get the respective entity's weight
							if (queryEntityMap.get(subj) == null)
								System.out.println("BAL");
							weight = queryEntityMap.get(subj).getWeight();
							// add weight to new entity based on associated
							// query
							// entity weight
							newEntityMap.inc(obj,
									neighboringEntityWeight(weight));
						}
					}
				}
			} else
				System.out.println("No results found. Query: " + queryString);
		} finally {
			qexec.close();
		}
		// finish return value
		Set<Annotatable> newEntities = new HashSet<Annotatable>();
		for (Entry<String, Double> entry : newEntityMap.entrySet()) {
			newEntities.add(new WeightedAnnotation(entry.getKey(), entry
					.getValue()));
		}
		return newEntities;
	}

	/**
	 * Expands the set along outgoing edges.
	 * 
	 * @param outEntities
	 * @return
	 */
	public Set<Annotatable> getNeighborsOutEdges(
			Collection<? extends Annotatable> outEntities) {

		Set<Annotatable> redirectedEnts = getRedirectedEntities(outEntities);
		Set<Annotatable> outGoing = getTransversalNeighbors(redirectedEnts,
				buildOutEdgesTransversalQuery(redirectedEnts));
		return outGoing;
	}

	/**
	 * Expands the set reversely along incoming edges.
	 * 
	 * @param inEntities
	 * @return
	 */
	public Set<Annotatable> getNeighborsInEdges(
			Collection<? extends Annotatable> inEntities) {

		Set<Annotatable> redirectedInEntities = getRedirectedEntities(inEntities);
		Set<Annotatable> filteredEnts = filterExpandableEntities(
				redirectedInEntities, PAGERANK_MAX);
		Set<Annotatable> inComing = getTransversalNeighbors(filteredEnts,
				buildInEdgesTransversalQuery(filteredEnts));
		return inComing;
	}

	private Set<Annotatable> getRedirectedEntities(
			Collection<? extends Annotatable> entities) {

		Set<Annotatable> redirectedSet = new HashSet<Annotatable>();
		for (Annotatable ann : entities) {

			String entName = ann.getEntity();
			Annotatable annCopy = ann.copy();
			annCopy.setEntity(redirectsTo(entName));
			redirectedSet.add(annCopy);
		}
		return redirectedSet;
	}

	/*
	 * Specifies formula for calculation of contribution of a query entity with
	 * given weight to the weight of a new entity linked to that query entity.
	 */
	private double neighboringEntityWeight(double weight) {
		return BETA * weight;
	}

	/*
	 * expand off of the given map
	 */
	private String buildTransversalQuery(
			Collection<? extends Annotatable> entities, boolean bidirectional) {

		// outer query. Needed to collect all UNION results
		String fullQuery = "SELECT * {{";
		fullQuery += outEdgesUnionQuery(entities);
		fullQuery += "}";
		/*
		 * add in-going stuff
		 */
		if (bidirectional) {
			// filter entities by pageRank to reduce complexity and noise
			Set<Annotatable> filteredEntities = filterExpandableEntities(
					entities, PAGERANK_MAX);
			// extend query
			fullQuery += " UNION {";
			fullQuery += inEdgesUnionQuery(filteredEntities);
			fullQuery += "}";
		}
		fullQuery += "}";
		return fullQuery;
	}

	private String buildOutEdgesTransversalQuery(
			Collection<? extends Annotatable> entities) {

		return "SELECT * WHERE {" + outEdgesUnionQuery(entities) + "}";
	}

	private String buildInEdgesTransversalQuery(
			Collection<? extends Annotatable> entities) {

		return "SELECT * WHERE {" + inEdgesUnionQuery(entities) + "}";
	}

	private String outEdgesUnionQuery(Collection<? extends Annotatable> entities) {
		String fullQuery = "";
		// iterate over URIs
		int ctr = 0;
		for (Annotatable ent : entities) {
			String s = ent.getEntity();
			String queryString = "{select (\"" + s + "\" AS ?s) ?p ?o where {";
			queryString += "<" + s + "> ?p ?o. ";
			queryString += "FILTER(!isLiteral(?o))";
			queryString += "}}";
			if (ctr < entities.size() - 1)
				queryString += " UNION ";
			fullQuery += queryString;
			ctr++;
		}
		return fullQuery;
	}

	private String inEdgesUnionQuery(Collection<? extends Annotatable> entities) {

		String fullQuery = "";
		int ctr;
		// iterate over URIs
		ctr = 0;
		for (Annotatable ent : entities) {
			String s = ent.getEntity();
			String queryString = "{select (\"" + s + "\" AS ?s) ?p ?o where {";
			queryString += "?o ?p <" + s + ">. ";
			queryString += "}}";
			if (ctr < entities.size() - 1)
				queryString += " UNION ";
			fullQuery += queryString;
			ctr++;
		}
		return fullQuery;
	}

	private Dataset ds;
	private HashSet<String> blacklistedEnts;
	private PageRankHandler pageRankHandler;
	private static Map<String, DBPediaHandler> instances = new HashMap<String, DBPediaHandler>();

	private DBPediaHandler(String path) {

		// create the dataset
		this.ds = TDBFactory.createDataset(path);
		// load unwanted DBPedia entities
		this.blacklistedEnts = StopURIHandler.getInstance()
				.loadBlackListedEntities("stopURIs.txt");
		// page rank stuff
		this.pageRankHandler = new PageRankHandler();
	}

	public static DBPediaHandler getInstance(String path) {

		if (instances.get(path) == null) {
			instances.put(path, new DBPediaHandler(path));
		}
		return instances.get(path);
	}

	public void close() {

		ds.close();
	}

	/**
	 * Calls transversal neighbors method, but first filters out those nodes
	 * with a pageRank that is higher than specified value.
	 * 
	 * @param set
	 * @param filterRankGreater
	 * @return
	 */
	public Set<Annotatable> filterExpandableEntities(
			Collection<? extends Annotatable> set, int filterRankGreater) {

		HashSet<Annotatable> resultSet = new HashSet<Annotatable>();
		// filter
		for (Annotatable elem : set) {
			String ent = elem.getEntity();
			double pageRank = pageRankHandler.getPageRank(ent);
			// safety:
			if (pageRank == -1)
				System.out.println("PageRank for entity " + ent
						+ " could not be found.");
			else if (pageRank <= filterRankGreater)
				resultSet.add(elem.copy());
		}
		return resultSet;
	}

}
