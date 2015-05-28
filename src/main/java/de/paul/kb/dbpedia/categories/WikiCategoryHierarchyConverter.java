package de.paul.kb.dbpedia.categories;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Converts hierarchy into triples.
 * 
 * LOWERCASES categories !!! Due to lowercased source data...
 * 
 * @author Chris
 *
 */
public class WikiCategoryHierarchyConverter {

	public static final String ROOT = "category:main topic classifications";
	private static final int ROWCOUNT = 1176945;
	public static final String DEPTH = "http://dbpedia_hierarchy.org/has_depth";
	public static final String IS_A = "http://dbpedia_hierarchy.org/is_a";
	private static final String PREFIX = "http://dbpedia.org/resource/";
	public static final String ANCESTOR = "http://dbpedia_hierarchy.org/has_ancestor";
	private Map<String, Integer> depthMap = new HashMap<String, Integer>();
	//ancestor stuff
	private Map<String, Set<String>> children = new HashMap<String, Set<String>>();
	private Map<String, Set<String>> ancestorMap = new HashMap<String, Set<String>>();
	private final int ANC_COUNT = 9516869;
	private final int ANC_STEP = ANC_COUNT / 10;
	
	private BufferedWriter bw = null;
	
	public static void main(String[] args) throws IOException {

//		BufferedReader fr = new BufferedReader(new FileReader("text_output_data/wiki_category_hierarchy.nt"));
////		BufferedReader fr = new BufferedReader(new FileReader("text_input_data/Hierarchy"));
//		int i = 0;
//		while (i<428247+200) {
//			String line = fr.readLine();
//			if (i >  428247)
//				System.out.println(line);
//			i++;
//		}
//		fr.close();
		WikiCategoryHierarchyConverter conv = new WikiCategoryHierarchyConverter();
		conv.processHierarchy("text_input_data/Hierarchy",//categories-removed.tsv",
				"text_output_data/wiki_category_hierarchy.nt");
	}
	
	public WikiCategoryHierarchyConverter() {
		
		depthMap.put(ROOT, 0);
	}
	
