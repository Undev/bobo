package com.browseengine.bobo.facets.impl;

import java.io.IOException;
import java.util.*;

import org.apache.log4j.Logger;

import com.browseengine.bobo.api.*;
import com.browseengine.bobo.api.FacetSpec.FacetSortSpec;
import com.browseengine.bobo.facets.FacetCountCollector;
import com.browseengine.bobo.facets.FacetCountCollectorSource;
import com.browseengine.bobo.facets.FacetHandler;
import com.browseengine.bobo.facets.data.FacetDataCache;
import com.browseengine.bobo.util.BoundedPriorityQueue;

public class SimpleSortByFacetHandler extends SimpleFacetHandler {
	private final String _sortByField;
	private SimpleFacetHandler _sortByFacetHandler;
    private static Logger logger = Logger.getLogger(SimpleSortByFacetHandler.class);

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

	public SimpleSortByFacetHandler newInstance() {
		return new SimpleSortByFacetHandler(_name,_sortByField);
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

    private static final class SortbyFacetCountCollector extends DefaultFacetCountCollector	{
        private final DefaultFacetCountCollector _subcollector;

		public SortbyFacetCountCollector(String name,FacetDataCache dataCache,int docBase,
                                         BrowseSelection sel,FacetSpec ospec,
                                         DefaultFacetCountCollector _subcollector) {
		    super(name,dataCache,docBase,sel,ospec);
            this._subcollector = _subcollector;
		}

		public final void collect(int docid) {
			_count[_array.get(docid)]++;
		}

		public final void collectAll() {
		  _count = _dataCache.freqs;
        }

        private final String getSortByFacetString(int idx){
            StringBuffer buf = new StringBuffer();
            if(_dataCache.indexesToDocids != null){
                int docid = _dataCache.indexesToDocids.get(idx);
                if(docid != -1) {
                    int sortByIdx = _subcollector._dataCache.orderArray.get(docid);
                    buf.append(_subcollector._dataCache.valArray.get(sortByIdx));
                } else { //no docid found
                    buf.append(_dataCache.valArray.get(idx));
                }
            } else { //
                logger.warn("no indexes to docids mapping, do usual sorting");
                buf.append(_dataCache.valArray.get(idx));
            }
            return buf.toString();
        }

        private final Object getRawFacetSortByValue(int idx){
            return _subcollector._dataCache.valArray.getInnerList().get(idx);
        }

        public List<BrowseFacet> getFacets() {
              if (_ospec!=null)
              {
                  int minCount=_ospec.getMinHitCount();
                  int max=_ospec.getMaxCount();
                  if (max <= 0) max=_count.length;

                  List<BrowseFacet> facetColl;
                  List<String> valList=_dataCache.valArray;
                  FacetSortSpec sortspec = _ospec.getOrderBy();
                  if (sortspec == FacetSortSpec.OrderValueAsc) {
                      facetColl=new ArrayList<BrowseFacet>(max);
                      for (int i = 1; i < _count.length;++i) // exclude zero
                      {
                        int hits=_count[i];
                        if (hits>=minCount)
                        {
                            BrowseFacet facet=new BrowseFacet(valList.get(i),hits);
                            facetColl.add(facet);
                        }
                        if (facetColl.size()>=max) break;
                      }
                  }
                  else {
                      ComparatorFactory comparatorFactory;
                      if (sortspec == FacetSortSpec.OrderHitsDesc){
                          comparatorFactory = new FacetHitcountComparatorFactory();
                      } else{
                          comparatorFactory = _ospec.getCustomComparatorFactory();
                      }
                      if (comparatorFactory == null){
                          throw new IllegalArgumentException("facet comparator factory not specified");
                      }

                      final Comparator<Integer> comparator = comparatorFactory.newComparator(new FieldValueAccessor(){

                        public String getFormatedValue(int index) {
                            return getSortByFacetString(index);
                        }

                        public Object getRawValue(int index) {
                            return getRawFacetSortByValue(index);
                        }

                      }, _count);
                      facetColl=new LinkedList<BrowseFacet>();

                      BoundedPriorityQueue<Integer> pq=new BoundedPriorityQueue<Integer>(comparator,max);

                      for (int i=1;i<_count.length;++i)
                      {
                        int hits=_count[i];
                        if (hits>=minCount)
                        {
                          pq.offer(i);
                        }
                      }

                      Integer val;
                      while((val = pq.poll()) != null)
                      {
                          BrowseFacet facet;
                          if(_dataCache.indexesToDocids != null){
                              int docid = _dataCache.indexesToDocids.get(val);
                              if(docid != -1 ){
                                  int sortVal = _subcollector._dataCache.orderArray.get(docid);
                                  facet = new SortByBrowseFacet(valList.get(val),_count[val],
                                  _subcollector._dataCache.valArray.get(sortVal));
                              }else {
                                  facet = new BrowseFacet(valList.get(val),_count[val]);
                              }
                          }else{
                              facet = new BrowseFacet(valList.get(val),_count[val]);
                          }

                          ((LinkedList<BrowseFacet>)facetColl).addFirst(facet);
                      }
                  }
                  return facetColl;
              }
              else
              {
                  return FacetCountCollector.EMPTY_FACET_LIST;
              }
          }

	}
}