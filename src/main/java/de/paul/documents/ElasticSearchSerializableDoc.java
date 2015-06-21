package de.paul.documents;

import java.util.Map;

/**
 * Specifies field name and value methods that an ElasticSearch handler will use
 * to understand what of a document to serialize into String fields.
 * 
 * @author Chris
 *
 */
public interface ElasticSearchSerializableDoc extends AnnotatableDocument {

	static final int ES_WORD_STRETCH_FACTOR = 50;

	public Map<String, String> getElasticSearchFields();

	public String getElasticSearchField(String key);

}
