package daris.client.ui.exmethod;

import java.util.List;
import java.util.Vector;

import arc.gui.ValidatedInterfaceComponent;
import arc.gui.form.Field;
import arc.gui.form.FieldDefinition;
import arc.gui.form.Form;
import arc.gui.form.FormEditMode;
import arc.gui.form.FormItem;
import arc.gui.form.FormItem.Property;
import arc.gui.form.FormItemListener;
import arc.gui.form.FormListener;
import arc.gui.gwt.colour.RGBA;
import arc.gui.gwt.data.DataLoadAction;
import arc.gui.gwt.data.DataLoadHandler;
import arc.gui.gwt.data.DataSource;
import arc.gui.gwt.data.filter.Filter;
import arc.gui.gwt.widget.BaseWidget;
import arc.gui.gwt.widget.HTML;
import arc.gui.gwt.widget.button.Button;
import arc.gui.gwt.widget.button.ButtonBar;
import arc.gui.gwt.widget.button.ButtonBar.Alignment;
import arc.gui.gwt.widget.button.ButtonBar.Position;
import arc.gui.gwt.widget.list.ListGrid;
import arc.gui.gwt.widget.list.ListGridDataSource;
import arc.gui.gwt.widget.list.ListGridEntry;
import arc.gui.gwt.widget.panel.SimplePanel;
import arc.gui.gwt.widget.panel.TabPanel;
import arc.gui.gwt.widget.panel.TabPanel.TabPosition;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.gui.gwt.widget.scroll.ScrollPanel;
import arc.gui.gwt.widget.scroll.ScrollPolicy;
import arc.gui.window.Window;
import arc.gui.window.WindowProperties;
import arc.mf.client.util.DynamicBoolean;
import arc.mf.client.util.ListUtil;
import arc.mf.client.util.StateChangeListener;
import arc.mf.client.util.Transformer;
import arc.mf.client.util.Validity;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlStringWriter;
import arc.mf.dtype.ConstantType;
import arc.mf.dtype.EnumerationType;
import arc.mf.dtype.TextType;
import arc.mf.event.Subscriber;
import arc.mf.event.SystemEvent;
import arc.mf.event.SystemEventChannel;
import arc.mf.object.ObjectMessage;
import arc.mf.object.ObjectMessageResponse;
import arc.mf.object.ObjectResolveHandler;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.user.client.ui.Widget;

import daris.client.Application;
import daris.client.Resource;
import daris.client.model.exmethod.ExMethodStep;
import daris.client.model.exmethod.ExMethodStudyStep;
import daris.client.model.exmethod.ExMethodSubjectStep;
import daris.client.model.exmethod.ExMethodTransformStep;
import daris.client.model.exmethod.State;
import daris.client.model.exmethod.messages.ExMethodStepStudyFind;
import daris.client.model.exmethod.messages.ExMethodStepUpdate;
import daris.client.model.exmethod.messages.ExMethodSubjectStepUpdate;
import daris.client.model.object.DObjectRef;
import daris.client.model.study.Study;
import daris.client.model.transform.Transform;
import daris.client.model.transform.TransformBuilder;
import daris.client.ui.exmethod.transform.TransformSelectDialog;
import daris.client.ui.form.XmlMetaForm;
import daris.client.ui.object.action.DObjectCreateAction;
import daris.client.ui.transform.TransformBrowser;
import daris.client.ui.transform.TransformForm;

public class ExMethodStepDialog extends ValidatedInterfaceComponent {

    public static final arc.gui.image.Image ICON_VALID = new arc.gui.image.Image(
            Resource.INSTANCE.tick12().getSafeUri().asString(), 12, 12);

    public static final arc.gui.image.Image ICON_INVALID = new arc.gui.image.Image(
            Resource.INSTANCE.cross12().getSafeUri().asString(), 12, 12);

    private MethodAndStep _mas;
    private ExMethodStep _step;

    private TabPanel _tp;
    private int _interfaceTabId = 0;
    private int _subjectTabId = 0;
    private int _studyTabId = 0;
    private int _transformTabId = 0;

    private SimplePanel _transformSP;
    private TransformForm _transformForm;
    private Button _transformExecuteButton;

