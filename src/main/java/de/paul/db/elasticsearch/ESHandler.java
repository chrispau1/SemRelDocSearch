package de.paul.db.elasticsearch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;

import org.apache.lucene.index.Terms;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.termvector.MultiTermVectorsItemResponse;
import org.elasticsearch.action.termvector.MultiTermVectorsRequestBuilder;
import org.elasticsearch.action.termvector.MultiTermVectorsResponse;
import org.elasticsearch.action.termvector.TermVectorRequest;
import org.elasticsearch.action.termvector.TermVectorResponse;
import org.elasticsearch.index.query.BaseQueryBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MoreLikeThisQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;

import de.paul.db.DocumentIndex;
import de.paul.documents.AnnotatedDoc;
import de.paul.documents.ElasticSearchSerializableDoc;

public class ESHandler extends ESTool implements DocumentIndex {

	static Map<String, ESHandler> insts = new HashMap<String, ESHandler>();

	public void shutdown() {

		super.shutdown();
		insts.remove(this.idxName);
	}

	public static ESHandler getInstance(String idxName, String docType) {

		if (insts.get(idxName) == null)
			insts.put(idxName, new ESHandler(idxName, docType));

		ESHandler inst = insts.get(idxName);
		if (!inst.getDocType().equals(docType)) {
			inst.setDocType(docType);
			insts.put(idxName, inst);
		}
		return inst;
	}

	/*
	 * init node, client
	 */
	private ESHandler(String idxName, String docType) {

		super.initES(idxName, docType);
	}

	/**
	 * uses traverse relations between documents only.
	 */
	public List<AnnotatedDoc> traverseSearch(ElasticSearchSerializableDoc doc,
			int resultCount) {
		HashMap<String, Float> map = new HashMap<String, Float>();
		map.put("neighbors", 1.0f);
		return mltSearch(doc, map, resultCount);
	}

	/**
	 * uses traverse relations between documents only.
	 */
	public List<AnnotatedDoc> traverseSearch(String id, int resultCount) {
		HashMap<String, Float> map = new HashMap<String, Float>();
		map.put("neighbors", 1.0f);
		return mltSearch(id, map, resultCount);
	}

	public List<AnnotatedDoc> combinationSearch(
			ElasticSearchSerializableDoc doc, int resultCount) {

		HashMap<String, Float> map = new HashMap<String, Float>();
		map.put("neighbors", MLTSearchParams.comboBoost("neighbors"));
		map.put("categories", MLTSearchParams.comboBoost("categories"));
		map.put("text", MLTSearchParams.comboBoost("text"));
		return mltSearch(doc, map, resultCount);
	}

	public List<AnnotatedDoc> combinationSearch(String id, int resultCount) {

		HashMap<String, Float> map = new HashMap<String, Float>();
		map.put("neighbors", MLTSearchParams.comboBoost("neighbors"));
		map.put("categories", MLTSearchParams.comboBoost("categories"));
		map.put("text", 0.0f);// MLTSearchParams.comboBoost("text"));
		return mltSearch(id, map, resultCount);
	}

	/**
	 * uses hierarchical relations between documents only.
	 */
	public List<AnnotatedDoc> hierarchicalSearch(
			ElasticSearchSerializableDoc doc, int resultCount) {
		HashMap<String, Float> map = new HashMap<String, Float>();
		map.put("categories", 1.0f);
		return mltSearch(doc, map, resultCount);
	}

	/**
	 * uses hierarchical relations between documents only.
	 */
	public List<AnnotatedDoc> hierarchicalSearch(String id, int resultCount) {
		HashMap<String, Float> map = new HashMap<String, Float>();
		map.put("categories", 1.0f);
		return mltSearch(id, map, resultCount);
	}

	/**
	 * uses textual similarity documents only.
	 * 
	 * @param resultCount
	 */
	public List<AnnotatedDoc> textSearch(ElasticSearchSerializableDoc doc,
			int resultCount) {
		HashMap<String, Float> map = new HashMap<String, Float>();
		map.put("text", 1.0f);
		return mltSearch(doc, map, resultCount);
	}

