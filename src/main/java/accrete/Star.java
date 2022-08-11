package accrete;

import static accrete.DoleParams.ScaleCubeRootMass;

/**
 * @param stellar_mass       in Solar masses
 * @param stellar_luminosity in Solar luminsoities
 */
public record Star(
        double stellar_mass,
        double stellar_luminosity
) {

    double InnermostPlanet() {
        return ScaleCubeRootMass(0.3, stellar_mass);
    }

    double OutermostPlanet() {
        return ScaleCubeRootMass(50.0, stellar_mass);
    }

}
