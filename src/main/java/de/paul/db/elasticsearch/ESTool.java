package de.paul.db.elasticsearch;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

import org.elasticsearch.client.Client;
import org.elasticsearch.node.Node;

public abstract class ESTool {

	// ElasticSearch API stuff
	// ElasticSearch needs name of cluster that is used
	protected static final String CLUSTER_NAME = "elasticsearch";
	// ElasticSearch attribute: document type
	protected String docType;
	protected Node node;
	protected Client client;
	// name of ES index
	protected String idxName;

	protected void initES(String idxName, String docType) {

		this.idxName = idxName;
		this.docType = docType;
		System.out.println("initializing ElasticSearch server...");
		node = nodeBuilder().clusterName(CLUSTER_NAME).node();
		client = node.client();
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("Done.");
	}

	public void setDocType(String docType) {

		this.docType = docType;
	}

	public String getDocType() {

		return this.docType;
	}

	public void shutdown() {

		node.close();
	}
}
