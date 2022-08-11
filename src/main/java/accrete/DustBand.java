
// Author: Ian Burrell  <iburrell@leland.stanford.edu>
// Created: 1997/02/09
// Modified: 

// Copyright 1997 Ian Burrell

package accrete;

/**
 * Stores the data for a band of dust of gas.  Contains the inner and
 * outer edge, and whether it has dust or gas present.  Has a pointer
 * to maintain the list of bands.
 * <p/>
 * The list of DustBands is maintained by the Accrete class; the
 * internals are exposed for manipulation.
 */
public record DustBand(
        double inner,
        double outer,
        boolean dust,
        boolean gas
) {
}


