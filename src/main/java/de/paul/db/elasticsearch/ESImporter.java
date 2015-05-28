package de.paul.db.elasticsearch;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.common.xcontent.XContentBuilder;

import de.paul.documents.ElasticSearchSerializableDoc;

public class ESImporter extends ESTool {

	static Map<String, ESImporter> insts = new HashMap<String, ESImporter>();
	private BulkRequestBuilder bulkRequest;
	private int currBulkSize = 0;
	private int maxBulkSize;

	public static ESImporter getInstance(String idxName, String docType) {

		if (insts.get(idxName) == null)
			insts.put(idxName, new ESImporter(idxName, docType));

		ESImporter inst = insts.get(idxName);
		if (!inst.getDocType().equals(docType)) {
			inst.setDocType(docType);
			insts.put(idxName, inst);
		}
		return inst;
	}

	/*
	 * init node, client
	 */
	private ESImporter(String idxName, String docType) {

		super.initES(idxName, docType);
		// this.idxName = idxName;
		// this.docType = docType;
		// node = nodeBuilder().clusterName(CLUSTER_NAME).node();
		// client = node.client();
		bulkRequest = client.prepareBulk();
	}

	private IndexRequestBuilder buildDocument(Map<String, String> fields,
			String uri) {

		try {
			XContentBuilder obj = jsonBuilder().startObject();
			// add fields
			for (Entry<String, String> field : fields.entrySet()) {
				obj = obj.field(field.getKey(), field.getValue());
			}
			obj.endObject();
			if (uri == null)
				return client.prepareIndex(idxName, docType).setSource(obj);
			else
				return client.prepareIndex(idxName, docType, uri)
						.setSource(obj);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public IndexResponse addExpandedDocument(ElasticSearchSerializableDoc doc) {

		return addStringDocument(doc.getElasticSearchFields(), doc.getId());
	}

	private IndexResponse addStringDocument(Map<String, String> fields,
			String uri) {

		if (fields == null || fields.size() == 0)
			return null;
		try {
			IndexRequestBuilder irb = buildDocument(fields, uri);
			IndexResponse resp = irb.execute().actionGet();
			// return response
			return resp;
		} catch (ElasticsearchException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void addDocToBulk(ElasticSearchSerializableDoc esDoc) {

		if (currBulkSize >= maxBulkSize) {
			bulkCommitNow();
			currBulkSize = 0;
		}
		bulkRequest.add(buildDocument(esDoc.getElasticSearchFields(),
				esDoc.getId()));
		currBulkSize++;
	}

	public void bulkCommitNow() {
		try {
			bulkRequest.execute().actionGet();
		} catch (ElasticsearchException e) {
			e.printStackTrace();
		}
		bulkRequest = client.prepareBulk();
	}

	public void setBulkSize(int bulkSize) {

		this.maxBulkSize = bulkSize;
	}

	public void shutdown() {

		super.shutdown();
		insts.remove(this.idxName);
	}

}
