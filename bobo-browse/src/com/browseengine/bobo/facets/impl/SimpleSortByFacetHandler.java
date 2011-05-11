package com.browseengine.bobo.facets.impl;

import java.io.IOException;
import java.util.List;


import com.browseengine.bobo.api.BoboIndexReader;
import com.browseengine.bobo.api.BrowseSelection;
import com.browseengine.bobo.api.FacetAccessible;
import com.browseengine.bobo.api.FacetSpec;
import com.browseengine.bobo.facets.FacetCountCollector;
import com.browseengine.bobo.facets.FacetCountCollectorSource;
import com.browseengine.bobo.facets.FacetHandler;
import com.browseengine.bobo.facets.SortByCombinedFacetAccessible;
import com.browseengine.bobo.facets.data.FacetDataCache;

public class SimpleSortByFacetHandler extends SimpleFacetHandler {
	private final String _sortByField;
	private SimpleFacetHandler _sortByFacetHandler;

	public SimpleSortByFacetHandler(String name, String sortByField) {
		super(name);
        getDependsOn().add(sortByField);
		_sortByField = sortByField;
		_sortByFacetHandler = null;
	}

    @Override
    public FacetDataCache load(BoboIndexReader reader) throws IOException {
        FacetDataCache dataCache = new FacetDataCache();
        dataCache.load(_indexFieldName, reader, _termListFactory, true);
        FacetHandler<?> handler = reader.getFacetHandler(_sortByField);
        if (handler==null || !(handler instanceof SimpleFacetHandler)){
            throw new IllegalStateException("only simple facet handlers supported");
        }
        _sortByFacetHandler = (SimpleFacetHandler)handler;
        return dataCache;
    }

    @Override
    public FacetCountCollectorSource getFacetCountCollectorSource(final BrowseSelection sel,final FacetSpec fspec) {
        return new FacetCountCollectorSource(){
            @Override
            public FacetCountCollector getFacetCountCollector(
                    BoboIndexReader reader, int docBase) {
                DefaultFacetCountCollector sortByCollector = (DefaultFacetCountCollector)
                        (_sortByFacetHandler.getFacetCountCollectorSource(sel, fspec).
                                getFacetCountCollector(reader, docBase));
                FacetDataCache dataCache = SimpleSortByFacetHandler.this.getFacetData(reader);
                return new SortbyFacetCountCollector(_name, dataCache, docBase, sel, fspec, sortByCollector);
            }

        };
    }

  @Override
  public FacetAccessible merge(FacetSpec fspec, List<FacetAccessible> facetList)
  {
    return new SortByCombinedFacetAccessible(fspec, facetList);
  }
}