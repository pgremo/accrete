package accrete;

import com.googlecode.totallylazy.BinaryPredicate;
import com.googlecode.totallylazy.Sequence;

public class Sequences extends com.googlecode.totallylazy.Sequences {
    public static <T> Sequence<Sequence<T>> partitionWith(Sequence<T> xs, BinaryPredicate<T> f) {
        return xs.foldRight(empty(), (t, s) -> {
            if (s.isEmpty()) return one(one(t));
            var h = s.head();
            var x = h.last();
            return f.matches(x, t) ? cons(h.append(t), s.tail()) : cons(sequence(t), s);
        });
    }
}