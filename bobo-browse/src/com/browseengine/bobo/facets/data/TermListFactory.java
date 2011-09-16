package com.browseengine.bobo.facets.data;


public interface TermListFactory<T>
{
	TermValueList<T> createTermList();
	Class<?> getType();
	
	public static TermListFactory<String> StringListFactory=new TermListFactory<String>()
	{
		public TermValueList<String> createTermList()
		{
			return new TermStringList();
		}
		public Class<?> getType()
		{
		  return String.class;
		}
	};

	public static TermListFactory<Integer> NUMERIC_INTEGER_LIST_FACTORY = new TermListFactory<Integer>() {

		public TermValueList<Integer> createTermList() {
			return new NumericTermIntList();
		}

		public Class<?> getType() {
			return Integer.class;
		}

	};
}
