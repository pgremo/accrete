
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

import static accrete.DoleParams.*;
import static accrete.Planetismal.PROTOPLANET_MASS;
import static accrete.Planetismal.RandomPlanetismal;
import static com.googlecode.totallylazy.Callables.curry;
import static com.googlecode.totallylazy.Callers.call;
import static com.googlecode.totallylazy.Option.none;
import static com.googlecode.totallylazy.Option.option;
import static com.googlecode.totallylazy.Pair.pair;
import static com.googlecode.totallylazy.Predicates.and;
import static com.googlecode.totallylazy.Predicates.predicate;
import static com.googlecode.totallylazy.Sequences.*;
import static com.googlecode.totallylazy.numbers.Numbers.add;
import static java.lang.Math.*;
import static java.util.Arrays.asList;

public class Accrete {

  public static final Comparator<Planetismal> axisComparator = new Comparator<Planetismal>() {
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
  public static final Callable2<Sequence<DustBand>, Planetismal, Option<? extends Pair<? extends Planetismal, ? extends Planetismal>>> massAccretion = new Callable2<Sequence<DustBand>, Planetismal, Option<? extends Pair<? extends Planetismal, ? extends Planetismal>>>() {
    @Override
    public Option<? extends Pair<? extends Planetismal, ? extends Planetismal>> call(Sequence<DustBand> dustBands, Planetismal tsml) throws Exception {
      double new_mass = dustBands.map(curry(collectDust).apply(tsml)).reduce(add).doubleValue();
      if (new_mass - tsml.mass <= 0.001 * new_mass) return none();
      Planetismal result = new Planetismal(tsml.star, tsml.axis, tsml.eccn, new_mass, tsml.gas_giant);
      result.gas_giant = tsml.mass >= tsml.CriticalMass();
      return option(pair(result, result));
    }
  };
  public static final Callable2<Planetismal, Planetismal, Object> coalesce = new Callable2<Planetismal, Planetismal, Object>() {
    @Override
    public Planetismal call(Planetismal tsml, Planetismal curr) throws Exception {
      double new_mass = curr.mass + tsml.mass;
      double new_axis = new_mass / ((curr.mass / curr.axis) + (tsml.mass / tsml.axis));
      double term1 = curr.mass * sqrt(curr.axis * (1.0 - pow(curr.eccn, 2)));
      double term2 = tsml.mass * sqrt(tsml.axis * (1.0 - pow(tsml.eccn, 2)));
      double term3 = (term1 + term2) / (new_mass * sqrt(new_axis));
      double term4 = 1.0 - pow(term3, 2);
      double new_eccn = sqrt(abs(term4));

      return new Planetismal(curr.star, new_axis, new_eccn, new_mass, curr.gas_giant || tsml.gas_giant);
    }
  };
  public static final Callable2<Planetismal, Planetismal, Boolean> tooClose = new Callable2<Planetismal, Planetismal, Boolean>() {
    @Override
    public Boolean call(Planetismal tsml, Planetismal curr) {
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
  };
  public static final Callable1<Sequence<DustBand>, DustBand> compressBand = new Callable1<Sequence<DustBand>, DustBand>() {
    @Override
    public DustBand call(Sequence<DustBand> dustBands) throws Exception {
      DustBand first = dustBands.first();
      DustBand last = dustBands.last();
      return new DustBand(first.inner, last.outer, first.dust, first.gas);
    }
  };
  public static final Callable1<DustBand, Comparable> memoizeBand = new Callable1<DustBand, Comparable>() {
    @Override
    public Comparable call(DustBand o) throws Exception {
      return (o.dust ? 10 : 0) + (o.gas ? 1 : 0);
    }
  };
  public static final Predicate<DustBand> bandHasDust = new Predicate<DustBand>() {
    @Override
    public boolean matches(DustBand curr) {
      return curr.dust;
    }
  };
  public static final Callable2<Star, DustBand, Boolean> bandIsInBounds = new Callable2<Star, DustBand, Boolean>() {
    @Override
    public Boolean call(Star star, DustBand curr) {
      return curr.outer >= star.InnermostPlanet() && curr.inner <= star.OutermostPlanet();
    }
  };

  private Star star;

  public Accrete() {
    this(new Star(1.0, 1.0));
  }

  public Accrete(Star star) {
    this.star = star;
  }

  public Sequence<Planetismal> DistributePlanets() {
    Sequence<DustBand> dustBands = sequence(new DustBand(InnerDustLimit(), OuterDustLimit(star.stellar_mass)));
    Sequence<Planetismal> planets = empty(Planetismal.class);

    while (CheckDustLeft(dustBands)) {
      Planetismal tsml = AccreteDust(dustBands, RandomPlanetismal(star));
      if (tsml.mass == 0.0 || tsml.mass == PROTOPLANET_MASS) continue;
      dustBands = UpdateDustLanes(dustBands, tsml);
      dustBands = CompressDustLanes(dustBands);
      planets = CoalescePlanetismals(planets, tsml);
    }

    return planets;
  }

  private Planetismal AccreteDust(Sequence<DustBand> dustBands, Planetismal nucleus) {
    return unfoldRight(curry(massAccretion).apply(dustBands), nucleus).lastOption().getOrElse(nucleus);
  }

  private Sequence<DustBand> UpdateDustLanes(Sequence<DustBand> dustBands, Planetismal tsml) {
    return flatten(dustBands.map(curry(dustExpansion).apply(tsml)));
  }

  private Sequence<DustBand> CompressDustLanes(Sequence<DustBand> dustBands) {
    return partitionBy(memoizeBand, dustBands).map(compressBand);
  }

  private boolean CheckDustLeft(Sequence<DustBand> dustBands) {
    return dustBands.exists(and(bandHasDust, predicate(curry(bandIsInBounds).apply(star))));
  }

  private Sequence<Planetismal> CoalescePlanetismals(Sequence<Planetismal> planets, final Planetismal tsml) {
    Pair<Sequence<Planetismal>, Sequence<Planetismal>> divided = planets.sortBy(axisComparator).breakOn(predicate(curry(tooClose).apply(tsml)));

    Sequence<Planetismal> previous = divided.first();
    Option<Planetismal> target = divided.second().headOption();
    Sequence<Planetismal> next = divided.second().size() < 2 ? empty(Planetismal.class) : divided.second().tail();

    Planetismal value = (Planetismal) target.map(curry(coalesce).apply(tsml)).getOrElse(tsml);

    return previous.join(sequence(value)).join(next);
  }

  public static <T, K extends Comparable<? super K>> Sequence<Sequence<T>> partitionBy(final Callable1<T, K> f, Sequence<T> coll) {
    if (coll.size() < 2) return sequence(asList(coll));
    T fst = coll.first();
    final K fv = call(f, fst);
    Pair<Sequence<T>, Sequence<T>> result = coll.tail().span(new Predicate<T>() {
      @Override
      public boolean matches(T other) {
        return call(f, other).compareTo(fv) == 0;
      }
    });
    return cons(cons(fst, result.first()), partitionBy(f, result.second()));
  }

  public static void main(String[] args) {
    System.out.println(new Accrete().DistributePlanets().sortBy(axisComparator).toString("\n"));
  }

}

