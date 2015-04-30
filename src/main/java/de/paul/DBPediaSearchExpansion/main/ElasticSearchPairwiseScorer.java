package de.paul.DBPediaSearchExpansion.main;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;

import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.DocsEnum;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.CollectionStatistics;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.TermStatistics;
import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.search.similarities.Similarity.SimWeight;
import org.apache.lucene.search.similarities.Similarity.SimScorer;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import de.paul.corpora.elasticsearch.ESHandler;

public class ElasticSearchPairwiseScorer {

	public static void main(String[] args) {
		String idxName = "pincombe";
		String docType = "news";
		ElasticSearchPairwiseScorer scorer = null;
		try {
			scorer = new ElasticSearchPairwiseScorer(idxName, docType);
			double res = scorer.score("31","29","text");
			System.out.println(res);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			scorer.close();
		}
	}

	private ESHandler sh;
	
	public void close() {
		sh.shutdown();
	}
	
	public ElasticSearchPairwiseScorer(String idxName, String docType) {

		sh = ESHandler.getInstance(idxName, docType);
	}
	
	public double score(String id1, String id2, String field) throws IOException {
		
//		Terms[][] termVectors = sh.getTermVectors(new String[]{id1, id2}, new String[]{field});
//		Terms t1 = termVectors[0][0];
//		Terms t2 = termVectors[1][0];
		return computeDefaultSimilarity(Integer.parseInt(id1), Integer.parseInt(id2), field);
	}

	private double computeDefaultSimilarity(int thisDoc, int otherDoc, String field) throws IOException {
		
		//coord * queryNorm * SUM over terms in query ( tf * idf * termBoost * doc/field/lengthNorm)
		TermsEnum it;
		DefaultSimilarity d = new DefaultSimilarity();
		
		float score = 0;
		float sumWeights = 0;
		FSDirectory index = null;
		DirectoryReader reader = null;
		Terms thisTV = null;
		Terms otherTV = null;
		try {
			index = FSDirectory.open(new File("C:/ElasticSearch/elasticsearch-1.4.1/data/elasticsearch/nodes/0/indices/pincombe/0/index"));
		    reader = DirectoryReader.open(index);
		    
		    thisTV = reader.getTermVector(thisDoc, field);
		    otherTV  = reader.getTermVector(otherDoc, field);
		    List<AtomicReaderContext> arContexts = reader.leaves();
		    assert (arContexts.size() == 1);
		    AtomicReaderContext arc = arContexts.get(0);
		    CollectionStatistics collectionStats = new CollectionStatistics(field, 
		    					reader.maxDoc(), reader.numDocs(), 
		    					reader.getSumTotalTermFreq(field), reader.getSumDocFreq(field));
			it = thisTV.iterator(null);
			while (true) {
				
				BytesRef next = it.next();
				if (next == null)
					break;
				
				TermStatistics termStats = new TermStatistics(next, it.docFreq(), it.totalTermFreq());
				SimWeight stats = d.computeWeight(1, collectionStats, termStats);
				stats.normalize(1, 1);
				//term frequency
				int freq = getTermFrequency(reader, field, thisDoc, next);
				/* 
				 * filter out terms the same way that a proper lucene MLT query would. 
				 * based on minTermFreq and minDocFreq.
				 * Doesn't work! MoreLikeThis must do some special stuff... 
				 */
//				if (freq >= MLTSearchParams.termFreq(field) && 
//									it.docFreq() >= MLTSearchParams.docFreq(field)) {
					sumWeights  += stats.getValueForNormalization();
					SimScorer ss = d.simScorer(stats , arc);
		        	score  += ss.score(otherDoc, freq);
//				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			reader.close();
			index.close();
		}
		int overlap = overlap(thisTV, otherTV);
		float queryNorm = d.queryNorm(sumWeights);
		float coord = d.coord(overlap, (int) Math.min(thisTV.size(), otherTV.size()));
		return coord * queryNorm * score;
	}

	private int getTermFrequency(IndexReader reader, String field, int docID, BytesRef term) throws IOException {

		DocsEnum docsEnum = MultiFields.getTermDocsEnum(reader, null, field, term);
        int docIdEnum;
        int freq = 0;
        while ((docIdEnum = docsEnum.nextDoc()) != DocIdSetIterator.NO_MORE_DOCS) {

        	if (docIdEnum == docID) {
        		
        		freq = docsEnum.freq();
        		break;
        	}
        	
        }
		return freq;
	}

	private int overlap(Terms t1, Terms t2) {
		
		int counter = 0;
		HashSet<String> set = new HashSet<String>();
		try {
			TermsEnum it = t1.iterator(null);
			while (true) {
				
				BytesRef next = it.next();
				if (next == null)
					break;
				
				set.add(next.utf8ToString());
			}
			TermsEnum it2 = t2.iterator(null);
			while (true) {
				
				BytesRef next = it2.next();
				if (next == null)
					break;
				
				if (set.contains(next.utf8ToString()))
					counter++;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
				
		return counter;
	}

}
