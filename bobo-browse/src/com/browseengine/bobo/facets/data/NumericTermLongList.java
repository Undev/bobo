package com.browseengine.bobo.facets.data;

import org.apache.lucene.index.Term;
import org.apache.lucene.util.NumericUtils;

public class NumericTermLongList extends TermLongList {

	// need to force formatString to be null
	public NumericTermLongList() {
		super(null);
	}

	// need to force formatString to be null
	public NumericTermLongList(int capacity) {
		super(capacity, null);
	}

	@Override
	protected long parse(String s) {
		if (s == null || s.length() == 0) {
			return 0;
		} else {
			try {
				return NumericUtils.prefixCodedToLong(s);
			} catch (NumberFormatException e) {
				return super.parse(s);
			}
		}
	}

	@Override
	public String get(int index) {
		return NumericUtils.longToPrefixCoded(_elements[index]);
	}

	@Override
	public boolean skipTerm(Term term) {
		String encoded = term.text();
		return !isFullPrecision(encoded);
	}

	private static boolean isFullPrecision(String encoded) {
		int shift = encoded.charAt(0) - NumericUtils.SHIFT_START_LONG;
		return shift == 0;
	}
}
