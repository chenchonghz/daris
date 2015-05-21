package daris.client.model.sc.messages;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;
import daris.client.model.sc.ShoppingCart;
import daris.client.model.sc.ShoppingCartRef;

public class ShoppingCartExists extends ObjectMessage<Boolean> {
    
    public static final String SERVICE_NAME = "om.pssd.shoppingcart.exists";

	private long _cartId;

	public ShoppingCartExists(long cartId) {

		_cartId = cartId;
	}
	
	public ShoppingCartExists(ShoppingCartRef cart){
	    this(cart.id());
	}

	@Override
	protected void messageServiceArgs(XmlWriter w) {

		w.add("sid", _cartId);
	}

	@Override
	protected String messageServiceName() {

		return SERVICE_NAME;
	}

	@Override
	protected Boolean instantiate(XmlElement xe) throws Throwable {

		if (xe != null) {
			return xe.booleanValue("exists");
		}
		return null;
	}

	@Override
	protected String objectTypeName() {

		return ShoppingCart.TYPE_NAME;
	}

	@Override
	protected String idToString() {

		return Long.toString(_cartId);
	}

}
