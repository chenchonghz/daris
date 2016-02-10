package daris.client.ui.exmethod;

import java.util.List;
import java.util.Vector;

import arc.gui.ValidatedInterfaceComponent;
import arc.gui.form.Field;
import arc.gui.form.FieldDefinition;
import arc.gui.form.FieldRenderOptions;
import arc.gui.form.Form;
import arc.gui.form.FormEditMode;
import arc.gui.form.FormItem;
import arc.gui.form.FormItem.Property;
import arc.gui.form.FormItemListener;
import arc.gui.form.FormListener;
import arc.gui.gwt.colour.RGB;
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
import arc.gui.gwt.widget.image.Image;
import arc.gui.gwt.widget.image.LinearGradient;
import arc.gui.gwt.widget.label.Label;
import arc.gui.gwt.widget.list.ListGrid;
import arc.gui.gwt.widget.list.ListGridDataSource;
import arc.gui.gwt.widget.list.ListGridEntry;
import arc.gui.gwt.widget.panel.AbsolutePanel;
import arc.gui.gwt.widget.panel.HorizontalPanel;
import arc.gui.gwt.widget.panel.TabPanel;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.gui.gwt.widget.scroll.ScrollPanel;
import arc.gui.gwt.widget.scroll.ScrollPolicy;
import arc.gui.window.Window;
import arc.mf.client.util.ActionListener;
import arc.mf.client.util.AsynchronousAction;
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

import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.BorderStyle;
import com.google.gwt.dom.client.Style.VerticalAlign;
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
import daris.client.model.exmethod.messages.ExMethodTransformStepExecute;
import daris.client.model.object.DObjectRef;
import daris.client.model.study.Study;
import daris.client.model.transform.Transform;
import daris.client.ui.form.XmlMetaForm;
import daris.client.ui.object.action.DObjectCreateAction;
import daris.client.ui.transform.TransformBrowser;

