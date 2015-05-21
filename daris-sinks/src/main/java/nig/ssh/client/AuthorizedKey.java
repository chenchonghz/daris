package nig.ssh.client;

public class AuthorizedKey {

    private String _algorithm;
    private String _key;
    private String _comment;

    public AuthorizedKey(String algorithm, String key, String comment) {
        this._algorithm = algorithm;
        this._key = key;
        this._comment = comment;
    }

    public AuthorizedKey(String key) throws Throwable {
        if (key == null) {
            throw new IllegalArgumentException("Invalid public key: " + key);
        }
        String[] parts = key.split(" +");
        if (parts.length < 1) {
            throw new IllegalArgumentException("Invalid public key: " + key);
        }
        if (parts.length == 1) {
            _key = key.trim();
            byte[] kb = Base64.decode(_key.getBytes());
            if (kb.length < 11 || !"ssh".equals(new String(kb, 4, 3))) {
                throw new IllegalArgumentException("Invalid ssh public key: " + _key);
            }
            int algLen = ByteUtil.decodeUInt32(kb, 0);
            _algorithm = new String(kb, 4, algLen);
            _comment = null;
        } else {
            _algorithm = parts[0];
            _key = parts[1];
            if (parts.length == 3) {
                _comment = parts[2];
            } else {
                _comment = null;
            }
        }
    }

    public String algorithm() {
        return _algorithm;
    }

    public String key() {
        return _key;
    }

    public String comment() {
        return _comment;
    }

    public void setComment(String comment) {
        _comment = comment;
    }

    @Override
    public final String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(_algorithm);
        sb.append(" ");
        sb.append(_key);
        if (_comment != null) {
            sb.append(" ");
            sb.append(_comment);
        }
        return sb.toString();
    }

    public static void main(String[] args) throws Throwable {
        String key = "AAAAB3NzaC1yc2EAAAABIwAAAQEAnUJvDjghQdse74q53HdaCnKlynk5FMf9AA0U3IoDvDuHilrkcjAXSI3dF+Ttrpijxdan/cWoWtLdqzZMyP+8reoa3usb5xCnxix38D4tysRrgGC3v3XObteTkqpeKQdz5MBxNHSy7Q7a22jycQOO3svf338l8siLLGq3ruZuIKWchAQolwXBFyfiW/NNEUVanvMIg6GIoIvA9tAeEokgTA571qEJFqtuS8pHKFyzkK9PaGMGpAkCyfc0XQCokE/zbE3bbovPsYFyS7xTXqA0xA85jCqu8fEEBK5KPfLnt26rCeX/rnGx6pOEfp9lLNTdR1Jnvr3p/L+Pjanop7UdbQ==";
        AuthorizedKey ak = new AuthorizedKey(key);
        System.out.print(ak.algorithm());
    }
}
