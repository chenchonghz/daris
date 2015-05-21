package daris.client.ui.query.result;

import arc.gui.gwt.widget.list.ListGrid;
import arc.gui.gwt.widget.list.ListGridEntry;
import arc.mf.model.asset.AssetRef;
import daris.client.model.query.ResultCollectionRef;

public class AssetResultNavigator extends ResultNavigator<AssetRef> {

    public AssetResultNavigator(ResultCollectionRef<AssetRef> rc) {
        super(rc);
    }

    @Override
    protected void addListGridColumnDefns(ListGrid<AssetRef> lg) {
        lg.addColumnDefn("asset_id", "asset_id").setWidth(80);
    }

    @Override
    protected void setListGridEntry(ListGridEntry<AssetRef> e) {
        e.set("asset_id", e.data().id());
//        AssetRef o = e.data();
//        if (o instanceof HasXPathValues) {
//            List<XPathValue> xpvs = ((HasXPathValues) o).xpathValues();
//            if (xpvs != null) {
//                for (XPathValue xpv : xpvs) {
//                    e.set(xpv.name(), xpv.value());
//                }
//            }
//        }
    }
}
