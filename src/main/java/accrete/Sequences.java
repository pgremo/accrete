package accrete;

import com.googlecode.totallylazy.BinaryPredicate;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.functions.Function1;

import static com.googlecode.totallylazy.Option.none;
import static com.googlecode.totallylazy.Option.option;
import static com.googlecode.totallylazy.Sequences.*;

public class Sequences {
    public static <T> Sequence<Sequence<T>> partitionWith(Sequence<T> xs, BinaryPredicate<T> f) {
        var callable = new Function1<Sequence<T>, Option<? extends Pair<? extends Sequence<T>, ? extends Sequence<T>>>>() {
            private T last;

            @Override
            public Option<? extends Pair<? extends Sequence<T>, ? extends Sequence<T>>> call(Sequence<T> x) {
                return isEmpty(x) ? none() : option(span(x, o -> {
                    var result = last == null || f.matches(last, o);
                    last = o;
                    return result;
                }));
            }
        };
        return unfoldRight(callable, xs);
    }
}