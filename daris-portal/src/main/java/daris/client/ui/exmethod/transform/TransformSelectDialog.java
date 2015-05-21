package daris.client.ui.exmethod.transform;

import java.util.List;

import arc.gui.form.Field;
import arc.gui.form.FieldDefinition;
import arc.gui.form.Form;
import arc.gui.form.FormItem;
import arc.gui.form.FormItem.Property;
import arc.gui.form.FormItemListener;
import arc.gui.gwt.widget.BaseWidget;
import arc.gui.gwt.widget.HTML;
import arc.gui.gwt.widget.button.Button;
import arc.gui.gwt.widget.button.ButtonBar;
import arc.gui.gwt.widget.format.WidgetFormatter;
import arc.gui.gwt.widget.image.LinearGradient;
import arc.gui.gwt.widget.list.ListGrid;
import arc.gui.gwt.widget.list.ListGridEntry;
import arc.gui.gwt.widget.list.ListGridHeader;
import arc.gui.gwt.widget.list.ListGridRowDoubleClickHandler;
import arc.gui.gwt.widget.paging.PagingControl;
import arc.gui.gwt.widget.paging.PagingListener;
import arc.gui.gwt.widget.panel.CenteringPanel;
import arc.gui.gwt.widget.panel.HorizontalPanel;
import arc.gui.gwt.widget.panel.HorizontalSplitPanel;
import arc.gui.gwt.widget.panel.SimplePanel;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.gui.gwt.widget.scroll.ScrollPanel;
import arc.gui.gwt.widget.scroll.ScrollPolicy;
import arc.gui.gwt.widget.window.Window;
import arc.gui.gwt.widget.window.WindowCloseListener;
import arc.gui.window.WindowProperties;
import arc.mf.client.util.Transformer;
import arc.mf.dtype.ConstantType;
import arc.mf.dtype.EnumerationType;
import arc.mf.object.CollectionResolveHandler;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DoubleClickEvent;

import daris.client.model.IDUtil;
import daris.client.model.exmethod.ExMethodTransformStep;
import daris.client.model.transform.RelatedTransformCollectionRef;
import daris.client.model.transform.Transform;
import daris.client.model.transform.Transform.Status.State;
import daris.client.ui.transform.TransformForm;
import daris.client.ui.transform.TransformNavigator;
import daris.client.ui.util.ButtonUtil;

public class TransformSelectDialog implements PagingListener {

    private static final int PAGE_SIZE = 100;

    public static interface SelectionHandler {
        void selected(Transform transform);
    }

    public static enum Scope {
        repository("repository"), project("project"), subject("subject"), ex_method("ex-method"), step("step");
        public final String name;

        Scope(String name) {
            this.name = name;
        }

        @Override
        public final String toString() {
            return this.name;
        }
    }

    private RelatedTransformCollectionRef _rtc;

    private Scope _scope;

    private ExMethodTransformStep _step;
    private SelectionHandler _sh;

    private arc.gui.gwt.widget.window.Window _win;
    private VerticalPanel _vp;
    private ListGrid<Transform> _grid;
    private PagingControl _pc;
    private SimplePanel _dv;

    private List<Transform> _resolved;
    private Transform _selected;

    private Button _cancelButton;
    private Button _selectButton;

    public TransformSelectDialog(ExMethodTransformStep step, SelectionHandler sh) {
        this(step, Scope.step, sh);
    }

