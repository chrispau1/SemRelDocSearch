package de.paul.annotations.annotators;

import java.io.UnsupportedEncodingException;
import java.util.LinkedList;

import org.apache.commons.httpclient.methods.GetMethod;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import de.paul.annotations.JSONAnnotation;
import de.paul.util.RESTHandler;

/**
 * Super class for entity annotators using REST calls to a public endpoint.
 * 
 * Provides handling of REST requests and printing of results to a file.
 * 
 * @author Chris
 *
 */
public abstract class RESTAnnotator extends RESTHandler {

	public LinkedList<JSONAnnotation> annotate(String docText)
			throws AnnotationException {

		String response;
		try {
			GetMethod getMethod = getMethod(docText);

			response = request(getMethod);
		} catch (UnsupportedEncodingException e) {
			throw new AnnotationException("Could not encode text.", e);
		}

		assert response != null;

		JSONObject resultJSON = null;

		try {
			resultJSON = new JSONObject(response);
		} catch (JSONException e) {

			try {
				resultJSON = XML.toJSONObject(response);
			} catch (JSONException e1) {
				System.out
						.println("Invalid response. Neither valid XML nor JSON");
			}
			// throw new
			// AnnotationException("Received invalid response from DBpedia Spotlight API.");
		}

		return processJSONResponse(resultJSON);
	}

	protected abstract GetMethod getMethod(String docText)
			throws UnsupportedEncodingException;

	protected abstract LinkedList<JSONAnnotation> processJSONResponse(
			JSONObject resultJSON);

}
