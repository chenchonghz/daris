package daris.client.model.query;

import java.util.List;

public interface HasXPathValues {
    List<XPathValue> xpathValues();

    void addXpathValue(XPathValue value);

    XPathValue xpathValue(String xpath);

    void clearXpathValues();
}
