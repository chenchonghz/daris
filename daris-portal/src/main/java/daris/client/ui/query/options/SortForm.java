package daris.client.ui.query.options;

import java.util.ArrayList;
import java.util.List;

import arc.gui.ValidatedInterfaceComponent;
import arc.gui.form.Field;
import arc.gui.form.FieldDefinition;
import arc.gui.form.FieldRenderOptions;
import arc.gui.form.Form;
import arc.gui.form.FormItem;
import arc.gui.form.FormItem.Property;
import arc.gui.form.FormItemListener;
import arc.gui.gwt.colour.RGB;
import arc.gui.gwt.data.DataLoadAction;
import arc.gui.gwt.data.DataLoadHandler;
import arc.gui.gwt.data.DataSource;
import arc.gui.gwt.data.filter.Filter;
import arc.gui.gwt.widget.BaseWidget;
import arc.gui.gwt.widget.combo.ComboBox;
import arc.gui.gwt.widget.combo.ComboBox.ChangeListener;
import arc.gui.gwt.widget.combo.ComboBoxEntry;
import arc.gui.gwt.widget.event.SelectionHandler;
import arc.gui.gwt.widget.image.Image;
import arc.gui.gwt.widget.label.Label;
import arc.gui.gwt.widget.list.ListGrid;
import arc.gui.gwt.widget.list.ListGridEntry;
import arc.gui.gwt.widget.panel.HorizontalPanel;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.gui.gwt.widget.scroll.ScrollPolicy;
import arc.mf.client.util.ListUtil;
import arc.mf.client.util.StateChangeListener;
import arc.mf.dtype.EnumerationType;

import com.google.gwt.dom.client.Style.BorderStyle;
import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.user.client.ui.Widget;

import daris.client.model.query.options.QueryOptions;
import daris.client.model.query.options.SortKey;
import daris.client.model.query.options.SortOptions;
import daris.client.model.query.options.SortOptions.Nulls;
import daris.client.model.query.options.SortOptions.Order;

public class SortForm extends ValidatedInterfaceComponent {

    public static final int INDENT = 20;

    private static class SortKeyList extends ValidatedInterfaceComponent {

        public static final int ICON_SIZE = 13;

        private QueryOptions _opts;

        private VerticalPanel _vp;
        private ListGrid<SortKey> _list;
        private SortKeySelectComboBox _keyCombo;
        private ComboBox<SortOptions.Order> _orderCombo;
        private Image _addIcon;
        private Image _removeIcon;

