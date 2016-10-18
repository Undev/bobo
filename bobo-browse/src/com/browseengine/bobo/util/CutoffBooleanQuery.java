package com.browseengine.bobo.util;

import java.io.IOException;
import java.util.Set;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreCachingWrappingScorer;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.Similarity;
import org.apache.lucene.search.Weight;

public class CutoffBooleanQuery extends Query {

	private static final long serialVersionUID = -8251827396290700138L;
	private final float maxScore;
	private final float threshold;
	private final Query query;

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

	public CutoffBooleanQuery(Query query, float maxScore, float threshold) {
		this.query = query;
		this.maxScore = maxScore;
		this.threshold = threshold;
	}

	@Override
	public void setBoost(float b) {
		query.setBoost(b);
	}

	@Override
	public float getBoost() {
		return query.getBoost();
	}

	@Override
	public String toString(String field) {
		return query.toString(field);
	}

	@Override
	public String toString() {
		return query.toString();
	}

	@Override
	public Weight createWeight(Searcher searcher) throws IOException {
		return wrap(query.createWeight(searcher));
	}

	private CutoffWeightWrapper wrap(Weight weight) {
		return new CutoffWeightWrapper(weight);
	}

	@Override
	public Query rewrite(IndexReader reader) throws IOException {
		Query rewritten = query.rewrite(reader);
		if (rewritten == query) {
			return this;
		}
		return new CutoffBooleanQuery(rewritten, maxScore, threshold);
	}

	@Override
	public Query combine(Query[] queries) {
		Query[] extracted = new Query[queries.length];
		for (int i = 0; i < queries.length; i++) {
			Query query = queries[i];
			if (query instanceof CutoffBooleanQuery) {
				CutoffBooleanQuery wrapped = (CutoffBooleanQuery) query;
				extracted[i] = wrapped.query;
			} else {
				extracted[i] = query;
			}

		}
		return new CutoffBooleanQuery(query.combine(extracted), maxScore, threshold);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void extractTerms(Set terms) {
		query.extractTerms(terms);
	}

	@Override
	public Similarity getSimilarity(Searcher searcher) {
		return query.getSimilarity(searcher);
	}

	@Override
	public Object clone() {
		return new CutoffBooleanQuery((Query) query.clone(), maxScore, threshold);
	}

	@Override
	public int hashCode() {
		return query.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return query.equals(obj);
	}
}
