package nig.ssh.client;

public class ByteUtil {

    static byte[] encodeUInt32(int value) {
        byte[] b = new byte[4];
        b[0] = (byte) ((value >>> 24) & 0xff);
        b[1] = (byte) ((value >>> 16) & 0xff);
        b[2] = (byte) ((value >>> 8) & 0xff);
        b[3] = (byte) (value & 0xff);
        return b;
    }

    public static int decodeUInt32(byte[] b, int off) {
        int b1 = b[off] & 0xff;
        int b2 = b[off + 1] & 0xff;
        int b3 = b[off + 2] & 0xff;
        int b4 = b[off + 3] & 0xff;
        return (((((b1 << 8) | b2) << 8) | b3) << 8) | b4;
    }

    public static void main(String[] args) {
        System.out.println(decodeUInt32(new byte[] { 0, 0, 0, 7 }, 0));
    }

}
