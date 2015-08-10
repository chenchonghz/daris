package daris.client.ui.query.filter.item.mf;

import java.util.ArrayList;
import java.util.List;

import arc.gui.ValidatedInterfaceComponent;
import arc.gui.form.Field;
import arc.gui.form.FieldDefinition;
import arc.gui.form.Form;
import arc.gui.form.FormItem;
import arc.gui.form.FormItem.Property;
import arc.gui.form.FormItemListener;
import arc.gui.gwt.colour.RGB;
import arc.gui.gwt.colour.RGBA;
import arc.gui.gwt.widget.BaseWidget;
import arc.gui.gwt.widget.HTML;
import arc.gui.gwt.widget.button.Button;
import arc.gui.gwt.widget.button.ButtonBar;
import arc.gui.gwt.widget.button.ButtonBar.Alignment;
import arc.gui.gwt.widget.button.ButtonBar.Position;
import arc.gui.gwt.widget.event.SelectionHandler;
import arc.gui.gwt.widget.list.ListGrid;
import arc.gui.gwt.widget.list.ListGridEntry;
import arc.gui.gwt.widget.list.ListGridRowDoubleClickHandler;
import arc.gui.gwt.widget.panel.AbsolutePanel;
import arc.gui.gwt.widget.panel.HorizontalPanel;
import arc.gui.gwt.widget.panel.ResizeHandle;
import arc.gui.gwt.widget.panel.SimplePanel;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.gui.gwt.widget.popup.PopupCloseHandler;
import arc.gui.gwt.widget.popup.PopupPanel;
import arc.gui.gwt.widget.scroll.ScrollPolicy;
import arc.mf.client.util.IsNotValid;
import arc.mf.client.util.StateChangeListener;
import arc.mf.client.util.Validity;
import arc.mf.dtype.EnumerationType;
import arc.mf.dtype.StringType;
import arc.mf.object.ObjectMessageResponse;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.user.client.ui.PopupPanel.PositionCallback;
import com.google.gwt.user.client.ui.Widget;

import daris.client.model.query.filter.mf.CTypeFilter;
import daris.client.model.type.TypeStringEnum;
import daris.client.model.type.messages.TypesFromExt;
import daris.client.ui.query.filter.form.CompositeFilterForm;
import daris.client.ui.query.filter.item.FilterItem;
import daris.client.ui.query.filter.item.mf.CTypeFilterItem.SelectByExtPopupPanel.TypeSelectionListener;
import daris.client.ui.util.ButtonUtil;

public class CTypeFilterItem extends FilterItem<CTypeFilter> {

    private HorizontalPanel _hp;
    private SimplePanel _formSP;
    private Form _form;
    private Field<String> _ctypeField;
    private SimplePanel _buttonSP;
    private Button _button;

