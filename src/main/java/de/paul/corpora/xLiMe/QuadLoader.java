package de.paul.corpora.xLiMe;

import java.util.LinkedList;
import java.util.List;

import de.paul.annotations.Annotatable;
import de.paul.corpora.ElasticSearchLoader;
import de.paul.dbpedia.DBPediaHandler;
import de.paul.dbpedia.categories.HierarchyHandler;
import de.paul.docs.AnnotatedDoc;
import de.paul.docs.impl.FullyExpandedDoc;
import de.paul.docs.impl.QuadDoc;
import de.paul.util.Paths;

public class QuadLoader extends ElasticSearchLoader {

	private static final int EXPANSION_RADIUS = 2;
	private XLimeQuadStoreHelper helper;
	private DBPediaHandler dbpHandler;
	private HierarchyHandler hierHandler;

	public QuadLoader(String idxName, String docType, String quadStorePath) {
		super(idxName, docType);
		helper = XLimeQuadStoreHelper.getInstance(quadStorePath);
		dbpHandler = DBPediaHandler.getInstance(Paths.TDB_DBPEDIA);
		hierHandler = HierarchyHandler.getInstance(Paths.TDB_DBPEDIA_HIERARCHY);
	}

	public static void main(String[] args) {

		QuadLoader quadLoader = new QuadLoader("es_xlime", "news",
				Paths.XLIME_NEWS_PATH);

		// QuadLoader quadLoader = new QuadLoader("es_xlime", "social",
		// "C:/xLime_corpus/tdb_social");
		// QuadLoader quadLoader = new QuadLoader("pincombe", "news");
		// this is with Elasticsearch store IDs;
		quadLoader.loadInBulks(0, 10, 50);
	}

	@Override
	protected List<AnnotatedDoc> getThisBulksDocs(int offset, int count) {

		List<String> bulkDocURIs = helper.getBulkDocs(offset, count);
		List<AnnotatedDoc> bulkDocs = new LinkedList<AnnotatedDoc>();
		int ctr = 0;
		System.out.print("Creating bulk docs...");
		for (String docUri : bulkDocURIs) {
			bulkDocs.add(createNewDoc(docUri));
			ctr++;
			System.out.print(ctr + " ");
		}
		System.out.print("\n");
		return bulkDocs;
	}

	public FullyExpandedDoc createNewDoc(String docId) {

		String title = helper.getTitle(docId);
		String text = helper.getText(docId);
		List<Annotatable> annotations = helper.getAnnotations(docId);
		QuadDoc plainDoc = new QuadDoc(docId, text, title, annotations);
		FullyExpandedDoc doc = new FullyExpandedDoc(plainDoc, EXPANSION_RADIUS,
				dbpHandler, hierHandler, null);
		return doc;
	}
}
