package accrete;

import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.collections.PersistentCollection;
import com.googlecode.totallylazy.functions.Function2;

import java.util.Comparator;
import java.util.Random;

import static accrete.DoleParams.*;
import static accrete.Planetesimal.protoplanetMass;
import static accrete.Planetesimal.randomPlanetesimal;
import static accrete.Sequences.partitionWith;
import static com.googlecode.totallylazy.Option.none;
import static com.googlecode.totallylazy.Option.some;
import static com.googlecode.totallylazy.Pair.pair;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.Sequences.unfoldRight;
import static com.googlecode.totallylazy.collections.PersistentSortedSet.constructors.sortedSet;
import static com.googlecode.totallylazy.functions.Functions.apply;
import static com.googlecode.totallylazy.numbers.Numbers.add;
import static com.googlecode.totallylazy.predicates.Predicates.and;
import static java.lang.Math.*;
import static java.util.Comparator.comparingDouble;

public class Accrete {

    public static final Comparator<Planetesimal> axisComparator = comparingDouble(Planetesimal::axis);
    public static final Function2<Planetesimal, DustBand, Sequence<DustBand>> sweepBand = (tsml, curr) -> {
        var min = tsml.innerSweptLimit();
        var max = tsml.outerSweptLimit();
        var inner = curr.inner();
        var outer = curr.outer();
        var new_gas = curr.gas() && !tsml.gasGiant();

        // Current is...
        // Case 1: Wider
        if (inner < min && outer > max) {
            return sequence(
                    new DustBand(inner, min, curr.dust(), curr.gas()),
                    new DustBand(min, max, false, new_gas),
                    new DustBand(max, outer, curr.dust(), curr.gas())
            );
        }
        // Case 2: Outer
        else if (inner < max && outer > max) {
            return sequence(
                    new DustBand(inner, max, false, new_gas),
                    new DustBand(max, outer, curr.dust(), curr.gas())
            );
        }
        // Case 3: Inner
        else if (inner < min && outer > min) {
            return sequence(
                    new DustBand(inner, min, curr.dust(), curr.gas()),
                    new DustBand(min, outer, false, new_gas)
            );
        }
        // Case 4: Narrower
        else if (inner >= min && outer <= max) {
            return sequence(new DustBand(inner, outer, false, new_gas));
        }
        return sequence(curr);
    };
    public static final Function2<Planetesimal, DustBand, Double> collectDust = (tsml, dustBand) -> {
        var swept_inner = tsml.innerSweptLimit();
        var swept_outer = tsml.outerSweptLimit();
        if (dustBand.outer() <= swept_inner || dustBand.inner() >= swept_outer) return 0.0;

        var dust_density = tsml.dustDensity();
        var crit_mass = tsml.criticalMass();
        var mass_density = MassDensity(dust_density, crit_mass, tsml.mass());
        var density = !dustBand.gas() || tsml.mass() < crit_mass ? dust_density : mass_density;

        var swept_width = swept_outer - swept_inner;
        var outside = max(swept_outer - dustBand.outer(), 0);
        var inside = max(dustBand.inner() - swept_inner, 0);
        var width = swept_width - outside - inside;

        var term1 = 4.0 * PI * pow(tsml.axis(), 2);
        var term2 = 1.0 - tsml.eccn() * (outside - inside) / swept_width;
        var volume = term1 * tsml.reducedMargin() * width * term2;

        return volume * density;
    };
    public static final Function2<Sequence<DustBand>, Planetesimal, Option<? extends Pair<? extends Planetesimal, ? extends Planetesimal>>> accreteMass = (dustBands, tsml) -> {
        var new_mass = dustBands.filter(DustBand::dust).map(apply(collectDust, tsml)).reduce(add).doubleValue();
        if (new_mass - tsml.mass() <= 0.001 * new_mass) return none();
        var result = new Planetesimal(tsml.star(), tsml.axis(), tsml.eccn(), new_mass, tsml.mass() >= tsml.criticalMass());
        return some(pair(result, result));
    };
    private final Star star = new Star(1.0, 1.0);

    private boolean isDustLeft(Sequence<DustBand> dustBands) {
        return dustBands.exists(and(
                DustBand::dust,
                x -> x.outer() >= star.innermostPlanet() && x.inner() <= star.outermostPlanet()
        ));
    }

    private Planetesimal accreteDust(Sequence<DustBand> dustBands, Planetesimal nucleus) {
        return unfoldRight(apply(accreteMass, dustBands), nucleus).lastOption().getOrElse(nucleus);
    }

    private Sequence<DustBand> updateDustBands(Sequence<DustBand> dustBands, Planetesimal tsml) {
        return dustBands.flatMap(apply(sweepBand, tsml));
    }

    private Sequence<DustBand> compressDustBands(Sequence<DustBand> dustBands) {
        return partitionWith(dustBands, (x, y) -> x.dust() == y.dust() && x.gas() == y.gas())
                .map(xs -> {
                    var f = xs.first();
                    var l = xs.last();
                    return new DustBand(f.inner(), l.outer(), f.dust(), f.gas());
                });
    }

    private PersistentCollection<Planetesimal> coalescePlanetesimals(PersistentCollection<Planetesimal> source, Planetesimal x) {
        if (source.isEmpty()) return sortedSet(axisComparator, x);
        var h = source.head();
        var t = source.tail();
        return x.isTooClose(h) ? t.cons(x.coalesceWith(h)) : coalescePlanetesimals(t, x).cons(h);
    }

    public PersistentCollection<Planetesimal> distributePlanets(Random random) {
        var dustBands = sequence(new DustBand(innerDustLimit(), outerDustLimit(star.mass()), true, true));
        PersistentCollection<Planetesimal> planets = sortedSet(axisComparator);

        while (isDustLeft(dustBands)) {
            var tsml = accreteDust(dustBands, randomPlanetesimal(random, star));
            if (sequence(0.0, protoplanetMass).contains(tsml.mass())) continue;
            planets = coalescePlanetesimals(planets, tsml);
            dustBands = updateDustBands(dustBands, tsml);
            dustBands = compressDustBands(dustBands);
        }

        return planets;
    }

    public static void main(String... args) {
        System.out.println(new Accrete().distributePlanets(new Random()).toSequence().toString("\n"));
    }
}

