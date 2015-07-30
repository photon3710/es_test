package com.jidian.util;

import com.google.gson.Gson;

import java.io.StringReader;
import java.util.function.Function;

/**
 * This converts String to object of given type T using Gson library, assuming that
 * the line is the json serialization of object of type T.
 * <p>
 * Created by xiaoyun on 4/26/14.
 */
public class GsonConverter<T> implements Function<String, T> {

    final Class<T> type;
    final Gson gson = new Gson();

    public GsonConverter(final Class<T> t) {
        type = t;
    }

    @Override
    public T apply(String s) {
        return gson.fromJson(new StringReader(s), type);
    }
}
