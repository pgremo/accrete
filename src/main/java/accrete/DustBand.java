
// Author: Ian Burrell  <iburrell@leland.stanford.edu>
// Created: 1997/02/09
// Modified: 

// Copyright 1997 Ian Burrell

package accrete;

import java.util.Iterator;

import static java.lang.String.format;

/**
 * Stores the data for a band of dust of gas.  Contains the inner and
 * outer edge, and whether it has dust or gas present.  Has a pointer
 * to maintain the list of bands.
 * <p/>
 * The list of DustBands is maintained by the Accrete class; the
 * internals are exposed for manipulation.
 */
class DustBand implements Iterable<DustBand> {

  double inner;       // inner edge (in AU)
  double outer;       // outer edge (in AU)
  boolean dust;       // dust present
  boolean gas;        // gas present
  DustBand next;

  DustBand(double inner_limit, double outer_limit) {
    this(inner_limit, outer_limit, true, true);
  }

  DustBand(double inner_limit, double outer_limit,
           boolean dust_present, boolean gas_present) {
    inner = inner_limit;
    outer = outer_limit;
    dust = dust_present;
    gas = gas_present;
    next = null;
  }

  public String toString() {
    return format("%s %s %s %s", inner, outer, dust, gas);
  }

  @Override
  public Iterator<DustBand> iterator() {
    return new Iterator<DustBand>() {
      DustBand curr = DustBand.this;

      @Override
      public boolean hasNext() {
        return curr != null;
      }

      @Override
      public DustBand next() {
        DustBand result = curr;
        curr = curr.next;
        return result;
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }
}