	/**
	 * uses textual similarity documents only.
	 * 
	 * @param resultCount
	 */
	public List<AnnotatedDoc> textSearch(String id, int resultCount) {

		HashMap<String, Float> map = new HashMap<String, Float>();
		map.put("text", 1.0f);
		return mltSearch(id, map, resultCount);
	}

	private BoolQueryBuilder addShouldMLTQuery(BoolQueryBuilder bqb,
			String field, String likeText, float boost) {

		if (boost > 0) {
			MoreLikeThisQueryBuilder mltqb = shouldMLTQueryHeader(field, boost);
			return bqb.should(mltqb.likeText(likeText));
		} else
			return bqb;
	}

	private BoolQueryBuilder addShouldMLTQueryByID(BoolQueryBuilder bqb,
			String field, String id, float boost) {

		if (boost > 0) {
			MoreLikeThisQueryBuilder mltqb = shouldMLTQueryHeader(field, boost);
			return bqb.should(mltqb.ids(id));
		} else
			return bqb;
	}

	private MoreLikeThisQueryBuilder shouldMLTQueryHeader(String field,
			float boost) {

		int minTermFreq = MLTSearchParams.termFreq(field);
		int minDocFreq = MLTSearchParams.docFreq(field);
		return QueryBuilders.moreLikeThisQuery(field).minTermFreq(minTermFreq)
				.minDocFreq(minDocFreq).boost(boost);
	}

	public List<AnnotatedDoc> mltSearch(String id, Map<String, Float> boostMap,
			int resultCount) {

		// results object
		List<AnnotatedDoc> res = new ArrayList<AnnotatedDoc>();
		// query
		BaseQueryBuilder qb = null;
		if (boostMap.size() > 1) {
			// distinguish between documents already in corpus and those not
			qb = QueryBuilders.boolQuery();
			for (Entry<String, Float> entry : boostMap.entrySet()) {
				if (entry.getValue() > 0.001)
					qb = addShouldMLTQueryByID((BoolQueryBuilder) qb,
							entry.getKey(), id, entry.getValue());
			}
		} else if (boostMap.size() == 1) {
			String field = boostMap.keySet().iterator().next();
			qb = shouldMLTQueryHeader(field, boostMap.get(field)).ids(id);
		}
		if (qb != null)
			return /* filterRankingNonZero( */executeMLTQuery(res, qb,
					resultCount);// );
		else
			return res;
	}

	public List<AnnotatedDoc> mltSearch(ElasticSearchSerializableDoc esDoc,
			Map<String, Float> boostMap, int resultCount) {

		// results object
		List<AnnotatedDoc> res = new ArrayList<AnnotatedDoc>();
		// distinguish between documents already in corpus and those not
		BaseQueryBuilder qb = null;
		if (esDoc.getId() == null) {
			// if more than one field is investigated
			if (boostMap.size() > 1) {
				qb = QueryBuilders.boolQuery();
				for (Entry<String, Float> entry : boostMap.entrySet()) {
					qb = addShouldMLTQuery((BoolQueryBuilder) qb,
							entry.getKey(),
							esDoc.getElasticSearchField(entry.getKey()),
							entry.getValue());
				}
			} else if (boostMap.size() == 1) {
				// if only one field is investigated
				String field = boostMap.keySet().iterator().next();
				qb = shouldMLTQueryHeader(field, boostMap.get(field)).likeText(
						esDoc.getElasticSearchField(field));
			}
			if (qb != null)
				return filterRankingNonZero(executeMLTQuery(res, qb,
						resultCount));
			else
				return res;
		} else {
			// in case that ID is given, simpler method can be called
			return mltSearch(esDoc.getId(), boostMap, resultCount);
		}
	}

	private List<AnnotatedDoc> filterRankingNonZero(List<AnnotatedDoc> ranking) {

		LinkedList<AnnotatedDoc> res = new LinkedList<AnnotatedDoc>();
		for (AnnotatedDoc el : ranking) {
			if (Math.abs(el.getScore()) >= 0.0001) {
				res.add(el);
			}
		}
		return res;
	}

