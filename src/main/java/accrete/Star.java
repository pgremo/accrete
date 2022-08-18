package accrete;

import static accrete.DoleParams.ScaleCubeRootMass;

public record Star(
        double mass,
        double luminosity
) {

    double innermostPlanet() {
        return ScaleCubeRootMass(0.3, mass);
    }

    double outermostPlanet() {
        return ScaleCubeRootMass(50.0, mass);
    }

}
