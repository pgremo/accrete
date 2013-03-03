
// Author: Ian Burrell  <iburrell@leland.stanford.edu>
// Created: 1997/01/14
// Modified: 1997/02/09

// Copyright 1997 Ian Burrell

/*
 *
 * Simulates the creation of planetary systems using the Dole
 * accretion model.
 *
 * This program simulates the creation of a planetary system by
 * accretion of planetismals.  Individual planets sweep out dust and
 * gas until all of the dust is swept up.  Planets whose orbits are
 * close are coalesced.
 *
 * See http://www-leland.stanford.edu/~iburrell/create/accrete.html
 * for more history of this model and programs.
 *
 * References:
 *      Dole, Stephen H.  "Computer Simulation of the Formation of
 *          Planetary Systems."  _Icarus_.  13 (1970), pg 494-508.
 *      Isaacman & Sagan.  "Computer Simulations of Planetary Accretion
 *          Dynamics."  _Icarus_.  31 (1997), pg 510-533.
 *      Fogg, Martyn J.  "Extra-Solar Planetary Systems: A Microcomputer
 *          Simulation".  Journal of the British Interplanetary
 *          Society, vol 38, 501-514, 1985.
 *
 *
 */

package accrete;

import com.googlecode.totallylazy.*;

import java.util.Comparator;
import java.util.Iterator;

import static accrete.DoleParams.*;
import static accrete.Planetismal.PROTOPLANET_MASS;
import static accrete.Planetismal.RandomPlanetismal;
import static com.googlecode.totallylazy.Callables.curry;
import static com.googlecode.totallylazy.Option.none;
import static com.googlecode.totallylazy.Option.option;
import static com.googlecode.totallylazy.Pair.pair;
import static com.googlecode.totallylazy.Sequences.*;
import static com.googlecode.totallylazy.numbers.Numbers.add;
import static java.lang.Math.*;

/**
 * This class does the accretion process and returns a list of
 * Planetismals with the results.  It is constructed for a given star
 * size.  Multiple random systems can be created for the given star
 * using DistributePlanets.
 */
public class Accrete {

  public static final Comparator<Planetismal> planetismalComparator = new Comparator<Planetismal>() {
    @Override
    public int compare(Planetismal o1, Planetismal o2) {
      return Double.valueOf(o1.axis).compareTo(o2.axis);
    }
  };
  public static final Callable2<Planetismal, DustBand, Sequence<DustBand>> dustExpansion = new Callable2<Planetismal, DustBand, Sequence<DustBand>>() {
    @Override
    public Sequence<DustBand> call(Planetismal tsml, DustBand curr) throws Exception {
      double min = tsml.InnerSweptLimit();
      double max = tsml.OuterSweptLimit();
      boolean new_gas = curr.gas && !tsml.gas_giant;

      // Current is...
      // Case 1: Wider
      if (curr.inner < min && curr.outer > max) {
        return sequence(
          new DustBand(curr.inner, min, curr.dust, curr.gas),
          new DustBand(min, max, false, new_gas),
          new DustBand(max, curr.outer, curr.dust, curr.gas));
      }
      // Case 2: Outer
      else if (curr.inner < max && curr.outer > max) {
        return sequence(
          new DustBand(curr.inner, max, false, new_gas),
          new DustBand(max, curr.outer, curr.dust, curr.gas));
      }
      // Case 3: Inner
      else if (curr.inner < min && curr.outer > min) {
        return sequence(
          new DustBand(curr.inner, min, curr.dust, curr.gas),
          new DustBand(min, curr.outer, false, new_gas));
      }
      // Case 4: Narrower
      else if (curr.inner >= min && curr.outer <= max) {
        return sequence(new DustBand(curr.inner, curr.outer, false, new_gas));
      }
      return sequence(curr);
    }
  };
  public static final Callable2<Planetismal, DustBand, Double> collectDust = new Callable2<Planetismal, DustBand, Double>() {
    @Override
    public Double call(Planetismal tsml, DustBand dustBand) throws Exception {
      if (!dustBand.dust) return 0.0;

      double swept_inner = max(0.0, tsml.InnerSweptLimit());
      double swept_outer = tsml.OuterSweptLimit();
      if (dustBand.outer <= swept_inner || dustBand.inner >= swept_outer) return 0.0;

      double dust_density = tsml.DustDensity();
      double crit_mass = tsml.CriticalMass();
      double mass_density = MassDensity(dust_density, crit_mass, tsml.mass);
      double density = !dustBand.gas || tsml.mass < crit_mass ? dust_density : mass_density;

      double swept_width = swept_outer - swept_inner;
      double outside = max(swept_outer - dustBand.outer, 0);
      double inside = max(dustBand.inner - swept_inner, 0);
      double width = swept_width - outside - inside;

      double term1 = 4.0 * PI * pow(tsml.axis, 2);
      double term2 = 1.0 - tsml.eccn * (outside - inside) / swept_width;
      double volume = term1 * tsml.ReducedMargin() * width * term2;

      return volume * density;
    }
  };
  private Star star;
  double inner_bound, outer_bound;    // in AU
  double inner_dust, outer_dust;      // in AU

  /**
   * Creates a Accretion class for the star of the given size.
   * Pre-calculates all of those values that depend on the size of
   * the star.
   */
  public Accrete() {
    this(new Star(1.0, 1.0));
  }

  public Accrete(Star star) {
    this.star = star;
    inner_bound = InnermostPlanet(star.stellar_mass);
    outer_bound = OutermostPlanet(star.stellar_mass);
    inner_dust = InnerDustLimit();
    outer_dust = OuterDustLimit(star.stellar_mass);
  }


