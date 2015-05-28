package de.paul.db.triplestore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.update.GraphStore;
import com.hp.hpl.jena.update.GraphStoreFactory;
import com.hp.hpl.jena.update.UpdateExecutionFactory;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateProcessor;
import com.hp.hpl.jena.update.UpdateRequest;

import de.paul.annotations.AncestorAnnotation;
import de.paul.annotations.Annotatable;
import de.paul.annotations.Category;
import de.paul.annotations.DepthAnnotation;
import de.paul.annotations.NeighborhoodAnnotation;
import de.paul.annotations.SemanticallyExpandedAnnotation;
import de.paul.annotations.WeightedAnnotation;
import de.paul.db.DocumentIndex;
import de.paul.documents.impl.SemanticallyExpandedDoc;
import de.paul.similarity.entityScorers.CombinedEntityPairScorer.CombineMode;
import de.paul.util.Paths;

public class TDBHandler implements DocumentIndex {

	private static final String HAS_ANNOTATION = "http://dbpExpandedDocs.org/has_Annotation";
	private static final String HAS_TITLE = "http://dbpExpandedDocs.org/has_Title";
	private static final String HAS_TEXT = "http://dbpExpandedDocs.org/has_Text";
	private static final String HAS_NEIGHBOR = "http://dbpExpandedDocs.org/has_Neighbors";
	private static final String HAS_ANCESTOR = "http://dbpExpandedDocs.org/has_Ancestors";
	private static final String HAS_WEIGHT = "http://dbpExpandedDocs.org/has_Weight";
	private static final String HAS_NAME = "http://dbpExpandedDocs.org/has_Name";
	private static final String HAS_DEPTH = "http://dbpExpandedDocs.org/has_Depth";
	private static final String HAS_ENTITY_DEPTH = "http://dbpExpandedDocs.org/has_Ent_Depth";
	private Dataset ds;
	private CombineMode combineMode = CombineMode.PLUS;
	private static Map<String, TDBHandler> insts = new HashMap<String, TDBHandler>();

	public static void main(String[] args) {

		// List<Annotatable> annots = new LinkedList<Annotatable>();
		// Annotatable ann1 = new WeightedAnnotation(
		// "http://dbpedia.org/resource/Kobe_Bryant", 1.0);
		// annots.add(ann1);
		// Annotatable ann2 = new WeightedAnnotation(
		// "http://dbpedia.org/resource/Karlsruhe", 0.5);
		// annots.add(ann2);
		// QuadDoc plainDoc = new QuadDoc("ExampleId", "Example text",
		// "Example title", annots);
		// FullyExpandedDoc doc = new FullyExpandedDoc(plainDoc, 2,
		// DBPediaHandler.getInstance(Paths.TDB_DBPEDIA),
		// HierarchyHandler.getInstance(Paths.TDB_DBPEDIA_HIERARCHY), null);
		TDBHandler h = TDBHandler.getInstance(Paths.EXP_DOCS_TDB_R2);
		// h.addExpandedDocument(doc);
		SemanticallyExpandedDoc res = h
				.getDocument("http://ijs.si/enryched/265215060");
		System.out.println(res.toString());
	}

	private TDBHandler(String storePath) {

		this.ds = TDBFactory.createDataset(storePath);
	}

	private TDBHandler(String storePath, CombineMode combineMode) {

		this.ds = TDBFactory.createDataset(storePath);
		this.combineMode = combineMode;
	}

	public static TDBHandler getInstance(String path) {

		if (insts.get(path) == null) {
			insts.put(path, new TDBHandler(path));
		}
		return insts.get(path);
	}

	public double getTopicScore(Set<String> topics) {

		throw new UnsupportedOperationException();
	}

	public SemanticallyExpandedDoc getDocument(String docId) {

		String qString = "SELECT ?title ?text WHERE {" + "<" + docId + "> <"
				+ HAS_TITLE + "> ?title." + "<" + docId + "> <" + HAS_TEXT
				+ "> ?text." + "}";
		Query query = QueryFactory.create(qString);
		QueryExecution qexec = QueryExecutionFactory.create(query, ds);
		String title = null;
		String text = null;
		try {
			ResultSet rs = qexec.execSelect();
			while (rs.hasNext()) {
				QuerySolution soln = rs.nextSolution();
				title = soln.get("title").asLiteral().getString();
				text = soln.get("text").asLiteral().getString();

				break;
			}
		} finally {
			qexec.close();
		}
		List<Annotatable> annotations = getAnnotations(docId);
		if (title != null && text != null)
			return new SemanticallyExpandedDoc(docId, title, text, annotations);
		else
			return null;
	}

