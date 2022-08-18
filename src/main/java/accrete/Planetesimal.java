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

    public static final double protoplanetMass = 1.0E-15;

    public static Planetesimal randomPlanetesimal(Random random, Star star) {
        return new Planetesimal(star, random.nextDouble(star.innermostPlanet(), star.outermostPlanet()), RandomEccentricity(random), protoplanetMass, false);
    }

    Planetesimal coalesceWith(Planetesimal curr) {
        var new_mass = curr.mass() + mass();
        var new_axis = new_mass / ((curr.mass() / curr.axis()) + (mass() / axis()));
        var term1 = curr.mass() * sqrt(curr.axis() * (1.0 - pow(curr.eccn(), 2)));
        var term2 = mass() * sqrt(axis() * (1.0 - pow(eccn(), 2)));
        var term3 = (term1 + term2) / (new_mass * sqrt(new_axis));
        var term4 = 1.0 - pow(term3, 2);
        var new_eccn = sqrt(abs(term4));

        return new Planetesimal(curr.star(), new_axis, new_eccn, new_mass, curr.gasGiant() || gasGiant());
    }

    Boolean isTooClose(Planetesimal curr) {
        var dist = curr.axis() - axis();
        double dist1, dist2;
        if (dist > 0.0) {
            dist1 = outerEffectLimit() - axis();
            dist2 = curr.axis() - curr.innerEffectLimit();
        } else {
            dist1 = axis() - innerEffectLimit();
            dist2 = curr.outerEffectLimit() - curr.axis();
        }

        return abs(dist) <= dist1 || abs(dist) <= dist2;
    }

    public double dustDensity() {
        return DUST_DENSITY_COEFF * sqrt(star.mass()) * exp(-ALPHA * pow(axis, 1.0 / N));
    }

    public double getMassEarth() {
        return mass * SOLAR_MASS_EARTH_MASS;
    }

    double reducedMargin() {
        return ReducedMargin(mass);
    }

    double innerEffectLimit() {
        return InnerEffectLimit(axis, eccn, ReducedMargin(mass));
    }

    double outerEffectLimit() {
        return OuterEffectLimit(axis, eccn, ReducedMargin(mass));
    }

    double innerSweptLimit() {
        return max(InnerSweptLimit(axis, eccn, ReducedMargin(mass)), 0);
    }

    double outerSweptLimit() {
        return OuterSweptLimit(axis, eccn, ReducedMargin(mass));
    }

    double criticalMass() {
        return CriticalMass(axis, eccn, star.luminosity());
    }
}
