package daris.io;

import java.io.InputStream;
import java.io.OutputStream;

public class StreamUtils {

    public static final int BUFFER_SIZE = 8192;

    public static void copy(InputStream in, OutputStream out,
            boolean closeInput, boolean closeOutput) throws Throwable {
        byte[] buf = new byte[BUFFER_SIZE];
        int len;
        try {
            while ((len = in.read(buf)) >= 0) {
                out.write(buf, 0, len);
            }
        } finally {
            out.flush();
            if (closeOutput) {
                out.close();
            }
            if (closeInput) {
                in.close();
            }
        }
    }

    public static void copy(InputStream in, OutputStream out) throws Throwable {
        copy(in, out, false, false);
    }

}
