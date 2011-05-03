package com.browseengine.bobo.facets;

import it.unimi.dsi.fastutil.ints.Int2IntMap;

import com.browseengine.bobo.api.FacetIterator;
import com.browseengine.bobo.facets.data.FacetDataCache;
import com.browseengine.bobo.facets.data.TermValueList;
import com.browseengine.bobo.util.BigSegmentedArray;

public class SortByFacetIterator extends FacetIterator
{

  public Comparable sortBy;

  private TermValueList _valList;
  private Int2IntMap indexesToDocids;
  private TermValueList _sortByValList;
  private BigSegmentedArray _sortByOrderArray;
  private int[] _count;
  private int _countlength;
  private int _index;
  private int _lastIndex;

  public SortByFacetIterator(FacetDataCache dataCache, FacetDataCache sortByDataCache, int[] counts, int countsLength,
      boolean zeroBased)
  {
    _valList = dataCache.valArray;
    indexesToDocids = dataCache.indexesToDocids;
    _sortByValList = sortByDataCache.valArray;
    _sortByOrderArray = sortByDataCache.orderArray;
    _count = counts;
    _countlength = countsLength;
    _index = -1;
    _lastIndex = _countlength - 1;
    if (!zeroBased)
    {
      _index++;
    }
    
    sortBy = null;
    facet = null;
    count = 0;
  }

  @Override
  public boolean hasNext()
  {
    return (_index < _lastIndex);
  }

  @Override
  public void remove()
  {
    throw new UnsupportedOperationException("remove() method not supported for Facet Iterators");
  }

  @Override
  public Comparable next()
  {
    _index++;
    facet = (Comparable) _valList.getRawValue(_index);
    count = _count[_index];
    sortBy = _sortByValList.get(_sortByOrderArray.get(indexesToDocids.get(_index)));
    return format(facet);
  }

  @Override
  public Comparable next(int minHits)
  {
    while (++_index < _countlength)
    {
      if (_count[_index] >= minHits)
      {
        facet = (Comparable) _valList.getRawValue(_index);
        count = _count[_index];
        sortBy = _sortByValList.get(_sortByOrderArray.get(indexesToDocids.get(_index)));
        return format(facet);
      }
    }
    facet = null;
    sortBy = null;
    count = 0;
    return null;
  }

  @Override
  public String format(Object val)
  {
    return _valList.format(val);
  }

}
