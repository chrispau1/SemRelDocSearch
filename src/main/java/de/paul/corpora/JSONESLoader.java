package de.paul.corpora;

import java.util.LinkedList;
import java.util.List;

import de.paul.dbpedia.DBPediaHandler;
import de.paul.dbpedia.categories.HierarchyHandler;
import de.paul.docs.AnnotatedDoc;
import de.paul.docs.impl.FullyExpandedDoc;
import de.paul.docs.impl.JSONTextDoc;
import de.paul.util.Paths;

/**
 * Loads annotated texts from a JSON file, expands them and adds them to
 * ElasticSearch corpus
 * 
 * @author Chris
 *
 */
public class JSONESLoader extends ElasticSearchLoader {

	private static final int EXPANSION_RADIUS = 2;
	private HierarchyHandler hierHandler;
	private DBPediaHandler dbpHandler;
	private JSONLoader jsonParser;

	public static void main(String[] args) {

		JSONESLoader l = new JSONESLoader("pincombe", "news",
				Paths.PINCOMBE_ANNOTATED_JSON);
		// List<String> bulkIDs = l.getBulkIDs(0, 50);
		// for (String id:bulkIDs)
		// System.out.println("doc: \n" + l.getDoc(Integer.parseInt(id)));
		l.loadInBulks(0, 50, 1);
	}

	public JSONESLoader(String idxName, String docType, String path) {

		super(idxName, docType);
		jsonParser = new JSONLoader(path);
	}

	public JSONESLoader(String path) {
		super();
		this.jsonParser = new JSONLoader(path);
	}

	public AnnotatedDoc createNewDoc(String docId) {

		JSONTextDoc textDoc = jsonParser.getDoc(docId);
		AnnotatedDoc esDoc = produceExpDocFromText(textDoc);
		return esDoc;
	}

	private AnnotatedDoc produceExpDocFromText(JSONTextDoc textDoc) {

		if (textDoc.getAnnotations().size() > 0) {
			if (dbpHandler == null)
				dbpHandler = DBPediaHandler.getInstance(Paths.TDB_DBPEDIA);
			if (hierHandler == null)
				hierHandler = HierarchyHandler
						.getInstance(Paths.TDB_DBPEDIA_HIERARCHY);
			FullyExpandedDoc esDoc = new FullyExpandedDoc(textDoc,
					EXPANSION_RADIUS, dbpHandler, hierHandler, null);
			return esDoc;
		} else
			return null;
	}

	@Override
	protected List<AnnotatedDoc> getThisBulksDocs(int offset, int count) {

		int docsCount = jsonParser.getDocCount();
		List<AnnotatedDoc> res = new LinkedList<AnnotatedDoc>();
		if (offset < docsCount) {
			for (int i = offset; i < Math.round(Math.min(offset + count,
					docsCount)); i++) {
				res.add(createNewDoc(Integer.toString(i)));
			}
		}
		return res;
	}

}
