package accrete;

import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.functions.Function1;
import com.googlecode.totallylazy.functions.Function2;

import java.util.Comparator;
import java.util.Random;

import static accrete.DoleParams.*;
import static accrete.Planetesimal.PROTOPLANET_MASS;
import static accrete.Planetesimal.RandomPlanetismal;
import static accrete.Sequences.partitionBy;
import static com.googlecode.totallylazy.Option.none;
import static com.googlecode.totallylazy.Option.option;
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
    public static final Function2<Planetesimal, DustBand, Sequence<DustBand>> dustExpansion = (tsml, curr) -> {
        double min = tsml.InnerSweptLimit();
        double max = tsml.OuterSweptLimit();
        boolean new_gas = curr.gas() && !tsml.gasGiant();

        // Current is...
        // Case 1: Wider
        if (curr.inner() < min && curr.outer() > max) {
            return sequence(
                    new DustBand(curr.inner(), min, curr.dust(), curr.gas()),
                    new DustBand(min, max, false, new_gas),
                    new DustBand(max, curr.outer(), curr.dust(), curr.gas()));
        }
        // Case 2: Outer
        else if (curr.inner() < max && curr.outer() > max) {
            return sequence(
                    new DustBand(curr.inner(), max, false, new_gas),
                    new DustBand(max, curr.outer(), curr.dust(), curr.gas()));
        }
        // Case 3: Inner
        else if (curr.inner() < min && curr.outer() > min) {
            return sequence(
                    new DustBand(curr.inner(), min, curr.dust(), curr.gas()),
                    new DustBand(min, curr.outer(), false, new_gas));
        }
        // Case 4: Narrower
        else if (curr.inner() >= min && curr.outer() <= max) {
            return sequence(new DustBand(curr.inner(), curr.outer(), false, new_gas));
        }
        return sequence(curr);
    };
    public static final Function2<Planetesimal, DustBand, Double> collectDust = (tsml, dustBand) -> {
        double swept_inner = tsml.InnerSweptLimit();
        double swept_outer = tsml.OuterSweptLimit();
        if (dustBand.outer() <= swept_inner || dustBand.inner() >= swept_outer) return 0.0;

        double dust_density = tsml.DustDensity();
        double crit_mass = tsml.CriticalMass();
        double mass_density = MassDensity(dust_density, crit_mass, tsml.mass());
        double density = !dustBand.gas() || tsml.mass() < crit_mass ? dust_density : mass_density;

        double swept_width = swept_outer - swept_inner;
        double outside = max(swept_outer - dustBand.outer(), 0);
        double inside = max(dustBand.inner() - swept_inner, 0);
        double width = swept_width - outside - inside;

        double term1 = 4.0 * PI * pow(tsml.axis(), 2);
        double term2 = 1.0 - tsml.eccn() * (outside - inside) / swept_width;
        double volume = term1 * tsml.ReducedMargin() * width * term2;

        return volume * density;
    };
    public static final Function2<Sequence<DustBand>, Planetesimal, Option<? extends Pair<? extends Planetesimal, ? extends Planetesimal>>> massAccretion = (dustBands, tsml) -> {
        var new_mass = dustBands.filter(DustBand::dust).map(apply(collectDust, tsml)).reduce(add).doubleValue();
        if (new_mass - tsml.mass() <= 0.001 * new_mass) return none();
        var result = new Planetesimal(tsml.star(), tsml.axis(), tsml.eccn(), new_mass, tsml.mass() >= tsml.CriticalMass());
        return option(pair(result, result));
    };
    public static final Function2<Planetesimal, Planetesimal, Planetesimal> coalesce = (tsml, curr) -> {
        var new_mass = curr.mass() + tsml.mass();
        var new_axis = new_mass / ((curr.mass() / curr.axis()) + (tsml.mass() / tsml.axis()));
        var term1 = curr.mass() * sqrt(curr.axis() * (1.0 - pow(curr.eccn(), 2)));
        var term2 = tsml.mass() * sqrt(tsml.axis() * (1.0 - pow(tsml.eccn(), 2)));
        var term3 = (term1 + term2) / (new_mass * sqrt(new_axis));
        var term4 = 1.0 - pow(term3, 2);
        var new_eccn = sqrt(abs(term4));

        return new Planetesimal(curr.star(), new_axis, new_eccn, new_mass, curr.gasGiant() || tsml.gasGiant());
    };
    public static final Function2<Planetesimal, Planetesimal, Boolean> tooClose = (tsml, curr) -> {
        var dist = curr.axis() - tsml.axis();
        double dist1, dist2;
        if (dist > 0.0) {
            dist1 = tsml.OuterEffectLimit() - tsml.axis();
            dist2 = curr.axis() - curr.InnerEffectLimit();
        } else {
            dist1 = tsml.axis() - tsml.InnerEffectLimit();
            dist2 = curr.OuterEffectLimit() - curr.axis();
        }

        return abs(dist) <= dist1 || abs(dist) <= dist2;
    };
    public static final Function1<Sequence<DustBand>, DustBand> compressBand = dustBands -> {
        var first = dustBands.first();
        var last = dustBands.last();
        return new DustBand(first.inner(), last.outer(), first.dust(), first.gas());
    };
    public static final Function1<DustBand, Integer> memoizeBand = o -> (o.dust() ? 10 : 0) + (o.gas() ? 1 : 0);
    public static final Function2<Star, DustBand, Boolean> bandIsInBounds = (star, curr) -> curr.outer() >= star.InnermostPlanet() && curr.inner() <= star.OutermostPlanet();

    public Sequence<Planetesimal> DistributePlanets(Random random) {
        var star = new Star(1.0, 1.0);
        var dustBands = sequence(new DustBand(InnerDustLimit(), OuterDustLimit(star.stellar_mass()), true, true));
        var planets = empty(Planetesimal.class);

        while (CheckDustLeft(dustBands, star)) {
            var tsml = AccreteDust(dustBands, RandomPlanetismal(random, star));
            if (sequence(0.0, PROTOPLANET_MASS).contains(tsml.mass())) continue;
            planets = CoalescePlanetismals(planets, tsml);
            dustBands = UpdateDustLanes(dustBands, tsml);
            dustBands = CompressDustLanes(dustBands);
        }

        return planets;
    }

    private boolean CheckDustLeft(Sequence<DustBand> dustBands, Star star) {
        return exists(dustBands, and(DustBand::dust, predicate(apply(bandIsInBounds, star))));
    }

    private Planetesimal AccreteDust(Sequence<DustBand> dustBands, Planetesimal nucleus) {
        return unfoldRight(apply(massAccretion, dustBands), nucleus).lastOption().getOrElse(nucleus);
    }

    private Sequence<DustBand> UpdateDustLanes(Sequence<DustBand> dustBands, Planetesimal tsml) {
        return flatten(map(dustBands, apply(dustExpansion, tsml)));
    }

    private Sequence<DustBand> CompressDustLanes(Sequence<DustBand> dustBands) {
        return map(partitionBy(memoizeBand, dustBands).toList(), compressBand);
    }

    private Sequence<Planetesimal> CoalescePlanetismals(Sequence<Planetesimal> planets, Planetesimal tsml) {
        var divided = sortBy(planets, axisComparator).breakOn(predicate(apply(tooClose, tsml)));

        var previous = divided.first();
        var value = sequence(headOption(divided.second()).map(apply(coalesce, tsml)).getOrElse(tsml));
        var next = divided.second().splitAt(1).second();

        return previous.join(value).join(next);
    }

    public static void main(String... args) {
        var random = new Random();
        random.setSeed(1660075613494L);
        System.out.println(new Accrete().DistributePlanets(random).sortBy(axisComparator).toString("\n"));
    }
}

