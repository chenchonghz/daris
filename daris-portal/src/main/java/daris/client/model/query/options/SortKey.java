package daris.client.model.query.options;

import arc.mf.client.util.ObjectUtil;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;

public class SortKey {

    public static final String CTIME = "ctime";
    public static final String MTIME = "mtime";
    public static final String STIME = "stime";
    public static final String CSIZE = "csize";
    public static final String CONTENT_SIZE = "content/size";
    public static final String CONTENT_CSUM = "content/csum";
    public static final String NAME = "name";

    private String _key;
    private SortOptions.Order _order;

    public SortKey(XmlElement ke) {
        _key = ke.value();
        String o = ke.value("@order");
        _order = o == null ? null : SortOptions.Order.parse(o);
    }

    public SortKey(String key, SortOptions.Order order) {
        _key = key;
        _order = order;
    }

    public String key() {
        return _key;
    }

    public SortOptions.Order order() {
        return _order;
    }

    @Override
    public boolean equals(Object o) {
        if (o != null) {
            if (o instanceof SortKey) {
                SortKey ko = (SortKey) o;
                return ObjectUtil.equals(ko.key(), key()) && ObjectUtil.equals(ko.order(), order());
            }
        }
        return false;
    }

    public void save(XmlWriter w) {
        if (_order == null) {
            w.add("key", _key);
        } else {
            w.add("key", new String[] { "order", _order.toString() }, _key);
        }
    }

    public void setKey(String key) {
        _key = key;
    }

    public void setOrder(SortOptions.Order order) {
        _order = order;
    }
}
