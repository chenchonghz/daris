package nig.ssh.client;

import java.io.InputStream;
import java.io.OutputStream;

public class StreamCopy {

    public static void copy(InputStream in, OutputStream out) throws Throwable {

        byte[] buffer = new byte[1024];
        int len;
        while ((len = in.read(buffer)) != -1) {
            out.write(buffer, 0, len);
        }
    }

}
