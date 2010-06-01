package com.browseengine.bobo.facets.impl;

import java.util.Comparator;

import com.browseengine.bobo.api.BrowseFacet;
import com.browseengine.bobo.api.ComparatorFactory;
import com.browseengine.bobo.api.FieldValueAccessor;
import com.browseengine.bobo.api.SortByBrowseFacet;
import org.apache.log4j.Logger;
import com.browseengine.bobo.util.IntBoundedPriorityQueue.IntComparator;

public class FacetSortByDescComparatorFactory implements ComparatorFactory {
    private static Logger logger = Logger.getLogger(FacetSortByDescComparatorFactory.class);


	public IntComparator newComparator(final FieldValueAccessor valueList,
			final int[] counts) {
        return new IntComparator(){
            public int compare(Integer o1, Integer o2) {
                return valueList.getFormatedValue(o2).compareTo(valueList.getFormatedValue(o1));
            }

            public int compare(int f1, int f2) {
                return valueList.getFormatedValue(f2).compareTo(valueList.getFormatedValue(f1));
            }
        };
	}

	public static Comparator<BrowseFacet> FACET_SORTBY_DESC_COMPARATOR = new Comparator<BrowseFacet>(){
        public int compare(BrowseFacet o1, BrowseFacet o2) {
            logger.debug("compare browse facets");
            logger.debug("^^ " + o2.toString() + " <> " + o1.toString());
            if (o1 instanceof SortByBrowseFacet &&
                o2 instanceof SortByBrowseFacet){
                return ((SortByBrowseFacet)o2).getSortByValue().compareTo(
                        ((SortByBrowseFacet)o1).getSortByValue());
            }
            return o2.getValue().compareTo(o1.getValue());
        }
	};

	public Comparator<BrowseFacet> newComparator() {
		return FACET_SORTBY_DESC_COMPARATOR;
	}
}