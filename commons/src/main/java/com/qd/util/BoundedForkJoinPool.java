package com.qd.util;

import java.lang.Exception;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;
import java.util.function.Supplier;

/*
 * This is one time use only pool that allow people to submit the callable while
 * being throttled so that there is not too many callables get into this pool.
 * 
 */
public class BoundedForkJoinPool<T> {

    private final Semaphore semaphore;
    private final ForkJoinPool pool;
    private ValueAggregator<T> aggregator;
    private List<Future<T>> results = new ArrayList<>();

    public BoundedForkJoinPool(int bound, ValueAggregator<T> a) {
        semaphore = new Semaphore(bound);
        pool = new ForkJoinPool(bound);
        aggregator = a;
    }

    public void submitTask(final Supplier<T> command) throws InterruptedException {
        semaphore.acquire();
        try {
            Future<T> result = pool.submit(new Callable<T>() {
                @Override
                public T call() throws Exception {
                    try {
                        return command.get();
                    } finally {
                        semaphore.release();
                    }
                }
            });

            results.add(result);
        } catch (RejectedExecutionException e) {
            semaphore.release();
        }
    }

    public T get() throws InterruptedException, ExecutionException {
        for (Future<T> future : results) {
            aggregator.add(future.get());
        }
        return aggregator.get();
    }
}
