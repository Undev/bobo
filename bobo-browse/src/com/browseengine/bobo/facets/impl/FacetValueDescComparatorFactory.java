package com.browseengine.bobo.facets.impl;

import java.util.Comparator;

import com.browseengine.bobo.api.BrowseFacet;
import com.browseengine.bobo.api.ComparatorFactory;
import com.browseengine.bobo.api.FieldValueAccessor;
import org.apache.log4j.Logger;

public class FacetValueDescComparatorFactory implements ComparatorFactory {
	public Comparator<Integer> newComparator(FieldValueAccessor valueList,
			final int[] counts) {
        return new Comparator<Integer>(){
            public int compare(Integer o1, Integer o2) {
                return o2-o1;
            }
        };
	}

	public static Comparator<BrowseFacet> FACET_VALUE_DESC_COMPARATOR = new Comparator<BrowseFacet>(){
		public int compare(BrowseFacet f1, BrowseFacet f2) {
			return f2.getValue().compareTo(f1.getValue());
		}
	};

	public Comparator<BrowseFacet> newComparator() {
		return FACET_VALUE_DESC_COMPARATOR;
	}
}