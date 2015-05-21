package daris.client.model.query.filter.mf;

import arc.mf.client.util.IsNotValid;
import arc.mf.client.util.IsValid;
import arc.mf.client.util.Validity;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import daris.client.model.query.filter.Filter;

public class ContentStatusFilter extends Filter {

    public static enum ContentStatus {
        online, offline, scheduled, migrating, missing, reachable, unreachable, unknown, invalid, external, internal;
        public static ContentStatus parse(String s) {
            return s == null ? null : valueOf(s);
        }
    }

    private ContentStatus _status;

    public ContentStatusFilter(XmlElement xe) {
        _status = xe == null ? null : ContentStatus.parse(xe.value("content-status"));
    }

    public ContentStatusFilter(ContentStatus status) {
        _status = status;
    }

    public ContentStatusFilter() {
        _status = null;
    }

    public ContentStatus status() {
        return _status;
    }

    @Override
    public void save(StringBuilder sb) {
        sb.append("content is " + _status);
    }

    @Override
    protected void saveXml(XmlWriter w) {
        w.add("content-status", _status);
    }

    @Override
    public Validity valid() {
        if (_status == null) {
            return new IsNotValid("Content status is not set");
        }
        return IsValid.INSTANCE;
    }

    public void setStatus(ContentStatus status) {
        _status = status;
    }

    @Override
    public Filter copy() {
        return new ContentStatusFilter(status());
    }

}
