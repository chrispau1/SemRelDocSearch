package de.paul.docs.impl;

import de.paul.docs.AnnotatedDoc;

public class DocPair<x extends AnnotatedDoc> {

	private x doc1;
	private x doc2;

	public DocPair(x doc12, x doc22) {

		this.doc1 = doc12;
		this.doc2 = doc22;
	}

	public x getDoc2() {
		return doc2;
	}

	public void setDoc2(x doc2) {
		this.doc2 = doc2;
	}

	public x getDoc1() {
		return doc1;
	}

	public void setDoc1(x doc1) {
		this.doc1 = doc1;
	}

}
