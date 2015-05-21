package daris.client.model.secure.wallet;

import daris.client.model.secure.wallet.SecureWalletEntry.Type;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlStringWriter;
import arc.mf.object.ObjectRef;

public class SecureWalletEntryRef extends ObjectRef<SecureWalletEntry> {

    public static interface Filter {
        boolean matches(SecureWalletEntryRef entry);
    }

    private String _key;
    private SecureWalletEntry.Type _type;
    private Usage _usage;

    public SecureWalletEntryRef(String key, SecureWalletEntry.Type type) {
        this(key, type, null);
    }

    public SecureWalletEntryRef(String key, Usage usage) {
        this(key, SecureWalletEntry.Type.string, usage);
    }

    public SecureWalletEntryRef(String key, SecureWalletEntry.Type type, Usage usage) {
        _key = key;
        _type = type;
        _usage = usage;
    }

    public SecureWalletEntryRef(XmlElement ke) {
        _key = ke.value();
        _type = SecureWalletEntry.Type.fromString(ke.value("@type"));
        if (_type == null) {
            _type = Type.string;
        }
        String usage = ke.value("@usage");
        if (usage != null) {
            _usage = new Usage(Usage.Type.fromString(ke.value("@usage-type")), usage);
        }
    }

    public String key() {
        return _key;
    }

    public SecureWalletEntry.Type type() {
        return _type;
    }

    public Usage usage() {
        return _usage;
    }

    @Override
    protected void resolveServiceArgs(XmlStringWriter w) {
        w.add("key", _key);
    }

    @Override
    protected String resolveServiceName() {
        return "secure.wallet.get";
    }

    @Override
    protected SecureWalletEntry instantiate(XmlElement xe) throws Throwable {
        if (xe != null && xe.element("value") != null) {
            return new SecureWalletEntry(xe.element("value"));
        }
        return null;
    }

    @Override
    public String referentTypeName() {
        return "secure wallet entry";
    }

    @Override
    public String idToString() {
        return _key;
    }

    @Override
    public boolean equals(Object o) {
        if (o != null && (o instanceof SecureWalletEntryRef)) {
            SecureWalletEntryRef eo = (SecureWalletEntryRef) o;
            return _key.equals(eo.key());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return _key.hashCode();
    }

    @Override
    public String toString() {
        return _key;
    }

}