	public Terms[][] getTermVectors(String[] ids, String[] fields) {

		TermVectorResponse[] tvrs = executeGetTermVectors(ids);
		Terms[][] res = new Terms[ids.length][fields.length];
		for (int i = 0; i < tvrs.length; i++) {
			TermVectorResponse tvr = tvrs[i];
			for (int j = 0; j < fields.length; j++) {
				String field = fields[j];
				Terms terms = null;
				try {
					terms = tvr.getFields().terms(field);
				} catch (IOException e) {
					System.out.println("Field doesn't exist?");
					e.printStackTrace();
				}
				res[i][j] = terms;
			}
		}
		return res;
	}

	private TermVectorResponse[] executeGetTermVectors(String[] ids) {

		TermVectorResponse[] res = new TermVectorResponse[ids.length];
		MultiTermVectorsRequestBuilder mtvrb = client.prepareMultiTermVectors();
		for (int i = 0; i < ids.length; i++) {
			TermVectorRequest tvr = new TermVectorRequest(idxName, docType,
					ids[i]).termStatistics(true);
			mtvrb.add(tvr);
		}
		MultiTermVectorsResponse mtvRes = mtvrb.execute().actionGet();
		Iterator<MultiTermVectorsItemResponse> it = mtvRes.iterator();
		int count = 0;
		while (it.hasNext()) {
			if (count < res.length) {
				TermVectorResponse next = it.next().getResponse();
				res[count] = next;
				count++;
			}
		}
		return res;
	}

	private List<AnnotatedDoc> executeMLTQuery(List<AnnotatedDoc> res,
			BaseQueryBuilder qb, int resultCount) {
		// see:
		// http://www.elasticsearch.org/guide/en/elasticsearch/guide/current/relevance-is-broken.html
		// at bottom
		SearchResponse rs = client
				.prepareSearch(idxName)
				.setTypes(docType)
				.setSearchType(SearchType.QUERY_THEN_FETCH/* DFS_QUERY_THEN_FETCH */)
				.setQuery(qb)./* addField("text").addField("title"). */setFrom(0)
				.setSize(resultCount)./* setExplain(true). */execute()
				.actionGet();
		// convert results into more useful format
		Iterator<SearchHit> it = rs.getHits().iterator();
		while (it.hasNext()) {
			SearchHit next = it.next();
			// System.out.println("bla: " + next.explanation().toHtml());
			try {
				// AnnotatedDoc resDoc = new AnnotatedDoc(new HashMap<String,
				// Object>(
				// next.getSource()), Integer.parseInt(next.getId()));
				// Map<String, SearchHitField> resFields = next.getFields();
				// String text = (String) resFields.get("text").getValue();
				// String title = (String) resFields.get("title").getValue();
				String id = next.getId();
				AnnotatedDoc resDoc = new AnnotatedDoc("", "", id);

				resDoc.setScore(next.getScore());
				res.add(resDoc);
			} catch (NumberFormatException e) {
				System.err
						.println("Document ID could not be parsed ! Document IDs must be"
								+ " Integers.");
				e.printStackTrace();
			}
		}
		return res;
	}

	/**
	 * Queries for document of this id, puts the returned content directly into
	 * the body of a semantically expanded document. Entities and classes are
	 * read from ElasticSearch index.
	 * 
	 * @param id
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public AnnotatedDoc getDocument(String docId) {

		try {
			GetResponse resp = client.prepareGet(idxName, docType, docId)
					.setFields("title", "text").execute().actionGet();
			if (resp != null) {
				String text = (String) resp.getField("text").getValue();
				String title = (String) resp.getField("title").getValue();
				if (text != null) {
					return new AnnotatedDoc(text, title, resp.getId());
				}
			} else
				return null;
		} catch (ElasticsearchException e) {
			e.printStackTrace();
		}
		return null;
	}

	public int getDocCount() {

		int count = -1;
		try {
			CountResponse countResponse = client.prepareCount(idxName)
					.execute().get();
			count = (int) countResponse.getCount();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return count;
	}

	public <x extends AnnotatedDoc> x getDocument(int docId) {

		return (x) getDocument(Integer.toString(docId));
	}

	public String[] getDocIds() {

		throw new UnsupportedOperationException();
	}

}
