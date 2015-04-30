package de.paul.util;

import java.io.IOException;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.params.HttpMethodParams;

import de.paul.annotations.annotators.AnnotationException;

public abstract class RESTHandler {
	// Create an instance of HttpClient.
	private static HttpClient client = new HttpClient();

	public String request(HttpMethod method) throws AnnotationException {

		String response = null;

		// Provide custom retry handler is necessary
		method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
				new DefaultHttpMethodRetryHandler(3, false));

		try {
			// Execute the method.
			int statusCode = client.executeMethod(method);

			if (statusCode != HttpStatus.SC_OK) {
				System.out.println("Method failed: " + method.getStatusLine());
			}

			// Read the response body.
			byte[] responseBody = method.getResponseBody(); // TODO Going to
															// buffer response
															// body of large or
															// unknown size.
															// Using
															// getResponseBodyAsStream
															// instead is
															// recommended.

			// Deal with the response.
			// Use caution: ensure correct character encoding and is not binary
			// data
			response = new String(responseBody);

		} catch (HttpException e) {
			throw new AnnotationException(
					"Protocol error executing HTTP request.", e);
		} catch (IOException e) {
			throw new AnnotationException(
					"Transport error executing HTTP request.", e);
		} finally {
			// Release the connection.
			method.releaseConnection();
		}
		return response;

	}

}
