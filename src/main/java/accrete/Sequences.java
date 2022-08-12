package accrete;

import com.googlecode.totallylazy.Callers;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.functions.Function1;

import static com.googlecode.totallylazy.Option.none;
import static com.googlecode.totallylazy.Option.option;
import static com.googlecode.totallylazy.Pair.pair;
import static com.googlecode.totallylazy.Sequences.*;

public class Sequences {
    public static <T, K extends Comparable<? super K>> Sequence<Sequence<T>> partitionBy(final Function1<T, K> f, Sequence<T> coll) {
        var callable = new Function1<Sequence<T>, Option<? extends Pair<? extends Sequence<T>, ? extends Sequence<T>>>>() {
            @Override
            public Option<? extends Pair<? extends Sequence<T>, ? extends Sequence<T>>> call(Sequence<T> o) {
                if (isEmpty(o)) return none();
                var split = splitAt(o, 1);
                var fst = split.first().first();
                var fv = Callers.call(f, fst);
                var result = split.second().span(other -> Callers.call(f, other).compareTo(fv) == 0);
                return option(pair(cons(fst, result.first()), result.second()));
            }
        };
        return unfoldRight(callable, coll);
    }
}