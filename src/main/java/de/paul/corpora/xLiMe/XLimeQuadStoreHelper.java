package de.paul.corpora.xLiMe;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.tdb.TDBFactory;

import de.paul.annotations.Annotatable;
import de.paul.annotations.WeightedAnnotation;
import de.paul.docs.AnnotatedDoc;
import de.paul.docs.impl.QuadDoc;
import de.paul.util.URIConverter;

/**
 * Provides means to access Jena QuadStore that holds documents in the xLime
 * format.
 * 
 * @author Chris
 *
 */
public class XLimeQuadStoreHelper {

	private Dataset ds;
	private static Map<String, XLimeQuadStoreHelper> insts = new HashMap<String, XLimeQuadStoreHelper>();

	private XLimeQuadStoreHelper(String quadStorePath) {

		this.ds = TDBFactory.createDataset(quadStorePath);
	}

	public static XLimeQuadStoreHelper getInstance(String path) {

		if (insts.get(path) == null) {
			insts.put(path, new XLimeQuadStoreHelper(path));
		}
		return insts.get(path);
	}

	public List<Annotatable> getAnnotations(String docURI) {

		List<Annotatable> annotations = new LinkedList<Annotatable>();
		String qString = "SELECT ?e ?w WHERE {" + "GRAPH <" + docURI + "> {"
				+ "?s <http://xlime-project.org/vocab/hasEntity> ?e. "
				+ "?s <http://xlime-project.org/vocab/hasConfidence> ?w." + "}"
				+ "}";
		Query query = QueryFactory.create(qString);
		QueryExecution qexec = QueryExecutionFactory.create(query, ds);
		try {
			ResultSet rs = qexec.execSelect();
			while (rs.hasNext()) {
				QuerySolution soln = rs.nextSolution();
				String ent = soln.get("e").asNode().getURI();
				if (ent.startsWith("http://en") || ent.startsWith("wikien")) {
					// in the data at hand, they point to WIKIpedia. So prefix
					// is stripped
					// and later DBPedia Resource prefix is added
					char splitChar = '/';
					if (ent.startsWith("wikien"))
						splitChar = ':';
					ent = URIConverter.removePrefix(ent, splitChar);
					double weight = soln.getLiteral("w").getDouble();
					WeightedAnnotation ann = new WeightedAnnotation(
							URIConverter.makeURI(ent), weight);
					annotations.add(ann);
				}
			}
		} finally {
			qexec.close();
		}
		return annotations;
	}

	public String getText(String docURI) {

		String text = null;
		String qString = "SELECT ?o WHERE {" + "GRAPH <" + docURI
				+ "> {?s <http://rdfs.org/sioc/ns#content> ?o}" + "}";
		Query query = QueryFactory.create(qString);
		QueryExecution qexec = QueryExecutionFactory.create(query, ds);
		try {
			ResultSet rs = qexec.execSelect();
			while (rs.hasNext()) {
				QuerySolution soln = rs.nextSolution();
				text = soln.getLiteral("o").getString();
			}
		} finally {
			qexec.close();
		}
		return text;
	}

	public AnnotatedDoc getDocument(String id) {

		String text = this.getText(id);
		String title = this.getTitle(id);
		List<Annotatable> annots = this.getAnnotations(id);
		return new QuadDoc(id, text, title, annots);
	}

	public List<String> getBulkDocs(int offset, int count) {

		List<String> res = new LinkedList<String>();
		String qString = "SELECT ?g WHERE {" + "GRAPH ?g {}" + "} OFFSET "
				+ offset + " LIMIT " + count;
		Query query = QueryFactory.create(qString);
		QueryExecution qexec = QueryExecutionFactory.create(query, ds);
		try {
			ResultSet rs = qexec.execSelect();
			while (rs.hasNext()) {
				QuerySolution soln = rs.nextSolution();
				String uri = soln.get("g").asNode().getURI();
				res.add(uri);
			}
		} finally {
			qexec.close();
		}
		return res;
	}

	public String getTitle(String docURI) {

		String title = null;
		String qString = "SELECT ?o WHERE {" + "GRAPH <" + docURI
				+ "> {?s <http://purl.org/dc/terms/title> ?o}" + "}";
		Query query = QueryFactory.create(qString);
		QueryExecution qexec = QueryExecutionFactory.create(query, ds);
		try {
			ResultSet rs = qexec.execSelect();
			while (rs.hasNext()) {
				QuerySolution soln = rs.nextSolution();
				title = soln.getLiteral("o").getString();
			}
		} finally {
			qexec.close();
		}
		return title;
	}

}
