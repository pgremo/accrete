
// Author: Ian Burrell  <iburrell@leland.stanford.edu>
// Created: 1997/02/09
// Modified: 

// Copyright 1997 Ian Burrell

package accrete;

import static accrete.Astro.SOLAR_MASS_EARTH_MASS;
import static accrete.DoleParams.*;
import static java.lang.Math.*;
import static java.lang.String.format;

/**
 * This class stores the data needed for a planetismal, which is
 * basically mass and the orbit parameters.  It also includes a
 * pointer to the next in the list.  Also, has some functions that
 * calls the main calculators in the DoleParams class with the proper
 * values for the nucleus.
 * <p/>
 * The Accrete class manipulates the internals of this class directly.
 * Outside clients use the accessors.
 */
public class Planetismal {

  Star star;
  double axis;        // semi-major axis (AU)
  double eccn;        // eccentricity
  double mass;        // mass (solar mass)
  boolean gas_giant;

  /**
   * Calculates the density of dust at the given radius from the
   * star.
   */
  double DustDensity() {
    return DUST_DENSITY_COEFF * sqrt(star.stellar_mass) * exp(-ALPHA * pow(axis, 1.0 / DoleParams.N));
  }

  // Accessors

  public double getOrbitalAxis() {
    return axis;
  }

  public double getMassSolar() {
    return mass;
  }

  public double getMassEarth() {
    return mass * SOLAR_MASS_EARTH_MASS;
  }

  public boolean isGasGiant() {
    return gas_giant;
  }

  static final double PROTOPLANET_MASS = 1.0E-15; // units of solar masses

  Planetismal(Star star, double a, double e) {
    this(star, a, e, PROTOPLANET_MASS, false);
  }

  Planetismal(Star star, double a, double e, double m, boolean giant) {
    this.star = star;
    axis = a;
    eccn = e;
    mass = m;
    gas_giant = giant;
  }

  static Planetismal RandomPlanetismal(Star star, double inner, double outer) {
    return new Planetismal(star, Random(inner, outer), RandomEccentricity());
  }

  double ReducedMargin() {
    return DoleParams.ReducedMargin(mass);
  }

  double InnerEffectLimit() {
    return DoleParams.InnerEffectLimit(axis, eccn, DoleParams.ReducedMargin(mass));
  }

  double OuterEffectLimit() {
    return DoleParams.OuterEffectLimit(axis, eccn, DoleParams.ReducedMargin(mass));
  }

  double InnerSweptLimit() {
    return DoleParams.InnerSweptLimit(axis, eccn, DoleParams.ReducedMargin(mass));
  }

  double OuterSweptLimit() {
    return DoleParams.OuterSweptLimit(axis, eccn, DoleParams.ReducedMargin(mass));
  }

  double CriticalMass() {
    return DoleParams.CriticalMass(axis, eccn, star.stellar_luminosity);
  }

  public String toString() {
    String s = format("%s %s %s", axis, eccn, mass);
    if (mass > 2e-15) s = format("%s (%s)", s, mass * SOLAR_MASS_EARTH_MASS);
    if (gas_giant) s = s + " giant";
    return s;
  }
}
