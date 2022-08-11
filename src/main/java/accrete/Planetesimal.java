
// Author: Ian Burrell  <iburrell@leland.stanford.edu>
// Created: 1997/02/09
// Modified: 

// Copyright 1997 Ian Burrell

package accrete;

import java.util.Random;

import static accrete.Astro.SOLAR_MASS_EARTH_MASS;
import static accrete.DoleParams.*;
import static java.lang.Math.*;

public record Planetesimal(
        Star star,
        double axis,
        double eccn,
        double mass,
        boolean gasGiant
) {

    public static final double PROTOPLANET_MASS = 1.0E-15;

    public static Planetesimal RandomPlanetismal(Random random, Star star) {
        return new Planetesimal(star, random.nextDouble(star.InnermostPlanet(), star.OutermostPlanet()), RandomEccentricity(random), PROTOPLANET_MASS, false);
    }

    public double DustDensity() {
        return DUST_DENSITY_COEFF * sqrt(star.stellar_mass()) * exp(-ALPHA * pow(axis, 1.0 / DoleParams.N));
    }

    public double getMassEarth() {
        return mass * SOLAR_MASS_EARTH_MASS;
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
        return max(DoleParams.InnerSweptLimit(axis, eccn, DoleParams.ReducedMargin(mass)), 0);
    }

    double OuterSweptLimit() {
        return DoleParams.OuterSweptLimit(axis, eccn, DoleParams.ReducedMargin(mass));
    }

    double CriticalMass() {
        return DoleParams.CriticalMass(axis, eccn, star.stellar_luminosity());
    }
}
