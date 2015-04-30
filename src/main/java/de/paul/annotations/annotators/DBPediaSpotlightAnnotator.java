package de.paul.annotations.annotators;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.LinkedList;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.methods.GetMethod;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.paul.annotations.JSONAnnotation;

public class DBPediaSpotlightAnnotator extends RESTAnnotator {
	
	private final static String API_URL = "http://spotlight.dbpedia.org/";
	private static final double CONFIDENCE = 0.4;
	private static final int SUPPORT = 1;
	

	protected LinkedList<JSONAnnotation> processJSONResponse(JSONObject resultJSON) {

		JSONArray entities = resultJSON.getJSONArray("Resources");
		LinkedList<JSONAnnotation> resources = new LinkedList<JSONAnnotation>();
		if (entities != null) {
			for(int i = 0; i < entities.length(); i++) {
				try {
					JSONObject entity = entities.getJSONObject(i);
					resources.add(
							new JSONAnnotation(entity.getString("@URI"),
									Double.parseDouble(entity.getString("@similarityScore"))));
	
				} catch (JSONException e) {
	               	System.out.println("JSON exception "+e);
	            }
	
			}
		}
		return resources;
	}

	protected GetMethod getMethod(String docText) throws UnsupportedEncodingException {
		
		GetMethod getMethod = new GetMethod(API_URL + "rest/annotate/?" +
				"confidence=" + CONFIDENCE
				+ "&support=" + SUPPORT
				+ "&text=" + URLEncoder.encode(docText, "utf-8"));
		getMethod.addRequestHeader(new Header("Accept", "application/json"));
		return getMethod;
	}
	
	

}
