package de.paul.dbpedia.expansion;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;

public class StopURIHandler {
	
	private static StopURIHandler instance = null;
	private HashSet<String> blacklistedEnts = null;

	public HashSet<String> loadBlackListedEntities(String path) {
		
		if (blacklistedEnts == null) {
			this.blacklistedEnts = new HashSet<String>();
			BufferedReader br = null; 
			try {
				br = new BufferedReader(new FileReader(path));
				String line;
				while ((line = br.readLine()) != null) {
					blacklistedEnts.add(line.trim());
				}
//				addBirthsDeathsByYear();
			} catch (FileNotFoundException e) {
				System.out.println("File \"stopURIs.txt\" needs to be in project root folder");
				e.printStackTrace();
			} catch (IOException e) {
				System.out.println("Stop URI file needs to be line separated URIs");
				e.printStackTrace();
			} finally {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return blacklistedEnts;
	}
	
	@SuppressWarnings("unused")
	/*
	 * not used for now. An "inverse category frequency" approach appears
	 * more promising because it might indeed have (a little) semantic meaning.
	 * For example because people lived in the same era, died at a similar time,..
	 */
	private void addBirthsDeathsByYear() {
		
		for (int i = 1500; i < 2016; i++) {
			blacklistedEnts.add("http://dbpedia.org/resource/Category:" + i + "_births");
			blacklistedEnts.add("http://dbpedia.org/resource/Category:" + i + "_deaths");
		}
	}
	
	private StopURIHandler() {
		
	}

	public static StopURIHandler getInstance() {
	
		if (instance == null)
			instance = new StopURIHandler();
		return instance;
	}

}
