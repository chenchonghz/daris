package daris.util;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;

public class ChecksumUtils {

    public static String getChecksum(InputStream in, String type) throws Throwable {
        if ("crc32".equalsIgnoreCase(type)) {
            return ChecksumUtils.getCRC32(in);
        } else if ("md5".equalsIgnoreCase(type)) {
            return ChecksumUtils.getMD5(in);
        } else if ("sha1".equalsIgnoreCase(type)) {
            return ChecksumUtils.getSHA1(in);
        } else if ("sha256".equalsIgnoreCase(type)) {
            return ChecksumUtils.getSHA256(in);
        } else {
            throw new IllegalArgumentException("Unknown checksum type: " + type);
        }
    }

    public static String getCRC32(InputStream in) throws Throwable {
        CheckedInputStream cin = new CheckedInputStream(new BufferedInputStream(in), new CRC32());
        byte[] buffer = new byte[1024];
        try {
            while (cin.read(buffer) != -1) {
                // Read file in completely
            }
        } finally {
            cin.close();
            in.close();
        }
        long value = cin.getChecksum().getValue();
        return Long.toHexString(value);
    }

    public static String getSHA1(InputStream in) throws Throwable {
        return getDigest("SHA-1", in);
    }

    public static String getSHA256(InputStream in) throws Throwable {
        return getDigest("SHA-256", in);
    }

    public static String getMD5(InputStream in) throws Throwable {
        return getDigest("MD5", in);
    }

    public static byte[] getDigest(InputStream in, String algorithm) throws Throwable {
        MessageDigest md = MessageDigest.getInstance(algorithm);
        DigestInputStream dis = new DigestInputStream(in, md);
        try {
            byte[] buffer = new byte[1024];
            while (dis.read(buffer) != -1) {
                // Read the stream fully
            }
        } finally {
            dis.close();
            in.close();
        }
        return md.digest();
    }

    public static String getDigest(String algorithm, InputStream in) throws Throwable {
        return toHexString(getDigest(in, algorithm));
    }

    public static String toHexString(byte[] bytes) {
        BigInteger bi = new BigInteger(1, bytes);
        return String.format("%0" + (bytes.length << 1) + "x", bi);
    }
}
