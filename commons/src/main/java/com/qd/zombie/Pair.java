package com.jidian.util;

public class Pair<A extends Comparable<A>, B extends Comparable<B>> implements Comparable<Pair<A, B>> {

	public final A first;
	public final B second;

	public Pair(A first, B second) {
		this.first = first;
		this.second = second;
	}

	@Override
	public int compareTo(Pair<A, B> o) {
		int cmp = first.compareTo(o.first);
		return cmp == 0 ? second.compareTo(o.second) : cmp;
	}


	@Override
	public int hashCode() {
		int first_code = first == null ? 0 : first.hashCode();
		int second_code = second == null ? 0 : second.hashCode();
		return 31 * first_code + second_code;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Pair<?, ?>))
			return false;
		if (this == obj)
			return true;
		return equal(first, ((Pair<?, ?>) obj).first)
				&& equal(second, ((Pair<?, ?>) obj).second);
	}

	// todo move this to a helper class.
	private boolean equal(Object o1, Object o2) {
		return o1 == null ? o2 == null : (o1 == o2 || o1.equals(o2));
	}

	@Override
	public String toString() {
		return "(" + first + ", " + second + ')';
	}
}