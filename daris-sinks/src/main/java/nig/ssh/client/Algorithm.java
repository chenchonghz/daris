package nig.ssh.client;

public enum Algorithm {
    RSA("ssh-rsa"), DSA("ssh-dss");

    private String _signature;

    Algorithm(String signature) {
        _signature = signature;
    }

    public final String signature() {
        return _signature;
    }

    @Override
    public final String toString() {
        return super.toString().toLowerCase();
    }

    public static Algorithm fromString(String s) {
        if (s != null) {
            if (RSA.toString().equalsIgnoreCase(s)) {
                return RSA;
            }
            if (DSA.toString().equalsIgnoreCase(s)) {
                return DSA;
            }
        }
        return null;
    }
}
