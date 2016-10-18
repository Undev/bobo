package com.browseengine.bobo.facets.impl;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.browseengine.bobo.api.BoboIndexReader;
import com.browseengine.bobo.api.BrowseSelection;
import com.browseengine.bobo.api.ComparatorFactory;
import com.browseengine.bobo.api.FacetAccessible;
import com.browseengine.bobo.api.FacetSpec;
import com.browseengine.bobo.facets.FacetCountCollector;
import com.browseengine.bobo.facets.FacetCountCollectorSource;
import com.browseengine.bobo.facets.FacetHandler;
import com.browseengine.bobo.facets.SortByCombinedFacetAccessible;
import com.browseengine.bobo.facets.data.FacetDataCache;

public class MultiSortByFacetHandler extends SimpleFacetHandler
{

  private final Collection<String> facetsToSortBy;
  private final Map<String, SimpleFacetHandler> facetHandlers;

  public MultiSortByFacetHandler(String facetToReturn, Collection<String> facetsToSortBy)
  {
    super(facetToReturn);
    this.facetsToSortBy = facetsToSortBy;
    for (String facet : facetsToSortBy)
    {
      getDependsOn().add(facet);
    }
    facetHandlers = new HashMap<String, SimpleFacetHandler>(facetsToSortBy.size());
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  public FacetDataCache load(BoboIndexReader reader) throws IOException
  {
    FacetDataCache dataCache = new FacetDataCache();
    dataCache.load(_indexFieldName, reader, _termListFactory, true);

    for (String facet : facetsToSortBy)
    {
      FacetHandler<?> facetHandler = reader.getFacetHandler(facet);
      if (facetHandler == null || !(facetHandler instanceof SimpleFacetHandler))
      {
        throw new IllegalStateException("only simple facet handlers supported");
      }
      facetHandlers.put(facet, (SimpleFacetHandler) facetHandler);
    }

    return dataCache;
  };

  @Override
  public FacetAccessible merge(FacetSpec fspec, List<FacetAccessible> facetList)
  {
    return new SortByCombinedFacetAccessible(fspec, facetList);
  }

  @Override
  public FacetCountCollectorSource getFacetCountCollectorSource(final BrowseSelection sel, final FacetSpec fspec)
  {
    return new FacetCountCollectorSource()
    {

      @Override
      public FacetCountCollector getFacetCountCollector(BoboIndexReader reader, int docBase)
      {
        final DefaultFacetCountCollector _subcollector;
        ComparatorFactory comparatorFactory = fspec.getCustomComparatorFactory();
        if (comparatorFactory != null && comparatorFactory instanceof MultiSortByInfo)
        {
          _subcollector = (DefaultFacetCountCollector) facetHandlers
              .get(((MultiSortByInfo) comparatorFactory).getFacetToSort())
              .getFacetCountCollectorSource(sel, fspec).getFacetCountCollector(reader, docBase);
        } else
        {
          _subcollector = null;
        }

        FacetDataCache dataCache = MultiSortByFacetHandler.this.getFacetData(reader);
        return new SortbyFacetCountCollector(_name, dataCache, docBase, sel, fspec, _subcollector);
      }
    };
  }

}
