package nig.mf.plugin.analyzer;

import java.io.IOException;
import java.io.InputStream;

public class FileSignatures {

    private static final int SIG_GZIP_OFFSET = 0;
    private static final byte[] SIG_GZIP = new byte[] { (byte) 0x1f, (byte) 0x8b, (byte) 0x08 };
    private static final int SIG_DICOM_OFFSET = 128;
    private static final byte[] SIG_DICOM = new byte[] { 'D', 'I', 'C', 'M' };
    private static final int SIG_NIFTI1_OFFSET = 344;
    private static final byte[] SIG_NIFTI1 = new byte[] { 'n', '+', '1', 0x0 };
    private static final byte[] SIG_NIFTI1_PAIR = new byte[] { 'n', '+', '1', 0x0 };
    private static final int SIG_MINC_OFFSET = 0;
    private static final byte[] SIG_MINC = new byte[] { 'C', 'D', 'F', 0x1 };
    private static final int SIG_TAR_OFFSET = 257;
    private static final byte[] SIG_TAR1 = new byte[] { 'u', 's', 't', 'a', 'r', 0x0, '0', '0' };
    private static final byte[] SIG_TAR2 = new byte[] { 'u', 's', 't', 'a', 'r', 0x20, 0x20, 0x0 };

    public static boolean isGZIP(InputStream in) throws Throwable {
        return hasMagic(in, SIG_GZIP, SIG_GZIP_OFFSET);
    }

    public static boolean isDICOM(InputStream in) throws Throwable {
        return hasMagic(in, SIG_DICOM, SIG_DICOM_OFFSET);
    }

    public static boolean isNIFTI1(InputStream in) throws Throwable {
        return hasMagic(in, SIG_NIFTI1, SIG_NIFTI1_OFFSET);
    }
    
    public static boolean isNIFTI1Pair(InputStream in) throws Throwable {
        return hasMagic(in, SIG_NIFTI1_PAIR, SIG_NIFTI1_OFFSET);
    }

    public static boolean isMINC(InputStream in) throws Throwable {
        return hasMagic(in, SIG_MINC, SIG_MINC_OFFSET);
    }

    public static boolean isTAR(InputStream in) throws Throwable {
        return hasMagic(in, SIG_TAR1, SIG_TAR_OFFSET) || hasMagic(in, SIG_TAR2, SIG_TAR_OFFSET);
    }

    public static boolean hasMagic(InputStream in, byte[] magic, int offset) throws Throwable {
        if (!in.markSupported()) {
            throw new IOException("The stream does not support mark.");
        }
        boolean hasMagic = true;
        try {
            in.mark(offset + magic.length);
            if (offset > 0) {
                skipFully(in, offset);
            }
            for (int i = 0; i < magic.length; i++) {
                if (magic[i] != (byte) in.read()) {
                    hasMagic = false;
                    break;
                }
            }
            in.reset();
        } catch (IOException e) {
            hasMagic = false;
        }
        return hasMagic;
    }

    private static void skipFully(InputStream in, int len) throws IOException {
        int nRead = 0;
        int nRemaining = len;
        byte[] buffer = new byte[512];
        do {
            nRead = in.read(buffer, 0, nRemaining);
            if (nRead >= 0) {
                nRemaining -= nRead;
            } else {
                throw new IOException("Stream is empty. However, there is still " + nRemaining + " bytes need to read.");
            }
        } while (nRemaining > 0);
    }
}
