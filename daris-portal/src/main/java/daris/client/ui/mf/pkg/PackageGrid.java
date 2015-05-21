package daris.client.ui.mf.pkg;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gwt.i18n.client.DateTimeFormat;

import arc.gui.gwt.data.DataLoadAction;
import arc.gui.gwt.data.DataLoadHandler;
import arc.gui.gwt.data.DataSource;
import arc.gui.gwt.data.filter.Filter;
import arc.gui.gwt.widget.BaseWidget;
import arc.gui.gwt.widget.HTML;
import arc.gui.gwt.widget.format.WidgetFormatter;
import arc.gui.gwt.widget.list.ListGrid;
import arc.gui.gwt.widget.list.ListGridEntry;
import arc.gui.gwt.widget.scroll.ScrollPolicy;
import arc.mf.client.util.DateTime;
import arc.mf.object.ObjectMessageResponse;
import daris.client.mf.pkg.Package;
import daris.client.mf.pkg.PackageDescribe;

public class PackageGrid extends ListGrid<daris.client.mf.pkg.Package> {

    private static class PackageDataSource implements
            DataSource<ListGridEntry<daris.client.mf.pkg.Package>> {

        private PackageDataSource() {

        }

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
                final DataLoadHandler<ListGridEntry<daris.client.mf.pkg.Package>> lh) {

            new PackageDescribe()
                    .send(new ObjectMessageResponse<List<daris.client.mf.pkg.Package>>() {

                        @Override
                        public void responded(List<daris.client.mf.pkg.Package> ps) {

                            if (ps != null && !ps.isEmpty()) {
                                List<ListGridEntry<daris.client.mf.pkg.Package>> es = new ArrayList<ListGridEntry<daris.client.mf.pkg.Package>>(
                                        ps.size());
                                for (daris.client.mf.pkg.Package p : ps) {
                                    ListGridEntry<daris.client.mf.pkg.Package> e = new ListGridEntry<daris.client.mf.pkg.Package>(
                                            p);
                                    e.set("name", p.name());
                                    e.set("description", p.description());
                                    e.set("buildTime", p.buildTime());
                                    e.set("version", p.version());
                                    e.set("vendor", p.vendor());
                                    e.set("vendorURL", p.vendorURL());
                                    es.add(e);
                                }
                                int total = es.size();
                                int start1 = start < 0 ? 0
                                        : (start > total ? total : (int) start);
                                int end1 = end > total ? total : (int) end;
                                if (start1 < 0 || end1 > total || start1 > end) {
                                    lh.loaded(start, end, total, null, null);
                                } else {
                                    es = es.subList(start1, end1);
                                    lh.loaded(start1, end1, total, es,
                                            DataLoadAction.REPLACE);
                                }
                                return;
                            }
                            lh.loaded(0, 0, 0, null, null);
                        }
                    });
        }
    }

    public PackageGrid() {

        super(new PackageDataSource(), ScrollPolicy.AUTO);
        addColumnDefn("name", "Package").setWidth(100);
        addColumnDefn("version", "Version").setWidth(50);
        addColumnDefn("buildTime", "Build Time", null,
                new WidgetFormatter<daris.client.mf.pkg.Package, Date>() {

                    @Override
                    public BaseWidget format(Package pkg, Date buildTime) {
                        if (buildTime != null) {
                            return new HTML(DateTimeFormat.getFormat(
                                    DateTime.DATE_TIME_FORMAT)
                                    .format(buildTime));
                        }
                        return null;
                    }
                }).setWidth(110);
        addColumnDefn("vendor", "Vendor", null,
                new WidgetFormatter<daris.client.mf.pkg.Package, String>() {
                    @Override
                    public BaseWidget format(daris.client.mf.pkg.Package pkg,
                            String vendor) {
                        if (vendor != null) {
                            if (pkg.vendorURL() != null) {
                                return new HTML("<a href=\"" + pkg.vendorURL()
                                        + "\">" + vendor + "</a>");
                            } else {
                                return new HTML(vendor);
                            }
                        }
                        return null;
                    }

                }).setWidth(180);
        addColumnDefn("description", "Description").setWidth(300);

        fitToParent();
        setShowHeader(true);
        setShowRowSeparators(true);
        setMultiSelect(false);
        setFontSize(10);
        setCellSpacing(0);
        setCellPadding(1);
        setEmptyMessage("");
        setLoadingMessage("Loading packages...");
        setCursorSize(Integer.MAX_VALUE);

        refresh();
    }

}