    private arc.gui.gwt.widget.window.Window _win;

    public ExMethodStepDialog(MethodAndStep mas, ExMethodStep step) {

        _mas = mas;
        _step = step;

        _tp = new TabPanel(TabPosition.TOP);
        _tp.fitToParent();
        addInterfaceTab(_step);

        /*
         * type specific tab
         */
        if (_step instanceof ExMethodSubjectStep) {
            addSubjectTab((ExMethodSubjectStep) _step);
        } else if (_step instanceof ExMethodStudyStep) {
            addStudyTab((ExMethodStudyStep) _step);
        } else if (_step instanceof ExMethodTransformStep) {
            ((ExMethodTransformStep) _step).getTransformBuilder(
                    new ObjectResolveHandler<TransformBuilder>() {

                        @Override
                        public void resolved(TransformBuilder tb) {
                            if (tb != null) {
                                addTransformTab(tb);
                            }
                        }
                    });
        }

    }

    private void addInterfaceTab(ExMethodStep step) {

        VerticalPanel vp = new VerticalPanel();
        vp.fitToParent();

        _interfaceTabId = _tp.addTab("interface",
                "The interface elements for the step (name, state, notes).",
                vp);
        _tp.setActiveTabById(_interfaceTabId);

        Form form = new Form(FormEditMode.UPDATE);
        form.fitToParent();
        Field<String> nameField = new Field<String>(new FieldDefinition("step",
                ConstantType.DEFAULT, "step name", null, 1, 1));
        nameField.setInitialValue(_step.name(), false);
        form.add(nameField);

        Field<State> stateField = new Field<State>(new FieldDefinition("state",
                new EnumerationType<State>(State.values()), "state", null, 1,
                1));
        stateField.setInitialValue(_step.state(), false);
        stateField.addListener(new FormItemListener<State>() {
            @Override
            public void itemValueChanged(FormItem<State> f) {
                _step.setState(f.value());
            }

            @Override
            public void itemPropertyChanged(FormItem<State> f,
                    FormItem.Property property) {

            }
        });
        form.add(stateField);

        Field<String> notesField = new Field<String>(new FieldDefinition(
                "notes", TextType.DEFAULT, "notes", null, 0, 1));
        // FieldRenderOptions fro = new FieldRenderOptions();
        // fro.setWidth100();
        // fro.setHeight100();
        // notesField.setRenderOptions(fro);
        notesField.setValue(_step.notes());
        notesField.addListener(new FormItemListener<String>() {

            @Override
            public void itemValueChanged(FormItem<String> f) {
                _step.setNotes(f.value());
            }

            @Override
            public void itemPropertyChanged(FormItem<String> f,
                    FormItem.Property p) {

            }
        });
        form.add(notesField);

        form.render();
        vp.add(form);
        addMustBeValid(form);

        final HTML status = new HTML();
        status.setForegroundColour(RGBA.RED);
        status.setFontSize(12);
        status.setHeight(22);
        status.setWidth100();
        vp.add(status);

        ButtonBar bb = new ButtonBar(Position.BOTTOM, Alignment.CENTER);
        final Button updateButton = bb.addButton("Apply");
        updateButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                updateButton.disable();
                ObjectMessage<Boolean> msg = (_step instanceof ExMethodSubjectStep)
                        ? new ExMethodSubjectStepUpdate(
                                (ExMethodSubjectStep) _step)
                        : new ExMethodStepUpdate(_step);
                msg.send(new ObjectMessageResponse<Boolean>() {

                    @Override
                    public void responded(Boolean r) {
                        updateButton.enable();
                    }
                });
            }
        });

        vp.add(bb);

        addChangeListener(new StateChangeListener() {

            @Override
            public void notifyOfChangeInState() {
                Validity v = valid();
                if (v.valid()) {
                    status.setHTML(null);
                    updateButton.enable();
                } else {
                    status.setHTML(v.reasonForIssue());
                    updateButton.disable();
                }
            }
        });
    }

    private void addSubjectTab(final ExMethodSubjectStep step) {
        VerticalPanel vp = new VerticalPanel();
        vp.fitToParent();

        _subjectTabId = _tp.addTab("subject", null, vp);
        TabPanel tp = new TabPanel();

        tp.fitToParent();
        int psTabId = -1;
        int rsTabId = -1;
        XmlElement psMetaEditable = step.psPublicMetadataEditable();
        List<XmlElement> psmes = psMetaEditable == null ? null
                : psMetaEditable.elements();
        if (psmes != null && !psmes.isEmpty()) {
            Form psForm = XmlMetaForm.formFor(psmes, FormEditMode.UPDATE);
            psForm.addListener(new FormListener() {

                @Override
                public void rendering(Form f) {

                }

                @Override
                public void rendered(Form f) {

                }

                @Override
                public void formValuesUpdated(Form f) {
                    savePSPublicMetadata(step, f);
                }

                @Override
                public void formStateUpdated(Form f, Property p) {

                }
            });
            addMustBeValid(psForm);
            psForm.render();
            savePSPublicMetadata(step, psForm);
            psTabId = tp.addTab("public", null,
                    new ScrollPanel(psForm, ScrollPolicy.AUTO));
        }
        XmlElement rsMetaEditable = step.rsPublicMetadataEditable();
        List<XmlElement> rsmes = rsMetaEditable == null ? null
                : rsMetaEditable.elements();
        if (rsmes != null && !rsmes.isEmpty()) {
            Form rsForm = XmlMetaForm.formFor(rsmes, FormEditMode.UPDATE);
            rsForm.addListener(new FormListener() {

                @Override
                public void rendering(Form f) {

                }

                @Override
                public void rendered(Form f) {

                }

                @Override
                public void formValuesUpdated(Form f) {
                    saveRSPublicMetadata(step, f);
                }

                @Override
                public void formStateUpdated(Form f, Property p) {

                }
            });
            addMustBeValid(rsForm);
            rsForm.render();
            saveRSPublicMetadata(step, rsForm);
            rsTabId = tp.addTab("private", null,
                    new ScrollPanel(rsForm, ScrollPolicy.AUTO));
        }
        if (psTabId > 0) {
            tp.setActiveTabById(psTabId);
        } else if (rsTabId > 0) {
            tp.setActiveTabById(rsTabId);
        }
        vp.add(tp);

        final HTML status = new HTML();
        status.setForegroundColour(RGBA.RED);
        status.setFontSize(12);
        status.setHeight(22);
        status.setWidth100();
        vp.add(status);

        ButtonBar bb = new ButtonBar(Position.BOTTOM, Alignment.CENTER);
        final Button updateButton = bb.addButton("Apply");
        updateButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                updateButton.disable();
                ObjectMessage<Boolean> msg = (_step instanceof ExMethodSubjectStep)
                        ? new ExMethodSubjectStepUpdate(
                                (ExMethodSubjectStep) _step)
                        : new ExMethodStepUpdate(_step);
                msg.send(new ObjectMessageResponse<Boolean>() {

                    @Override
                    public void responded(Boolean r) {
                        updateButton.enable();
                    }
                });
            }
        });

        vp.add(bb);

        addChangeListener(new StateChangeListener() {

            @Override
            public void notifyOfChangeInState() {
                Validity v = valid();
                if (v.valid()) {
                    status.setHTML(null);
                    updateButton.enable();
                } else {
                    status.setHTML(v.reasonForIssue());
                    updateButton.disable();
                }
            }
        });
    }

    private static void savePSPublicMetadata(ExMethodSubjectStep step, Form f) {
        XmlStringWriter w = new XmlStringWriter();
        f.save(w);
        step.setPSPublicMetadata(w.document());
    }

    private static void saveRSPublicMetadata(ExMethodSubjectStep step, Form f) {
        XmlStringWriter w = new XmlStringWriter();
        f.save(w);
        step.setRSPublicMetadata(w.document());
    }

    private void addStudyTab(ExMethodStudyStep step) {

        VerticalPanel vp = new VerticalPanel();
        vp.fitToParent();

        _subjectTabId = _tp.addTab("study", null, vp);

        final ListGrid<Study> studyGrid = new ListGrid<Study>(
                ScrollPolicy.AUTO);

        vp.add(studyGrid);

        studyGrid.setDataSource(
                new ListGridDataSource<Study>(new DataSource<Study>() {

                    @Override
                    public boolean isRemote() {

                        return true;
                    }

                    @Override
                    public boolean supportCursor() {

                        return false;
                    }

                    @Override
                    public void load(final Filter filter, final long start,
                            final long end, final DataLoadHandler<Study> lh) {

                        ObjectMessage<List<Study>> msg = new ExMethodStepStudyFind(
                                _step.exMethodId(), _step.exMethodProute(),
                                _mas.stepPath());
                        msg.send(new ObjectMessageResponse<List<Study>>() {

                            @Override
                            public void responded(List<Study> studies) {

                                if (studies != null) {
                                    List<Study> rstudies = studies;
                                    if (filter != null) {
                                        List<Study> fstudies = new Vector<Study>();
                                        for (Study a : studies) {
                                            if (filter.matches(a)) {
                                                fstudies.add(a);
                                            }
                                        }
                                        rstudies = fstudies;
                                    }
                                    long total = rstudies.size();
                                    int start1 = (int) start;
                                    int end1 = (int) end;
                                    if (start1 > 0 || end1 < rstudies.size()) {
                                        if (start1 >= rstudies.size()) {
                                            rstudies = null;
                                        } else {
                                            if (end1 > rstudies.size()) {
                                                end1 = rstudies.size();
                                            }
                                            rstudies = rstudies.subList(start1,
                                                    end1);
                                        }
                                    }
                                    if (rstudies != null) {
                                        if (rstudies.isEmpty()) {
                                            rstudies = null;
                                        }
                                    }
                                    lh.loaded(start1, end1, total, rstudies,
                                            rstudies == null ? null
                                                    : DataLoadAction.REPLACE);
                                } else {
                                    lh.loaded(0, 0, 0, null, null);
                                }
                            }
                        });
                    }
                }, new Transformer<Study, ListGridEntry<Study>>() {

                    @Override
                    protected ListGridEntry<Study> doTransform(Study study)
                            throws Throwable {

                        ListGridEntry<Study> entry = new ListGridEntry<Study>(
                                study);
                        entry.set("id", study.id());
                        entry.set("name", study.name());
                        entry.set("path", study.stepPath());
                        return entry;
                    }
                }));
        studyGrid.setCursorSize(500);
        studyGrid.addColumnDefn("id", "id");
        studyGrid.addColumnDefn("name", "name");
        studyGrid.setEmptyMessage("");
        studyGrid.fitToParent();
        final Subscriber subscriber = new Subscriber() {
            @Override
            public List<arc.mf.event.Filter> systemEventFilters() {
                return ListUtil.list(new arc.mf.event.Filter("pssd-object",
                        _step.exMethodId(), DynamicBoolean.FALSE));
            }

            @Override
            public void process(SystemEvent se) {
                studyGrid.refresh();
            }
        };
        SystemEventChannel.add(subscriber);

        studyGrid.addAttachHandler(new AttachEvent.Handler() {

            @Override
            public void onAttachOrDetach(AttachEvent event) {
                if (event.isAttached()) {
                    // SystemEventChannel.add(subscriber);
                } else {
                    SystemEventChannel.remove(subscriber);
                }
            }
        });

        ButtonBar bb = new ButtonBar(Position.BOTTOM, Alignment.CENTER);
        final Button addStudyButton = bb.addButton("Add Study...");
        addStudyButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                addStudyButton.disable();
                DObjectRef exmr = new DObjectRef(_step.exMethodId(),
                        _mas.exMethod().proute(), false, false, -1);
                DObjectCreateAction action = new DObjectCreateAction(exmr,
                        window()) {
                    @Override
                    protected void finished() {
                        addStudyButton.enable();
                    }

                    @Override
                    protected void cancelled() {
                        addStudyButton.enable();
                    }
                };
                Study study = (Study) action.objectCreating();
                study.setStepPath(_step.stepPath());
                study.setStudyType(((ExMethodStudyStep) _step).studyType());
                action.execute();
            }
        });
        vp.add(bb);

    }

    private void addTransformTab(final TransformBuilder tb) {

        final ExMethodTransformStep step = (ExMethodTransformStep) _step;
        VerticalPanel vp = new VerticalPanel();
        vp.fitToParent();

        _transformTabId = _tp.addTab("transform", null, vp);

        _transformSP = new SimplePanel();
        _transformSP.fitToParent();
        vp.add(_transformSP);

        _transformForm = new TransformForm(tb);
        _transformSP.setContent(_transformForm.gui());

        ButtonBar bb = new ButtonBar(Position.BOTTOM, Alignment.CENTER);
        Button loadFromExistingButton = bb
                .addButton("Load parameter values from an existing transform");
        loadFromExistingButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                new TransformSelectDialog(step,
                        new TransformSelectDialog.SelectionHandler() {

                    @Override
                    public void selected(Transform transform) {
                        if (transform != null) {
                            tb.loadParameterValues(step, transform);
                            _transformForm = new TransformForm(tb);
                            _transformForm.addChangeListener(
                                    new StateChangeListener() {

                                @Override
                                public void notifyOfChangeInState() {
                                    _transformExecuteButton.setEnabled(
                                            _transformForm.valid().valid());
                                }
                            });
                            _transformSP.setContent(_transformForm.gui());
                            _transformExecuteButton
                                    .setEnabled(_transformForm.valid().valid());
                            _transformForm.addChangeListener(
                                    new StateChangeListener() {

                                @Override
                                public void notifyOfChangeInState() {
                                    _transformExecuteButton.setEnabled(
                                            _transformForm.valid().valid());
                                }
                            });
                        }
                    }
                }).show(_win);
            }
        });
        _transformExecuteButton = bb.addButton("Execute");
        _transformExecuteButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                _transformExecuteButton.disable();
                tb.executeTransform(new ObjectMessageResponse<String>() {

                    @Override
                    public void responded(String r) {
                        if (r != null) {
                            _transformExecuteButton.enable();
                            hide();
                            // TransformMonitor tm = TransformMonitor.get();
                            // tm.start();
                            // tm.show(Application.window(), 0.5, 0.5);
                            TransformBrowser.get().show(Application.window());
                        }
                    }
                });
                // @formatter:off
