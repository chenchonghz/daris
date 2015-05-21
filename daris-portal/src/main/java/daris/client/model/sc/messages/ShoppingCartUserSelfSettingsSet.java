package daris.client.model.sc.messages;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.Null;
import arc.mf.object.ObjectMessage;
import daris.client.model.sc.DeliveryArg;
import daris.client.model.sc.DeliveryMethod;
import daris.client.model.sc.Layout;
import daris.client.model.sc.ShoppingCart;

public class ShoppingCartUserSelfSettingsSet extends ObjectMessage<Null> {

    public static final String SERVICE_NAME = "om.pssd.shoppingcart.user.self.settings.set";

    private ShoppingCart _cart;

    public ShoppingCartUserSelfSettingsSet(ShoppingCart cart) {
        _cart = cart;
    }

    @Override
    protected void messageServiceArgs(XmlWriter w) {
        if (_cart.archive() != null) {
            w.push("archive");
            w.add("type", _cart.archive().type().typeName());
            Map<String, String> params = _cart.archive().params();
            if (params != null) {
                Set<String> names = params.keySet();
                if (names != null) {
                    for (String name : names) {
                        w.add("parameter", new String[] { "name", name }, params.get(name));
                    }
                }
            }
            w.pop();
        }

        if (_cart.destination() != null) {
            w.push("delivery");
            w.add("method", _cart.destination().method().name());
            if (_cart.destination().method() == DeliveryMethod.deposit) {
                w.add("destination", _cart.destination().sink().url());
            }
            Collection<DeliveryArg> deliveryArgs = _cart.destination().args();
            if (deliveryArgs != null) {
                for (DeliveryArg arg : deliveryArgs) {
                    if (arg.isDeliveryArg()) {
                        w.add("arg", new String[] { "name", arg.name() }, arg.value());
                    } else if (arg.isSecureWalletDeliveryArg()) {
                        w.add("arg", new String[] { "name", arg.name(), "in-secure-wallet", "true" }, arg.value());
                    }
                }
            }
            w.pop();
        }

        if (_cart.layout() != null) {
            w.push("layout");
            w.add("type", _cart.layout().type().name());
            if (_cart.layout().type() == Layout.Type.custom) {
                w.add("pattern", _cart.layout().pattern().pattern());
            }
            w.pop();

        }
    }

    @Override
    protected String messageServiceName() {
        return SERVICE_NAME;
    }

    @Override
    protected Null instantiate(XmlElement xe) throws Throwable {
        return new Null();
    }

    @Override
    protected String objectTypeName() {
        return ShoppingCart.TYPE_NAME;
    }

    @Override
    protected String idToString() {
        return Long.toString(_cart.id());
    }

}
