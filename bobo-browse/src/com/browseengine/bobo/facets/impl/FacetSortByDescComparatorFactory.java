package com.browseengine.bobo.facets.impl;

import java.util.Comparator;

import com.browseengine.bobo.api.BrowseFacet;
import com.browseengine.bobo.api.ComparatorFactory;
import com.browseengine.bobo.api.FieldValueAccessor;
import com.browseengine.bobo.api.SortByBrowseFacet;
import org.apache.log4j.Logger;

public class FacetSortByDescComparatorFactory implements ComparatorFactory {
    private static Logger logger = Logger.getLogger(FacetSortByDescComparatorFactory.class);


	public Comparator<Integer> newComparator(final FieldValueAccessor valueList,
			final int[] counts) {
        return new Comparator<Integer>(){
            public int compare(Integer o1, Integer o2) {
                logger.debug("compare ");
                logger.debug("== " + valueList);
                logger.debug("== " + o2.toString() + " <> " + o1.toString());
                logger.debug("== " + valueList.getFormatedValue(o2));
                logger.debug("== " + valueList.getFormatedValue(o1));
                logger.debug("== " + valueList.getFormatedValue(o2).compareTo(valueList.getFormatedValue(o1)));
                return valueList.getFormatedValue(o2).compareTo(valueList.getFormatedValue(o1));
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