        @SuppressWarnings("unchecked")
        public SortKeyList(QueryOptions opts) {
            _opts = opts;

            _vp = new VerticalPanel();
            _vp.fitToParent();

            HorizontalPanel hp = new HorizontalPanel();
            hp.setSpacing(5);
            Label keyLabel = new Label("key:");
            keyLabel.setFontSize(11);
            keyLabel.setMarginTop(10);
            hp.add(keyLabel);
            hp.setSpacing(3);

            _keyCombo = new SortKeySelectComboBox(null, _opts.sortKeyTree());
            _keyCombo.addChangeListener(new StateChangeListener() {

                @Override
                public void notifyOfChangeInState() {
                    updateIconStates();
                }
            });
            _keyCombo.setWidth(267);
            hp.add(_keyCombo);
            hp.setSpacing(10);

            Label orderLabel = new Label("order:");
            orderLabel.setFontSize(11);
            orderLabel.setHeight100();
            orderLabel.setMarginTop(10);
            hp.add(orderLabel);
            hp.setSpacing(3);
            _orderCombo = new ComboBox<SortOptions.Order>(ListUtil.list(new ComboBoxEntry<SortOptions.Order>(
                    SortOptions.Order.asc), new ComboBoxEntry<SortOptions.Order>(SortOptions.Order.desc)));
            _orderCombo.setWidth(70);
            _orderCombo.addChangeListener(new ChangeListener<SortOptions.Order>() {

                @Override
                public void changed(ComboBox<Order> cb) {
                    updateIconStates();
                }
            });
            _orderCombo.setPaddingTop(4);
            hp.add(_orderCombo);

            hp.setSpacing(3);
            _addIcon = new Image("resources/images/add.png");
            _addIcon.setWidth(ICON_SIZE);
            _addIcon.setHeight(ICON_SIZE);
            _addIcon.setMarginTop(8);
            _addIcon.disable();
            _addIcon.setToolTip("Add key");

            _addIcon.addClickHandler(new com.google.gwt.event.dom.client.ClickHandler() {
                public void onClick(com.google.gwt.event.dom.client.ClickEvent event) {
                    addKey(_keyCombo.value(), _orderCombo.value());
                    _keyCombo.clear();
                    _orderCombo.clear();
                }
            });
            hp.setSpacing(5);
            hp.add(_addIcon);

            _removeIcon = new Image("resources/images/remove.png");
            _removeIcon.setWidth(ICON_SIZE);
            _removeIcon.setHeight(ICON_SIZE);
            _removeIcon.setMarginTop(8);
            _removeIcon.disable();
            _removeIcon.setToolTip("Remove key");

            _removeIcon.addClickHandler(new com.google.gwt.event.dom.client.ClickHandler() {
                public void onClick(com.google.gwt.event.dom.client.ClickEvent event) {
                    List<SortKey> selected = _list.selections();
                    removeKey(selected.get(0));
                    _keyCombo.clear();
                    _orderCombo.clear();
                }
            });
            hp.setSpacing(3);
            hp.add(_removeIcon);
            _vp.add(hp);

            _list = new ListGrid<SortKey>(new DataSource<ListGridEntry<SortKey>>() {
                @Override
                public boolean isRemote() {
                    return false;
                }

                @Override
                public boolean supportCursor() {
                    return false;
                }

                @Override
                public void load(Filter f, long start, long end, DataLoadHandler<ListGridEntry<SortKey>> lh) {
                    List<SortKey> keys = _opts.sortOptions().keys();
                    if (keys == null || keys.isEmpty()) {
                        lh.loaded(0, 0, 0, null, null);
                        return;
                    }
                    List<ListGridEntry<SortKey>> es = new ArrayList<ListGridEntry<SortKey>>(keys.size());
                    for (SortKey key : keys) {
                        ListGridEntry<SortKey> e = new ListGridEntry<SortKey>(key);
                        e.set("key", key.key());
                        e.set("order", key.order());
                        es.add(e);
                    }
                    lh.loaded(start, end, es.size(), es, DataLoadAction.REPLACE);
                }
            }, Integer.MAX_VALUE, ScrollPolicy.VERTICAL);

            _list.fitToParent();
            _list.setShowHeader(true);
            _list.setLoadingMessage("");
            _list.setEmptyMessage("No sort keys");
            _list.setMultiSelect(false);
            _list.addColumnDefn("key", "key", "key").setWidth(300);
            _list.addColumnDefn("order", "order", "order").setWidth(105);
            _list.refresh();
            _list.setSelectionHandler(new SelectionHandler<SortKey>() {
                public void selected(SortKey key) {
                    _keyCombo.setValue(key.key());
                    _orderCombo.setValue(key.order());
                    // ComboBox.setValue(T o) does not fire change event.
                    updateIconStates();
                }

                public void deselected(SortKey key) {
                    _keyCombo.clear();
                    _orderCombo.clear();
                }
            });

            _vp.setSpacing(2);
            _vp.add(_list);

            _vp.setBorder(1, BorderStyle.DOTTED, new RGB(0xcc, 0xcc, 0xcc));
        }

        private boolean containsKey(String key, SortOptions.Order order) {
            return _opts.sortOptions().containsKey(key, order);
        }

        private void addKey(String key, SortOptions.Order order) {

            SortKey sk = new SortKey(key, order);
            boolean changed = _opts.sortOptions().addKey(sk);
            if (changed) {
                _list.refresh();
                _list.select(sk);
                notifyOfChangeInState();
            }
        }

