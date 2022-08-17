package accrete;

import com.googlecode.totallylazy.BinaryPredicate;
import com.googlecode.totallylazy.Sequence;

public class Sequences extends com.googlecode.totallylazy.Sequences {
    public static <T> Sequence<Sequence<T>> partitionWith(Sequence<T> xs, BinaryPredicate<T> f) {
        return xs.foldRight(empty(), (x, s) -> {
            if (s.isEmpty()) return one(one(x));
            var h = s.head();
            return f.matches(h.last(), x) ? cons(h.append(x), s.tail()) : cons(sequence(x), s);
        });
    }
}