package de.paul.docs.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import de.paul.annotations.Annotatable;
import de.paul.dbpedia.DBPediaHandler;
import de.paul.docs.AnnotatedDoc;

/**
 * Represents a document whose annotations get expanded transversally, not per
 * annotation, but document-wide. Meaning that all entities neighboring its
 * annotations get thrown together in a set and get weighted in the process,
 * depending on distance to original annotations and number and weight of
 * annotations that are connected to an entity.
 * 
 * @author Chris
 *
 */
public class TransversalDocWideExpandedDoc extends AnnotatedDoc {

	private Map<Integer, Set<Annotatable>> entitiesAtDistance;
	private DBPediaHandler dbpediaHandler;

	public TransversalDocWideExpandedDoc(AnnotatedDoc doc,
			DBPediaHandler dbpHandler, int expansionRadius) {

		super(doc.getText(), doc.getTitle(), doc.getId());
		this.dbpediaHandler = dbpHandler;
		Collection<Annotatable> plainAnnots = unifyAnnotationsSumScores(doc
				.getAnnotations());
		this.annotations.addAll(plainAnnots);
		// build annotations
		expandAnnotations(expansionRadius);
	}

	public TransversalDocWideExpandedDoc(TransversalDocWideExpandedDoc copy) {

		super(copy.getText(), copy.getTitle(), copy.getId());
		for (Annotatable annot : copy.getAnnotations()) {

			this.annotations.add(annot.copy());
		}
	}

	private void expandAnnotations(int expansionRadius) {

		// find layered neighbors
		this.entitiesAtDistance = new HashMap<Integer, Set<Annotatable>>();
		for (int i = 0; i < expansionRadius; i++) {

			expandEntitiesAt(i);
		}
		// summarize in one map
		// TODO
	}

	/*
	 * Queries DBPedia for transversal neighbors of the entities currently in
	 * annotations (if parameter is 0) or at distance indicated by parameter.
	 * 
	 * Adds them to entities by distance map.
	 */
	private void expandEntitiesAt(int i) {

		Collection<? extends Annotatable> queryEntities = (i == 0) ? this.annotations
				: entitiesAtDistance.get(i - 1);
		Set<Annotatable> newEntities = dbpediaHandler
				.getNeighborsOutEdges(queryEntities);
		entitiesAtDistance.put(i, newEntities);
	}

	@Override
	public AnnotatedDoc copy() {

		return new TransversalDocWideExpandedDoc(this);
	}

}