	private List<Annotatable> getAnnotations(String docURI) {

		String qString = "SELECT ?ann ?annname ?annweight WHERE {"
				+ "{SELECT ?ann WHERE {" + "<" + docURI + "> <"
				+ HAS_ANNOTATION + "> ?ann." + "}}." + "?ann <" + HAS_NAME
				+ "> ?annname." + "?ann <" + HAS_WEIGHT + "> ?annweight." + "}";
		Query query = QueryFactory.create(qString);
		QueryExecution qexec = QueryExecutionFactory.create(query, ds);
		List<WeightedAnnotation> plainAnns = new ArrayList<WeightedAnnotation>();
		List<String> annURIs = new ArrayList<String>();
		try {
			ResultSet rs = qexec.execSelect();
			while (rs.hasNext()) {
				QuerySolution soln = rs.nextSolution();
				String annURI = soln.get("ann").asNode().getURI();
				String annName = soln.get("annname").asLiteral().getString();
				double annWeight = Double.parseDouble(soln.get("annweight")
						.asLiteral().getString());
				plainAnns.add(new WeightedAnnotation(annName, annWeight));
				annURIs.add(annURI);
			}
		} finally {
			qexec.close();
		}

		List<Annotatable> annots = getFullAnnotations(plainAnns, annURIs);
		return annots;
	}

