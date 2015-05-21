package daris.client.model.query.filter.mf;

import arc.mf.client.util.IsNotValid;
import arc.mf.client.util.IsValid;
import arc.mf.client.util.Validity;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import daris.client.model.query.filter.Filter;

public class AssetModifiedFilter extends Filter {

    public static enum State {
        has_been_modified, has_not_been_modified;
        @Override
        public String toString() {
            return super.toString().toLowerCase().replace('_', ' ');
        }

        public static State parse(String s) {
            if (s != null) {
                State[] vs = values();
                for (State state : vs) {
                    if (s.toString().equals(state)) {
                        return state;
                    }
                }
            }
            return null;
        }
    }

    private State _state;

    public AssetModifiedFilter(State state) {
        _state = state;
    }

    public AssetModifiedFilter(XmlElement xe) {
        _state = State.parse(xe.value("state"));
    }

    public AssetModifiedFilter() {
        _state = null;
    }

    public State state() {
        return _state;
    }

    public void setState(State state) {
        _state = state;
    }

    @Override
    public void save(StringBuilder sb) {
        sb.append("asset " + _state);
    }

    @Override
    public void saveXml(XmlWriter w) {
        w.add("state", _state);
    }

    @Override
    public Validity valid() {
        if (_state == null) {
            return new IsNotValid("The state that indicates if asset has been modified is not set.");
        }
        return IsValid.INSTANCE;
    }

    @Override
    public Filter copy() {
        return new AssetModifiedFilter(state());
    }

}
