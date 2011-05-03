package com.browseengine.bobo.facets;

import it.unimi.dsi.fastutil.ints.Int2IntMap;

import org.apache.log4j.Logger;

import com.browseengine.bobo.api.FacetIterator;
import com.browseengine.bobo.facets.data.FacetDataCache;
import com.browseengine.bobo.facets.data.TermValueList;
import com.browseengine.bobo.util.BigSegmentedArray;

public class SortByFacetIterator extends FacetIterator
{

	private static Logger logger = Logger.getLogger(SortByFacetIterator.class);

  public Comparable sortBy;

	private final TermValueList _valList;
	private final Int2IntMap indexesToDocids;
	private final TermValueList _sortByValList;
	private final BigSegmentedArray _sortByOrderArray;
	private final int[] _count;
	private final int _countlength;
	private final int _lastIndex;

  private int _index;


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
		syncStateToIndex();
    return format(facet);
  }

	private void syncStateToIndex() {
		facet = (Comparable) _valList.getRawValue(_index);
		count = _count[_index];
		if (count > 0) {
			sortBy = _sortByValList.get(_sortByOrderArray.get(indexesToDocids.get(_index)));
		} else {
			sortBy = null;
		}
	}

  @Override
  public Comparable next(int minHits)
  {
    while (++_index < _countlength)
    {
      if (_count[_index] >= minHits)
      {
				syncStateToIndex();
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