  /**
   * This function does the main work of creating the planets.  It
   * calls all of the other functions to create an entire system of
   * planetismals.  It returns a list of the Planetismals.
   *
   * @return Vector containing all of the planets written out.
   */
  public Sequence<Planetismal> DistributePlanets() {
    Sequence<DustBand> dustBands = sequence(new DustBand(inner_dust, outer_dust));
    Sequence<Planetismal> planets = empty(Planetismal.class);

    while (CheckDustLeft(dustBands)) {
      Planetismal tsml = RandomPlanetismal(star, inner_bound, outer_bound);
      tsml = AccreteDust(dustBands, tsml);
      if (tsml.mass == 0.0 || tsml.mass == PROTOPLANET_MASS) continue;
      tsml.gas_giant = tsml.mass >= tsml.CriticalMass();
      dustBands = UpdateDustLanes(dustBands, tsml);
      dustBands = CompressDustLanes(dustBands);
      planets = CoalescePlanetismals(planets, tsml);
    }

    return planets;
  }

  /**
   * Repeatedly accretes dust and gas onto the new planetismal by
   * sweeping out gas lanes until the planetismal doesn't grow any
   * more.  Returns the new mass for the planetismal; also changes
   * the planetismal's mass.
   */
  Planetismal AccreteDust(Sequence<DustBand> dustBands, Planetismal nucleus) {
    return unfoldRight(curry(new Callable2<Sequence<DustBand>, Planetismal, Option<? extends Pair<? extends Planetismal, ? extends Planetismal>>>() {
      @Override
      public Option<? extends Pair<? extends Planetismal, ? extends Planetismal>> call(Sequence<DustBand> dustBands, Planetismal tsml) throws Exception {
        double new_mass = dustBands.map(curry(collectDust).apply(tsml)).reduce(add).doubleValue();
        if (new_mass - tsml.mass <= 0.001 * new_mass) return none();
        Planetismal result = new Planetismal(tsml.star, tsml.axis, tsml.eccn, new_mass, tsml.gas_giant);
        return option(pair(result, result));
      }
    }).apply(dustBands), nucleus).lastOption().getOrElse(nucleus);
  }

  private Sequence<DustBand> UpdateDustLanes(Sequence<DustBand> dustBands, Planetismal tsml) {
    return flatten(dustBands.map(curry(dustExpansion).apply(tsml)));
  }

  /**
   * Compresses adjacent lanes that have the same status.
   *
   * @param dustBands
   */
  Sequence<DustBand> CompressDustLanes(Sequence<DustBand> dustBands) {
    Iterator<DustBand> bands = dustBands.iterator();
    if (!bands.hasNext()) return empty(DustBand.class);
    DustBand curr = bands.next();
    Sequence<DustBand> new_bands = sequence(curr);

    while (bands.hasNext()) {
      DustBand next = bands.next();
      if (curr.dust != next.dust || curr.gas != next.gas) {
        curr = next;
        new_bands = new_bands.add(curr);
      }
      curr.outer = next.outer;
    }

    return new_bands;
  }

  /**
   * Checks if there is any dust remaining in any bands inside the
   * bounds where planets can form.
   *
   * @param dustBands
   */
  boolean CheckDustLeft(Sequence<DustBand> dustBands) {
    return dustBands.exists(new Predicate<DustBand>() {
      @Override
      public boolean matches(DustBand curr) {
        return curr.dust && curr.outer >= inner_bound && curr.inner <= outer_bound;
      }
    });
  }

  /**
   * Searches the existing planet list for any that overlap with the
   * new planetismal.  If there is an overlap their effect radii,
   * the two planets are coalesced into one.
   */
  Sequence<Planetismal> CoalescePlanetismals(Sequence<Planetismal> planets, final Planetismal tsml) {
    Pair<Sequence<Planetismal>, Sequence<Planetismal>> divided = planets.sortBy(planetismalComparator).breakOn(new Predicate<Planetismal>() {
      @Override
      public boolean matches(Planetismal curr) {
        double dist = curr.axis - tsml.axis;
        double dist1, dist2;
        if (dist > 0.0) {
          dist1 = tsml.OuterEffectLimit() - tsml.axis;
          dist2 = curr.axis - curr.InnerEffectLimit();
        } else {
          dist1 = tsml.axis - tsml.InnerEffectLimit();
          dist2 = curr.OuterEffectLimit() - curr.axis;
        }

        return abs(dist) <= dist1 || abs(dist) <= dist2;
      }
    });

    Sequence<Planetismal> previous = divided.first();
    Option<Planetismal> target = divided.second().headOption();
    Sequence<Planetismal> next = divided.second().size() < 2 ? empty(Planetismal.class) : divided.second().tail();

    Planetismal value = (Planetismal) target.map(new Function1<Planetismal, Planetismal>() {
      @Override
      public Planetismal call(Planetismal curr) throws Exception {
        double new_mass = curr.mass + tsml.mass;
        double new_axis = new_mass / ((curr.mass / curr.axis) + (tsml.mass / tsml.axis));
        double term1 = curr.mass * sqrt(curr.axis * (1.0 - curr.eccn * curr.eccn));
        double term2 = tsml.mass * sqrt(tsml.axis * (1.0 - tsml.eccn * tsml.eccn));
        double term3 = (term1 + term2) / (new_mass * sqrt(new_axis));
        double term4 = 1.0 - term3 * term3;
        double new_eccn = sqrt(abs(term4));

        return new Planetismal(star, new_axis, new_eccn, new_mass, curr.gas_giant || tsml.gas_giant);
      }
    }).toEither(tsml).value();

    return flatten(sequence(previous, sequence(value), next));
  }

  public static void main(String[] args) {
    System.out.println(new Accrete().DistributePlanets().sortBy(planetismalComparator).toString("\n"));
  }

}

