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
        var actual = gen.DistributePlanets(random).sortBy(comparingDouble(Planetesimal::axis)).toList();

        var star = new Star(1.0, 1.0);

        var expected = List.of(new Planetesimal(star, 0.4178567041419378, 0.23408332543327948, 2.167348292635512E-7, false),new Planetesimal(star, 0.6350509393941814, 0.1885303572412591, 3.293532824112172E-7, false),new Planetesimal(star, 0.8673035920013914, 0.13998847849128285, 4.5118455137081636E-6, false),new Planetesimal(star, 1.8176450702804516, 0.047285701897312626, 1.7700577489631473E-5, true),new Planetesimal(star, 3.7126415075316253, 0.023656279732320096, 3.793535256484035E-4, true),new Planetesimal(star, 8.125507250822741, 0.021658400938846523, 4.3191013213353304E-4, true),new Planetesimal(star, 18.091816336108582, 0.021010306994702587, 4.3781415778087645E-5, true),new Planetesimal(star, 38.437067887219754, 0.17393247153991004, 2.6539942281379695E-6, true),new Planetesimal(star, 48.4055131867508, 0.1276334274500348, 6.459424409599317E-8, false));

        assertPlanetsEquals(expected, actual);
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