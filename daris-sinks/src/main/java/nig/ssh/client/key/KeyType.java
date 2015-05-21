package nig.ssh.client.key;

public enum KeyType {
    rsa, dsa;
    public String signature() {
        if (this == rsa) {
            return "ssh-rsa";
        } else {
            return "ssh-dss";
        }
    }

    public static KeyType fromString(String s) throws Throwable {
        if (s == null) {
            return null;
        }
        KeyType[] vs = values();
        for (KeyType v : vs) {
            if (v.name().equalsIgnoreCase(s)) {
                return v;
            }
        }
        throw new IllegalArgumentException("Failed to parse key type: " + s);
    }

    public static KeyType fromSignature(String signature) {
        if (rsa.signature().equals(signature)) {
            return rsa;
        }
        if (dsa.signature().equals(signature)) {
            return dsa;
        }
        throw new IllegalArgumentException("Invalid ssh key type: " + signature);
    }
}
