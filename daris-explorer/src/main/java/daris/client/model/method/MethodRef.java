package daris.client.model.method;

import daris.client.model.object.DObjectRef;

public class MethodRef extends DObjectRef {

    public MethodRef(Method obj, boolean resolved) {
        super(obj, resolved);
    }

    public MethodRef(String route, String cid, String name,
            String description) {
        super(route, cid, name, description, 0, false);
    }

    public MethodRef(String cid, String name, String description) {
        this(null, cid, name, description);
    }

    public MethodRef(String cid) {
        this(null, cid, null, null);
    }

}
