package daris.io;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class IOUtils {

    private static final Logger LOG = Logger.getLogger(IOUtils.class.getName());

    private static byte[] EXHAUST_BUFFER = new byte[8192];

    public static void exhaustInputStream(InputStream in) {
        if (in == null) {
            return;
        }
        try {
            try {
                while (in.read(EXHAUST_BUFFER) >= 0) {
                }
            } finally {
                in.close();
            }
        } catch (Throwable t) {
            LOG.log(Level.WARNING, "Failed to exhaust input stream.", t);
        }
    }

    public static String readString(InputStream in) throws Throwable {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = in.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        return result.toString("UTF-8");
    }

}
