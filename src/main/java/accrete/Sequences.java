package accrete;

import com.googlecode.totallylazy.*;

import java.util.Arrays;

public class Sequences extends com.googlecode.totallylazy.Sequences {
  public static <T, K extends Comparable<? super K>> Sequence<Sequence<T>> partitionBy(final Callable1<T, K> f, Sequence<T> coll) {
    if (coll.size() < 2) return com.googlecode.totallylazy.Sequences.sequence(Arrays.asList(coll));
    T fst = coll.first();
    final K fv = Callers.call(f, fst);
    Pair<Sequence<T>, Sequence<T>> result = coll.tail().span(new Predicate<T>() {
      @Override
      public boolean matches(T other) {
        return Callers.call(f, other).compareTo(fv) == 0;
      }
    });
    return com.googlecode.totallylazy.Sequences.cons(com.googlecode.totallylazy.Sequences.cons(fst, result.first()), partitionBy(f, result.second()));
  }

  public static <T> Sequence<T> tail(final Iterable<? extends T> iterable) {
    if (size(iterable) < 2) return empty();
    return com.googlecode.totallylazy.Sequences.tail(iterable);
  }
}