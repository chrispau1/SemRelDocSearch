package de.paul.annotations.annotators;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.LinkedList;

import org.apache.commons.httpclient.methods.GetMethod;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.paul.annotations.JSONAnnotation;

public class XLisaAnnotator extends RESTAnnotator {

	private final static String API_URL = "http://km.aifb.kit.edu/services/text-annotation/";
	private static final String MODEL = "ngram";
	private static final String LANGUAGE = "en";

	protected LinkedList<JSONAnnotation> processJSONResponse(
			JSONObject resultJSON) {

		JSONObject wrapperJSON = resultJSON
				.getJSONObject("CLAnnotationResponse");
		JSONObject entitiesObject = null;
		JSONArray entities = null;
		try {
			entitiesObject = wrapperJSON.getJSONObject("DetectedTopics");
			entities = entitiesObject.getJSONArray("DetectedTopic");
		} catch (JSONException e) {
			System.out.println("no entities extracted for given text");
			entitiesObject = null;
			entities = null;
		}
		LinkedList<JSONAnnotation> resources = new LinkedList<JSONAnnotation>();
		if (entities != null) {
			for (int i = 0; i < entities.length(); i++) {
				try {
					JSONObject entity = entities.getJSONObject(i);
					resources.add(new JSONAnnotation(entity.getString("URL"),
							entity.getDouble("weight")));

				} catch (JSONException e) {
					System.out.println("JSON exception " + e);
				}

			}
		}
		return resources;
	}

	@Override
	protected GetMethod getMethod(String docText)
			throws UnsupportedEncodingException {

		GetMethod getMethod = new GetMethod(API_URL + "?" + "model=" + MODEL
				+ "&source=" + URLEncoder.encode(docText, "utf-8") + "&lang1="
				+ LANGUAGE + "&kb=dbpedia");
		// getMethod.addRequestHeader(new Header("Accept", "application/json"));
		return getMethod;
	}

}
