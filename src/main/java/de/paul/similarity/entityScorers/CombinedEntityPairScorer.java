package de.paul.similarity.entityScorers;

import de.paul.annotations.SemanticallyExpandedAnnotation;

public class CombinedEntityPairScorer implements
		ScorableEntityPair<SemanticallyExpandedAnnotation> {

	public enum CombineMode {
		MAX, PLUS, MULT;
	};

	private CombineMode combineMode = CombineMode.PLUS;
	private SemanticallyExpandedAnnotation leftAnn;
	private SemanticallyExpandedAnnotation rightAnn;
	private TransversalEntityPairScorer transScorer;
	private TaxonomicEntityPairScorer taxonScorer;

	// mean and variance for the two combined measures
	// RADIUS: 1
	// private final double TRANS_MEAN = 0.025676365005277587;
	// private final double TAXON_MEAN = 0.1406030306728683;
	// private final double TRANS_VAR = 0.001767484452580766;
	// private final double TAXON_VAR = 0.005312166344853058;

	// RADIUS: 2
	private final double TRANS_MEAN = 0.028002799231326602;
	private final double TAXON_MEAN = 0.1406030306728683;
	private final double TRANS_VAR = 0.0023979329049633132;
	private final double TAXON_VAR = 0.005312166344853058;

	// RADIUS: 3
	// private final double TRANS_MEAN = 0.06531360781427822;
	// private final double TAXON_MEAN = 0.1406030306728683;
	// private final double TRANS_VAR = 0.01441234226547803;
	// private final double TAXON_VAR = 0.005312166344853058;

	public CombinedEntityPairScorer(SemanticallyExpandedAnnotation ann1,
			SemanticallyExpandedAnnotation ann2, CombineMode combineMode) {
		this.leftAnn = ann1.copy();
		this.rightAnn = ann2.copy();
		this.transScorer = ann1.getNeighbors().createEdge(ann2.getNeighbors());
		this.taxonScorer = ann1.getAncestors().createEdge(ann2.getAncestors());
		this.combineMode = combineMode;
	}

	public void setLeft() {

		if (transScorer != null)
			transScorer.setLeft();
		if (taxonScorer != null)
			taxonScorer.setLeft();
	}

	public void setRight() {

		if (transScorer != null)
			transScorer.setRight();
		if (taxonScorer != null)
			taxonScorer.setRight();
	}

	public String getEntityName() {

		return leftAnn.getEntity();
	}

	public String getEntity2Name() {

		return rightAnn.getEntity();
	}

	public double getEntityWeight() {

		return leftAnn.getWeight();
	}

	public double getEntity2Weight() {

		return rightAnn.getWeight();
	}

	public SemanticallyExpandedAnnotation getAnnotation() {

		return leftAnn;
	}

	public SemanticallyExpandedAnnotation getAnnotation2() {

		return rightAnn;
	}

	public double leftScore() {

		// scorers can be null, which means that there is no connection (no lca,
		// no path respectively)
		double taxonScore = 0;
		if (taxonScorer != null)
			taxonScore = taxonScorer.leftScore();
		double transScore = 0;
		if (transScorer != null)
			transScore = transScorer.leftScore();
		/*
		 * does not normalize, as it changes zeroes into non-zeroes! However, if
		 * one of the two scores is zero, it must never be chosen.
		 */
		return combine(taxonScore, transScore);
		// normalizeScore(transScore, TRANS_MEAN, TAXON_MEAN, TRANS_VAR,
		// TAXON_VAR));
	}

	public double rightScore() {
		// scorers can be null, which means that there is no connection (no lca,
		// no path respectively)
		double taxonScore = 0;
		if (taxonScorer != null)
			taxonScore = taxonScorer.rightScore();
		double transScore = 0;
		if (transScorer != null)
			transScore = transScorer.rightScore();
		/*
		 * does not normalize, as it changes zeroes into non-zeroes! However, if
		 * one of the two scores is zero, it must never be chosen.
		 */
		return combine(taxonScore, transScore);
		// normalizeScore(transScore, TRANS_MEAN, TAXON_MEAN, TRANS_VAR,
		// TAXON_VAR));
	}

	public double score() {

		// scorers can be null, which means that there is no connection (no lca,
		// no path respectively)
		double taxonScore = 0;
		if (taxonScorer != null)
			taxonScore = taxonScorer.score();
		double transScore = 0;
		if (transScorer != null)
			transScore = transScorer.score();
		/*
		 * count stuff
		 */
		// taxonSum += Math.pow(taxonScore - TAXON_MEAN, 2);
		// transSum += Math.pow(transScore - TRANS_MEAN, 2);
		// valCtr++;
		return combine(
				taxonScore,
				normalizeScore(transScore, TRANS_MEAN, TAXON_MEAN, TRANS_VAR,
						TAXON_VAR));
	}

	private double normalizeScore(double score, double itsMean,
			double normMean, double itsVar, double normVar) {

		if (Math.abs(score) > 0.00001) {
			double distToMean = score - itsMean;
			// double meanNormScore = normMean + distToMean;
			double itsVarNormed = distToMean * distToMean * normVar / itsVar;
			double meanNormScore = normMean + Math.signum(distToMean)
					* Math.sqrt(itsVarNormed);
			return meanNormScore;
		} else
			return score;
	}

	/*
	 * Combines scores from the two embedded scorers.
	 */
	private double combine(double taxonScore, double transScore) {

		double res = -1;
		switch (combineMode) {
		case MAX:
			res = Math.max(taxonScore, transScore);
			break;
		case PLUS:
			res = taxonScore + transScore;
			break;
		case MULT:
			res = taxonScore * transScore;
			break;
		default:
			break;
		}
		return res;
	}

}
