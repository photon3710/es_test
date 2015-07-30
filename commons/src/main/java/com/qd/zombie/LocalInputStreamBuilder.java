package com.jidian.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * This implementation simply return local FileInputStream.
 *
 * Created by xiaoyun on 4/17/14.
 */
public class LocalInputStreamBuilder implements InputStreamBuilder {

    String fileName;

    @Override
    public void init(Properties prop, String prefix) {
        OptionAccessor accessor = new OptionAccessor(prop, prefix);
        fileName = accessor.getValue("url");
        if (fileName == null) throw new RuntimeException("url is null, don't have file to work with.");
    }

    @Override
    public InputStream buildInputStream() throws IOException {
        return new FileInputStream(fileName);
    }
}
