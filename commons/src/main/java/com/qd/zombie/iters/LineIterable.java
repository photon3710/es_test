package com.jidian.util.iters;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.function.Function;

/**
 * This allow one to iterate through many lines from reader and have
 * these line converted to object of type T. For now, we
 * assume that this is not reentrant, or its iterator can only be called once..
 *
 * Created by xiaoyun on 4/24/14.
 */
public class LineIterable<T> implements Iterable<T> {
    /**
     * This wrap up the JsonReader as iterator so that we can use it uniformly with other tools.
     *
     * @param <T>
     */
    public static class LineIterator<T> implements Iterator<T> {
        BufferedReader reader;
        final Function<String, T> converter;

        String line = null;

        public LineIterator(Reader s, final Function<String, T> c) {
            reader = new BufferedReader(s);
            converter = c;
        }

        @Override
        public boolean hasNext() {
            try {
                line = reader.readLine();
                return line != null;
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException("IO Issues");
            }
        }

        @Override
        public T next() {
            return converter.apply(line);
        }
    }

    Reader reader;
    final Function<String, T> converter;

    public LineIterable(Reader s, final Function<String, T> c) {
        reader = s;
        converter = c;
    }

    @Override
    public Iterator<T> iterator() {
        return new LineIterator<T>(reader, converter);
    }
}
