package daris.client.model.query.filter.mf;

import arc.mf.client.util.Validity;
import arc.mf.client.xml.XmlWriter;
import daris.client.model.query.filter.Filter;

public class LockFilter extends Filter {

    public static enum LockType {
        read, write, any
    }

    private String _lockName;
    private LockType _lockType;
    private String _ownerDomain;
    private String _ownerUser;

    protected LockFilter(String lockName) {
        _lockName = lockName;
    }

    protected LockFilter(LockType lockType) {
        _lockType = lockType;
    }

    protected LockFilter(String ownerDomain, String ownerUser) {
        _ownerDomain = ownerDomain;
        _ownerUser = ownerUser;
    }

    @Override
    public void save(StringBuilder sb) {
        if (_lockName != null) {
            sb.append("lock name '" + _lockName + "'");
        } else if (_lockType != null) {
            sb.append("lock type " + _lockType);
        } else {
            sb.append("lock owner " + _ownerDomain + ":" + _ownerUser);
        }
    }

    @Override
    protected void saveXml(XmlWriter w) {
        // TODO Auto-generated method stub

    }

    @Override
    public Validity valid() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Filter copy() {
        // TODO Auto-generated method stub
        return null;
    }
}
