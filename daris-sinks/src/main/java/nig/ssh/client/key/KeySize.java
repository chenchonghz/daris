package nig.ssh.client.key;

public enum KeySize {

    b1024(1024), b2048(2048), b4096(4096);
    public final int size;

    KeySize(int size) {
        this.size = size;
    }

    public static String[] stringValues() {
        KeySize[] values = KeySize.values();
        String[] svs = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            svs[i] = Integer.toString(values[i].size);
        }
        return svs;
    }

    public static KeySize fromString(String s) throws Throwable {
        if (s == null) {
            return null;
        }
        KeySize[] vs = values();
        for (KeySize v : vs) {
            if (v.size == Integer.parseInt(s)) {
                return v;
            }
        }
        throw new IllegalArgumentException("Failed to parse key size: " + s);
    }
}