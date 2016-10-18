package com.browseengine.bobo.facets.impl;


public class MultiSortByComparatorFactory extends FacetSortByComparatorFactory implements MultiSortByInfo
{

  private final String facetToSort;

  public MultiSortByComparatorFactory(String facetToSort)
  {
    this.facetToSort = facetToSort;
  }

  public String getFacetToSort()
  {
    return facetToSort;
  }

}