	/*
	 * Read tab separated file line by line.
	 */
	private void processHierarchy(String inputPath, String outputPath) throws FileNotFoundException {
		
		/*
		 * Reading stuff
		 */
		File inputFile = new File(inputPath);
		BufferedReader br = new BufferedReader(new FileReader(inputFile));
		String line;
		/*
		 * Loop print support stuff
		 */
		int i = 0;
		int percent =  ROWCOUNT / 10;
		int stepCount = 0;
		int anc_counter = 0;
		int anc_stepcount = 0;
		/*
		 * Catch file exceptions
		 */
		try {
			/*
			 * Writing stuff
			 */
			File outputFile = new File(outputPath);
			bw = new BufferedWriter(new FileWriter(outputFile));
			/*
			 * Loop through input tsv file
			 */
			int counter = 0;
			while((line = br.readLine()) != null) {
				counter++;
				//split at tabs
				String[] items = line.split("\\t");
				/*
				 * Format is: <sub-category><tab><category><tab><priority>
				 */
				if (items.length == 3)
					processOneRow(items[0], items[1], items[2]);
				else
					System.out.println("Odd row: " + line);
				i++;
				if (i >= percent) {
					stepCount+=10;
					i = 0;
					System.out.println(stepCount + "% read");
				}
			}
			System.out.println("Row count: " + counter);
			/*
			 * Write all the depth triples
			 */
			for (Entry<String, Integer> entry:depthMap.entrySet()) {
				writeDepthTriple(entry.getKey(), entry.getValue());
			}
			/*
			 * Write all ancestor triples, recursively.
			 * Begin with root and empty ancestor list (root has no ancestors)
			 */
			System.out.println("Computing ancestor relationships...");
			computeAncestorsDFS(ROOT, new LinkedList<String>());
			System.out.println("Done.");
			for (Entry<String, Set<String>> entry: ancestorMap.entrySet()) {
				
				String subcat = entry.getKey();
				for (String anc: entry.getValue()) {
					writeAncestorTriple(subcat, anc);
					anc_counter++;
					//count number writes
					//output if certain percentage reached
					if (anc_counter >= ANC_STEP) {
						
						anc_stepcount ++;
						anc_counter = 0;
						System.out.println((anc_stepcount*10) + " % of ancestors written.");
					}
				}
			}
			System.out.println("Ancestor relationships written: " + (anc_stepcount*ANC_STEP + anc_counter));
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/*
	 * Convert that "triple" into a real triple.
	 * 
	 * Also, generate triples for each category that state the
	 * category's hierarchical depth
	 */
	private void processOneRow(String subcat, String cat, String prior) {
		
		subcat = subcat.replace((char) 65533, '–');
		cat = cat.replace((char) 65533, '–');
		//write the hierarchy triple
		writeHierarchicalTriple(subcat, cat);
		//do the depth stuff
		addToDepthMap(subcat, cat);
		//find and register ancestors of current sub category
		addChild(subcat, cat);
	}

	/*
	 * Adds children to nodes. This way, ancestor triples can 
	 * be generated later using BFS or DFS starting at the node.
	 */
	private void addChild(String subcat, String cat) {
		
		Set<String> kids = children.get(cat);
		if (kids == null)
			kids = new HashSet<String>();
		kids.add(subcat);
		children.put(cat, kids);
	}
	
	/*
	 * Write triples for current node. Recursively call method 
	 * for its children
	 * Parameter ancestors is the path of ancestors that the 
	 * algorithm took to get to this node.
	 */
	private void computeAncestorsDFS(String currNode, List<String> ancestors) {
		
		/*
		 * add all ancestral triples for current node to print map
		 */
		Set<String> currAncestors = ancestorMap.get(currNode);
		if (currAncestors == null)
			currAncestors = new HashSet<String>();
		for (String anc_i:ancestors) {
			//writeAncestorTriple(currNode, anc_i);
			currAncestors.add(anc_i);
		}
		ancestorMap.put(currNode, currAncestors);
		/*
		 * prepare ancestor list for children
		 */
		LinkedList<String> updatedAncestors = new LinkedList<String>(ancestors);
		updatedAncestors.addLast(currNode);
		//get children
		Set<String> kids = children.get(currNode);
		/*
		 * call this method for all children with updated parameters
		 */
		if (kids != null) {
			for (String kid:kids) {
				computeAncestorsDFS(kid, updatedAncestors);
			}
		}
	}

	/*
	 * Adds the sub-category to the depth map iff its parent 
	 * category is registered in the depth map.
	 */
	private boolean addToDepthMap(String subcat, String cat) {
		
		Integer depth = depthMap.get(cat);
		if (depth != null) {
			Integer subcatDepth = depthMap.get(subcat);
			//add it to depthMap
			int newDepth = depth+1;
			if (subcatDepth != null) {
				depthMap.put(subcat, Math.min(newDepth, subcatDepth));
			} else {
				depthMap.put(subcat, newDepth);
			}
			return true;
		} else {
			//write it to the parent list
			return false;
		}
	}
	
	/*
	 * Uses a global file writer to add a triple to the output file.
	 * Hierarchy triple.
	 */
	private void writeHierarchicalTriple(String subcat, String cat) {
		
		String dbpSubCat = dbpFormat(subcat);
		String dbpCat = dbpFormat(cat);
		writeLineToFile(dbpSubCat, IS_A, dbpCat);
	}
	
	/*
	 * Uses a global file writer to add a triple to the output file.
	 * Ancestor triple.
	 */
	private void writeAncestorTriple(String subcat, String cat) {
		
		String dbpSubCat = dbpFormat(subcat);
		String dbpCat = dbpFormat(cat);
		writeLineToFile(dbpSubCat, ANCESTOR, dbpCat);
	}
	
	/*
	 * Uses a global file writer to add a triple to the output file.
	 * Depth triple.
	 */
	private void writeDepthTriple(String cat, int depth) {
		
		String dbpCat = dbpFormat(cat);
		writeLineToFile(dbpCat, DEPTH, "\"" + Integer.toString(depth) + "\"^^<http://www.w3.org/2001/XMLSchema#integer>");
	}

	private void writeLineToFile(String subj, String pred, String obj) {
		
		if (bw != null)
			try {
				if (!subj.startsWith("\""))
					subj = "<" + subj + ">";
				if (!pred.startsWith("\""))
					pred = "<" + pred + ">";
				if (!obj.startsWith("\""))
					obj = "<" + obj + ">";
				bw.write(subj + " " + pred + " " + obj + " .\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
		else
			throw new NullPointerException();
	}

	/*
	 * Add prefix and adjust format.
	 */
	private String dbpFormat(String cat) {
		
//		try {
			//encode as URL
//			cat = PREFIX + URLEncoder.encode(cat.replace(' ', '_'), "UTF-8");
			cat = PREFIX + cat.replace(' ', '_');
			//correct erroneous replacement of colon
//			cat = cat.replace("%3A", ":");
//		} catch (UnsupportedEncodingException e) {
//			e.printStackTrace();
//		}
		return cat;
	}
	
}
