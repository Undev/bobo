package com.browseengine.bobo.facets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.apache.lucene.util.PriorityQueue;

import com.browseengine.bobo.api.BrowseFacet;
import com.browseengine.bobo.api.FacetAccessible;
import com.browseengine.bobo.api.FacetIterator;
import com.browseengine.bobo.api.FacetSpec;
import com.browseengine.bobo.api.FacetSpec.FacetSortSpec;
import com.browseengine.bobo.api.SortByBrowseFacet;

public class SortByCombinedFacetAccessible extends CombinedFacetAccessible
{

  private final FacetSpec fspec;
  private final List<FacetAccessible> list;

  public SortByCombinedFacetAccessible(FacetSpec fspec,
      List<FacetAccessible> list)
  {
    super(fspec, list);
    this.fspec = fspec;
    this.list = list;
  }

  @Override
  public List<BrowseFacet> getFacets()
  {
    if (!FacetSortSpec.OrderByCustom.equals(fspec.getOrderBy()))
    {
      return super.getFacets();
    }

    Comparator<BrowseFacet> comparator = fspec.getCustomComparatorFactory()
        .newComparator();

    int maxCnt = fspec.getMaxCount();
    if (maxCnt <= 0)
    {
      maxCnt = Integer.MAX_VALUE;
    }
    int minHits = fspec.getMinHitCount();

    CombinedSortByFacetIterator iter = (CombinedSortByFacetIterator) iterator();

    LinkedList<BrowseFacet> list = new LinkedList<BrowseFacet>();
    @SuppressWarnings("rawtypes")
    Comparable facet = null;
    if (maxCnt != Integer.MAX_VALUE)
    {
      PriorityQueue queue = createPQ(maxCnt, comparator);
      SortByBrowseFacet browseFacet = new SortByBrowseFacet();
      int qsize = 0;
      while ((qsize < maxCnt) && ((facet = iter.next(minHits)) != null))
      {
        queue.add(new SortByBrowseFacet(String.valueOf(facet), iter.count, String.valueOf(iter.sortBy)));
        qsize++;
      }
      if (facet != null)
      {
        while ((facet = iter.next(minHits)) != null)
        {
          // check with the top of min heap
          browseFacet.setHitCount(iter.count);
          browseFacet.setValue(String.valueOf(facet));
          browseFacet.setSortByValue(String.valueOf(iter.sortBy));
          browseFacet = (SortByBrowseFacet) queue.insertWithOverflow(browseFacet);
        }
      }
      // remove from queue and add to the list
      while (qsize-- > 0)
        list.addFirst((BrowseFacet) queue.pop());
    } else
    {
      while ((facet = iter.next(minHits)) != null)
      {
        list.add(new SortByBrowseFacet(String.valueOf(facet), iter.count, String.valueOf(iter.sortBy)));
      }
      Collections.sort(list, comparator);
    }
    
    return list;
  }

  private PriorityQueue createPQ(final int max, final Comparator<BrowseFacet> comparator)
  {
    PriorityQueue queue = new PriorityQueue()
    {
      {
        this.initialize(max);
      }

      @Override
      protected boolean lessThan(Object arg0, Object arg1)
      {
        BrowseFacet o1 = (BrowseFacet) arg0;
        BrowseFacet o2 = (BrowseFacet) arg1;
        return comparator.compare(o1, o2) > 0;
      }
    };
    return queue;
  }

  public FacetIterator iterator()
  {
    List<SortByFacetIterator> iterList = new ArrayList<SortByFacetIterator>(
        list.size());

    for (FacetAccessible accessible : list)
    {
      iterList.add((SortByFacetIterator) accessible.iterator());
    }

    return new CombinedSortByFacetIterator(iterList);
  };
}
