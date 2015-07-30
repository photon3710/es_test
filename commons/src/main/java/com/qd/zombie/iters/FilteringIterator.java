package com.jidian.util.iters;

import java.util.Iterator;
import java.util.function.Predicate;

/**
 * Created by xiaoyun on 4/25/14.
 */
public class FilteringIterator<T> implements Iterator<T> {
    Iterator<? extends T> iter;
    final Predicate<T> predicate;
    private T nextElement = null;

    public FilteringIterator(Iterator<T> it, final Predicate<T> pred) {
        iter = it;
        predicate = pred;
    }

    @Override
    public boolean hasNext() {
        nextElement = nextMatch();
        return nextElement != null;
    }

    @Override
    public T next() {
        return nextElement;
    }

    private T nextMatch() {
        while (iter.hasNext()) {
            T o = iter.next();
            if (predicate.test(o)) {
                return o;
            }
        }
        return null;
    }
}
