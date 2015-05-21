package nig.ssh.client;

import java.util.Arrays;

public class Base64 {

    private static final byte[] B64 = new byte[] { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
            'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h',
            'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2',
            '3', '4', '5', '6', '7', '8', '9', '+', '/', '=' };

    private static byte valueOf(byte c) {
        if (c == '=')
            return 0;
        for (int j = 0; j < B64.length; j++) {
            if (c == B64[j]) {
                return (byte) j;
            }
        }
        return 0;
    }

    public static byte[] decode(byte[] buf, int off, int len) {
        byte[] tmp = new byte[len];
        int j = 0;
        for (int i = off; i < off + len; i += 4) {
            tmp[j] = (byte) ((valueOf(buf[i]) << 2) | ((valueOf(buf[i + 1]) & 0x30) >>> 4));
            if (buf[i + 2] == (byte) '=') {
                j++;
                break;
            }
            tmp[j + 1] = (byte) (((valueOf(buf[i + 1]) & 0x0f) << 4) | ((valueOf(buf[i + 2]) & 0x3c) >>> 2));
            if (buf[i + 3] == (byte) '=') {
                j += 2;
                break;
            }
            tmp[j + 2] = (byte) (((valueOf(buf[i + 2]) & 0x03) << 6) | (valueOf(buf[i + 3]) & 0x3f));
            j += 3;
        }
        return Arrays.copyOfRange(tmp, 0, j);
    }

    public static byte[] decode(byte[] b) {
        return decode(b, 0, b.length);
    }

    public static byte[] encode(byte[] buf, int off, int len) {

        byte[] tmp = new byte[len * 2];
        int i, j, k;
        int foo = (len / 3) * 3 + off;
        i = 0;
        for (j = off; j < foo; j += 3) {
            k = (buf[j] >>> 2) & 0x3f;
            tmp[i++] = B64[k];
            k = (buf[j] & 0x03) << 4 | (buf[j + 1] >>> 4) & 0x0f;
            tmp[i++] = B64[k];
            k = (buf[j + 1] & 0x0f) << 2 | (buf[j + 2] >>> 6) & 0x03;
            tmp[i++] = B64[k];
            k = buf[j + 2] & 0x3f;
            tmp[i++] = B64[k];
        }

        foo = (off + len) - foo;
        if (foo == 1) {
            k = (buf[j] >>> 2) & 0x3f;
            tmp[i++] = B64[k];
            k = ((buf[j] & 0x03) << 4) & 0x3f;
            tmp[i++] = B64[k];
            tmp[i++] = (byte) '=';
            tmp[i++] = (byte) '=';
        } else if (foo == 2) {
            k = (buf[j] >>> 2) & 0x3f;
            tmp[i++] = B64[k];
            k = (buf[j] & 0x03) << 4 | (buf[j + 1] >>> 4) & 0x0f;
            tmp[i++] = B64[k];
            k = ((buf[j + 1] & 0x0f) << 2) & 0x3f;
            tmp[i++] = B64[k];
            tmp[i++] = (byte) '=';
        }
        return Arrays.copyOfRange(tmp, 0, i);
    }

    public static byte[] encode(byte[] b) {
        return encode(b, 0, b.length);
    }

}
