package com.browseengine.bobo.facets.impl;

import java.util.Comparator;

import com.browseengine.bobo.api.BrowseFacet;
import com.browseengine.bobo.api.ComparatorFactory;
import com.browseengine.bobo.api.FieldValueAccessor;

public class FacetHitcountAscComparatorFactory implements ComparatorFactory {
	public Comparator<Integer> newComparator(FieldValueAccessor valueList,
			final int[] counts) {
		return new Comparator<Integer>(){

			public int compare(Integer f1, Integer f2) {
				int val = counts[f2] - counts[f1];
				if (val==0)
		        {
		            val=f1-f2;
		        }
		        return val;
			}

		};
	}

	public static Comparator<BrowseFacet> FACET_HITS_ASC_COMPARATOR = new Comparator<BrowseFacet>(){

		public int compare(BrowseFacet f1, BrowseFacet f2) {
			int val = f1.getHitCount() - f2.getHitCount();
			if (val==0)
	        {
	            val=f2.getValue().compareTo(f1.getValue());
	        }
	        return val;
		}
	};

	public Comparator<BrowseFacet> newComparator() {
		return FACET_HITS_ASC_COMPARATOR;
	}
}