        private void removeKey(SortKey key) {
            boolean changed = _opts.sortOptions().removeKey(key);
            if (changed) {
                _list.refresh();
                notifyOfChangeInState();
            }
        }

        private void updateIconStates() {
            String kk = _keyCombo.value();
            SortOptions.Order ko = _orderCombo.value();

            if (kk == null) {
                _addIcon.disable();
                _removeIcon.disable();
            } else {
                if (containsKey(kk, ko)) {
                    _addIcon.disable();
                    _removeIcon.enable();
                } else {
                    _addIcon.enable();
                    _removeIcon.disable();
                }
            }
        }

        @Override
        public Widget gui() {
            return _vp;
        }

        public BaseWidget widget() {
            return _vp;
        }

    }

    private QueryOptions _opts;

    private VerticalPanel _vp;
    private SortKeyList _sortKeyList;
    private Form _form;

    public SortForm(QueryOptions opts) {
        _opts = opts;

        _vp = new VerticalPanel();
        _vp.setWidth(750);
        _vp.setHeight(200);
        _vp.setPaddingRight(10);
        _vp.setBorder(1, BorderStyle.DOTTED, new RGB(0xcc, 0xcc, 0xcc));

        HorizontalPanel hp = new HorizontalPanel();
        hp.setHeight(22);
        hp.setPaddingTop(8);
        hp.setSpacing(5);
        Label label = new Label("sort:");
        label.setFontSize(11);
        label.setFontWeight(FontWeight.BOLD);
        hp.add(label);
        _vp.add(hp);

        _sortKeyList = new SortKeyList(_opts);
        _sortKeyList.widget().setMarginLeft(INDENT);
        _sortKeyList.addChangeListener(this);
        _vp.add(_sortKeyList.widget());
        addMustBeValid(_sortKeyList);

        _form = new Form();
        _form.setHeight(25);
        _form.setShowHelp(false);
        _form.setNumberOfColumns(2);

        Field<SortOptions.Nulls> nulls = new Field<SortOptions.Nulls>(new FieldDefinition("nulls",
                new EnumerationType<SortOptions.Nulls>(SortOptions.Nulls.values()),
                "When sorting should paths with null values be included or excluded from the result set.", null, 0, 1));
        nulls.setInitialValue(_opts.sortOptions().nulls());
        FieldRenderOptions fro = new FieldRenderOptions();
        fro.setWidth(168);
        nulls.setRenderOptions(fro);
        nulls.addListener(new FormItemListener<SortOptions.Nulls>() {

            @Override
            public void itemValueChanged(FormItem<Nulls> f) {
                _opts.sortOptions().setNulls(f.value());
            }

            @Override
            public void itemPropertyChanged(FormItem<Nulls> f, Property property) {

            }
        });
        _form.add(nulls);

        Field<SortOptions.Order> order = new Field<SortOptions.Order>(new FieldDefinition("order",
                new EnumerationType<SortOptions.Order>(SortOptions.Order.values()),
                "Sort ascending or descending. Default is ascending.", null, 0, 1));
        order.setInitialValue(_opts.sortOptions().order());
        fro = new FieldRenderOptions();
        fro.setWidth(70);
        order.setRenderOptions(fro);
        order.addListener(new FormItemListener<SortOptions.Order>() {

            @Override
            public void itemValueChanged(FormItem<SortOptions.Order> f) {
                _opts.sortOptions().setOrder(f.value());
            }

            @Override
            public void itemPropertyChanged(FormItem<SortOptions.Order> f, Property property) {

            }
        });
        _form.add(order);
        _form.setMarginLeft(INDENT);
        _form.render();
        _form.addChangeListener(this);
        _vp.add(_form);
        addMustBeValid(_form);

    }

    @Override
    public Widget gui() {
        return _vp;
    }
}
