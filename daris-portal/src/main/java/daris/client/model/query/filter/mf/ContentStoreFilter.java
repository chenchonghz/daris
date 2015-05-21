package daris.client.model.query.filter.mf;

import arc.mf.client.util.IsNotValid;
import arc.mf.client.util.IsValid;
import arc.mf.client.util.Validity;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.model.asset.store.DataStoreRef;
import daris.client.model.query.filter.Filter;

public class ContentStoreFilter extends Filter {


    private DataStoreRef _store;

    public ContentStoreFilter(DataStoreRef store) {
        _store = store;
    }

    public ContentStoreFilter(XmlElement xe) {
        String store = xe.value("content-store");
        _store = store == null ? null : new DataStoreRef(store);
    }

    public ContentStoreFilter() {
        _store = null;
    }

    public DataStoreRef store() {
        return _store;
    }

    @Override
    public void save(StringBuilder sb) {
        if (valid().valid()) {
            sb.append("content store '" + _store.name() + "'");
        }
    }

    @Override
    protected void saveXml(XmlWriter w) {
        w.add("content-store", _store);
    }

    @Override
    public Validity valid() {
        if (_store == null) {
            return new IsNotValid("Content store is not set.");
        }
        return IsValid.INSTANCE;
    }

    public void setStore(DataStoreRef store) {
        _store = store;
    }

    @Override
    public Filter copy() {
        return new ContentStoreFilter(store());
    }

}
