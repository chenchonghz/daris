package daris.client.model.sc.messages;

import java.util.Collections;
import java.util.List;
import java.util.Vector;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;
import daris.client.model.sc.ShoppingCartRef;
import daris.client.model.sc.Status;

public class ShoppingCartList extends ObjectMessage<List<ShoppingCartRef>> {

    public static final String SERVICE_NAME = "shopping.cart.list";

    private Status _status;

    public ShoppingCartList(Status status) {

        _status = status;
    }

    public ShoppingCartList() {
        this(null);
    }

    @Override
    protected void messageServiceArgs(XmlWriter w) {

        w.add("size", "infinity");
        if (_status != null) {
            w.add("status", _status.toString());
        }
    }

    @Override
    protected String messageServiceName() {

        return SERVICE_NAME;
    }

    @Override
    protected List<ShoppingCartRef> instantiate(XmlElement xe) throws Throwable {

        if (xe != null) {
            List<XmlElement> ces = xe.elements("cart");
            if (ces != null) {
                List<ShoppingCartRef> cs = new Vector<ShoppingCartRef>(ces.size());
                for (XmlElement ce : ces) {
                    cs.add(new ShoppingCartRef(ce.longValue("@id"), ce.value("@name"), Status.fromString(ce
                            .value("@status"))));
                }
                if (!cs.isEmpty()) {
                    Collections.sort(cs);
                    return cs;
                }
            }
        }
        return null;
    }

    @Override
    protected String objectTypeName() {

        return null;
    }

    @Override
    protected String idToString() {

        return null;
    }

}
