package com.jidian.util.iters;

import java.util.Iterator;
import java.util.function.Function;

/**
 * Given an <code>Iterator</code> object and a function, yields an <code>Iterator</code> 
 * representing the result of applying that function to every element of the supplied <code>Iterator</code> 
 * 
 * @author b.elliottsmith from jjoost
 *
 * @param <X>
 * @param <Y>
 */
public class MappedIterator<X, Y> implements Iterator<Y> {

    final Iterator<? extends X> base ;
    private final Function<? super X, ? extends Y> function ;

    public MappedIterator(Iterator<? extends X> base, Function<? super X, ? extends Y> function) {
        this.base = base ;
        this.function = function ;
    }

    public boolean hasNext() {
        return base.hasNext() ;
    }

    public Y next() {
        return function.apply(base.next()) ;
    }

    public void remove() {
        base.remove() ;
    }
    
}