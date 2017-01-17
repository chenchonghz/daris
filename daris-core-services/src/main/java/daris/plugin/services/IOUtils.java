package daris.plugin.services;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class IOUtils {

    public static final int BUFFER_SIZE = 8192;

    public static void copy(InputStream in, OutputStream out, boolean closeInput, boolean closeOutput)
            throws Throwable {
        byte[] buffer = new byte[BUFFER_SIZE];
        int len;
        try {
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
        } finally {
            Throwable t = null;
            if (closeInput) {
                try {
                    in.close();
                } catch (Throwable ex) {
                    t = ex;
                }
            }
            if (closeOutput) {
                try {
                    out.close();
                } catch (Throwable ex) {
                    t = ex;
                }
            }
            if (t != null) {
                throw t;
            }
        }
    }

    public static void copy(InputStream in, OutputStream out) throws Throwable {
        copy(in, out, false, false);
    }

    public static void copy(InputStream in, File outputFile, boolean closeInput) throws IOException {
        try {
            Files.copy(in, outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } finally {
            if (closeInput) {
                in.close();
            }
        }
    }

    public static void copy(InputStream in, File outputFile) throws IOException {
        copy(in, outputFile, false);
    }

    public static void copy(File inputFile, OutputStream out, boolean closeOutput) throws IOException {
        try {
            Files.copy(inputFile.toPath(), out);
        } finally {
            if (closeOutput) {
                out.close();
            }
        }
    }

    public static void copy(File inputFile, OutputStream out) throws IOException {
        copy(inputFile, out, false);
    }

    public static void copy(File inputFile, File outputFile) throws IOException {
        Files.copy(inputFile.toPath(), outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

}
