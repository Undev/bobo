package com.browseengine.bobo.util;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreCachingWrappingScorer;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.Similarity;
import org.apache.lucene.search.Weight;

public class CutoffBooleanQuery extends BooleanQuery {

	private static final long serialVersionUID = -8251827396290700138L;
	private final float maxScore;
	private final float threshold;

	private class CutoffScorerWrapper extends Scorer {

		private final Scorer scorer;

		public CutoffScorerWrapper(Scorer scorer) {
			super(scorer.getSimilarity());
			this.scorer = scorer;
		}

		@Override
		public float score() throws IOException {
			return scorer.score();
		}

		@Override
		public int docID() {
			return scorer.docID();
		}

		@Override
		public int nextDoc() throws IOException {
			return checkAndRoll(scorer.nextDoc());
		}

		private float normilize(float score) {
			return score / maxScore;
		}

		@Override
		public int advance(int target) throws IOException {
			return checkAndRoll(scorer.advance(target));
		}

		private int checkAndRoll(int current) throws IOException {
			int nextDoc = current;
			while (nextDoc != NO_MORE_DOCS && normilize(scorer.score()) < threshold) {
				nextDoc = scorer.nextDoc();
			}
			return nextDoc;
		}

		@Override
		public void score(Collector collector) throws IOException {
			scorer.score(collector);
		}

		@Override
		public Similarity getSimilarity() {
			return scorer.getSimilarity();
		}

	}

	private class CutoffWeightWrapper extends Weight {

		private static final long serialVersionUID = 2315191356656103343L;
		private final Weight weight;

		public CutoffWeightWrapper(Weight weight) {
			this.weight = weight;
		}

		@Override
		public Explanation explain(IndexReader reader, int doc) throws IOException {
			return weight.explain(reader, doc);
		}

		@Override
		public Query getQuery() {
			return weight.getQuery();
		}

		@Override
		public float getValue() {
			return weight.getValue();
		}

		@Override
		public void normalize(float norm) {
			weight.normalize(norm);
		}

		@Override
		public Scorer scorer(IndexReader reader, boolean scoreDocsInOrder, boolean topScorer) throws IOException {
			Scorer scorer = weight.scorer(reader, scoreDocsInOrder, topScorer);
			return new CutoffScorerWrapper(new ScoreCachingWrappingScorer(scorer));
		}

		@Override
		public float sumOfSquaredWeights() throws IOException {
			return weight.sumOfSquaredWeights();
		}

		@Override
		public boolean scoresDocsOutOfOrder() {
			return weight.scoresDocsOutOfOrder();
		}

	}

	public CutoffBooleanQuery(float maxScore, float threshold) {
		this.maxScore = maxScore;
		this.threshold = threshold;
	}

	@Override
	public Weight weight(Searcher searcher) throws IOException {
		Weight weight = super.weight(searcher);
		return new CutoffWeightWrapper(weight);
	}


}
