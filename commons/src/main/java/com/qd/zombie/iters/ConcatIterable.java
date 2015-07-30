package com.jidian.util.iters;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This class can be used to lazily concatenate together zero or more
 * <code>Iterable</code> classes whose elements share a common super type
 * 
 * @author b.elliottsmith
 * 
 * @param <E>
 */
public class ConcatIterable<E> implements Iterable<E> {

	public static final class ConcatIterableIterator<E> implements Iterator<E> {
		Iterator<? extends E> current;
		final Iterator<? extends Iterable<? extends E>> next;

		public ConcatIterableIterator(
				Iterator<? extends Iterable<? extends E>> members) {
			next = members;
		}

		public boolean hasNext() {
			while ((current == null || !current.hasNext()) && next.hasNext())
				current = next.next().iterator();
			return current != null && current.hasNext();
		}

		public E next() {
			while ((current == null || !current.hasNext()) && next.hasNext())
				current = next.next().iterator();
			return current.next();
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	private final List<Iterable<? extends E>> members;

	public ConcatIterable(
			final Iterator<? extends Iterable<? extends E>> members) {
		this.members = new ArrayList<Iterable<? extends E>>();
		while (members.hasNext())
			this.members.add(members.next());
	}

	public ConcatIterable(
			final Iterable<? extends Iterable<? extends E>> members) {
		this.members = new ArrayList<Iterable<? extends E>>();
		for (Iterable<? extends E> member : members)
			this.members.add(member);
	}

	public ConcatIterable(final Iterable<? extends E>... members) {
		this.members = new ArrayList<Iterable<? extends E>>();
		for (Iterable<? extends E> member : members)
			this.members.add(member);
	}

	public Iterator<E> iterator() {
		return new ConcatIterableIterator<E>(members.iterator());
	}

	public void add(Iterable<? extends E> iter) {
		this.members.add(iter);
	}

}