public class StepEditForm extends ValidatedInterfaceComponent
        implements AsynchronousAction {

    public static final arc.gui.image.Image ICON_VALID = new arc.gui.image.Image(
            Resource.INSTANCE.tick12().getSafeUri().asString(), 12, 12);

    public static final arc.gui.image.Image ICON_INVALID = new arc.gui.image.Image(
            Resource.INSTANCE.cross12().getSafeUri().asString(), 12, 12);

    private MethodAndStep _mas;
    private ExMethodStep _step;

    private VerticalPanel _vp;

    private Image _statusIcon;
    private HTML _statusHTML;

    public StepEditForm(MethodAndStep mas, ExMethodStep step) {
        _mas = mas;
        _step = step;

        // step form
        _vp = new VerticalPanel();
        _vp.fitToParent();

        /*
         * step status
         */
        Form stepForm = new Form(FormEditMode.UPDATE);
        stepForm.setWidth100();
        stepForm.setHeight(125);
        Field<String> stepNameField = new Field<String>(new FieldDefinition(
                "Step", ConstantType.DEFAULT, "Step name", null, 1, 1));
        stepNameField.setInitialValue(_step.name(), false);
        stepForm.add(stepNameField);
        Field<State> stateField = new Field<State>(new FieldDefinition("State",
                new EnumerationType<State>(State.values()), "State", null, 1,
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
        stepForm.add(stateField);
        Field<String> notesField = new Field<String>(new FieldDefinition(
                "Notes", TextType.DEFAULT, "Notes", null, 0, 1));
        FieldRenderOptions fro = new FieldRenderOptions();
        fro.setWidth(1.0);
        notesField.setRenderOptions(fro);
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
        stepForm.add(notesField);
        addMustBeValid(stepForm);
        stepForm.render();
        _vp.add(stepForm);

        //
        if (_step instanceof ExMethodStudyStep) {
            addStudyForm((ExMethodStudyStep) _step);
        } else if (_step instanceof ExMethodSubjectStep) {
            addSubjectForm((ExMethodSubjectStep) _step);
        } else if (_step instanceof ExMethodTransformStep) {
            addTransformForm((ExMethodTransformStep) _step);
        }

        // status
        AbsolutePanel statusAP = new AbsolutePanel();
        statusAP.setHeight(20);
        statusAP.setWidth100();
        statusAP.setBorderTop(1, BorderStyle.SOLID, new RGB(0xaa, 0xaa, 0xaa));

        HorizontalPanel statusHP = new HorizontalPanel();
        statusHP.setHeight100();
        statusHP.setPosition(Style.Position.ABSOLUTE);
        statusHP.setLeft(0);
        statusAP.add(statusHP);

        _statusIcon = new Image(ICON_VALID, 12, 12);
        _statusIcon.setDisabledImage(ICON_INVALID);
        _statusIcon.setMarginTop(3);
        _statusIcon.hide();
        statusHP.setSpacing(3);
        statusHP.add(_statusIcon);

        _statusHTML = new HTML();
        _statusHTML.setMarginTop(1);
        _statusHTML.setVerticalAlign(VerticalAlign.MIDDLE);
        _statusHTML.setFontSize(11);
        statusHP.add(_statusHTML);

        addChangeListener(new StateChangeListener() {

            @Override
            public void notifyOfChangeInState() {
                Validity v = valid();
                _statusIcon.setEnabled(v.valid());
                _statusIcon.setVisible(!v.valid());
                _statusHTML.setHTML(v.valid() ? null : v.reasonForIssue());
            }
        });

        _vp.add(statusAP);

        notifyOfChangeInState();

    }

    private void addTransformForm(final ExMethodTransformStep step) {

        TabPanel tp = new TabPanel();
        tp.fitToParent();

        VerticalPanel vp = new VerticalPanel();
        vp.fitToParent();

        int tabId = tp.addTab("Transform", null, vp);
        tp.setActiveTabById(tabId);

        ButtonBar bb = new ButtonBar(Position.TOP, Alignment.LEFT);
        bb.setHeight(26);
        final Button btn = bb.addButton("Execute");
        btn.setFontSize(12);
        btn.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                btn.disable();
                // TODO: link with paramGrid.
                new ExMethodTransformStepExecute(step, true, null)
                        .send(new ObjectMessageResponse<List<String>>() {

                    @Override
                    public void responded(List<String> r) {
                        btn.enable();
                        TransformBrowser.get().show(Application.window());
                        // TransformMonitor tm = TransformMonitor.get();
                        // tm.start();
                        // tm.show(Application.window(), 0.5, 0.5);
                    }
                });
            }
        });
        btn.disable();
        if (step.iterator() != null) {
            btn.enable();
        }
        vp.add(bb);

        Label paramLabel = new Label("Parameters");
        paramLabel.setHeight(20);
        paramLabel.setWidth100();
        paramLabel.setFontSize(12);
        paramLabel.setPaddingLeft(15);
        paramLabel.setBackgroundImage(
                new LinearGradient(LinearGradient.Orientation.TOP_TO_BOTTOM,
                        new RGB(0xcc, 0xcc, 0xcc), new RGB(0x99, 0x99, 0x99)));
        vp.add(paramLabel);

        ListGrid<Transform.Parameter> paramGrid = new ListGrid<Transform.Parameter>(
                ScrollPolicy.AUTO);
        paramGrid.setCursorSize(500);
        paramGrid.addColumnDefn("name", "name");
        paramGrid.addColumnDefn("value", "value");
        paramGrid.setEmptyMessage("");
        paramGrid.fitToParent();
        paramGrid.setMultiSelect(false);
        vp.add(paramGrid);

        _vp.add(tp);

    }

    private void addStudyForm(ExMethodStudyStep step) {

        TabPanel tp = new TabPanel();
        tp.fitToParent();

        VerticalPanel vp = new VerticalPanel();
        vp.fitToParent();

        int tabId = tp.addTab("Study", null, vp);
        tp.setActiveTabById(tabId);

        final ListGrid<Study> studyGrid = new ListGrid<Study>(
                ScrollPolicy.AUTO);
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

        ButtonBar bb = new ButtonBar(Position.TOP, Alignment.LEFT);
        bb.setHeight(26);
        final Button btn = bb.addButton("Add Study...");
        btn.setFontSize(12);
        btn.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                btn.disable();

                DObjectRef exmr = new DObjectRef(_step.exMethodId(),
                        _mas.exMethod().proute(), false, false, -1);

                DObjectCreateAction action = new DObjectCreateAction(exmr,
                        window()) {
                    @Override
                    protected void finished() {
                        btn.enable();
                    }

                    @Override
                    protected void cancelled() {
                        btn.enable();
                    }
                };
                Study study = (Study) action.objectCreating();
                study.setStepPath(_step.stepPath());
                study.setStudyType(((ExMethodStudyStep) _step).studyType());
                action.execute();
            }
        });
        vp.add(bb);
        vp.add(studyGrid);

        _vp.add(tp);

    }

    private void addSubjectForm(final ExMethodSubjectStep step) {
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
                    XmlStringWriter w = new XmlStringWriter();
                    f.save(w);
                    step.setPSPublicMetadata(w.document());
                }

                @Override
                public void formStateUpdated(Form f, Property p) {

                }
            });
            addMustBeValid(psForm);
            psForm.render();
            psTabId = tp.addTab("Subject", "",
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
                    XmlStringWriter w = new XmlStringWriter();
                    f.save(w);
                    step.setRSPublicMetadata(w.document());
                }

                @Override
                public void formStateUpdated(Form f, Property p) {

                }
            });
            addMustBeValid(rsForm);
            rsForm.render();
            rsTabId = tp.addTab("R-subject", "",
                    new ScrollPanel(rsForm, ScrollPolicy.AUTO));
        }
        if (psTabId > 0) {
            tp.setActiveTabById(psTabId);
        } else if (rsTabId > 0) {
            tp.setActiveTabById(rsTabId);
        }
        _vp.add(tp);
    }

    @Override
    public Widget gui() {
        return _vp;
    }

    public BaseWidget widget() {
        return _vp;
    }

    public Window window() {
        return _vp.window();
    }

    @Override
    public void execute(final ActionListener l) {
        ObjectMessage<Boolean> msg = (_step instanceof ExMethodSubjectStep)
                ? new ExMethodSubjectStepUpdate((ExMethodSubjectStep) _step)
                : new ExMethodStepUpdate(_step);
        msg.send(new ObjectMessageResponse<Boolean>() {

            @Override
            public void responded(Boolean r) {
                l.executed(r);
            }
        });
    }

}
