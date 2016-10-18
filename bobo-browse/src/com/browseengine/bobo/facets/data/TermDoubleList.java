package com.browseengine.bobo.facets.data;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import org.apache.lucene.util.NumericUtils;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

public class TermDoubleList extends TermNumberList<Double>
{

	protected double[] _elements = null;

  protected double parse(String s)
  {
    if (s == null || s.length() == 0)
    {
      return 0.0;
    } else
    {
        // TODO [Greg Bowyer]: This is crappy, we need to work out if the term relates to a field that
        // is actually a lucene numeric field, there are all sorts of screwball interactions that can occur
        // here (like the effects of position gaps)
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException nfe) {
            return NumericUtils.prefixCodedToDouble(s);
        }
    }
  }

  public TermDoubleList()
  {
    super();
  }

  public TermDoubleList(int capacity)
  {
    super(capacity);
  }

  public TermDoubleList(String formatString)
  {
    super(formatString);
  }

  public TermDoubleList(int capacity, String formatString)
  {
    super(capacity, formatString);
  }

  @Override
  public boolean add(String o)
  {
    return ((DoubleArrayList) _innerList).add(parse(o));
  }

  @Override
  protected List<?> buildPrimitiveList(int capacity)
  {
    _type = Double.class;
    return capacity > 0 ? new DoubleArrayList(capacity) : new DoubleArrayList();
  }

  @Override
  public String get(int index)
  {
    DecimalFormat formatter = _formatter.get();
    if (formatter == null)
      return String.valueOf(_elements[index]);
    return formatter.format(_elements[index]);
  }

  public double getPrimitiveValue(int index)
  {
    if (index < _elements.length)
      return _elements[index];
    else
      return -1;
  }

  @Override
  public int indexOf(Object o)
  {
    double val = parse((String) o);
    double[] elements = ((DoubleArrayList) _innerList).elements();
    return Arrays.binarySearch(elements, val);
  }

  public int indexOf(double val)
  {
    return Arrays.binarySearch(_elements, val);
  }

  @Override
  public void seal()
  {
    ((DoubleArrayList) _innerList).trim();
    _elements = ((DoubleArrayList) _innerList).elements();
  }

  @Override
  protected Object parseString(String o)
  {
    return parse(o);
  }

  public boolean contains(double val)
  {
    return Arrays.binarySearch(_elements, val) >= 0;
  }

  @Override
  public boolean containsWithType(Double val)
  {
    return Arrays.binarySearch(_elements, val) >= 0;
  }

  public boolean containsWithType(double val)
  {
    return Arrays.binarySearch(_elements, val) >= 0;
  }

  @Override
  public int indexOfWithType(Double o)
  {
    return Arrays.binarySearch(_elements, o);
  }

  public int indexOfWithType(double val)
  {
    return Arrays.binarySearch(_elements, val);
  }
}
