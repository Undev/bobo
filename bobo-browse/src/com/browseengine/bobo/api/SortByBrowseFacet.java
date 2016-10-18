package com.browseengine.bobo.api;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;

/**
 * This class represents a facet with separate value to sort by.
 * @see com.browseengine.bobo.facets.impl.SimpleSortByFacetHandler
 */
public class SortByBrowseFacet extends BrowseFacet {
	private static final long serialVersionUID = 1L;

	private String _sortByValue;

	public SortByBrowseFacet() {
        super();
	}

	public SortByBrowseFacet(String value,int hitcount)
	{
	    this(value, hitcount, value);

	}

    public SortByBrowseFacet(String value,int hitcount, String sortByValue)
    {
        super(value, hitcount);
        this._sortByValue = sortByValue;
    }

	/**
	 * Gets the facet value to sort by
	 * @return value
	 * @see #setValue(String)
	 */
	public String getSortByValue(){
		return _sortByValue;
	}

	/**
	 * Sets the facet value to sort by
	 * @param sortByValue Facet sort value
	 * @see #getSortByValue()
	 */
	public void setSortByValue(String sortByValue){
		_sortByValue=sortByValue;
	}

	@Override
	public String toString(){
		StringBuilder buf=new StringBuilder();
		buf.append(super.toString()).append("[").append(_sortByValue).append("]");
		return buf.toString();
	}


	@Override
	public boolean equals(Object obj) {
		boolean equals=false;

		if (obj instanceof SortByBrowseFacet){
			SortByBrowseFacet c2=(SortByBrowseFacet)obj;
			if (_sortByValue==c2._sortByValue && super.equals(obj)){
				equals=true;
			}
		}
		return equals;
	}
}