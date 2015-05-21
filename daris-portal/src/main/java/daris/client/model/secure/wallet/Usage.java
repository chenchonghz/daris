package daris.client.model.secure.wallet;

import arc.mf.client.util.ObjectUtil;

public class Usage {

    public static final Usage SHOPPING_CART_DELIVERY = new Usage(Usage.Type.system, "shopping.cart.delivery");

    public static enum Type {
        system, service;
        public static final Type fromString(String s) {
            if (s != null) {
                Type[] vs = values();
                for (int i = 0; i < vs.length; i++) {
                    if (vs[i].name().equals(s)) {
                        return vs[i];
                    }
                }
            }
            return null;
        }
    }

    public final Type type;
    public final String usage;

    @Override
    public boolean equals(Object o) {
        if (o != null && o instanceof Usage) {
            Usage uo = (Usage) o;
            return uo.type == this.type && ObjectUtil.equals(this.usage, uo.usage);
        }
        return false;
    }

    public Usage(Usage.Type type, String usage) {
        this.type = type;
        this.usage = usage;
    }
}
