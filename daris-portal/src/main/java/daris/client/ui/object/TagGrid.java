package daris.client.ui.object;

import java.util.ArrayList;
import java.util.List;

import arc.gui.gwt.data.DataLoadAction;
import arc.gui.gwt.data.DataLoadHandler;
import arc.gui.gwt.data.DataSource;
import arc.gui.gwt.data.filter.Filter;
import arc.gui.gwt.dnd.DropCheck;
import arc.gui.gwt.dnd.DropHandler;
import arc.gui.gwt.dnd.DropListener;
import arc.gui.gwt.widget.BaseWidget;
import arc.gui.gwt.widget.format.WidgetFormatter;
import arc.gui.gwt.widget.image.Image;
import arc.gui.gwt.widget.list.ListGrid;
import arc.gui.gwt.widget.list.ListGridEntry;
import arc.gui.gwt.widget.scroll.ScrollPolicy;
import arc.mf.model.dictionary.Term;
import arc.mf.object.Null;
import arc.mf.object.ObjectMessageResponse;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;

import daris.client.Resource;
import daris.client.model.object.DObjectRef;
import daris.client.model.object.Tag;
import daris.client.model.object.messages.ObjectTagAdd;
import daris.client.model.object.messages.ObjectTagDescribe;
import daris.client.model.object.messages.ObjectTagRemove;

public class TagGrid extends ListGrid<Tag> implements DropHandler {

    public static final arc.gui.image.Image ICON_DELETE = new arc.gui.image.Image(Resource.INSTANCE.closeBoxRed16()
            .getSafeUri().asString(), 12, 12);

    private DObjectRef _o;

    public TagGrid(DObjectRef o, ScrollPolicy sp) {
        super(sp);
        _o = o;
        addColumnDefn("name", "", "remove this tag", new WidgetFormatter<Tag, String>() {

            @Override
            public BaseWidget format(final Tag tag, String name) {
                final Image deleteIcon = new Image(ICON_DELETE, 12, 12);
                deleteIcon.addClickHandler(new ClickHandler() {

                    @Override
                    public void onClick(ClickEvent event) {
                        new ObjectTagRemove(_o, tag).send(new ObjectMessageResponse<Null>() {

                            @Override
                            public void responded(Null r) {
                                if (r != null) {
                                    refresh();
                                }
                            }
                        });
                    }
                });
                deleteIcon.setToolTip("Remove tag " + tag.name() + ".");
                deleteIcon.setOpacity(0.8);
                deleteIcon.addMouseOverHandler(new MouseOverHandler() {

                    @Override
                    public void onMouseOver(MouseOverEvent event) {
                        deleteIcon.setOpacity(1.0);
                    }
                });
                deleteIcon.addMouseOutHandler(new MouseOutHandler() {

                    @Override
                    public void onMouseOut(MouseOutEvent event) {
                        deleteIcon.setOpacity(0.8);
                    }
                });
                deleteIcon.setMarginTop(3);
                return deleteIcon;
            }
        }).setWidth(20);
        addColumnDefn("id", "id").setWidth(100);
        addColumnDefn("name", "name").setWidth(100);
        addColumnDefn("description", "description").setWidth(300);
        setDataSource(new DataSource<ListGridEntry<Tag>>() {

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
                    final DataLoadHandler<ListGridEntry<Tag>> lh) {
                new ObjectTagDescribe(_o).send(new ObjectMessageResponse<List<Tag>>() {

                    @Override
                    public void responded(List<Tag> tags) {
                        if (tags == null || tags.isEmpty()) {
                            lh.loaded(0, 0, 0, null, null);
                            return;
                        }
                        List<ListGridEntry<Tag>> entries = new ArrayList<ListGridEntry<Tag>>();
                        for (Tag tag : tags) {
                            if (f != null && !f.matches(tag)) {
                                continue;
                            }
                            ListGridEntry<Tag> e = new ListGridEntry<Tag>(tag);
                            e.set("id", tag.id());
                            e.set("name", tag.name());
                            e.set("description", tag.description());
                            entries.add(e);
                        }
                        int total = entries.size();
                        int start1 = start < 0 ? 0 : (start > total ? total : (int) start);
                        int end1 = end > total ? total : (int) end;
                        if (start1 < 0 || end1 > total || start1 > end) {
                            lh.loaded(0, 0, 0, null, null);
                        } else {
                            entries = entries.subList(start1, end1);
                            lh.loaded(start1, end1, total, entries, DataLoadAction.REPLACE);
                        }
                    }
                });
            }
        });
        setMultiSelect(false);
        setEmptyMessage(null);
        
        setDropHandler(this);
        enableDropTarget(false);
    }

    @Override
    public DropCheck checkCanDrop(Object data) {
        if (data instanceof Term) {
            return DropCheck.CAN;
        }
        return DropCheck.CANNOT;
    }

    @Override
    public void drop(BaseWidget target, List<Object> data, final DropListener dl) {
        if (data == null) {
            dl.dropped(DropCheck.CANNOT);
        }
        List<Term> terms = new ArrayList<Term>();
        for (Object o : data) {
            if (o instanceof Term) {
                terms.add((Term) o);
            }
        }
        if (terms.isEmpty()) {
            dl.dropped(DropCheck.CANNOT);
        }
        new ObjectTagAdd(_o, terms).send(new ObjectMessageResponse<Null>() {

            @Override
            public void responded(Null r) {
                if (r != null) {
                    dl.dropped(DropCheck.CAN);
                    refresh();
                } else {
                    dl.dropped(DropCheck.CANNOT);
                }
            }
        });
    }
}
