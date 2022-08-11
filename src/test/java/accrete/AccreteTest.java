package accrete;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static java.util.Comparator.comparingDouble;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class AccreteTest {
    @Test
    public void shouldGenerate() {
        var seed = 1660075613494L;
        var random = new Random(seed);
        var gen = new Accrete();
        var pl = gen.DistributePlanets(random).sortBy(comparingDouble(a -> a.axis())).toList();
        var star = new Star(1.0, 1.0);

        var expected = List.of(new Planetesimal(star, 0.4178567041419378, 0.23408332543327948, 2.1680730674292936E-7, false), new Planetesimal(star, 0.6350509393941814, 0.1885303572412591, 3.2949153509418623E-7, false), new Planetesimal(star, 0.8673039148623127, 0.139988757614189, 4.514788162079317E-6, false), new Planetesimal(star, 1.8176450702804516, 0.047285701897312626, 1.777079528637176E-5, true), new Planetesimal(star, 3.7126415075316253, 0.023656279732320096, 3.802670328459427E-4, true), new Planetesimal(star, 8.125507250822741, 0.021658400938846523, 4.328868650915578E-4, true), new Planetesimal(star, 18.091880458244223, 0.020993942315183627, 4.390133878233909E-5, true), new Planetesimal(star, 38.43640080768976, 0.17377237150640693, 2.6604313052810256E-6, true), new Planetesimal(star, 48.405442866527956, 0.1276325052586728, 6.466022928344574E-8, false));

        assertPlanetsEquals(expected, pl);
    }

    private void assertPlanetsEquals(Iterable<Planetesimal> expected, Iterable<Planetesimal> actual) {
        var es = expected.iterator();
        var as = actual.iterator();
        var count = 0;
        while (as.hasNext() && es.hasNext()) {
            var e = es.next();
            var a = as.next();
            assertEquals(e.axis(), a.axis(), 0.0, "[%s] axis do not match".formatted(count));
            assertEquals(e.eccn(), a.eccn(), 0.0, "[%s] eccn do not match".formatted(count));
            assertEquals(e.mass(), a.mass(), 0.0, "[%s] mass do not match".formatted(count));
            assertEquals(e.gasGiant(), a.gasGiant(), "[%s] gas_giant do not match".formatted(count));
            count++;
        }
        if (es.hasNext() || as.hasNext()) fail();
    }
}