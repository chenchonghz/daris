package nig.ssh.client.key;

import nig.ssh.client.Base64;
import nig.ssh.client.ByteUtil;

public class KeyPair {

    private String _privateKey;
    private String _publicKey;
    private String _passphrase;

    public KeyPair(String privateKey, String publicKey, String passphrase) throws Throwable {
        validate(privateKey, publicKey);
        _privateKey = privateKey;
        if (publicKey.split(" +").length == 1) {
            KeyType t = keyTypeFromPublicKey(publicKey);
            _publicKey = t.signature() + " " + publicKey;
        } else {
            _publicKey = publicKey;
        }
        _passphrase = passphrase;
    }

    public String publicKey() {
        return _publicKey;
    }

    public String privateKey() {
        return _privateKey;
    }

    public String passphrase() {
        return _passphrase;
    }

    public KeyType type() {
        if (_privateKey.indexOf("RSA PRIVATE KEY") != -1) {
            return KeyType.rsa;
        } else if (_privateKey.indexOf("DSA PRIVATE KEY") != -1) {
            return KeyType.dsa;
        } else {
            return null;
        }
    }

    private static void validate(String privateKey, String publicKey) throws Throwable {
        KeyType type1 = keyTypeFromPrivateKey(privateKey);
        KeyType type2 = keyTypeFromPublicKey(publicKey);
        if (type1 != type2) {
            throw new IllegalArgumentException("The algorithms for private key and public key do not match.");
        }
    }

    private static KeyType keyTypeFromPublicKey(String publicKey) throws Throwable {
        if (publicKey == null) {
            throw new IllegalArgumentException("Invalid public key: " + publicKey);
        }
        String[] parts = publicKey.split(" +");
        if (parts.length < 1) {
            throw new IllegalArgumentException("Invalid public key: " + publicKey);
        }
        String signature = null;
        if (parts.length == 1) {
            publicKey = publicKey.trim();
            byte[] kb = Base64.decode(publicKey.getBytes());
            if (kb.length < 11 || !"ssh".equals(new String(kb, 4, 3))) {
                throw new IllegalArgumentException("Invalid ssh public key: " + publicKey);
            }
            int algLen = ByteUtil.decodeUInt32(kb, 0);
            signature = new String(kb, 4, algLen);
        } else {
            signature = parts[0];
        }
        return KeyType.fromSignature(signature);
    }

    private static KeyType keyTypeFromPrivateKey(String privateKey) throws Throwable {
        if (privateKey.indexOf("RSA PRIVATE KEY") != -1) {
            return KeyType.rsa;
        } else if (privateKey.indexOf("DSA PRIVATE KEY") != -1) {
            return KeyType.dsa;
        } else {
            throw new IllegalArgumentException("Invalid private key.");
        }
    }

}
