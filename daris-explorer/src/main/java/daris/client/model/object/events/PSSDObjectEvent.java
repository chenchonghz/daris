package daris.client.model.object.events;

import arc.mf.event.Filter;
import arc.mf.event.SystemEvent;
import arc.utils.ObjectUtil;
import daris.client.model.CiteableIdUtils;
import daris.client.model.object.DObjectRef;

public class PSSDObjectEvent extends SystemEvent {

    public enum Action {
        CREATE, MODIFY, MEMBERS, DESTROY;
    }

    public static final String SYSTEM_EVENT_NAME = "pssd-object";

    private DObjectRef _o;
    private Action _action;

    public PSSDObjectEvent(DObjectRef o, Action action) {

        super(SYSTEM_EVENT_NAME, o.citeableId());
        _o = o;
        _action = action;
    }

    public Action action() {
        return _action;
    }

    public DObjectRef objectRef() {
        return _o;
    }

    @Override
    public boolean matches(Filter f) {

        if (!type().equals(f.type())) {
            return false;
        }
        switch (_action) {
        case CREATE:
        case DESTROY:
            if (f.object() == null) {
                // local object type: repository
                // event object type = project?
                return CiteableIdUtils.isProjectCID(object());
            } else {
                return CiteableIdUtils.isDirectParent(f.object(), object());
            }
        case MODIFY:
        case MEMBERS:
            return ObjectUtil.equals(f.object(), object());
        default:
            return ObjectUtil.equals(f.object(), object());
        }
    }

    public String toString() {
        return _action + ": " + _o.citeableId();
    }

}
