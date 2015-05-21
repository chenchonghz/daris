package daris.client.model.query.options;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import arc.mf.client.util.ObjectUtil;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;

public class SortOptions {

    public static final String KEY_OBJECT_TYPE = "meta/daris:pssd-object/type";
    public static final String KEY_OBJECT_NAME = "meta/daris:pssd-object/name";
    public static final String KEY_OBJECT_DESCRIPTION = "meta/daris:pssd-object/description";

    public static enum Order {
        asc, desc;

        public static Order parse(String s) {
            if (s == null) {
                return null;
            }
            return valueOf(s);
        }
    }

    public static enum Nulls {
        exclude, include;

        public static Nulls parse(String s) {
            if (s == null) {
                return null;
            }
            return valueOf(s);
        }
    }

    private List<SortKey> _keys;
    private Nulls _nulls;
    private Order _order;

    public SortOptions(XmlElement xe) {

        List<XmlElement> kes = xe.elements("key");
        _keys = new ArrayList<SortKey>();
        if (kes != null && !kes.isEmpty()) {
            for (XmlElement ke : kes) {
                _keys.add(new SortKey(ke));
            }
        }
        _nulls = Nulls.parse(xe.value("nulls"));
        _order = Order.parse(xe.value("order"));
    }

    public SortOptions() {

        _keys = null;
        _nulls = null;
        _order = null;
    }

    public List<SortKey> keys() {
        if (_keys == null) {
            return null;
        } else {
            return Collections.unmodifiableList(_keys);
        }
    }

    public boolean addKey(String key, SortOptions.Order order) {
        if (key == null) {
            return false;
        }
        return addKey(new SortKey(key, order));
    }

    public boolean addKey(SortKey key) {
        if (key == null) {
            return false;
        }
        if (_keys == null) {
            _keys = new ArrayList<SortKey>();
            _keys.add(key);
            return true;
        }
        int exists = -1;
        for (int i = 0; i < _keys.size(); i++) {
            if (ObjectUtil.equals(_keys.get(i).key(), key.key())) {
                exists = i;
                break;
            }
        }
        if (exists >= 0) {
            if (_keys.get(exists).order() == key.order()) {
                return false;
            } else {
                _keys.set(exists, key);
                return true;
            }
        } else {
            _keys.add(key);
            return true;
        }
    }

    public boolean removeKey(String key) {
        if (key == null) {
            return false;
        }
        if (_keys == null) {
            return false;
        }
        for (Iterator<SortKey> it = _keys.iterator(); it.hasNext();) {
            if (key.equals(it.next().key())) {
                it.remove();
                return true;
            }
        }
        return false;
    }

    public boolean removeKey(SortKey key) {
        if (key == null) {
            return false;
        }
        return removeKey(key.key());
    }

    public SortKey getKey(String key) {
        if (key == null) {
            return null;
        }
        if (_keys == null) {
            return null;
        }
        for (SortKey k : _keys) {
            if (key.equals(k.key())) {
                return k;
            }
        }
        return null;
    }

    public boolean containsKey(String key) {
        return getKey(key) != null;
    }

    public boolean containsKey(String key, Order order) {
        SortKey sk = getKey(key);
        if (sk == null) {
            return false;
        }
        return sk.order() == order;
    }

    public Nulls nulls() {
        return _nulls;
    }

    public void setNulls(Nulls nulls) {
        _nulls = nulls;
    }

    public Order order() {
        return _order;
    }

    public void setOrder(Order order) {
        _order = order;
    }

    public boolean hasKeys() {
        return _keys != null && !_keys.isEmpty();
    }

    public void save(XmlWriter w) {
        if (!hasKeys()) {
            return;
        }
        w.push("sort");
        for (SortKey key : keys()) {
            key.save(w);
        }
        if (_nulls != null) {
            w.add("nulls", _nulls);
        }
        if (_order != null) {
            w.add("order", _order);
        }
        w.pop();
    }

}