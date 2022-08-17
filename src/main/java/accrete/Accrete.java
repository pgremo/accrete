package accrete;

import com.googlecode.totallylazy.BinaryPredicate;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.functions.Function1;
import com.googlecode.totallylazy.functions.Function2;

import java.util.Comparator;
import java.util.Random;

import static accrete.DoleParams.*;
import static accrete.Planetesimal.PROTOPLANET_MASS;
import static accrete.Planetesimal.randomPlanetesimal;
import static accrete.Sequences.partitionWith;
import static com.googlecode.totallylazy.Option.none;
import static com.googlecode.totallylazy.Option.some;
import static com.googlecode.totallylazy.Pair.pair;
import static com.googlecode.totallylazy.Sequences.*;
import static com.googlecode.totallylazy.functions.Functions.apply;
import static com.googlecode.totallylazy.numbers.Numbers.add;
import static com.googlecode.totallylazy.predicates.Predicates.and;
import static com.googlecode.totallylazy.predicates.Predicates.predicate;
import static java.lang.Math.*;
import static java.util.Comparator.comparingDouble;

public class Accrete {

    public static final Comparator<Planetesimal> axisComparator = comparingDouble(Planetesimal::axis);
    public static final Function2<Planetesimal, DustBand, Sequence<DustBand>> sweepBand = (tsml, curr) -> {
        var min = tsml.InnerSweptLimit();
        var max = tsml.OuterSweptLimit();
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
        var swept_inner = tsml.InnerSweptLimit();
        var swept_outer = tsml.OuterSweptLimit();
        if (dustBand.outer() <= swept_inner || dustBand.inner() >= swept_outer) return 0.0;

        var dust_density = tsml.DustDensity();
        var crit_mass = tsml.CriticalMass();
        var mass_density = MassDensity(dust_density, crit_mass, tsml.mass());
        var density = !dustBand.gas() || tsml.mass() < crit_mass ? dust_density : mass_density;

        var swept_width = swept_outer - swept_inner;
        var outside = max(swept_outer - dustBand.outer(), 0);
        var inside = max(dustBand.inner() - swept_inner, 0);
        var width = swept_width - outside - inside;

        var term1 = 4.0 * PI * pow(tsml.axis(), 2);
        var term2 = 1.0 - tsml.eccn() * (outside - inside) / swept_width;
        var volume = term1 * tsml.ReducedMargin() * width * term2;

        return volume * density;
    };
    public static final Function2<Sequence<DustBand>, Planetesimal, Option<? extends Pair<? extends Planetesimal, ? extends Planetesimal>>> accreteMass = (dustBands, tsml) -> {
        var new_mass = dustBands.filter(DustBand::dust).map(apply(collectDust, tsml)).reduce(add).doubleValue();
        if (new_mass - tsml.mass() <= 0.001 * new_mass) return none();
        var result = new Planetesimal(tsml.star(), tsml.axis(), tsml.eccn(), new_mass, tsml.mass() >= tsml.CriticalMass());
        return some(pair(result, result));
    };
    public static final Function1<Sequence<DustBand>, DustBand> compressBand = dustBands -> {
        var first = dustBands.first();
        var last = dustBands.last();
        return new DustBand(first.inner(), last.outer(), first.dust(), first.gas());
    };
    public static final BinaryPredicate<DustBand> compareBand = (x, y) -> x.dust() == y.dust() && x.gas() == y.gas();
    public static final Function2<Star, DustBand, Boolean> bandIsInBounds = (star, curr) -> curr.outer() >= star.InnermostPlanet() && curr.inner() <= star.OutermostPlanet();

    private boolean CheckDustLeft(Sequence<DustBand> dustBands, Star star) {
        return exists(dustBands, and(DustBand::dust, predicate(apply(bandIsInBounds, star))));
    }

    private Planetesimal AccreteDust(Sequence<DustBand> dustBands, Planetesimal nucleus) {
        return unfoldRight(apply(accreteMass, dustBands), nucleus).lastOption().getOrElse(nucleus);
    }

    private Sequence<DustBand> UpdateDustLanes(Sequence<DustBand> dustBands, Planetesimal tsml) {
        return flatMap(dustBands, apply(sweepBand, tsml));
    }

    private Sequence<DustBand> CompressDustLanes(Sequence<DustBand> dustBands) {
        return map(partitionWith(dustBands, compareBand), compressBand);
    }

    private Sequence<Planetesimal> CoalescePlanetesimals(Sequence<Planetesimal> source, Planetesimal x) {
        if (source.isEmpty()) return sequence(x);
        var h = source.head();
        var t = source.tail();
        return x.isTooClose(h) ? cons(x.coalesceWith(h), t) : cons(h, CoalescePlanetesimals(t, x));
    }

    public Sequence<Planetesimal> DistributePlanets(Random random) {
        var star = new Star(1.0, 1.0);
        var dustBands = sequence(new DustBand(InnerDustLimit(), OuterDustLimit(star.stellar_mass()), true, true));
        var planets = empty(Planetesimal.class);

        while (CheckDustLeft(dustBands, star)) {
            var tsml = AccreteDust(dustBands, randomPlanetesimal(random, star));
            if (sequence(0.0, PROTOPLANET_MASS).contains(tsml.mass())) continue;
            planets = CoalescePlanetesimals(planets.sortBy(axisComparator), tsml);
            dustBands = UpdateDustLanes(dustBands, tsml);
            dustBands = CompressDustLanes(dustBands);
        }

        return planets;
    }

    public static void main(String... args) {
        System.out.println(new Accrete().DistributePlanets(new Random()).sortBy(axisComparator).toString("\n"));
    }
}

