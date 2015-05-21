package daris.client.ui.query.options;

import java.util.ArrayList;
import java.util.List;

import arc.gui.ValidatedInterfaceComponent;
import arc.gui.dialog.DialogProperties;
import arc.gui.dialog.DialogProperties.Type;
import arc.gui.gwt.colour.RGB;
import arc.gui.gwt.data.DataLoadAction;
import arc.gui.gwt.data.DataLoadHandler;
import arc.gui.gwt.data.DataSource;
import arc.gui.gwt.data.filter.Filter;
import arc.gui.gwt.widget.button.Button;
import arc.gui.gwt.widget.button.ButtonBar;
import arc.gui.gwt.widget.button.ButtonBar.Alignment;
import arc.gui.gwt.widget.button.ButtonBar.Position;
import arc.gui.gwt.widget.dialog.Dialog;
import arc.gui.gwt.widget.event.SelectionHandler;
import arc.gui.gwt.widget.image.Image;
import arc.gui.gwt.widget.list.ListGrid;
import arc.gui.gwt.widget.list.ListGridEntry;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.gui.gwt.widget.scroll.ScrollPolicy;
import arc.gui.resource.RelativeResource;
import arc.mf.client.util.ActionListener;

import com.google.gwt.dom.client.Style.BorderStyle;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;

import daris.client.Resource;
import daris.client.model.query.options.QueryOptions;
import daris.client.model.query.options.XPath;

public class XPathForm extends ValidatedInterfaceComponent {
    public static final int INDENT = 20;
    public static arc.gui.image.Image ICON_ADD = new arc.gui.image.Image(new RelativeResource(
            "resources/images/add.png").path());
    public static arc.gui.image.Image ICON_EDIT = new arc.gui.image.Image(Resource.INSTANCE.edit16().getSafeUri()
            .asString());
    public static arc.gui.image.Image ICON_REMOVE = new arc.gui.image.Image(new RelativeResource(
            "resources/images/remove.png").path());
    public static arc.gui.image.Image ICON_CLEAR = new arc.gui.image.Image(Resource.INSTANCE.clear16().getSafeUri()
            .asString());

    private QueryOptions _opts;
    private VerticalPanel _vp;

    private Button _addButton;
    private Button _editButton;
    private Button _removeButton;
    private Button _clearButton;

    private ListGrid<XPath> _lg;

    public XPathForm(QueryOptions opts) {

        _opts = opts;
        _vp = new VerticalPanel();
        _vp.setWidth(750);
        _vp.setHeight(200);
        _vp.setPaddingRight(10);
        _vp.setBorder(1, BorderStyle.DOTTED, new RGB(0xcc, 0xcc, 0xcc));

        ButtonBar bb = new ButtonBar(Position.TOP, Alignment.LEFT);
        bb.setHeight(26);

        _addButton = new Button("Add", new Image(ICON_ADD), false);
        _addButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(com.google.gwt.event.dom.client.ClickEvent event) {
                showXPathAddDialog();
            }
        });
        bb.add(_addButton);

        _editButton = new Button("Edit", new Image(ICON_EDIT), false);
        _editButton.disable();
        _editButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(com.google.gwt.event.dom.client.ClickEvent event) {
                showXPathEditDialog();
            }

        });
        bb.add(_editButton);

        _removeButton = new Button("Remove", new Image(ICON_REMOVE), false);
        _removeButton.disable();
        _removeButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(com.google.gwt.event.dom.client.ClickEvent event) {
                List<XPath> selections = _lg.selections();
                if (selections == null || selections.isEmpty()) {
                    return;
                }
                XPath xpath = selections.get(0);
                _opts.removeXPath(xpath);
                _lg.refresh();
            }

        });
        bb.add(_removeButton);

        _clearButton = new Button("Clear", new Image(ICON_CLEAR), false);
        _clearButton.disable();
        _clearButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(com.google.gwt.event.dom.client.ClickEvent event) {
                _opts.clearXPaths();
                _lg.refresh();
            }
        });
        bb.add(_clearButton);

        _vp.add(bb);

        _lg = new ListGrid<XPath>(new DataSource<ListGridEntry<XPath>>() {
            @Override
            public boolean isRemote() {
                return false;
            }

            @Override
            public boolean supportCursor() {
                return false;
            }

            @Override
            public void load(Filter f, long start, long end, DataLoadHandler<ListGridEntry<XPath>> lh) {
                _editButton.disable();
                _removeButton.disable();
                _clearButton.disable();
                List<XPath> xpaths = _opts.xpaths();
                if (xpaths == null || xpaths.isEmpty()) {
                    lh.loaded(0, 0, 0, null, null);
                    return;
                }
                List<ListGridEntry<XPath>> es = new ArrayList<ListGridEntry<XPath>>(xpaths.size());
                for (XPath xpath : xpaths) {
                    ListGridEntry<XPath> e = new ListGridEntry<XPath>(xpath);
                    e.set("name", xpath.name());
                    e.set("key", xpath.key());
                    e.set("xpath", xpath.value());
                    e.set("dictionary", xpath.dictionary());
                    e.set("dictionary-variant", xpath.dictionaryVariant());
                    es.add(e);
                }
                lh.loaded(start, end, es.size(), es, DataLoadAction.REPLACE);
                _addButton.enable();
                _clearButton.enable();
            }
        }, Integer.MAX_VALUE, ScrollPolicy.VERTICAL);

        _lg.fitToParent();
        _lg.setShowHeader(true);
        _lg.setLoadingMessage("");
        _lg.setEmptyMessage("");
        _lg.setMultiSelect(false);
        _lg.addColumnDefn("xpath", "xpath", "xpath").setWidth(300);
        _lg.addColumnDefn("name", "name", "name").setWidth(200);
        _lg.addColumnDefn("dictionary", "dictionary", "dictionary").setWidth(100);
        _lg.addColumnDefn("dictionary-variant", "variant", "variant").setWidth(150);
        _lg.refresh();
        _lg.setSelectionHandler(new SelectionHandler<XPath>() {
            public void selected(XPath xpath) {
                _removeButton.enable();
                _editButton.enable();
                _clearButton.enable();
            }

            public void deselected(XPath xpath) {
                _removeButton.disable();
                _editButton.disable();
            }

        });
        _vp.add(_lg);

    }

    @Override
    public Widget gui() {
        return _vp;
    }

    private void showXPathAddDialog() {
        XPathAddForm form = new XPathAddForm(_opts);
        DialogProperties dp = new DialogProperties(Type.ACTION, "Select meta-data node", form);
        dp.setButtonLabel("Add");
        dp.setButtonAction(form);
        dp.setOwner(_vp.window());
        dp.setSize(360, 230);
        Dialog dlg = Dialog.postDialog(dp, new ActionListener() {
            @Override
            public void executed(boolean succeeded) {
                if (succeeded) {
                    _lg.refresh();
                }
            }
        });
        dlg.show();
    }

    private void showXPathEditDialog() {
        if (_lg.selections() == null || _lg.selections().isEmpty()) {
            return;
        }
        XPathEditForm form = new XPathEditForm(_opts, _lg.selections().get(0));
        DialogProperties dp = new DialogProperties(Type.ACTION, "Edit xpath", form);
        dp.setButtonLabel("Modify");
        dp.setButtonAction(form);
        dp.setOwner(_vp.window());
        dp.setSize(360, 230);
        Dialog dlg = Dialog.postDialog(dp, new ActionListener() {
            @Override
            public void executed(boolean succeeded) {
                if (succeeded) {
                    _lg.refresh();
                }
            }
        });
        dlg.show();
    }
}
