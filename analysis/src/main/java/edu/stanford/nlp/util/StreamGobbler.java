package edu.stanford.nlp.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;

/**
 * Reads the output of a process started by Process.exec()
 * <p>
 * Adapted from:
 * <p>
 * http://www.velocityreviews.com/forums/t130884-process-runtimeexec-causes-subprocess-hang.html
 *
 * @author pado
 */

public class StreamGobbler extends Thread {

    InputStream is;
    Writer outputFileHandle;

    public StreamGobbler(InputStream is, Writer outputFileHandle) {
        this.is = is;
        this.outputFileHandle = outputFileHandle;
    }

    public void run() {

        try {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);

            for (String s; (s = br.readLine()) != null; ) {
                outputFileHandle.write(s);
                outputFileHandle.write("\n");
            }

            isr.close();
            br.close();
            outputFileHandle.flush();
        } catch (Exception ex) {
            System.out.println("Problem reading stream :" + is.getClass().getCanonicalName() + " " + ex);
            ex.printStackTrace();
        }

    }
}
