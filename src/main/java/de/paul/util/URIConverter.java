package de.paul.util;

public class URIConverter {

	private final static String DBP_RESOURCE = "http://dbpedia.org/resource/";

	/*
	 * SHOULD maybe check if it is really a DBPedia resource URI.
	 */
	public static String removePrefix(String uri, char charToSplitAt) {

		return uri.substring(uri.lastIndexOf(charToSplitAt) + 1);
	}

	public static String makeURI(String name) {

		return DBP_RESOURCE + name;
	}

}
