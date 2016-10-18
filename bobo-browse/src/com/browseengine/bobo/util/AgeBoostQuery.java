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

public class AgeBoostQuery extends CustomScoreQuery {

	private class CustomBooster extends CustomScoreProvider {

		private BigSegmentedArray beginningOrderArray;
		private TermLongList beginningValList;
		private String[] timestamps;

		@SuppressWarnings("rawtypes")
		public CustomBooster(IndexReader reader) throws IOException {
			super(reader);
			if (reader instanceof BoboIndexReader) {
				BoboIndexReader boboIndexReader = (BoboIndexReader) reader;
				
				SimpleFacetHandler beginningFacetHandler = (SimpleFacetHandler) boboIndexReader
						.getFacetHandler(fieldname);
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
			return (float) (1.0 + (multiplier / age));
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
			return new Explanation(boost(doc), "(1.0 + (multiplier:" + multiplier + " / age:" + age + ")");
		}

		private Explanation explain(int doc, Explanation subQueryExpl) {
			Explanation exp = new Explanation(subQueryExpl.getValue() * boost(doc), "simple age boost, product of:");
			exp.addDetail(subQueryExpl);
			exp.addDetail(buildExplanation(doc));
			return exp;
		}

		@Override
		public Explanation customExplain(int doc, Explanation subQueryExpl, Explanation valSrcExpl)
				throws IOException {
			return explain(doc, subQueryExpl);
		}

		@Override
		public Explanation customExplain(int doc, Explanation subQueryExpl, Explanation[] valSrcExpls)
				throws IOException {
			return explain(doc, subQueryExpl);
		}

	}

	private static final long serialVersionUID = 1L;
	private final float multiplier;
	private final long reference;
	private final String fieldname;

	public AgeBoostQuery(Query query, long reference, float multiplier, String fieldname) {
		super(query);
		this.reference = reference;
		this.multiplier = multiplier;
		this.fieldname = fieldname;
	}

	public CustomScoreProvider getCustomScoreProvider(IndexReader reader) throws IOException {
		return new CustomBooster(reader);
	}
}