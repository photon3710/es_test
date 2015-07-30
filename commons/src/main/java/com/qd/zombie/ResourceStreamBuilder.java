package com.jidian.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by xiaoyun on 6/6/14.
 */
public class ResourceStreamBuilder<T> implements InputStreamBuilder {
    String fileName;
    final Class<T> type;

    public ResourceStreamBuilder(final Class<T> t) {
        type = t;
    }


    @Override
    public void init(Properties prop, String prefix) {
        OptionAccessor accessor = new OptionAccessor(prop, prefix);
        fileName = accessor.getValue("url");
        if (fileName == null) throw new RuntimeException("url is null, don't have file to work with.");
    }

    @Override
    public InputStream buildInputStream() throws IOException {
        return type.getClassLoader().getResourceAsStream(fileName);
    }
}
