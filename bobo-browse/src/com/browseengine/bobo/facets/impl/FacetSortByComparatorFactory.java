package com.browseengine.bobo.facets.impl;

import java.util.Comparator;

import com.browseengine.bobo.api.BrowseFacet;
import com.browseengine.bobo.api.ComparatorFactory;
import com.browseengine.bobo.api.FieldValueAccessor;
import com.browseengine.bobo.api.SortByBrowseFacet;
import com.browseengine.bobo.util.IntBoundedPriorityQueue.IntComparator;

public class FacetSortByComparatorFactory implements ComparatorFactory {

	public IntComparator newComparator(final FieldValueAccessor valueList,
			final int[] counts) {
        return new IntComparator(){

          public int compare(Integer f1, Integer f2) {
              return valueList.getFormatedValue(f1).compareTo(valueList.getFormatedValue(f2));
          }

          // use polymorphism to avoid auto-boxing
          public int compare(int f1, int f2)
          {
              return valueList.getFormatedValue(f1).compareTo(valueList.getFormatedValue(f2));
          }
        };
	}


	public static Comparator<BrowseFacet> FACET_SORTBY_COMPARATOR = new Comparator<BrowseFacet>(){
        public int compare(BrowseFacet o1, BrowseFacet o2) {
            if (o1 instanceof SortByBrowseFacet &&
                o2 instanceof SortByBrowseFacet){
                return ((SortByBrowseFacet)o1).getSortByValue().compareTo(
                        ((SortByBrowseFacet)o2).getSortByValue());
            }
            return o1.getValue().compareTo(o2.getValue());
        }
	};

	public Comparator<BrowseFacet> newComparator() {
		return FACET_SORTBY_COMPARATOR;
	}
}