//                new ExMethodTransformStepExecute(step, true, null).send(new ObjectMessageResponse<List<String>>() {
//
//                    @Override
//                    public void responded(List<String> r) {
//                        executeButton.enable();
//                        TransformMonitor tm = TransformMonitor.get();
//                        tm.start();
//                        tm.show(Application.window(), 0.5, 0.5);
//                    }
//                });
                // @formatter:on
            }
        });
        _transformExecuteButton.setEnabled(_transformForm.valid().valid());
        _transformForm.addChangeListener(new StateChangeListener() {

            @Override
            public void notifyOfChangeInState() {
                _transformExecuteButton
                        .setEnabled(_transformForm.valid().valid());
            }
        });
        vp.add(bb);
    }

    protected void activateInterfaceTab() {
        if (_interfaceTabId > 0) {
            _tp.setActiveTabById(_interfaceTabId);
        }
    }

    protected void activateSubjectTab() {
        if (_subjectTabId > 0) {
            _tp.setActiveTabById(_subjectTabId);
        }
    }

    protected void activateStudyTab() {
        if (_studyTabId > 0) {
            _tp.setActiveTabById(_studyTabId);
        }
    }

    protected void activateTransformTab() {
        if (_transformTabId > 0) {
            _tp.setActiveTabById(_transformTabId);
        }
    }

    @Override
    public Widget gui() {
        return _tp;
    }

    public BaseWidget widget() {
        return _tp;
    }

    public Window window() {
        return _tp.window();
    }

    public void show(Window owner) {

        WindowProperties wp = new WindowProperties();
        wp.setTitle("Editing step " + _step.stepPath() + " of ex-method ");
        wp.setOwnerWindow(owner);
        wp.setModal(false);
        wp.setCanBeClosed(true);
        wp.setCanBeResized(true);
        wp.setCenterInPage(true);
        wp.setSize(0.7, 0.7);

        _win = arc.gui.gwt.widget.window.Window.create(wp);
        _win.setContent(_tp);
        _win.show();
    }

    private void hide() {
        if (_win != null) {
            _win.closeIfOK();
        }
    }
}
