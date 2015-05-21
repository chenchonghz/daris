package daris.client.model.query.filter;

import java.util.ArrayList;
import java.util.List;

import arc.mf.client.util.IsNotValid;
import arc.mf.client.util.IsValid;
import arc.mf.client.util.Validity;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;

public class CompositeFilter extends Filter {

    public static class Member {
        private LogicOperator _leftOp;
        private boolean _negated;
        private Filter _filter;
        private boolean _readOnly;

        public Member(XmlElement me) throws Throwable {
            _leftOp = LogicOperator.parse(me.value("@operator"));
            _negated = me.booleanValue("@negated", false);
            _filter = Filter.instantiate(me);
            _readOnly = false;
        }

        public Member(LogicOperator leftOp, boolean negated, Filter filter) {
            _leftOp = leftOp;
            _negated = negated;
            _filter = filter;
            _readOnly = false;
        }

        public boolean readOnly() {
            return _readOnly;
        }

        public void setReadOnly(boolean readOnly) {
            _readOnly = readOnly;
        }

        public boolean negated() {
            return _negated;
        }

        public void setNegated(boolean negated) {
            _negated = negated;
        }

        public LogicOperator operator() {
            return _leftOp;
        }

        public void setOperator(LogicOperator leftOp) {
            _leftOp = leftOp;
        }

        public Filter filter() {
            return _filter;
        }

        public void setFilter(Filter filter) {
            _filter = filter;
        }

        public void save(XmlWriter w) {
            _filter.save(w, new String[] { "operator", _leftOp == null ? null : _leftOp.toString(), "negated",
                    _negated ? "true" : null });
        }

        public void save(StringBuilder sb) {
            if (_leftOp != null) {
                sb.append(" " + _leftOp + " ");
            }
            sb.append("(");
            if (_negated) {
                sb.append("not (");
            }
            _filter.save(sb);
            if (_negated) {
                sb.append(")");
            }
            sb.append(")");
        }
    }

    private List<Member> _members;

    public CompositeFilter(List<Member> members) {
        _members = members;
    }

    public CompositeFilter(Member... members) {
        if (members != null) {
            for (Member m : members) {
                addMember(m);
            }
        }
    }

    public CompositeFilter(XmlElement xe) throws Throwable {
        List<XmlElement> mes = xe.elements("filter");
        if (mes != null && !mes.isEmpty()) {
            for (XmlElement me : mes) {
                addMember(new CompositeFilter.Member(me));
            }
        }
    }

    public List<Member> members() {
        return _members;
    }

    public void addMember(Member m) {
        if (_members == null) {
            _members = new ArrayList<Member>();
        }
        if (_members.isEmpty()) {
            m.setOperator(null);
        }
        _members.add(m);
    }

    public void addMember(LogicOperator leftOp, boolean negated, Filter filter) {
        addMember(new Member(leftOp, negated, filter));
    }

    public Member memberAt(int index) {
        if (_members == null) {
            return null;
        }
        return _members.get(index);
    }

    public void removeMember(int index) {
        if (_members != null) {
            _members.remove(index);
            if (!_members.isEmpty()) {
                _members.get(0).setOperator(null);
            }
        }

    }

    public int indexOf(Member m) {
        if (_members != null) {
            return _members.indexOf(m);
        }
        return -1;
    }

    @Override
    public void save(StringBuilder sb) {
        if (_members != null) {
            for (Member m : _members) {
                m.save(sb);
            }
        }
    }

    public boolean hasMembers() {
        return _members != null && !_members.isEmpty();
    }

    public boolean isEmpty() {
        return isEmpty(this);
    }

    @Override
    protected void saveXml(XmlWriter w) {
        if (_members != null) {
            for (Member m : _members) {
                m.save(w);
            }
        }
    }

    public static boolean isEmpty(CompositeFilter cf) {
        List<CompositeFilter.Member> ms = cf.members();
        if (ms == null || ms.isEmpty()) {
            return true;
        }
        for (Member m : ms) {
            Filter f = m.filter();
            if (f instanceof CompositeFilter) {
                if (isEmpty((CompositeFilter) f) == false) {
                    return false;
                }
            } else {
                return false;
            }
        }
        return true;
    }

    @Override
    public Validity valid() {
        if (!hasMembers()) {
            return new IsNotValid("At least one sub-filter is required.");
        }
        for (Member m : _members) {
            Filter f = m.filter();
            Validity v = f.valid();
            if (!v.valid()) {
                return v;
            }
        }
        return IsValid.INSTANCE;
    }

    @Override
    public Filter copy() {
        CompositeFilter ncf = new CompositeFilter();
        if (_members != null) {
            for (CompositeFilter.Member m : _members) {
                ncf.addMember(new CompositeFilter.Member(m.operator(), m.negated(), Filter.copy(m.filter())));
            }
        }
        return ncf;
    }

    public void clearMembers() {
        if (_members != null) {
            _members.clear();
        }
    }

    public static CompositeFilter wrapIfItIsNotComposite(Filter f) {
        if (f == null) {
            return new CompositeFilter();
        } else if (f instanceof CompositeFilter) {
            return (CompositeFilter) f;
        } else {
            return new CompositeFilter(new Member(null, false, f));
        }
    }

}
