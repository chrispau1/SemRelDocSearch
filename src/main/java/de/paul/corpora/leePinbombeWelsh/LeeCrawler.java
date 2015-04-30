package de.paul.corpora.leePinbombeWelsh;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.node.Node;

public class LeeCrawler {

	private String[] texts;

	public LeeCrawler(String path) throws IOException {

		File f = new File(path);
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(f));
			String line;
			this.texts = new String[50];
			int i = 0;
			while ((line = br.readLine()) != null) {

				int endOfText = line.lastIndexOf("(");
				if (endOfText < 3)
					endOfText = line.length();
				texts[i] = line.substring(3, endOfText).trim();
				i++;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			br.close();
		}
	}

	public void writeToPlainTextIndex() throws IOException {

		Node node = nodeBuilder().clusterName("elasticsearch").node();
		Client client = node.client();
		for (int i = 0; i < texts.length; i++) {
			XContentBuilder obj = jsonBuilder().startObject()
					.field("text", texts[i]).endObject();
			IndexRequestBuilder irb = client.prepareIndex("plainpincombe",
					"textDoc", String.valueOf(i)).setSource(obj);
			irb.execute().actionGet();
		}
		node.close();
	}

	public static void main(String[] args) throws IOException {

		new LeeCrawler(
				"text_input_data/LeePincombeWelsh/LeePincombeWelshDocuments.txt")
				.writeToMalletFormat("text_output_data/MalletCorpus.txt");
		// .writeToPlainTextIndex();
	}

	private void writeToMalletFormat(String outputPath) {

		BufferedWriter bw = null;

		try {
			bw = new BufferedWriter(new FileWriter(outputPath));
			for (int i = 0; i < texts.length; i++) {
				String s = i + "	X	" + texts[i];
				if (i < texts.length - 1)
					s += "\n";
				bw.write(s);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public String[] getDocTexts() {

		return texts;
	}

}
