package de.paul.corpora;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import de.paul.annotations.JSONAnnotation;
import de.paul.docs.AnnotatedDoc;
import de.paul.docs.impl.JSONTextDoc;
import de.paul.util.TimeKeeper;

public class JSONLoader {

	private Map<String, JSONTextDoc> docs = null;
	private String jsonPath;

	public JSONLoader(String path) {
		this.jsonPath = path;
		loadDocs();
	}

	private void loadDocs() {

		long t1 = System.currentTimeMillis();
		BufferedReader br = null;
		String fileText = null;
		try {
			br = new BufferedReader(new FileReader(jsonPath));
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = br.readLine()) != null) {
				line = line.replaceAll("%(?![0-9a-fA-F]{2})", "%25");
				line = line.replaceAll("\\+", "%2B");
				line = URLDecoder.decode(line, "utf-8");
				sb.append(line);
			}
			fileText = sb.toString();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (fileText != null) {
			// get array of docs
			JSONArray arr = new JSONArray(fileText);
			// init result docs
			docs = new HashMap<String, JSONTextDoc>();
			for (int i = 0; i < arr.length(); i++) {
				JSONObject obj = arr.getJSONObject(i);
				String text = obj.getString("text");
				JSONArray ann = obj.getJSONArray("annotations");
				List<JSONAnnotation> annots = new LinkedList<JSONAnnotation>();
				for (int j = 0; j < ann.length(); j++) {
					JSONObject annj = ann.getJSONObject(j);
					String ent = annj.getString("entity");
					double w = annj.getDouble("weight");
					annots.add(new JSONAnnotation(ent, w));
				}
				String idAsString = Integer.toString(i);
				int endOfTitle = text.indexOf(' ', 50);
				if (endOfTitle == -1)
					endOfTitle = text.length();
				docs.put(
						idAsString,
						new JSONTextDoc(idAsString, text, text.substring(0,
								endOfTitle), annots));
			}
		}
		long t2 = System.currentTimeMillis();
		TimeKeeper.getInstance().addLoadDocsTime(t2 - t1);
	}

	public JSONTextDoc getDoc(String id) {

		if (docs == null)
			loadDocs();
		return docs.get(id);
	}

	public int getDocCount() {
		if (docs == null)
			loadDocs();
		return docs.size();
	}

	public List<AnnotatedDoc> getAllDocs() {

		LinkedList<AnnotatedDoc> res = new LinkedList<AnnotatedDoc>();
		Collection<JSONTextDoc> values = docs.values();
		for (JSONTextDoc v : values) {
			res.add(v);
		}
		return res;
	}

}
