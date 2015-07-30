package com.qd.util;

/**
 * This interface allow one to aggregate the values that got add to sync.
 * 
 * @author xiaoyun
 * 
 * @param <T>
 */
public abstract class ValueAggregator<T> {
	public abstract void add(T t);

	public abstract T get();

	public static ValueAggregator<Integer> buildIntegerAggregator() {
		return new ValueAggregator<Integer>() {
			private int sum = 0;

			@Override
			public void add(Integer t) {
				sum += t.intValue();
			}

			@Override
			public Integer get() {
				return new Integer(sum);
			}
		};
	}

	public static ValueAggregator<Long> buildLongAggregator() {
		return new ValueAggregator<Long>() {
			private long sum = 0;

			@Override
			public void add(Long t) {
				sum += t.longValue();
			}

			@Override
			public Long get() {
				return new Long(sum);
			}
		};
	}

}
