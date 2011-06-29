package com.browseengine.bobo.test;

import static org.junit.Assert.assertEquals;
import it.unimi.dsi.fastutil.ints.Int2DoubleMap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.SortField;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;
import org.junit.Test;

import com.browseengine.bobo.api.BoboBrowser;
import com.browseengine.bobo.api.BoboCustomSortField;
import com.browseengine.bobo.api.BoboIndexReader;
import com.browseengine.bobo.api.Browsable;
import com.browseengine.bobo.api.BrowseHit;
import com.browseengine.bobo.api.BrowseRequest;
import com.browseengine.bobo.api.BrowseResult;
import com.browseengine.bobo.api.BrowseSelection;
import com.browseengine.bobo.facets.FacetHandler;
import com.browseengine.bobo.facets.data.PredefinedTermListFactory;
import com.browseengine.bobo.facets.impl.MultiValueFacetHandler;
import com.browseengine.bobo.facets.impl.SimpleFacetHandler;
import com.browseengine.bobo.sort.DvagsWeightComparatorSource;

public class SortByDvagsWeightTest {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void test() throws Exception {
		Collection<FacetHandler<?>> facetHandlers = new ArrayList<FacetHandler<?>>();
		facetHandlers.add(new SimpleFacetHandler("id", new PredefinedTermListFactory(Integer.class, "000000000000")));
		facetHandlers.add(new MultiValueFacetHandler("dvag_ids", new PredefinedTermListFactory(Integer.class,
				"000000000000")));

		Browsable browser = new BoboBrowser(BoboIndexReader.getInstanceAsSubReader(prepareIndex(), facetHandlers));

		BrowseSelection selection = new BrowseSelection("dvag_ids");
		selection.setValues(new String[] { "111", "222", "333", "444" });

		Map<Integer, Double> dvag_weight = new HashMap<Integer, Double>();
		dvag_weight.put(111, 0.6);
		dvag_weight.put(222, 0.5);
		dvag_weight.put(333, 0.2);
		dvag_weight.put(444, 0.1);

		DvagsWeightComparatorSource comparatorSource = new DvagsWeightComparatorSource(dvag_weight, "000000000000", true);

		BrowseRequest req = new BrowseRequest();
		req.setCount(10);
		req.addSelection(selection);
		req.setSort(new SortField[] { new BoboCustomSortField("not_in_facets", true, comparatorSource) });

		BrowseResult browseResult = browser.browse(req);

		BrowseHit[] hits = browseResult.getHits();

		for (int i = 0; i < hits.length; i++) {
			String id_str = hits[i].getField("id");
			System.out.print(id_str);
			int episdoe_id = Integer.valueOf(id_str).intValue();
			System.out.println(" weight: " + comparatorSource.episode2weight.get(episdoe_id));
			Int2DoubleMap int2DoubleMap = comparatorSource.episode2dvags.get(episdoe_id);
			for (Integer dvag_id : int2DoubleMap.keySet()) {
				System.out.println("dvag id: " + dvag_id + " dvag weight: " + int2DoubleMap.get(dvag_id));
			}
		}


		assertEquals(3, hits.length);
		assertEquals(new Integer(22), Integer.valueOf(hits[0].getField("id")));
		assertEquals(new Integer(11), Integer.valueOf(hits[1].getField("id")));
		assertEquals(new Integer(33), Integer.valueOf(hits[2].getField("id")));


	}

	private IndexReader prepareIndex() throws CorruptIndexException, LockObtainFailedException, IOException {
		Directory directory = new RAMDirectory();
		IndexWriter writer = new IndexWriter(directory, new SimpleAnalyzer(), IndexWriter.MaxFieldLength.UNLIMITED);

		writer.addDocument(episode(22, new int[] { 111, 222, 888 }));
		writer.addDocument(episode(11, new int[] { 222, 333, 444 }));
		writer.addDocument(episode(33, new int[] { 333, 444 }));
		writer.addDocument(episode(44, new int[] { 999 }));

		writer.close();

		return IndexReader.open(directory, true);
	}

	private Document episode(int id, int[] dvag_ids) {
		Document document = new Document();
		
		document.add(stored_numeric_field("id", id));
		for (int i : dvag_ids) {
			document.add(stored_numeric_field("dvag_ids", i));
		}

		return document;
	}

	private Field stored_numeric_field(String name, int val) {
		return new Field(name, format_int(val), Field.Store.YES, Field.Index.NOT_ANALYZED);
	}

	private String format_int(int i) {
		return String.format("%012d", i);
	}

}