    protected TransformSelectDialog(ExMethodTransformStep step, Scope scope, SelectionHandler sh) {
        _step = step;
        _scope = scope;
        _sh = sh;

        _rtc = _scope == Scope.step ? new RelatedTransformCollectionRef(_step.exMethodId(), _step.stepPath())
                : new RelatedTransformCollectionRef(_step.transformDefinitionUid(), _step.transformDefinitionVersion(),
                        scopeId(_step.exMethodId(), _scope));

        _vp = new VerticalPanel();
        _vp.fitToParent();

        CenteringPanel scopeCP = new CenteringPanel();
        scopeCP.setHeight(32);
        scopeCP.setWidth100();
        scopeCP.setBackgroundImage(new LinearGradient(LinearGradient.Orientation.TOP_TO_BOTTOM,
                ListGridHeader.HEADER_COLOUR_LIGHT, ListGridHeader.HEADER_COLOUR_DARK));

        Form scopeForm = new Form();
        scopeForm.setNumberOfColumns(2);
        Field<Long> definitionField = new Field<Long>(new FieldDefinition("definition", ConstantType.DEFAULT,
                "The unique id of the transform definition.", null, 1, 1));
        definitionField.setInitialValue(_step.transformDefinitionUid(), false);
        scopeForm.add(definitionField);
        Field<Scope> scopeField = new Field<Scope>(new FieldDefinition("scope", new EnumerationType<Scope>(
                Scope.values()), "he citeable id of the parent object that the transform output datasets belong to.",
                null, 1, 1));
        scopeField.setInitialValue(_scope, false);
        scopeField.addListener(new FormItemListener<Scope>() {

            @Override
            public void itemValueChanged(FormItem<Scope> f) {
                _scope = f.value();
                _rtc = _scope == Scope.step ? new RelatedTransformCollectionRef(_step.exMethodId(), _step.stepPath())
                        : new RelatedTransformCollectionRef(_step.transformDefinitionUid(), _step
                                .transformDefinitionVersion(), scopeId(_step.exMethodId(), _scope));
                gotoOffset(0);
            }

            @Override
            public void itemPropertyChanged(FormItem<Scope> f, Property property) {

            }
        });
        scopeForm.add(scopeField);
        scopeForm.fitToParent();
        scopeForm.render();
        scopeCP.setContent(scopeForm);
        _vp.add(scopeCP);

        HorizontalSplitPanel hsp = new HorizontalSplitPanel(5);
        hsp.fitToParent();
        _vp.add(hsp);

        ButtonBar bb = ButtonUtil.createButtonBar(ButtonBar.Position.BOTTOM, ButtonBar.Alignment.RIGHT, 28);

        _cancelButton = bb.addButton("Cancel");
        _cancelButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                if (_win != null) {
                    _win.closeIfOK();
                }
            }
        });

        _selectButton = bb.addButton("Select");
        _selectButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                if (_sh != null) {
                    _sh.selected(_selected);
                }
                if (_win != null) {
                    _win.closeIfOK();
                }
            }
        });
        _selectButton.setEnabled(_selected != null);
        _selectButton.setMarginRight(15);
        _vp.add(bb);

        VerticalPanel nav = new VerticalPanel();
        nav.setHeight100();
        nav.setPreferredWidth(0.6);

        hsp.add(nav);

        _dv = new SimplePanel();
        _dv.setPreferredWidth(0.4);
        _dv.setHeight100();

        hsp.add(_dv);

        _grid = new ListGrid<Transform>(ScrollPolicy.AUTO) {
            protected void preLoad() {
                _selected = (_grid.selections() != null && !_grid.selections().isEmpty()) ? _grid.selections().get(0)
                        : null;
            }

            protected void postLoad(long start, long end, long total, List<ListGridEntry<Transform>> entries) {
                if (entries != null && !entries.isEmpty()) {
                    if (_selected == null) {
                        select(0);
                    } else {
                        for (int i = 0; i < entries.size(); i++) {
                            if (entries.get(i).data().uid() == _selected.uid()) {
                                select(i);
                                break;
                            }
                        }
                    }
                }
            }
        };
        _grid.setSelectionHandler(new arc.gui.gwt.widget.event.SelectionHandler<Transform>() {

            @Override
            public void selected(Transform o) {
                _selected = o;
                _dv.clear();
                if (_selected != null) {
                    _dv.setContent(new ScrollPanel(TransformForm.formForView(_selected), ScrollPolicy.AUTO));
                }
                _selectButton.setEnabled(_selected != null);
            }

            @Override
            public void deselected(Transform o) {
                _selected = null;
                _dv.clear();
                _selectButton.setEnabled(_selected != null);
            }
        });
        _grid.setRowDoubleClickHandler(new ListGridRowDoubleClickHandler<Transform>() {

            @Override
            public void doubleClicked(Transform data, DoubleClickEvent event) {
                if (_sh != null) {
                    _sh.selected(data);
                }
                if (_win != null) {
                    _win.closeIfOK();
                }
            }
        });
        _grid.setClearSelectionOnRefresh(false);
        _grid.setMultiSelect(false);
        _grid.setEmptyMessage("No transforms found.");
        _grid.fitToParent();
        _grid.setLoadingMessage("Loading transforms.");
        _grid.setCursorSize(PAGE_SIZE);
        _grid.addColumnDefn("uid", "uid").setWidth(80);
        _grid.addColumnDefn("name", "name").setWidth(220);
        _grid.addColumnDefn("type", "type").setWidth(80);
        _grid.addColumnDefn("status", "status", null, new WidgetFormatter<Transform, Transform.Status.State>() {

            @Override
            public BaseWidget format(Transform t, State state) {
                arc.gui.gwt.widget.image.Image i = null;
                switch (state) {
                case pending:
                    i = new arc.gui.gwt.widget.image.Image(TransformNavigator.ICON_PENDING, 16, 16);
                    break;
                case running:
                    i = new arc.gui.gwt.widget.image.Image(TransformNavigator.ICON_RUNNING, 16, 16);
                    break;
                case suspended:
                    i = new arc.gui.gwt.widget.image.Image(TransformNavigator.ICON_SUSPENDED, 16, 16);
                    break;
                case terminated:
                    i = new arc.gui.gwt.widget.image.Image(TransformNavigator.ICON_TERMINATED, 16, 16);
                    break;
                case failed:
                    i = new arc.gui.gwt.widget.image.Image(TransformNavigator.ICON_FAILED, 16, 16);
                    break;
                case unknown:
                    i = new arc.gui.gwt.widget.image.Image(TransformNavigator.ICON_UNKNOWN, 16, 16);
                    break;
                default:
                    break;
                }
                HorizontalPanel hp = new HorizontalPanel();
                hp.setHeight(20);
                hp.add(i);
                hp.setSpacing(3);
                HTML label = new HTML(state.name());
                label.setFontSize(10);
                hp.add(label);
                return hp;
            }
        });
        _grid.addColumnDefn("modified", "modified").setWidth(120);
        nav.add(_grid);

        _pc = new PagingControl(PAGE_SIZE);
        _pc.setWidth100();
        _pc.setHeight(22);
        _pc.addPagingListener(this);
        nav.add(_pc);

        // initial load
        gotoOffset(0);
    }

    private static String scopeId(String exMethodId, Scope scope) {
        switch (scope) {
        case repository:
            return null;
        case project:
            return IDUtil.getProjectId(exMethodId);
        case subject:
            return IDUtil.getSubjectId(exMethodId);
        case ex_method:
        case step:
            return exMethodId;
        default:
            return null;
        }
    }

    @Override
    public void gotoOffset(final long offset) {
        _rtc.resolve(offset, offset + PAGE_SIZE, new CollectionResolveHandler<Transform>() {
            @Override
            public void resolved(List<Transform> data) throws Throwable {
                long total = _rtc.totalNumberOfMembers();
                _pc.setOffset(offset, total, true);
                _resolved = data;
                List<ListGridEntry<Transform>> entries = null;
                if (_resolved != null && !_resolved.isEmpty()) {
                    entries = arc.mf.client.util.Transform.transform(_resolved,
                            new Transformer<Transform, ListGridEntry<Transform>>() {

                                @Override
                                protected ListGridEntry<Transform> doTransform(Transform t) throws Throwable {
                                    ListGridEntry<Transform> e = new ListGridEntry<Transform>(t);
                                    e.set("uid", t.uid());
                                    e.set("name", t.name());
                                    e.set("type", t.type());
                                    e.set("status", t.status().state());
                                    e.set("modified", t.status().time());
                                    return e;
                                }
                            });
                }
                _grid.setData(entries);
            }
        });

    }

    public void show(arc.gui.window.Window owner) {
        WindowProperties wp = new WindowProperties();
        wp.setOwnerWindow(owner);
        wp.setSize(0.7, 0.7);
        wp.setCanBeClosed(false);
        wp.setCanBeMaximised(true);
        wp.setCanBeMoved(true);
        wp.setCanBeResized(true);
        wp.setCenterInPage(true);
        wp.setModal(true);
        wp.setTitle("Select a previously executed transfrom to copy parameter values from ...");

        _win = Window.create(wp);
        _win.addCloseListener(new WindowCloseListener() {

            @Override
            public void closed(Window w) {
                _win = null;
            }
        });
        _win.setContent(_vp);
        _win.centerInPage();
        _win.show();

    }
}
