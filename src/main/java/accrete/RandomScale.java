package accrete;

import java.util.Random;

public class RandomScale {

  public RandomScale() {
    gen = new Random();
  }

  public double randomDouble() {
    return gen.nextDouble();
  }

  public double randomDouble(double d) {
    return gen.nextDouble() * d;
  }

  public double randomDouble(double d, double d1) {
    return randomDouble(d1 - d) + d;
  }

  private Random gen;
}
