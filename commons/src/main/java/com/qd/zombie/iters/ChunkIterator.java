package com.jidian.util.iters;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * This converts the underlying iterator into iterator of chunk, where each chunk is a list of original
 * items.
 *  
 * @author xiaoyun
 *
 * @param <T>
 */
public class ChunkIterator<T> implements Iterator<List<T>> {
	int start = 0;
	int chunkSize;
	Iterator<T> underlyingIter;

	public ChunkIterator(int chunkSize, Iterator<T> iter) {
		this.chunkSize = chunkSize;
		underlyingIter = iter;
	}

	@Override
	public boolean hasNext() {
		return underlyingIter.hasNext();
	}

	@Override
	public List<T> next() {

		List<T> result = new ArrayList<T>();
		for (int i = 0; i < chunkSize && underlyingIter.hasNext(); ++i) {
			result.add(underlyingIter.next());
		}
		return result;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

};
