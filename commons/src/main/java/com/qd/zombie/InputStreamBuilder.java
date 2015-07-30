package com.jidian.util;

import java.io.IOException;
import java.io.InputStream;
import java.rmi.NoSuchObjectException;
import java.util.Properties;

/**
 * This interface is used to build inputstream for other
 * part of system to consume.
 *
 * Created by xiaoyun on 4/17/14.
 */
public interface InputStreamBuilder {

    /**
     * This method is used to configure the builder from Properties.
     *
     * @param prop
     */
    public void init(Properties prop, String prefix);


    /**
     * Build the stream based on the configuration. It throws ioexception.
     *
     * @return
     */
    public InputStream buildInputStream() throws IOException;


    /**
     * This creates the fully configured InputStreamBuilder based on properties, and prefix.
     *
     * @param prop
     * @param prefix
     * @return
     */
    static InputStreamBuilder getBuilder(Properties prop, String prefix) {
        OptionAccessor accessor = new OptionAccessor(prop, prefix);
        String builderName = accessor.getValue("InputStreamBuilder", "com.jidian.util.LocalInputStreamBuilder");
        InputStreamBuilder builder = null;
        try {
            builder = (InputStreamBuilder) Class.forName(builderName).newInstance();
            builder.init(prop, prefix);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        return builder;
    }
}
