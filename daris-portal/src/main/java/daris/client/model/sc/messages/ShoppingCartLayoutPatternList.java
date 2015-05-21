package daris.client.model.sc.messages;

import java.util.ArrayList;
import java.util.List;

import daris.client.model.sc.Layout;
import daris.client.model.sc.Layout.Pattern;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;

public class ShoppingCartLayoutPatternList extends ObjectMessage<List<Layout.Pattern>> {

    public static final String SERVICE_NAME = "om.pssd.shoppingcart.layout-pattern.list";

    public ShoppingCartLayoutPatternList() {

    }

    @Override
    protected void messageServiceArgs(XmlWriter w) {
    }

    @Override
    protected String messageServiceName() {
        return SERVICE_NAME;
    }

    @Override
    protected List<Pattern> instantiate(XmlElement xe) throws Throwable {
        if (xe != null) {
            List<XmlElement> lpes = xe.elements("layout-pattern");
            if (lpes != null) {
                List<Layout.Pattern> patterns = new ArrayList<Layout.Pattern>(lpes.size());
                for (XmlElement lpe : lpes) {
                    String name = lpe.value("@name");
                    String description = lpe.value("@description");
                    String pattern = lpe.value();
                    patterns.add(new Layout.Pattern(pattern, name, description));
                }
                if (!patterns.isEmpty()) {
                    return patterns;
                }
            }
        }
        return null;
    }

    @Override
    protected String objectTypeName() {
        return "shopping cart layout patterns";
    }

    @Override
    protected String idToString() {
        return null;
    }

}
