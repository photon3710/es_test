package com.jidian.util.iters;

import java.util.Iterator;
import java.util.function.Function;

/**
 * Given an <code>Iterable</code> object and a function, yields an <code>Iterable</code> 
 * representing the result of applying that function to every element of the supplied <code>Iterable</code> 
 * 
 * @author b.elliottsmith from jjoost
 * 
 *
 * @param <X>
 * @param <Y>
 */
public class MappedIterable<X, Y> implements Iterable<Y> {

    private Iterable<? extends X> wrapped ;
    private Function<? super X, ? extends Y> map ;

    public MappedIterable(Iterable<? extends X> wrapped, Function<? super X, ? extends Y> map) {
        this.wrapped = wrapped;
        this.map = map ;
    }

    public Iterator<Y> iterator() {
        return new MappedIterator<X, Y>(wrapped.iterator(), map) ;
    }
}