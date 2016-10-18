package com.browseengine.bobo.facets.impl;

public class MultiSortByDescComparatorFactory extends FacetSortByDescComparatorFactory implements MultiSortByInfo
{

  private final String facetToSort;

  public MultiSortByDescComparatorFactory(String facetToSort)
  {
    this.facetToSort = facetToSort;
  }

  public String getFacetToSort()
  {
    return facetToSort;
  }

}
