package de.paul.docs;

import java.util.Map;

/**
 * Specifies field name and value methods that an ElasticSearch handler will use
 * to understand what of a document to serialize into String fields.
 * 
 * @author Chris
 *
 */
public interface ElasticSearchSerializableDoc extends AnnotatableDocument {

	public Map<String, String> getElasticSearchFields();

	public String getElasticSearchField(String key);

}
