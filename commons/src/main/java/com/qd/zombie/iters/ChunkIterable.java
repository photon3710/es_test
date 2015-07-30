package com.jidian.util.iters;

import java.util.Iterator;
import java.util.List;

public class ChunkIterable<T> implements Iterable<List<T>> {

	protected final Iterable<T> underlyingIter;
	protected final int chunkSize;

	public ChunkIterable(int chunkSize, Iterable<T> list) {
		this.chunkSize = chunkSize;
		this.underlyingIter = list;
	}

	@Override
	public Iterator<List<T>> iterator() {
		return new ChunkIterator<T>(chunkSize, underlyingIter.iterator());
	}
}
