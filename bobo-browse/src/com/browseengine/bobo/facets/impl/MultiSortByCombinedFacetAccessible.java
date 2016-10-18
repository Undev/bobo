package com.browseengine.bobo.facets.impl;

import java.util.List;

import com.browseengine.bobo.api.BrowseFacet;
import com.browseengine.bobo.api.FacetAccessible;
import com.browseengine.bobo.api.FacetIterator;
import com.browseengine.bobo.api.FacetSpec;
import com.browseengine.bobo.facets.CombinedFacetAccessible;

public class MultiSortByCombinedFacetAccessible extends CombinedFacetAccessible
{

  private final FacetSpec fspec;
  private final List<FacetAccessible> list;

  public MultiSortByCombinedFacetAccessible(FacetSpec fspec, List<FacetAccessible> list)
  {
    super(fspec, list);
    this.fspec = fspec;
    this.list = list;
  }

  @Override
  public List<BrowseFacet> getFacets()
  {
    return null;
  }

  @Override
  public FacetIterator iterator()
  {
    throw new UnsupportedOperationException("Should not invoke without sortBy parameter");
  }

}
