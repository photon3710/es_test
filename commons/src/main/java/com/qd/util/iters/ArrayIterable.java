package com.qd.util.iters;

import java.util.Iterator;

/**
 * This class creates an <code>Iterable</code> (i.e. a class with an
 * <code>iterator()</code> method) from the supplied array
 * 
 * @author b.elliottsmith
 * 
 * @param <E>
 *            the element type
 */
public final class ArrayIterable<E> implements Iterable<E> {

	private final E[] vals;
	private final int lb;
	private final int ub;

	@SuppressWarnings("unchecked")
	public ArrayIterable(E... vals) {
		this.vals = vals == null ? (E[]) new Object[0] : vals;
		lb = 0;
		ub = this.vals.length;
	}

	public ArrayIterable(E[] vals, int lb, int ub) {
		this.vals = vals;
		this.lb = lb;
		this.ub = ub;
	}

	public Iterator<E> iterator() {
		return new ArrayIterator<E>(vals, lb, ub);
	}
}