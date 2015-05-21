package daris.client.model.sc.messages;

import java.util.List;

import arc.mf.client.Output;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.Null;
import arc.mf.object.ObjectMessage;
import arc.mf.object.ObjectResolveHandler;

import com.google.gwt.i18n.client.DateTimeFormat;

import daris.client.model.sc.ShoppingCart;
import daris.client.model.sc.ShoppingCartRef;

public class ShoppingCartOutputRetrieve extends ObjectMessage<Null> {

    public static final String SERVICE_NAME = "shopping.cart.output.retrieve";

    private ShoppingCartRef _cart;

    public ShoppingCartOutputRetrieve(ShoppingCartRef cart) {
        _cart = cart;
    }
    
    public ShoppingCartOutputRetrieve(ShoppingCart cart){
        this(new ShoppingCartRef(cart));
    }

    @Override
    protected void messageServiceArgs(XmlWriter w) {

        w.add("sid", _cart.id());
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

    @Override
    protected int numberOfOutputs() {

        return 1;
    }

    @Override
    protected void process(Null o, final List<Output> outputs) {

        if (outputs != null && !outputs.isEmpty()) {
            new ShoppingCartRef(_cart.id()).resolve(new ObjectResolveHandler<ShoppingCart>() {
                @Override
                public void resolved(ShoppingCart cart) {
                    for (Output output : outputs) {
                        output.download(generateArchiveFileName(cart));
                    }
                }
            });

        }
    }

    public static String generateArchiveFileName(ShoppingCart cart) {
        String ext = cart.archive().type().extension();
        String filename = "DARIS_SC_" + cart.id();
        if (cart.name() != null) {
            filename += "_" + cart.name();
        }
        filename += "_" + DateTimeFormat.getFormat("yyyy.MM.dd_HHmmss").format(cart.changed());
        filename += (ext != null ? ("." + ext) : "");
        return filename;
    }
}
