package daris.client.ui.dictionary;

import java.util.ArrayList;
import java.util.List;

import arc.gui.InterfaceComponent;
import arc.gui.gwt.data.DataLoadAction;
import arc.gui.gwt.data.DataLoadHandler;
import arc.gui.gwt.data.DataSource;
import arc.gui.gwt.data.filter.Filter;
import arc.gui.gwt.widget.ContainerWidget;
import arc.gui.gwt.widget.event.SelectionHandler;
import arc.gui.gwt.widget.image.Image;
import arc.gui.gwt.widget.list.ListGrid;
import arc.gui.gwt.widget.list.ListGridEntry;
import arc.gui.gwt.widget.panel.HorizontalPanel;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.gui.gwt.widget.scroll.ScrollPolicy;
import arc.mf.client.util.ActionListener;
import arc.mf.model.dictionary.DictionaryRef;
import arc.mf.model.dictionary.Term;
import arc.mf.model.dictionary.TermRef;
import arc.mf.model.dictionary.TermSetRef;
import arc.mf.model.dictionary.messages.CheckDictionaryExistance;
import arc.mf.model.dictionary.messages.RemoveEntry;
import arc.mf.object.CollectionResolveHandler;
import arc.mf.object.ObjectMessageResponse;

import com.google.gwt.user.client.ui.Widget;

import daris.client.ui.DObjectGUIRegistry;

public class TermGrid extends ContainerWidget implements InterfaceComponent {

    public static final int DEFAULT_CURSOR_SIZE = 1000;

    private DictionaryRef _dict;
    private VerticalPanel _vp;
    private ListGrid<Term> _grid;
    private arc.gui.gwt.widget.image.Image _addIcon;
    private arc.gui.gwt.widget.image.Image _removeIcon;

    public TermGrid(DictionaryRef dict, boolean projectAdmin) {

        _dict = dict;
        _vp = new VerticalPanel();
        _vp.fitToParent();

        _grid = new ListGrid<Term>(ScrollPolicy.AUTO);
        _grid.addColumnDefn("term", titleForTerm()).setWidth(100);
        _grid.addColumnDefn("definition", titleForDefinition()).setWidth(300);
        _grid.setDataSource(new DataSource<ListGridEntry<Term>>() {

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
                    final DataLoadHandler<ListGridEntry<Term>> lh) {
                new CheckDictionaryExistance(_dict.name()).send(new ObjectMessageResponse<Boolean>() {

                    @Override
                    public void responded(Boolean exists) {
                        if (exists != null && exists == true) {
                            new TermSetRef(_dict).resolve(start, end, new CollectionResolveHandler<Term>() {
                                @Override
                                public void resolved(List<Term> os) throws Throwable {
                                    if (os == null || os.isEmpty()) {
                                        lh.loaded(0, 0, 0, null, null);
                                        return;
                                    }
                                    List<ListGridEntry<Term>> es = new ArrayList<ListGridEntry<Term>>();
                                    for (Term o : os) {
                                        if (f != null && !f.matches(o)) {
                                            continue;
                                        }
                                        ListGridEntry<Term> e = new ListGridEntry<Term>(o);
                                        e.set("term", o.term());
                                        // NOTE: only the first definition is shown.
                                        e.set("definition", (o.definitions() != null && !o.definitions().isEmpty()) ? o
                                                .definitions().get(0).value() : null);
                                        es.add(e);
                                    }
                                    lh.loaded(start, end, es.size(), es, DataLoadAction.REPLACE);
                                }
                            });
                        } else {
                            lh.loaded(0, 0, 0, null, null);
                        }
                    }
                });

            }
        });
        _grid.setMultiSelect(false);
        _grid.setEmptyMessage(null);
        _grid.setCursorSize(DEFAULT_CURSOR_SIZE);
        _grid.setObjectRegistry(DObjectGUIRegistry.get());
        _grid.enableRowDrag();

        _grid.fitToParent();

        _vp.add(_grid);

        if (projectAdmin) {
            HorizontalPanel hp = new HorizontalPanel();
            hp.setHeight(20);
            hp.setPaddingLeft(10);
            _addIcon = new Image("resources/images/add.png");
            _addIcon.setWidth(12);
            _addIcon.setHeight(12);
            _addIcon.setMarginTop(8);
            _addIcon.setToolTip("Create new tag.");
            _addIcon.addClickHandler(new com.google.gwt.event.dom.client.ClickHandler() {
                public void onClick(com.google.gwt.event.dom.client.ClickEvent event) {
                    new TermAddForm("tag", _dict).showDialog(window(), new ActionListener() {

                        @Override
                        public void executed(boolean succeeded) {
                            _grid.refresh();
                        }
                    });
                }
            });
            hp.add(_addIcon);

            _removeIcon = new Image("resources/images/remove.png");
            _removeIcon.setWidth(12);
            _removeIcon.setHeight(12);
            _removeIcon.setMarginTop(8);
            _removeIcon.disable();
            _removeIcon.setToolTip("Remove tag.");
            _removeIcon.addClickHandler(new com.google.gwt.event.dom.client.ClickHandler() {
                public void onClick(com.google.gwt.event.dom.client.ClickEvent event) {
                    List<Term> selections = _grid.selections();
                    if (selections != null && !selections.isEmpty()) {
                        for (Term term : selections) {
                            new RemoveEntry(new TermRef(term)).send(new ObjectMessageResponse<TermRef>() {

                                @Override
                                public void responded(TermRef r) {
                                    _grid.refresh();
                                }
                            });
                        }
                    }
                }
            });
            hp.setSpacing(3);
            hp.add(_removeIcon);
            _grid.setSelectionHandler(new SelectionHandler<Term>() {

                @Override
                public void selected(Term o) {
                    _removeIcon.enable();
                }

                @Override
                public void deselected(Term o) {
                    _removeIcon.disable();
                }
            });
            _vp.add(hp);
        }

        initWidget(_vp);
    }

    protected String titleForTerm() {
        return "term";
    }

    protected String titleForDefinition() {
        return "definition";
    }

    @Override
    public Widget gui() {
        return this;
    }

}
