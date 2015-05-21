package daris.client.model.query.filter.pssd;

import java.util.List;

import arc.mf.client.util.IsNotValid;
import arc.mf.client.util.Validity;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import daris.client.model.object.DObjectRef;
import daris.client.model.query.filter.CompositeFilter;
import daris.client.model.query.filter.Filter;

public class ObjectCompositeFilter extends CompositeFilter implements ProjectAware {

    private DObjectRef _project;

    public ObjectCompositeFilter(XmlElement xe) throws Throwable {
        this(new DObjectRef(xe.value("project")), new CompositeFilter(xe));
    }

    public ObjectCompositeFilter(DObjectRef project) {
        this(project, null);
    }

    public ObjectCompositeFilter(DObjectRef project, CompositeFilter cf) {
        _project = project;
        if (cf != null) {
            List<Member> members = cf.members();
            if (members != null) {
                for (Member m : members) {
                    addMember(new Member(m.operator(), m.negated(), Filter.copy(m.filter())));
                }
            }
        }
    }

    public DObjectRef project() {
        return _project;
    }

    public void setProject(DObjectRef project) {
        _project = project;
    }

    @Override
    public void save(StringBuilder sb) {

        sb.append("(cid starts with '" + _project.id() + "')");
        if (!isEmpty()) {
            sb.append(" and (");
            super.save(sb);
            sb.append(")");
        }
    }

    @Override
    public void saveXml(XmlWriter w) {
        w.add("project", _project.id());
        if (!isEmpty()) {
            super.saveXml(w);
        }
    }

    @Override
    public Validity valid() {
        if (_project == null) {
            return new IsNotValid("project is not set.");
        }
        return super.valid();
    }

    @Override
    public Filter copy() {
        return new ObjectCompositeFilter(project() == null ? null : new DObjectRef(project().id()), this);
    }

}