    public CTypeFilterItem(CompositeFilterForm form, CTypeFilter filter, boolean editable) {
        super(form, filter, editable);

        _hp = new HorizontalPanel();
        _hp.setHeight(22);

        _formSP = new SimplePanel();
        _formSP.setHeight100();
        _formSP.setWidth(200);
        _hp.add(_formSP);

        _form = new Form();
        _form.setHeight100();
        _form.setMarginTop(8);
        _form.setShowLabels(true);
        _form.setShowDescriptions(false);
        _form.setShowHelp(false);

        _form.setNumberOfColumns(1);

        _ctypeField = new Field<String>(new FieldDefinition("asset.mime.type", new EnumerationType<String>(new TypeStringEnum()),
                "The asset MIME type.", null, 1, 1));
        _ctypeField.setInitialValue(filter().ctype(), false);
        _ctypeField.addListener(new FormItemListener<String>() {

            @Override
            public void itemValueChanged(FormItem<String> f) {
                filter().setCType(f.value());
            }

            @Override
            public void itemPropertyChanged(FormItem<String> f, Property property) {

            }
        });
        _form.add(_ctypeField);
        _form.render();
        addMustBeValid(_form);
        _formSP.setContent(_form);

        _hp.setSpacing(3);

        _buttonSP = new SimplePanel();
        _buttonSP.setHeight100();
        _hp.add(_buttonSP);

        _button = new Button("Select by extension");
        _button.setMarginTop(5);
        _buttonSP.setContent(_button);

        _button.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                new SelectByExtPopupPanel(_hp, new TypeSelectionListener() {

                    @Override
                    public void typeSelected(String type) {
                        if (type != null) {
                            _ctypeField.setValue(type);
                        }
                    }
                }).show();
            }
        });

    }

    static class SelectByExtPopupPanel extends ValidatedInterfaceComponent {

        public static interface TypeSelectionListener {
            void typeSelected(String type);
        }

        public static final int DEFAULT_WIDTH = 280;
        public static final int DEFAULT_HEIGHT = 180;

        private PopupPanel _pp;

        private AbsolutePanel _ap;

        private VerticalPanel _vp;
        private Form _form;
        private ListGrid<String> _grid;
        private HTML _sb;
        private Button _selectButton;

        private BaseWidget _partner;
        private TypeSelectionListener _tsl;

        private SelectByExtPopupPanel(BaseWidget partner, TypeSelectionListener typeSelectionListener) {
            _partner = partner;
            _tsl = typeSelectionListener;

            _vp = new VerticalPanel();
            _vp.fitToParent();

            _form = new Form();
            _form.setHeight(22);
            Field<String> extField = new Field<String>(new FieldDefinition("extension", StringType.DEFAULT,
                    "File extension", null, 1, 1));
            extField.addListener(new FormItemListener<String>() {

                @Override
                public void itemValueChanged(FormItem<String> f) {
                    String ext = f.value();
                    if (ext == null) {
                        _grid.setData(null);
                        return;
                    }
                    while (ext.startsWith(".")) {
                        ext = ext.substring(1);
                    }
                    final String ext1 = ext;
                    new TypesFromExt(ext1).send(new ObjectMessageResponse<List<String>>() {

                        @Override
                        public void responded(List<String> types) {
                            if (types == null || types.isEmpty()) {
                                _grid.setData(null);
                                return;
                            }
                            List<ListGridEntry<String>> entries = new ArrayList<ListGridEntry<String>>(types.size());
                            for (String type : types) {
                                ListGridEntry<String> entry = new ListGridEntry<String>(type);
                                entry.set("type", type);
                                entry.set("ext", ext1);
                                entries.add(entry);
                            }
                            _grid.setData(entries);
                        }
                    });
                }

                @Override
                public void itemPropertyChanged(FormItem<String> f, Property property) {

                }
            });
            _form.add(extField);

            _form.render();

            addMustBeValid(_form);
            _vp.add(_form);

            _grid = new ListGrid<String>(ScrollPolicy.AUTO);
            _grid.setMultiSelect(false);
            _grid.addColumnDefn("type", "MIME type").setWidth(250);
            _grid.setEmptyMessage("No MIME types found.");
            _grid.fitToParent();
            _grid.setSelectionHandler(new SelectionHandler<String>() {

                @Override
                public void selected(String type) {
                    notifyOfChangeInState();
                }

                @Override
                public void deselected(String type) {
                    notifyOfChangeInState();
                }
            });
            _grid.setRowDoubleClickHandler(new ListGridRowDoubleClickHandler<String>() {

                @Override
                public void doubleClicked(String data, DoubleClickEvent event) {
                    if (data != null) {
                        if (_tsl != null) {
                            _tsl.typeSelected(data);
                            if (_pp != null) {
                                _pp.hide();
                            }
                        }
                    }
                }
            });
            _vp.add(_grid);

            _sb = new HTML();
            _sb.setHeight(22);
            _sb.setFontSize(11);
            _sb.setColour(RGB.RED);
            _sb.setPaddingLeft(20);
            _vp.add(_sb);

            ButtonBar bb = ButtonUtil.createButtonBar(Position.BOTTOM, Alignment.RIGHT, 28);

            Button cancelButton = bb.addButton("Cancel");
            cancelButton.addClickHandler(new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {
                    if (_pp != null) {
                        _pp.hide();
                    }
                }
            });
            cancelButton.focus();

            _selectButton = bb.addButton("Select");
            _selectButton.addClickHandler(new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {
                    if (_tsl != null) {
                        assert _grid.selections() != null && !_grid.selections().isEmpty();
                        _tsl.typeSelected(_grid.selections().get(0));
                    }
                    _pp.hide();
                }

            });
            _selectButton.disable();
            _selectButton.setMarginRight(20);
            
            addChangeListener(new StateChangeListener(){

                @Override
                public void notifyOfChangeInState() {
                    Validity v = valid();
                    if (v.valid()) {
                        _sb.clear();
                    } else {
                        _sb.setHTML(v.reasonForIssue());
                    }
                   _selectButton.setEnabled(v.valid());
                }});

            _vp.add(bb);
            
            notifyOfChangeInState();
        }

        public Validity valid() {
            Validity v = super.valid();
            if (v.valid()) {
                if (_grid.selections() == null || _grid.selections().isEmpty()) {
                    v = new IsNotValid("No MIME type is selected.");
                }
            }
            return v;
        }

        @Override
        public Widget gui() {
            return _vp;
        }

        public void show() {
            if (_pp != null && _pp.isShowing()) {
                return;
            }
            int pLeft = _partner.getAbsoluteLeft();
            int pTop = _partner.getAbsoluteTop();
            int cw = com.google.gwt.user.client.Window.getClientWidth();
            int ch = com.google.gwt.user.client.Window.getClientHeight();
            if (_pp == null) {
                _pp = new PopupPanel();
                _pp.setPartner(_partner);
                _pp.setAutoHideEnabled(true);

                _pp.setBorder(1, new RGB(0xaa, 0xaa, 0xaa));
                _pp.setBackgroundColour(new RGB(0xf8, 0xf8, 0xf8));
                _pp.setBorderRadius(2);

                _pp.setWidth(DEFAULT_WIDTH <= cw - pLeft ? DEFAULT_WIDTH : cw - pLeft);
                _pp.setBoxShadow(2, 2, 4, 2, new RGBA(0, 0, 0, 0.3));

                _pp.setPopupPositionAndShow(new PositionCallback() {
                    public void setPosition(int offsetWidth, int offsetHeight) {

                        int x = _partner.absoluteLeft();
                        int y = _partner.absoluteBottom();
                        _pp.setPopupPosition(x, y);
                    }
                });

                _pp.addCloseHander(new PopupCloseHandler() {
                    public void closed(PopupPanel p) {
                        _pp = null;
                    }
                });

                _ap = new AbsolutePanel();
                _ap.fitToParent();
                _ap.add(new ResizeHandle(_ap, 15));
                _ap.add(_vp);
            }
            _pp.setContent(_ap);
            int h = DEFAULT_HEIGHT <= ch - pTop - 28 ? DEFAULT_HEIGHT : ch - pTop - 28;
            _pp.setHeight(h);
        }

    }

    @Override
    public Widget gui() {
        return _hp;
    }

}