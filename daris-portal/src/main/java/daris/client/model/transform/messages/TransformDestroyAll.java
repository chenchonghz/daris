package daris.client.model.transform.messages;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.Null;
import arc.mf.object.ObjectMessage;
import daris.client.model.transform.Transform.Status;

public class TransformDestroyAll extends ObjectMessage<Null> {

    private Set<Status.State> _states;

    public TransformDestroyAll(Collection<Status.State> states) {
        _states = new HashSet<Status.State>();
        if (states != null && !states.isEmpty()) {
            _states.addAll(states);
        }
    }

    @Override
    protected void messageServiceArgs(XmlWriter w) {
        w.add("ignore-dependants", true);
        // w.add("type", "kepler");
        for (Status.State state : _states) {
            w.add("status", state.toString());
        }
    }

    @Override
    protected String messageServiceName() {
        return "transform.destroy.all";
    }

    @Override
    protected Null instantiate(XmlElement xe) throws Throwable {
        return new Null();
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
