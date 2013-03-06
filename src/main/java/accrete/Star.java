package accrete;

import static accrete.DoleParams.ScaleCubeRootMass;

public class Star {
  double stellar_mass;        // in Solar masses
  double stellar_luminosity;  // in Solar luminsoities

  public Star(double stellar_mass, double stellar_luminosity) {
    this.stellar_mass = stellar_mass;
    this.stellar_luminosity = stellar_luminosity;
  }

   double InnermostPlanet() {
    return ScaleCubeRootMass(0.3, stellar_mass);
  }

   double OutermostPlanet() {
    return ScaleCubeRootMass(50.0, stellar_mass);
  }

}
