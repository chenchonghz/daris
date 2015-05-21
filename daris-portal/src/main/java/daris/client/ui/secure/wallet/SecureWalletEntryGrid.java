package daris.client.ui.secure.wallet;

import java.util.ArrayList;
import java.util.List;

import arc.gui.gwt.data.DataLoadAction;
import arc.gui.gwt.data.DataLoadHandler;
import arc.gui.gwt.data.DataSource;
import arc.gui.gwt.data.filter.Filter;
import arc.gui.gwt.widget.event.SelectionHandler;
import arc.gui.gwt.widget.list.ListGrid;
import arc.gui.gwt.widget.list.ListGridEntry;
import arc.gui.gwt.widget.scroll.ScrollPolicy;
import arc.mf.object.ObjectMessageResponse;
import daris.client.model.secure.wallet.SecureWallet;
import daris.client.model.secure.wallet.SecureWalletEntryRef;

public class SecureWalletEntryGrid extends ListGrid<SecureWalletEntryRef> implements
        SelectionHandler<SecureWalletEntryRef> {

    private String _selectedKey;

    private static class SecureWalletEntryDataSource implements DataSource<ListGridEntry<SecureWalletEntryRef>> {

        @Override
        public boolean isRemote() {
            return true;
        }

        @Override
        public boolean supportCursor() {
            return false;
        }

        @Override
        public void load(final Filter f, final long start, final long end,
                final DataLoadHandler<ListGridEntry<SecureWalletEntryRef>> lh) {
            SecureWallet.listEntries(new ObjectMessageResponse<List<SecureWalletEntryRef>>() {

                @Override
                public void responded(List<SecureWalletEntryRef> es) {
                    if (es == null || es.isEmpty()) {
                        lh.loaded(0, 0, 0, null, null);
                        return;
                    }
                    List<ListGridEntry<SecureWalletEntryRef>> lges = new ArrayList<ListGridEntry<SecureWalletEntryRef>>();
                    for (SecureWalletEntryRef e : es) {
                        if (f == null || (f != null && f.matches(e))) {
                            ListGridEntry<SecureWalletEntryRef> lge = new ListGridEntry<SecureWalletEntryRef>(e);
                            lge.set("key", e.key());
                            lge.set("type", e.type());
                            lge.set("usage-type", e.usage()==null?null:e.usage().type);
                            lge.set("usage", e.usage()==null?null:e.usage().usage);
                            lges.add(lge);
                        }
                    }
                    if (lges.isEmpty()) {
                        lh.loaded(0, 0, 0, null, null);
                        return;
                    }
                    int start1 = start < 0 ? 0 : (start >= lges.size() ? lges.size() : (int) start);
                    int end1 = end > lges.size() ? lges.size() : (int) end;
                    int total = end1 - start1;
                    if (total > 0) {
                        lh.loaded(start1, end1, total, lges.subList(start1, end1), DataLoadAction.REPLACE);
                    } else {
                        lh.loaded(0, 0, 0, null, null);
                    }
                }
            });
        }
    }

    public SecureWalletEntryGrid() {
        this(null);
    }

    public SecureWalletEntryGrid(String selectedKey) {
        super(new SecureWalletEntryDataSource(), ScrollPolicy.AUTO);

        _selectedKey = selectedKey;

        addColumnDefn("key", "Key", "The key name of the entry").setWidth(300);
        addColumnDefn("type", "Type", "The value type").setWidth(100);
        addColumnDefn("usage-type", "Usage type","The usage type." ).setWidth(100);
        addColumnDefn("usage", "Usage","The usage (context)." ).setWidth(200);
        setSelectionHandler(this);

        setMultiSelect(false);
        fitToParent();
    }

    @Override
    protected void postLoad(long start, long end, long total, List<ListGridEntry<SecureWalletEntryRef>> entries) {
        if (_selectedKey != null && entries != null && !entries.isEmpty()) {
            for (ListGridEntry<SecureWalletEntryRef> entry : entries) {
                SecureWalletEntryRef swe = entry.data();
                if (swe != null && _selectedKey.equals(swe.key())) {
                    select(swe);
                    return;
                }
            }
        }
    }

    public SecureWalletEntryRef selected() {
        List<SecureWalletEntryRef> sls = selections();
        if (sls != null && !sls.isEmpty()) {
            return sls.get(0);
        }
        return null;
    }

    @Override
    public void deselected(SecureWalletEntryRef o) {

    }

    @Override
    public void selected(SecureWalletEntryRef o) {
        _selectedKey = o.key();
    }
}
