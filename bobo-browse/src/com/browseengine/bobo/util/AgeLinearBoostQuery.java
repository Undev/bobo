package com.browseengine.bobo.util;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.function.CustomScoreProvider;
import org.apache.lucene.search.function.CustomScoreQuery;

import com.browseengine.bobo.api.BoboIndexReader;
import com.browseengine.bobo.facets.data.FacetDataCache;
import com.browseengine.bobo.facets.data.TermLongList;
import com.browseengine.bobo.facets.impl.SimpleFacetHandler;

public class AgeLinearBoostQuery extends CustomScoreQuery {

	private static final long serialVersionUID = 1L;

	private final long reference;
	private final long maxage;
	private final float multiplier;
	private final String fieldname;

	private class CustomBooster extends CustomScoreProvider {

		private BigSegmentedArray beginningOrderArray;
		private TermLongList beginningValList;
		private String[] timestamps;

		public CustomBooster(IndexReader reader) throws IOException {
			super(reader);
			if (reader instanceof BoboIndexReader) {
				BoboIndexReader boboIndexReader = (BoboIndexReader) reader;

				SimpleFacetHandler beginningFacetHandler = (SimpleFacetHandler) boboIndexReader.getFacetHandler(fieldname);
				@SuppressWarnings("rawtypes")
				FacetDataCache facetData = beginningFacetHandler.getFacetData(boboIndexReader);
				beginningOrderArray = facetData.orderArray;
				beginningValList = (TermLongList) facetData.valArray;
			} else {
				timestamps = FieldCache.DEFAULT.getStrings(reader, fieldname);
			}
		}

		@Override
		public float customScore(int doc, float subQueryScore, float valSrcScore) throws IOException {
			float boost = boost(doc);
			return subQueryScore * boost;
		}

		private float boost(int doc) {
			long age = age(doc);
			if (age == 0) {
				return (float) (1.0 + multiplier);
			}

			if (age >= maxage) {
				return 1.0f;
			}

			return (float) (1.0 + (multiplier * (maxage - age) / maxage));
		}

		private long age(int doc) {
			long timestamp = getTimestamp(doc);
			return Math.abs(reference - timestamp);
		}

		private long getTimestamp(int doc) {
			if (beginningOrderArray != null) {
				long timestamp = beginningValList.getPrimitiveValue(beginningOrderArray.get(doc));
				return timestamp;
			}

			return Long.parseLong(timestamps[doc]);
		}

		private Explanation buildExplanation(int doc) {
			long age = age(doc);

			if (age == 0) {
				return new Explanation((float) (1.0 + multiplier), "age is 0, so this is just: 1.0 + multiplier:" + multiplier);
			}

			if (age >= maxage) {
				return new Explanation(1.0f, "age >= maxage:" + maxage + ", so no boosting");
			}

			return new Explanation((float) (1.0 + (multiplier * (maxage - age) / maxage)), "(1.0 + (multiplier:" + multiplier
					+ " * (maxage:" + maxage + " - age:" + age + ") / maxage:" + maxage + ")");
		}

		private Explanation explain(int doc, Explanation subQueryExpl) {
			Explanation exp = new Explanation(subQueryExpl.getValue() * boost(doc), "linear age boost, product of:");
			exp.addDetail(subQueryExpl);

			exp.addDetail(buildExplanation(doc));
			return exp;
		}

		@Override
		public Explanation customExplain(int doc, Explanation subQueryExpl, Explanation valSrcExpl) throws IOException {
			return explain(doc, subQueryExpl);
		}

		@Override
		public Explanation customExplain(int doc, Explanation subQueryExpl, Explanation[] valSrcExpls) throws IOException {
			return explain(doc, subQueryExpl);
		}

	}

	public AgeLinearBoostQuery(Query query, long reference, long maxage, float multiplier, String fieldname) {
		super(query);
		this.reference = reference;
		this.maxage = maxage;
		this.multiplier = multiplier;
		this.fieldname = fieldname;
	}

	@Override
	protected CustomScoreProvider getCustomScoreProvider(IndexReader reader) throws IOException {
		return new CustomBooster(reader);
	}

}
