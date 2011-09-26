package com.browseengine.bobo.facets.data;

import org.apache.lucene.index.Term;
import org.apache.lucene.util.NumericUtils;

public class NumericTermDoubleList extends TermDoubleList {

	// need to force formatString to be null
	public NumericTermDoubleList() {
		super(null);
	}

	@Override
	protected double parse(String s) {
		if (s == null || s.length() == 0) {
			return 0.0;
		} else {
			try {
				return NumericUtils.prefixCodedToDouble(s);
			} catch (NumberFormatException e) {
				return super.parse(s);
			}
		}
	}

	@Override
	public String get(int index) {
		return NumericUtils.doubleToPrefixCoded(_elements[index]);
	}

	@Override
	public boolean skipTerm(Term term) {
		String encoded = term.text();
		return !isFullPrecision(encoded);
	}

	private boolean isFullPrecision(String encoded) {
		int shift = encoded.charAt(0) - NumericUtils.SHIFT_START_LONG;
		return shift == 0;
	}

}
