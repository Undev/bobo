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

	public static TermListFactory<Long> NUMERIC_LONG_LIST_FACTORY = new TermListFactory<Long>() {

		public TermValueList<Long> createTermList() {
			return new NumericTermLongList();
		}

		public Class<?> getType() {
			return Long.class;
		}

	};

	public static TermListFactory<Float> NUMERIC_FLOAT_LIST_FACTORY = new TermListFactory<Float>() {

		public TermValueList<Float> createTermList() {
			return new NumericTermFloatList();
		}

		public Class<?> getType() {
			return Float.class;
		}
	};

	public static TermListFactory<Double> NUMERIC_DOUBLE_LIST_FACTORY = new TermListFactory<Double>() {

		@Override
		public TermValueList<Double> createTermList() {
			return new NumericTermDoubleList();
		}

		@Override
		public Class<?> getType() {
			return Double.class;
		}

	};
}
