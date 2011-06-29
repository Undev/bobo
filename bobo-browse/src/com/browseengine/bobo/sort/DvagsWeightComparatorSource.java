package com.browseengine.bobo.sort;

import it.unimi.dsi.fastutil.ints.Int2DoubleLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleRBTreeMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.ScoreDoc;

import com.browseengine.bobo.api.BoboIndexReader;
import com.browseengine.bobo.facets.data.FacetDataCache;
import com.browseengine.bobo.facets.data.MultiValueFacetDataCache;
import com.browseengine.bobo.facets.data.TermIntList;
import com.browseengine.bobo.facets.impl.MultiValueFacetHandler;
import com.browseengine.bobo.facets.impl.SimpleFacetHandler;
import com.browseengine.bobo.util.BigSegmentedArray;

@SuppressWarnings("rawtypes")
public class DvagsWeightComparatorSource extends DocComparatorSource {

	private final static Log log = LogFactory.getLog(DvagsWeightComparatorSource.class);

	private final Int2DoubleMap dvag_weight;

	private final boolean verbose;

	// this is only for verbose answer
	public Int2DoubleMap episode2weight;
	public Int2ObjectMap<Int2DoubleMap> episode2dvags;

	@SuppressWarnings("unchecked")
	public DvagsWeightComparatorSource(Map<Integer, Double> dvag_weight, String formatString, boolean verbose) {
		DecimalFormat formatter = new DecimalFormat(formatString);

		this.dvag_weight = new Int2DoubleRBTreeMap();

		for (Entry<Integer, Double> entry : dvag_weight.entrySet()) {
			// double doubleValue = entry.getValue().doubleValue();
			// String formattedId = formatter.format(entry.getKey());
			this.dvag_weight.put(entry.getKey().intValue(), entry.getValue().doubleValue());
		}

		this.verbose = verbose;
		if (verbose) {
			episode2weight = new Int2DoubleLinkedOpenHashMap();
			episode2dvags = new Int2ObjectOpenHashMap<Int2DoubleMap>();
		}
	}

	@Override
	public DocComparator getComparator(IndexReader reader, final int docbase) throws IOException {
		if (!(reader instanceof BoboIndexReader))
			throw new IllegalStateException("reader must be instance of " + BoboIndexReader.class);
		BoboIndexReader boboReader = (BoboIndexReader) reader;
		MultiValueFacetHandler facetHandler = (MultiValueFacetHandler) boboReader.getFacetHandler("dvag_ids");
		
		
		final MultiValueFacetDataCache dataCache = facetHandler.getFacetData(boboReader);
		final TermIntList intArray = (TermIntList) dataCache.valArray;
		
		BigSegmentedArray idOrderArray = null;
		TermIntList idValList = null;
		if (verbose) {
			SimpleFacetHandler idFacetHandler = (SimpleFacetHandler) boboReader.getFacetHandler("id");
			FacetDataCache idFacetData = idFacetHandler.getFacetData(boboReader);

			idOrderArray = idFacetData.orderArray;
			idValList = (TermIntList) idFacetData.valArray;
		}
		
		return new DvagsWeightDocComparator(dataCache, intArray, idOrderArray, idValList, verbose, episode2weight,
				episode2dvags, dvag_weight);
	}

	private static class DvagsWeightDocComparator extends DocComparator {
		private final MultiValueFacetDataCache dataCache;
		private final TermIntList intArray;
		private final BigSegmentedArray idOrderArray;
		private final TermIntList idValList;
		private final boolean verbose;
		private final Int2DoubleMap episode2weight;
		private final Int2ObjectMap<Int2DoubleMap> episode2dvags;
		private final Int2DoubleMap dvag_weight;
		private final int[] buf = new int[1024 * 100];

		public DvagsWeightDocComparator(MultiValueFacetDataCache dataCache, TermIntList intArray,
				BigSegmentedArray idOrderArray, TermIntList idValList, boolean verbose, Int2DoubleMap episode2weight,
				Int2ObjectMap<Int2DoubleMap> episode2dvags, Int2DoubleMap dvag_weight) {
			this.dataCache = dataCache;
			// TODO Auto-generated constructor stub
			this.intArray = intArray;
			this.idOrderArray = idOrderArray;
			this.idValList = idValList;
			this.verbose = verbose;
			this.episode2dvags = episode2dvags;
			this.episode2weight = episode2weight;
			this.dvag_weight = dvag_weight;
		}

		@Override
		public int compare(ScoreDoc doc1, ScoreDoc doc2) {
			double score1 = calcTotalWeight(doc1.doc);
			double score2 = calcTotalWeight(doc2.doc);
			if (score1 < score2) {
				return -1;
			} else if (score1 > score2) {
				return 1;
			} else {
				return 0;
			}
		}

		@Override
		public Comparable value(ScoreDoc doc) {
			return new Double(calcTotalWeight(doc.doc));
		}

	  /*
		 * private Double calcTotalWeight(int doc) { String[] dvags =
		 * dataCache._nestedArray.getTranslatedData(doc, dataCache.valArray); double
		 * total_weight = 0.0; for (String dvag : dvags) { double weight =
		 * dvag_weight.getDouble(dvag); if (weight > 0.0) { total_weight += weight;
		 * } } return total_weight; }
		 */
		private double calcTotalWeight(int doc) {

			int episode_id = -1;
			if (verbose) {
				int index = idOrderArray.get(doc);
				episode_id = idValList.getPrimitiveValue(index);
			}

			double total_weight = 0.0;

			int num = dataCache._nestedArray.getData(doc, buf);
			for (int i = 0; i < num; i++) {
				int dvag_id = intArray.getPrimitiveValue(buf[i]);

				double weight = dvag_weight.get(dvag_id);
				if (weight > 0.0) {
					total_weight += weight;

					if (verbose) {
						store_dvag_info(episode_id, dvag_id, weight);
					}
				}
			}

			if (verbose) {
				store_total_weight(episode_id, total_weight);
			}
			return total_weight;
		}

		private void store_total_weight(int episode_id, double total_weight) {
			episode2weight.put(episode_id, total_weight);
		}

		private void store_dvag_info(int episode_id, int dvag_id, double weight) {
			Int2DoubleMap dvags = episode2dvags.get(episode_id);
			if (dvags == null) {
				dvags = new Int2DoubleLinkedOpenHashMap();
				episode2dvags.put(episode_id, dvags);
			}

			dvags.put(dvag_id, weight);
		}

	}

}
