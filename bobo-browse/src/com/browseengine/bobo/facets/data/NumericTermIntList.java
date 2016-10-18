package com.browseengine.bobo.facets.data;

import org.apache.lucene.index.Term;
import org.apache.lucene.util.NumericUtils;

public class NumericTermIntList extends TermIntList
{

	// need to force formatString to be null
	public NumericTermIntList() {
		super(null);
	}

	// need to force formatString to be null
		public NumericTermIntList(int capacity) {
			super(capacity, null);
		}

	@Override
	protected int parse(String s)
  {
    if (s == null || s.length() == 0)
    {
      return 0;
    } else
    {
			try {
				return NumericUtils.prefixCodedToInt(s);
			} catch (NumberFormatException e) {
				return super.parse(s);
			}
    }
  }

	@Override
	public String get(int index) {
		return NumericUtils.intToPrefixCoded(_elements[index]);
	}

	@Override
	public boolean skipTerm(Term term) {
		String encoded = term.text();
		return !isFullPrecision(encoded);
	}

	private static boolean isFullPrecision(String prefixCoded) {
		int shift = prefixCoded.charAt(0) - NumericUtils.SHIFT_START_INT;
		return shift == 0;
	}

}
