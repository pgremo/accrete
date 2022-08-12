package accrete;

import static accrete.DoleParams.ScaleCubeRootMass;

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
