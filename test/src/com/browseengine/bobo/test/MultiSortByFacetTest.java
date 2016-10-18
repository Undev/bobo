package com.browseengine.bobo.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.junit.Test;

import com.browseengine.bobo.api.BoboBrowser;
import com.browseengine.bobo.api.BoboIndexReader;
import com.browseengine.bobo.api.Browsable;
import com.browseengine.bobo.api.BrowseFacet;
import com.browseengine.bobo.api.BrowseRequest;
import com.browseengine.bobo.api.BrowseResult;
import com.browseengine.bobo.api.FacetSpec;
import com.browseengine.bobo.api.FacetSpec.FacetSortSpec;
import com.browseengine.bobo.facets.FacetHandler;
import com.browseengine.bobo.facets.impl.MultiSortByComparatorFactory;
import com.browseengine.bobo.facets.impl.MultiSortByDescComparatorFactory;
import com.browseengine.bobo.facets.impl.MultiSortByFacetHandler;
import com.browseengine.bobo.facets.impl.SimpleFacetHandler;

public class MultiSortByFacetTest
{
  private static final String FACET_TO_RETURN = "facet_to_return";
  private static final String FACET_TO_SORT1 = "facet_to_sort_by_1";
  private static final String FACET_TO_SORT2 = "facet_to_sort_by_2";

  @Test
  public void sort_by_two_different_fields() throws Exception
  {
    Collection<FacetHandler<?>> facetHandlers = new ArrayList<FacetHandler<?>>();
    facetHandlers.add(new SimpleFacetHandler(FACET_TO_SORT1));
    facetHandlers.add(new SimpleFacetHandler(FACET_TO_SORT2));
    Collection<String> facetsToSortBy = new LinkedList<String>();
    facetsToSortBy.add(FACET_TO_SORT1);
    facetsToSortBy.add(FACET_TO_SORT2);
    facetHandlers.add(new MultiSortByFacetHandler(FACET_TO_RETURN, facetsToSortBy));

    Browsable browser = new BoboBrowser(BoboIndexReader.getInstanceAsSubReader(prepareIndex(), facetHandlers));

    {
      FacetSpec facetSpec = new FacetSpec();
      facetSpec.setOrderBy(FacetSortSpec.OrderByCustom);
      facetSpec.setCustomComparatorFactory(new MultiSortByComparatorFactory(FACET_TO_SORT1));

      BrowseRequest br = new BrowseRequest();
      br.setFacetSpec(FACET_TO_RETURN, facetSpec);

      BrowseResult result = browser.browse(br);
      List<BrowseFacet> facets = result.getFacetMap().get(FACET_TO_RETURN).getFacets();
      assertEquals(new BrowseFacet("2", 1), facets.get(0));
      assertEquals(new BrowseFacet("1", 1), facets.get(1));
      assertEquals(new BrowseFacet("3", 1), facets.get(2));
    }
    {
      FacetSpec facetSpec = new FacetSpec();
      facetSpec.setOrderBy(FacetSortSpec.OrderByCustom);
      facetSpec.setCustomComparatorFactory(new MultiSortByDescComparatorFactory(FACET_TO_SORT1));

      BrowseRequest br = new BrowseRequest();
      br.setFacetSpec(FACET_TO_RETURN, facetSpec);

      BrowseResult result = browser.browse(br);
      List<BrowseFacet> facets = result.getFacetMap().get(FACET_TO_RETURN).getFacets();
      assertEquals(new BrowseFacet("3", 1), facets.get(0));
      assertEquals(new BrowseFacet("1", 1), facets.get(1));
      assertEquals(new BrowseFacet("2", 1), facets.get(2));
    }

    {
      FacetSpec facetSpec = new FacetSpec();
      facetSpec.setOrderBy(FacetSortSpec.OrderByCustom);
      facetSpec.setCustomComparatorFactory(new MultiSortByComparatorFactory(FACET_TO_SORT2));

      BrowseRequest br = new BrowseRequest();
      br.setFacetSpec(FACET_TO_RETURN, facetSpec);

      BrowseResult result = browser.browse(br);
      List<BrowseFacet> facets = result.getFacetMap().get(FACET_TO_RETURN).getFacets();
      assertEquals(new BrowseFacet("3", 1), facets.get(0));
      assertEquals(new BrowseFacet("1", 1), facets.get(1));
      assertEquals(new BrowseFacet("2", 1), facets.get(2));
    }

    {
      FacetSpec facetSpec = new FacetSpec();
      facetSpec.setOrderBy(FacetSortSpec.OrderValueAsc);

      BrowseRequest br = new BrowseRequest();
      br.setFacetSpec(FACET_TO_RETURN, facetSpec);

      BrowseResult result = browser.browse(br);
      List<BrowseFacet> facets = result.getFacetMap().get(FACET_TO_RETURN).getFacets();
      assertEquals(new BrowseFacet("1", 1), facets.get(0));
      assertEquals(new BrowseFacet("2", 1), facets.get(1));
      assertEquals(new BrowseFacet("3", 1), facets.get(2));
    }

  }

  private static IndexReader prepareIndex() throws CorruptIndexException, IOException
  {
    Directory directory = new RAMDirectory();
    IndexWriter indexWriter = new IndexWriter(directory, new SimpleAnalyzer(), IndexWriter.MaxFieldLength.UNLIMITED);

    indexWriter.addDocument(doc("1", "b", "b"));
    indexWriter.addDocument(doc("2", "a", "c"));
    indexWriter.addDocument(doc("3", "c", "a"));

    indexWriter.close();

    return IndexReader.open(directory, true);
  }

  private static Document doc(String id, String val1, String val2)
  {
    Document document = new Document();
    document.add(new Field(FACET_TO_RETURN, id, Field.Store.YES, Field.Index.NOT_ANALYZED));
    document.add(new Field(FACET_TO_SORT1, val1, Field.Store.YES, Field.Index.NOT_ANALYZED));
    document.add(new Field(FACET_TO_SORT2, val2, Field.Store.YES, Field.Index.NOT_ANALYZED));
    return document;
  }
}