	private List<Annotatable> getFullAnnotations(
			List<WeightedAnnotation> plainAnns, List<String> annURIs) {

		List<Annotatable> annots = new LinkedList<Annotatable>();
		Map<String, AncestorAnnotation> ancestors = getAllAncestors(plainAnns,
				annURIs);
		Map<String, NeighborhoodAnnotation> neighbors = getAllNeighbors(
				plainAnns, annURIs);
		for (Entry<String, AncestorAnnotation> entry : ancestors.entrySet()) {

			AncestorAnnotation ancAnn = entry.getValue();
			NeighborhoodAnnotation neiAnn = neighbors.get(entry.getKey());
			try {
				annots.add(new SemanticallyExpandedAnnotation(ancAnn, neiAnn,
						combineMode));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return annots;
	}

	private Map<String, NeighborhoodAnnotation> getAllNeighbors(
			List<WeightedAnnotation> plainAnns, List<String> annURIs) {

		Map<String, NeighborhoodAnnotation> neiAnns = new HashMap<String, NeighborhoodAnnotation>();

		String qString = "SELECT * WHERE {";
		HashMap<String, Double> weightsMap = new HashMap<String, Double>();
		for (int i = 0; i < plainAnns.size(); i++) {
			String annURI = annURIs.get(i);
			String annName = plainAnns.get(i).getEntity();
			double annWeight = plainAnns.get(i).getWeight();
			weightsMap.put(annName, annWeight);

			qString += "{SELECT (\"" + annName
					+ "\" AS ?annname) ?nei ?weight WHERE {"
					+ "{SELECT ?neiURI WHERE {" + "<" + annURI + "> <"
					+ HAS_NEIGHBOR + "> ?neiURI" + "}" + "}." + "?neiURI <"
					+ HAS_NAME + "> ?nei." + "?neiURI <" + HAS_WEIGHT
					+ "> ?weight." + "}" + "}";
			if (i < plainAnns.size() - 1)
				qString += " UNION ";
		}
		qString += "}";
		Query query = QueryFactory.create(qString);
		QueryExecution qexec = QueryExecutionFactory.create(query, ds);
		Map<String, HashSet<Annotatable>> neis = new HashMap<String, HashSet<Annotatable>>();
		try {
			ResultSet rs = qexec.execSelect();
			while (rs.hasNext()) {
				QuerySolution soln = rs.nextSolution();
				String entName = soln.get("annname").asLiteral().getString();
				String neiName = soln.get("nei").asLiteral().getString();
				String neiWeight = soln.get("weight").asLiteral().getString();
				WeightedAnnotation nei = new WeightedAnnotation(neiName,
						Double.parseDouble(neiWeight));
				HashSet<Annotatable> annSet = neis.get(entName);
				if (annSet == null)
					annSet = new HashSet<Annotatable>();
				annSet.add(nei);
				neis.put(entName, annSet);
			}
			for (Entry<String, HashSet<Annotatable>> nei : neis.entrySet()) {
				String annName = nei.getKey();
				HashSet<Annotatable> neiSet = nei.getValue();
				NeighborhoodAnnotation neiAnn = new NeighborhoodAnnotation(
						annName, weightsMap.get(annName), neiSet);
				neiAnns.put(annName, neiAnn);
			}
		} finally {
			qexec.close();
		}
		return neiAnns;
	}

	private Map<String, AncestorAnnotation> getAllAncestors(
			List<WeightedAnnotation> plainAnns, List<String> annURIs) {

		Map<String, AncestorAnnotation> ancAnns = new HashMap<String, AncestorAnnotation>();
		String qString = "SELECT * WHERE {";
		HashMap<String, Double> weightsMap = new HashMap<String, Double>();
		for (int i = 0; i < plainAnns.size(); i++) {
			String annURI = annURIs.get(i);
			String annName = plainAnns.get(i).getEntity();
			double annWeight = plainAnns.get(i).getWeight();
			weightsMap.put(annName, annWeight);
			qString += "{SELECT (\"" + annName
					+ "\" AS ?annname) ?cat ?depth ?entdepth WHERE {"
					+ "{SELECT ?catURI WHERE {" + "<" + annURI + "> <"
					+ HAS_ANCESTOR + "> ?catURI" + "}" + "}." + "?catURI <"
					+ HAS_NAME + "> ?cat." + "?catURI <" + HAS_DEPTH
					+ "> ?depth." + "?catURI <" + HAS_ENTITY_DEPTH
					+ "> ?entdepth." + "}" + "}";
			if (i < plainAnns.size() - 1)
				qString += " UNION ";
		}
		qString += "}";
		Query query = QueryFactory.create(qString);
		QueryExecution qexec = QueryExecutionFactory.create(query, ds);
		Map<String, HashSet<Category>> cats = new HashMap<String, HashSet<Category>>();
		try {
			ResultSet rs = qexec.execSelect();
			while (rs.hasNext()) {
				QuerySolution soln = rs.nextSolution();
				String entName = soln.get("annname").asLiteral().getString();
				double entWeight = weightsMap.get(entName);
				String catName = soln.get("cat").asLiteral().getString();
				String depth = soln.get("depth").asLiteral().getString();
				String entDepth = soln.get("entdepth").asLiteral().getString();
				Category cat = new Category(catName, Integer.parseInt(depth),
						new DepthAnnotation(entName, entWeight,
								Integer.parseInt(entDepth)));
				HashSet<Category> catsSet = cats.get(entName);
				if (catsSet == null)
					catsSet = new HashSet<Category>();
				catsSet.add(cat);
				// TODO see if this works or category gets empty cause shallow
				// copy
				cats.put(entName, catsSet);
			}
			for (Entry<String, HashSet<Category>> cat : cats.entrySet()) {
				String annName = cat.getKey();
				HashSet<Category> catSet = cat.getValue();
				AncestorAnnotation ancAnn = new AncestorAnnotation(annName,
						weightsMap.get(annName), catSet);
				ancAnns.put(annName, ancAnn);
			}
		} finally {
			qexec.close();
		}
		return ancAnns;
	}

	private NeighborhoodAnnotation getNeighbors(String annURI, String entName,
			double entWeight) {

		String qString = "SELECT ?nei ?weight WHERE {"
				+ "{SELECT ?neiURI WHERE {" + "<" + annURI + "> <"
				+ HAS_NEIGHBOR + "> ?neiURI" + "}" + "}." + "?neiURI <"
				+ HAS_NAME + "> ?nei." + "?neiURI <" + HAS_WEIGHT
				+ "> ?weight." + "}";
		Query query = QueryFactory.create(qString);
		QueryExecution qexec = QueryExecutionFactory.create(query, ds);
		Set<Annotatable> neis = new HashSet<Annotatable>();
		try {
			ResultSet rs = qexec.execSelect();
			while (rs.hasNext()) {
				QuerySolution soln = rs.nextSolution();
				String neiName = soln.get("nei").asLiteral().getString();
				String neiWeight = soln.get("weight").asLiteral().getString();
				WeightedAnnotation nei = new WeightedAnnotation(neiName,
						Double.parseDouble(neiWeight));
				neis.add(nei);
			}
		} finally {
			qexec.close();
		}
		return new NeighborhoodAnnotation(entName, entWeight, neis);
	}

	private AncestorAnnotation getAncestors(String annURI, String entName,
			double entWeight) {

		String qString = "SELECT ?cat ?depth ?entdepth WHERE {"
				+ "{SELECT ?catURI WHERE {" + "<" + annURI + "> <"
				+ HAS_ANCESTOR + "> ?catURI" + "}" + "}." + "?catURI <"
				+ HAS_NAME + "> ?cat." + "?catURI <" + HAS_DEPTH + "> ?depth."
				+ "?catURI <" + HAS_ENTITY_DEPTH + "> ?entdepth." + "}";
		Query query = QueryFactory.create(qString);
		QueryExecution qexec = QueryExecutionFactory.create(query, ds);
		Set<Category> cats = new HashSet<Category>();
		try {
			ResultSet rs = qexec.execSelect();
			while (rs.hasNext()) {
				QuerySolution soln = rs.nextSolution();
				String catName = soln.get("cat").asLiteral().getString();
				String depth = soln.get("depth").asLiteral().getString();
				String entDepth = soln.get("entdepth").asLiteral().getString();
				Category cat = new Category(catName, Integer.parseInt(depth),
						new DepthAnnotation(entName, entWeight,
								Integer.parseInt(entDepth)));
				cats.add(cat);
			}
		} finally {
			qexec.close();
		}
		return new AncestorAnnotation(entName, entWeight, cats);
	}

	public void addExpandedDocument(SemanticallyExpandedDoc doc) {
		ds.end();
		ds.begin(ReadWrite.WRITE);
		try {
			// perform a SPARQL Update
			GraphStore graphStore = GraphStoreFactory.create(ds);
			String sparqlUpdateString = createDocTriples(doc);
			// System.out.println(sparqlUpdateString);
			UpdateRequest request = UpdateFactory.create(sparqlUpdateString);
			UpdateProcessor proc = UpdateExecutionFactory.create(request,
					graphStore);
			proc.execute();

			// Finally, commit the transaction.
			ds.commit();
			// Or call .abort()
		} finally {
			ds.end();
			ds.begin(ReadWrite.READ);
		}
	}

	private String createDocTriples(SemanticallyExpandedDoc doc) {

		StringBuilder sb = new StringBuilder("INSERT {");
		List<Annotatable> annots = doc.getAnnotations();
		// title
		String title = doc.getTitle().replaceAll("\\\"", "\\\\\"");
		sb.append(genLiteralTriple(doc.getId(), HAS_TITLE, title));
		// text
		String text = doc.getText().replaceAll("\\\"", "\\\\\"");
		sb.append(genLiteralTriple(doc.getId(), HAS_TEXT, text));
		int annCtr = 0;
		for (Annotatable ann : annots) {
			// annotation
			String annURI = doc.getId() + "annotation" + annCtr;
			sb.append(genNodeTriple(doc.getId(), HAS_ANNOTATION, annURI));
			sb.append(genLiteralTriple(annURI, HAS_NAME, ann.getEntity()));
			sb.append(genLiteralTriple(annURI, HAS_WEIGHT,
					Double.toString(ann.getWeight())));
			// ancestors
			Set<Category> ancAnn = ((SemanticallyExpandedAnnotation) ann)
					.getAncestors().getAncestors();
			int catCtr = 0;
			for (Category cat : ancAnn) {
				String catURI = annURI + "ancestor" + catCtr;
				sb.append(genNodeTriple(annURI, HAS_ANCESTOR, catURI));
				sb.append(genLiteralTriple(catURI, HAS_NAME, cat.getName()));
				sb.append(genLiteralTriple(catURI, HAS_DEPTH,
						Integer.toString(cat.getDepth())));
				sb.append(genLiteralTriple(catURI, HAS_ENTITY_DEPTH,
						Integer.toString(cat.getEntityDepth())));
				catCtr++;
			}
			Collection<Annotatable> neighAnn = ((SemanticallyExpandedAnnotation) ann)
					.getNeighbors().getNeighbors();
			int neiCtr = 0;
			for (Annotatable nei : neighAnn) {
				String neiURI = annURI + "neighbor" + neiCtr;
				sb.append(genNodeTriple(annURI, HAS_NEIGHBOR, neiURI));
				sb.append(genLiteralTriple(neiURI, HAS_NAME, nei.getEntity()));
				sb.append(genLiteralTriple(neiURI, HAS_WEIGHT,
						Double.toString(nei.getWeight())));
				neiCtr++;
			}
			annCtr++;
		}
		sb.append("} WHERE {}");
		return sb.toString();
	}

	private String genNodeTriple(String s, String p, String o) {

		return "<" + s + "> <" + p + "> <" + o + ">.\n";
	}

	private String genLiteralTriple(String s, String p, String o) {

		return "<" + s + "> <" + p + "> \"\"\"" + o + "\"\"\".\n";
	}

	public int getDocCount() {

		throw new UnsupportedOperationException();
	}

	public String[] getDocIds() {
		throw new UnsupportedOperationException();
	}

}
