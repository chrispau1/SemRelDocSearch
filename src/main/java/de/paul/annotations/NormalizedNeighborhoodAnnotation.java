package de.paul.annotations;

import java.util.HashSet;
import java.util.Set;

import de.paul.dbpedia.DBPediaHandler;

public class NormalizedNeighborhoodAnnotation extends NeighborhoodAnnotation {

	public NormalizedNeighborhoodAnnotation(String ent, double weight,
			DBPediaHandler dbpHandler, int expansionRadius) {
		super(ent, weight, dbpHandler, expansionRadius);
	}

	public NormalizedNeighborhoodAnnotation(String ent, double weight,
			Set<Annotatable> neighbors) {
		super(ent, weight, neighbors);
	}

	public NormalizedNeighborhoodAnnotation(NeighborhoodAnnotation other) {

		super(other);
	}

	protected Set<Annotatable> postProcessLayer(int dist,
			Set<Annotatable> oneHopNeighbors) {
		HashSet<Annotatable> res = new HashSet<Annotatable>();
		// compute layer weight sum
		double sumWeights = 0.0;
		for (Annotatable ann : oneHopNeighbors) {
			sumWeights += ann.getWeight();
		}
		// now normalize entity weights
		for (Annotatable ann : oneHopNeighbors) {
			Annotatable newAnn = normalizeEntity(ann, dist, sumWeights);
			res.add(newAnn);
		}
		return res;
	}

	private WeightedAnnotation normalizeEntity(Annotatable ann, int dist,
			double sumWeights) {
		return new WeightedAnnotation(ann.getEntity(), ann.getWeight()
				* this.getWeight() / (sumWeights * Math.pow(2, dist)));
	}
}
