package daris.client.model.sc;

import arc.mf.client.xml.XmlElement;

public class Owner {

    public static enum Type {
        user;
        public static Type fromString(String type) {
            if (type != null) {
                Type[] vs = values();
                for (Type v : vs) {
                    if (v.name().equals(type)) {
                        return v;
                    }
                }
            }
            return null;
        }
    }

    public final Type type;
    public final String owner;

    private Owner(XmlElement oe) {
        type = Type.fromString(oe.value("@type"));
        owner = oe.value();
    }

    public static Owner instantiate(XmlElement oe) {
        if (oe != null) {
            return new Owner(oe);
        }
        return null;
    }

}
