package daris.client.model.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class AssetRef extends arc.mf.model.asset.AssetRef implements HasXPathValues {

    private Map<String, XPathValue> _xpathValues;

    public AssetRef(long id, int version) {
        super(id, version);
    }

    public void addXpathValue(XPathValue value) {
        if (_xpathValues == null) {
            _xpathValues = new TreeMap<String, XPathValue>();
        }
        _xpathValues.put(value.xpath(), value);
    }

    public XPathValue xpathValue(String xpath) {
        if (_xpathValues != null) {
            return _xpathValues.get(xpath);
        }
        return null;
    }

    public List<XPathValue> xpathValues() {
        if (_xpathValues != null && !_xpathValues.isEmpty()) {
            return new ArrayList<XPathValue>(_xpathValues.values());
        }
        return null;
    }

    public void clearXpathValues() {
        if (_xpathValues != null) {
            _xpathValues.clear();
        }
    }

}
