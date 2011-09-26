package com.browseengine.bobo.facets.data;

import org.apache.lucene.index.Term;
import org.apache.lucene.util.NumericUtils;

public class NumericTermFloatList extends TermFloatList {

	// need to force formatString to be null
	public NumericTermFloatList() {
		super(null);
	}

	@Override
	protected float parse(String s) {
		if (s == null || s.length() == 0) {
			return 0.0f;
		} else {
			try {
				return NumericUtils.prefixCodedToFloat(s);
			} catch (NumberFormatException e) {
				return super.parse(s);
			}
		}
	}

	@Override
	public String get(int index) {
		return NumericUtils.floatToPrefixCoded(_elements[index]);
	}

	@Override
	public boolean skipTerm(Term term) {
		String encoded = term.text();
		return !isFullPrecision(encoded);
	}

	private static boolean isFullPrecision(String encoded) {
		int shift = encoded.charAt(0) - NumericUtils.SHIFT_START_INT;
		return shift == 0;
	}

}
