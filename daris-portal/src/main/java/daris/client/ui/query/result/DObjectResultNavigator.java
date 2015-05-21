package daris.client.ui.query.result;

import arc.gui.gwt.widget.list.ListGrid;
import arc.gui.gwt.widget.list.ListGridEntry;
import daris.client.model.object.DObjectRef;
import daris.client.model.query.ResultCollectionRef;

public class DObjectResultNavigator extends ResultNavigator<DObjectRef> {

    public DObjectResultNavigator(ResultCollectionRef<DObjectRef> rc) {
        super(rc);
    }

    @Override
    protected void addListGridColumnDefns(ListGrid<DObjectRef> lg) {
        lg.addColumnDefn("id", "id").setWidth(80);
        lg.addColumnDefn("type", "type").setWidth(70);
        lg.addColumnDefn("name", "name").setWidth(200);
    }

    @Override
    protected void setListGridEntry(ListGridEntry<DObjectRef> e) {
        DObjectRef o = e.data();
        e.set("id", o.id());
        e.set("type", o.referentTypeName());
        e.set("name", o.referent() != null ? o.referent().name() : null);
    }

}
