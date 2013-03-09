package accrete;

import com.googlecode.totallylazy.*;

import static com.googlecode.totallylazy.Option.none;
import static com.googlecode.totallylazy.Option.option;
import static com.googlecode.totallylazy.Pair.pair;
import static com.googlecode.totallylazy.Sequences.*;

public class Sequences {
  public static <T, K extends Comparable<? super K>> Sequence<Sequence<T>> partitionBy(final Callable1<T, K> f, Sequence<T> coll) {
    Callable1<Sequence<T>, Option<? extends Pair<? extends Sequence<T>, ? extends Sequence<T>>>> callable = new Callable1<Sequence<T>, Option<? extends Pair<? extends Sequence<T>, ? extends Sequence<T>>>>() {
      @Override
      public Option<? extends Pair<? extends Sequence<T>, ? extends Sequence<T>>> call(Sequence<T> o) throws Exception {
        if (isEmpty(o)) return none();
        Pair<Sequence<T>, Sequence<T>> split = splitAt(o, 1);
        T fst = split.first().first();
        final K fv = Callers.call(f, fst);
        Pair<Sequence<T>, Sequence<T>> result = split.second().span(new Predicate<T>() {
          @Override
          public boolean matches(T other) {
            return Callers.call(f, other).compareTo(fv) == 0;
          }
        });
        return option(pair(cons(fst, result.first()), result.second()));
      }
    };
    return unfoldRight(callable, coll);
  }
}