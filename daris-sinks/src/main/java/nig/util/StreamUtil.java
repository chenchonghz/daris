package nig.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Scanner;

public class StreamUtil {

    public static final int BUFFER_SIZE = 8192;

    public static long copy(final InputStream input, final OutputStream output) throws IOException {

        return copy(input, output, BUFFER_SIZE);

    }

    public static long copy(final InputStream input, final OutputStream output, int bufferSize) throws IOException {

        final byte[] buffer = new byte[bufferSize];
        int n = 0;
        long count = 0;
        while ((n = input.read(buffer)) > 0) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

    public static void save(InputStream in, File f) throws Throwable {
        BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(f));
        try {
            StreamUtil.copy(in, os);
        } finally {
            os.close();
            in.close();
        }
    }

    public static String toString(InputStream in) throws Throwable {
        Scanner scanner = null;
        String s = "";
        try {
            scanner = new Scanner(in, "UTF-8").useDelimiter("\\A");
            s = scanner.hasNext() ? scanner.next() : "";
        } finally {
            scanner.close();
        }
        return s;
    }
    
    public static void main(String[] args) throws Throwable {
        
        System.out.println(toString(new FileInputStream(new File("/tmp/1.txt"))));
    }

}
