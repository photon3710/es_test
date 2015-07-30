package com.jidian.util.iters;

import java.util.Iterator;
import java.util.function.Predicate;

/**
 * This filters a iterator so that only good object will be iterated through.
 *
 * Created by xiaoyun on 4/25/14.
 */
public class FilteringIterable<T> implements Iterable<T> {

    Iterable<T> iterable;
    final Predicate<T> predicate;

    public FilteringIterable(Iterable<T> it, final Predicate<T> pred) {
        iterable = it;
        predicate = pred;
    }

    @Override
    public Iterator<T> iterator() {
        return new FilteringIterator<T>(iterable.iterator(), predicate);
    }
}
