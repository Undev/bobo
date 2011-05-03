package com.browseengine.bobo.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
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
import com.browseengine.bobo.api.MultiBoboBrowser;
import com.browseengine.bobo.facets.FacetHandler;
import com.browseengine.bobo.facets.impl.FacetSortByComparatorFactory;
import com.browseengine.bobo.facets.impl.SimpleFacetHandler;
import com.browseengine.bobo.facets.impl.SimpleSortByFacetHandler;

public class SortByFacetTest {
    private static final String FACET_TO_RETURN = "facet_to_return";
    private static final String FACET_TO_SORT_BY = "facet_to_sort_by";

    @Test
    public void order_by_another_facet_value() throws Exception {
        Collection<FacetHandler<?>> facetHandlers = createHandlers();

        Browsable browser = new BoboBrowser(
                BoboIndexReader.getInstanceAsSubReader(prepareIndex(),
                        facetHandlers));

        FacetSpec facetSpec = new FacetSpec();
        facetSpec.setOrderBy(FacetSortSpec.OrderByCustom);
        facetSpec
                .setCustomComparatorFactory(new FacetSortByComparatorFactory());

        BrowseRequest br = new BrowseRequest();
        br.setFacetSpec(FACET_TO_RETURN, facetSpec);

        BrowseResult result = browser.browse(br);
        List<BrowseFacet> facets = result.getFacetMap().get(FACET_TO_RETURN)
                .getFacets();
        assertEquals(new BrowseFacet("3", 1), facets.get(0));
        assertEquals(new BrowseFacet("2", 3), facets.get(1));
        assertEquals(new BrowseFacet("1", 2), facets.get(2));

    }

    private static Collection<FacetHandler<?>> createHandlers() {
        Collection<FacetHandler<?>> facetHandlers = new ArrayList<FacetHandler<?>>();
        facetHandlers.add(new SimpleFacetHandler(FACET_TO_SORT_BY));
        facetHandlers.add(new SimpleSortByFacetHandler(FACET_TO_RETURN,
                FACET_TO_SORT_BY));
        return facetHandlers;
    }

    @Test
    public void with_multiple_readers() throws Exception {
        Collection<FacetHandler<?>> handlers = createHandlers();
        List<BoboIndexReader> readerList = new ArrayList<BoboIndexReader>(2);
        readerList.add(BoboIndexReader.getInstanceAsSubReader(
                prepareIndexPart1(), handlers));
        readerList.add(BoboIndexReader.getInstanceAsSubReader(
                prepareIndexpart2(), handlers));
        Browsable browser = new MultiBoboBrowser(
                BoboBrowser.createBrowsables(readerList));

        FacetSpec facetSpec = new FacetSpec();
        facetSpec.setOrderBy(FacetSortSpec.OrderByCustom);
        facetSpec
                .setCustomComparatorFactory(new FacetSortByComparatorFactory());

        BrowseRequest br = new BrowseRequest();
        br.setFacetSpec(FACET_TO_RETURN, facetSpec);

        BrowseResult result = browser.browse(br);
        List<BrowseFacet> facets = result.getFacetMap().get(FACET_TO_RETURN)
                .getFacets();
        assertEquals(new BrowseFacet("3", 1), facets.get(0));
        assertEquals(new BrowseFacet("2", 3), facets.get(1));
        assertEquals(new BrowseFacet("1", 2), facets.get(2));
    }

    private static IndexReader prepareIndexpart2()
            throws CorruptIndexException, LockObtainFailedException,
            IOException {
        Directory directory = new RAMDirectory();

        addOneDocAtATime(directory, doc("1", "c"));
        addOneDocAtATime(directory, doc("1", "c"));
        addOneDocAtATime(directory, doc("2", "b"));

        return IndexReader.open(directory, true);
    }

    private static IndexReader prepareIndexPart1()
            throws CorruptIndexException, LockObtainFailedException,
            IOException {
        Directory directory = new RAMDirectory();

        addOneDocAtATime(directory, doc("2", "b"));
        addOneDocAtATime(directory, doc("2", "b"));
        addOneDocAtATime(directory, doc("3", "a"));

        return IndexReader.open(directory, true);
    }

    private static void addOneDocAtATime(Directory directory, Document doc)
            throws CorruptIndexException, LockObtainFailedException,
            IOException {
        IndexWriter indexWriter = new IndexWriter(directory,
                new SimpleAnalyzer(), IndexWriter.MaxFieldLength.UNLIMITED);

        indexWriter.addDocument(doc);

        indexWriter.close();
    }

    private static IndexReader prepareIndex() throws CorruptIndexException,
            LockObtainFailedException, IOException {
        Directory directory = new RAMDirectory();
        IndexWriter indexWriter = new IndexWriter(directory,
                new SimpleAnalyzer(), IndexWriter.MaxFieldLength.UNLIMITED);

        indexWriter.addDocument(doc("1", "c"));
        indexWriter.addDocument(doc("1", "c"));
        indexWriter.addDocument(doc("2", "b"));
        indexWriter.addDocument(doc("2", "b"));
        indexWriter.addDocument(doc("2", "b"));
        indexWriter.addDocument(doc("3", "a"));

        indexWriter.close();

        return IndexReader.open(directory, true);
    }

    private static Document doc(String id, String name) {
        Document document = new Document();
        document.add(new Field(FACET_TO_RETURN, id, Field.Store.YES,
                Field.Index.NOT_ANALYZED));
        document.add(new Field(FACET_TO_SORT_BY, name, Field.Store.YES,
                Field.Index.NOT_ANALYZED));
        return document;
    }

}
