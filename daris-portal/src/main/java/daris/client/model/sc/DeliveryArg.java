package daris.client.model.sc;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import arc.mf.client.util.ObjectUtil;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import daris.client.model.secure.wallet.SecureWalletEntry;
import daris.client.model.secure.wallet.SecureWalletEntryRef;
import daris.client.model.secure.wallet.SecureWalletKeyEnumerationDataSource;
import daris.client.model.secure.wallet.Usage;

public class DeliveryArg {

    public static enum Type {
        delivery_arg, secure_delivery_arg, secure_wallet_delivery_arg;

        @Override
        public final String toString() {
            return name().replace('_', '-');
        }
    }

    private String _name;
    private String _value;
    private boolean _committed;
    private Type _type;

    public DeliveryArg(String name, Type type, Object value) {
        _name = name;
        _type = type;
        _value = value == null ? null : String.valueOf(value);
        _committed = false;
    }

    public DeliveryArg(XmlElement ae) {
        _name = ae.value("@name");
        if (ae.name().equals(Type.delivery_arg.toString())) {
            _type = Type.delivery_arg;
            _value = ae.value();
        } else if (ae.name().equals(Type.secure_delivery_arg.toString())) {
            if (ae.value("@source") != null) {
                _value = ae.value("@source");
                _type = Type.secure_wallet_delivery_arg;
            } else {
                _value = ae.value();
                _type = Type.secure_delivery_arg;
            }
        }
        _committed = true;
    }

    public String name() {
        return _name;
    }

    public String value() {
        return _value;
    }

    public String source() {
        if (_type == Type.secure_wallet_delivery_arg) {
            return _value;
        } else {
            return null;
        }
    }

    public Type type() {
        return _type;
    }

    public boolean isDeliveryArg() {
        return _type == Type.delivery_arg;
    }

    public boolean isSecureDeliveryArg() {
        return _type == Type.secure_delivery_arg;
    }

    public boolean isSecureWalletDeliveryArg() {
        return _type == Type.secure_wallet_delivery_arg;
    }

    public boolean committed() {
        return _committed;
    }

    public void saveUpdateArgs(XmlWriter w) {
        if (_committed && _type == Type.secure_delivery_arg) {
            return;
        }
        String elementName = _type.toString();
        w.add(elementName, new String[] { "name", _name }, String.valueOf(_value));
    }

    public static Map<String, DeliveryArg> instantiate(XmlElement ce) {
        Map<String, DeliveryArg> args = new LinkedHashMap<String, DeliveryArg>();
        List<XmlElement> daes = ce.elements(Type.delivery_arg.toString());
        if (daes != null) {
            for (XmlElement dae : daes) {
                DeliveryArg arg = new DeliveryArg(dae);
                args.put(arg.name(), arg);
            }
        }
        List<XmlElement> sdaes = ce.elements(Type.secure_delivery_arg.toString());
        if (sdaes != null) {
            for (XmlElement sdae : sdaes) {
                DeliveryArg arg = new DeliveryArg(sdae);
                args.put(arg.name(), arg);
            }
        }
        List<XmlElement> swdaes = ce.elements(Type.secure_wallet_delivery_arg.toString());
        if (swdaes != null) {
            for (XmlElement swdae : swdaes) {
                DeliveryArg arg = new DeliveryArg(swdae);
                args.put(arg.name(), arg);
            }
        }
        if (args.isEmpty()) {
            return null;
        } else {
            return args;
        }
    }

    public static final SecureWalletEntryRef.Filter SECURE_WALLET_ENTRY_FILTER = new SecureWalletEntryRef.Filter() {

        @Override
        public boolean matches(SecureWalletEntryRef entry) {
            if (entry.usage() == null) {
                if (entry.type() == SecureWalletEntry.Type.string) {
                    return true;
                }
            } else {
                if (ObjectUtil.equals(entry.usage(), Usage.SHOPPING_CART_DELIVERY)) {
                    return true;
                }
            }
            return false;
        }
    };

    public static SecureWalletKeyEnumerationDataSource getSecureWalletKeyEnum() {
        return new SecureWalletKeyEnumerationDataSource(DeliveryArg.SECURE_WALLET_ENTRY_FILTER);
    }
}