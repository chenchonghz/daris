package daris.client.ui.object;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.dom.client.Style.BorderStyle;
import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Widget;

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
import arc.gui.gwt.widget.BaseWidget;
import arc.gui.gwt.widget.image.LinearGradient;
import arc.gui.gwt.widget.label.Label;
import arc.gui.gwt.widget.panel.CenteringPanel;
import arc.gui.gwt.widget.panel.CenteringPanel.Axis;
import arc.gui.gwt.widget.panel.HorizontalPanel;
import arc.gui.gwt.widget.panel.SimplePanel;
import arc.gui.gwt.widget.panel.TabPanel;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.gui.gwt.widget.scroll.ScrollPanel;
import arc.gui.gwt.widget.scroll.ScrollPolicy;
import arc.mf.client.util.Validity;
import arc.mf.client.xml.XmlStringWriter;
import arc.mf.dtype.BooleanType;
import arc.mf.dtype.ConstantType;
import arc.mf.dtype.ListOfType;
import arc.mf.dtype.StringType;
import arc.mf.dtype.TextType;
import arc.mf.object.ObjectMessageResponse;
import daris.client.model.IDUtil;
import daris.client.model.dataobject.DataObject;
import daris.client.model.dataset.DataSet;
import daris.client.model.exmethod.ExMethod;
import daris.client.model.object.DObject;
import daris.client.model.object.DObjectRef;
import daris.client.model.object.Tag;
import daris.client.model.object.Thumbnail;
import daris.client.model.object.messages.ObjectThumbnailGet;
import daris.client.model.project.Project;
import daris.client.model.repository.Repository;
import daris.client.model.study.Study;
import daris.client.model.subject.Subject;
import daris.client.ui.DObjectBrowser;
import daris.client.ui.dataobject.DataObjectDetails;
import daris.client.ui.dataset.DataSetDetails;
import daris.client.ui.exmethod.ExMethodDetails;
import daris.client.ui.form.XmlMetaForm;
import daris.client.ui.project.ProjectDetails;
import daris.client.ui.repository.RepositoryDetails;
import daris.client.ui.study.StudyDetails;
import daris.client.ui.subject.SubjectDetails;
import daris.client.util.StringUtil;

public abstract class DObjectDetails extends ValidatedInterfaceComponent {

    // public static final Colour BORDER_COLOR = new RGB(0xcd, 0xcd, 0xcd);
    // public static final Colour BORDER_COLOR_LIGHT = new RGB(0xef, 0xef,
    // 0xef);
    // public static final int BORDER_RADIUS = 5;
    // public static final int BORDER_WIDTH = 1;

    public static final String TAB_NAME_INTERFACE = "Interface";
    public static final String TAB_DESC_INTERFACE = "Interface";
    public static final String TAB_NAME_METADATA = "Metadata";
    public static final String TAB_DESC_METADATA = "Metadata";
    public static final String TAB_NAME_ATTACHMENT = "Attachments";
    public static final String TAB_DESC_ATTACHMENT = "Attachments";
    public static final String TAB_NAME_THUMBNAIL = "Thumbnails";
    public static final String TAB_DESC_THUMBNAIL = "Thumbnails";

    private static String _prevObjectTabName;

    private FormEditMode _mode;

    private VerticalPanel _vp;

    private TabPanel _tp;

    private Map<String, Integer> _tabIds;

    private Map<Integer, String> _tabNames;

    private VerticalPanel _interfaceVP;

    private Form _interfaceForm;

    private AttachmentPanel _attachmentPanel;

    private Form _metaForm;

    private Label _statusLabel;

    private DObject _o;

    private DObjectRef _po;

    protected DObjectDetails(DObjectRef po, DObject o, FormEditMode mode) {

        this(po, o, mode, mode.equals(FormEditMode.READ_ONLY) ? true : false);
    }

    protected DObjectDetails(DObjectRef po, DObject o, FormEditMode mode,
            boolean showHeader) {

        _o = o;
        _po = po;
        _mode = mode;
        _tabIds = new HashMap<String, Integer>();
        _tabNames = new HashMap<Integer, String>();
        _vp = new VerticalPanel();
        _vp.fitToParent();
        if (showHeader) {
            _vp.add(headerFor(o));
        }
        SimplePanel sp = new SimplePanel();
        sp.fitToParent();
        _tp = new TabPanel() {
            @Override
            protected void activated(int tabId) {
                // view mode: remember the tab name.
                if (_mode == FormEditMode.READ_ONLY) {
                    String tabName = _tabNames.get(tabId);
                    _prevObjectTabName = tabName;
                }
            }
        };
        _tp.fitToParent();

        sp.setContent(_tp);
        _vp.add(sp);

        if (FormEditMode.READ_ONLY != _mode) {
            HorizontalPanel aimHP = new HorizontalPanel();
            aimHP.setHeight(22);
            aimHP.setSpacing(5);

            Form aimForm = new Form(_mode);
            aimForm.setShowDescriptions(false);
            Field<Boolean> allowIncompleteMeta = new Field<Boolean>(
                    new FieldDefinition("allow-incomplete-metadata",
                            BooleanType.DEFAULT_TRUE_FALSE,
                            "allow incomplete metadata", null, 0, 1));
            allowIncompleteMeta.setInitialValue(_o.allowIncompleteMeta());
            allowIncompleteMeta.addListener(new FormItemListener<Boolean>() {

                @Override
                public void itemValueChanged(FormItem<Boolean> f) {
                    if (_o.allowIncompleteMeta() != f.value()) {
                        _o.setAllowIncompleteMeta(f.value());
                        allowIncompleteMetaChanged(f.value());
                    }
                }

                @Override
                public void itemPropertyChanged(FormItem<Boolean> f,
                        Property property) {

                }
            });
            aimForm.add(allowIncompleteMeta);
            aimForm.render();
            addMustBeValid(aimForm);
            aimHP.add(aimForm);
            _vp.add(aimHP);

            _statusLabel = new Label();
            _statusLabel.setHeight(20);
            _statusLabel.setWidth100();
            _statusLabel.setPaddingLeft(20);
            _statusLabel.setFontSize(12);
            _statusLabel.setFontWeight(FontWeight.BOLD);
            _statusLabel.setColour(RGB.RED);
            _vp.add(_statusLabel);
        }

        updateInterfaceTab();

        updateAttachmentTab();

        updateThumbnailTab();

        updateMetaTab();
    }

    protected void allowIncompleteMetaChanged(boolean allowIncompleteMeta) {
        if (_metaForm != null) {
            _metaForm.setAllowMissingMandatory(allowIncompleteMeta);
        }
    }

    @Override
    public Widget gui() {

        return _vp;
    }

    public BaseWidget widget() {
        return _vp;
    }

    public arc.gui.window.Window window() {
        return _vp.window();
    }

    @Override
    public Validity valid() {
        Validity v = super.valid();
        if (v.valid()) {
            _statusLabel.setText("");
        } else {
            _statusLabel.setText(v.reasonForIssue());
        }
        return v;
    }

    protected void addTab(String name, String description, BaseWidget w) {
        if (_tabIds.containsKey(name)) {
            _tp.setTabContent(_tabIds.get(name), w);
        } else {
            int tabId = _tp.addTab(name, description, w);
            _tabIds.put(name, tabId);
            _tabNames.put(tabId, name);
        }
    }

    protected void removeTab(String name) {
        if (_tabIds.containsKey(name)) {
            int tabId = _tabIds.get(name);
            _tp.removeTabById(_tabIds.get(name));
            _tabIds.remove(name);
            _tabNames.remove(tabId);
        }
    }

    protected void setTab(String name, String description, BaseWidget w) {
        addTab(name, description, w);
    }

    protected void setActiveTab() {
        if (_mode != FormEditMode.READ_ONLY) {
            // edit/create mode: do not try to reload last tab.
            if (_tp.activeTabId() <= 0) {
                _tp.setActiveTab(0);
            }
            return;
        }
        Timer timer = new Timer() {
            @Override
            public void run() {
                if (_tp.activeTabId() != -1) {
                    return;
                }
                DObjectRef prevObject = DObjectBrowser.get(false)
                        .prevSelected();
                DObject.Type prevObjectType = prevObject == null ? null
                        : prevObject.referentType();
                String prevObjectTabName = _prevObjectTabName;
                if (prevObjectType != null && prevObjectType == _o.type()
                        && prevObjectTabName != null
                        && _tabIds.containsKey(prevObjectTabName)) {
                    _tp.setActiveTabById(_tabIds.get(prevObjectTabName));
                } else {
                    _tp.setActiveTab(0);
                }
            }
        };
        timer.schedule(500);
    }

    protected DObjectRef parentObject() {

        return _po;
    }

    protected DObject object() {

        return _o;
    }

    protected FormEditMode mode() {

        return _mode;
    }

    protected void updateInterfaceTab() {

        if (_interfaceVP == null) {
            _interfaceVP = new VerticalPanel();
            _interfaceVP.fitToParent();
        } else {
            _interfaceVP.removeAll();
        }

        insertToInterfaceTab(_interfaceVP);

        if (_interfaceForm != null && !_mode.equals(FormEditMode.READ_ONLY)) {
            removeMustBeValid(_interfaceForm);
        }

        _interfaceForm = new Form(_mode);
        addToInterfaceForm(_interfaceForm);
        _interfaceForm.setHeight100();
        _interfaceForm.render();

        if (!_mode.equals(FormEditMode.READ_ONLY)) {
            addMustBeValid(_interfaceForm);
        }

        _interfaceVP.add(_interfaceForm);

        appendToInterfaceTab(_interfaceVP);

        setTab(TAB_NAME_INTERFACE, TAB_DESC_INTERFACE,
                new ScrollPanel(_interfaceVP, ScrollPolicy.AUTO));

    }

    protected void insertToInterfaceTab(VerticalPanel vp) {

    }

    protected void appendToInterfaceTab(VerticalPanel vp) {

    }

    protected void addToInterfaceForm(Form interfaceForm) {

        if (!_mode.equals(FormEditMode.CREATE)) {
            Field<String> idField = new Field<String>(new FieldDefinition("id",
                    ConstantType.DEFAULT, "object id", null, 1, 1));
            idField.setValue(_o.id());
            interfaceForm.add(idField);
            // @formatter:off
//            if (_o.assetId() != null) {
//                Field<String> assetIdField = new Field<String>(
//                        new FieldDefinition("asset_id", ConstantType.DEFAULT,
//                                "Asset ID", null, 1, 1));
//                assetIdField.setValue(_o.assetId());
//                interfaceForm.add(assetIdField);
//            }
//            if (_o.proute() != null) {
//                Field<String> prouteField = new Field<String>(
//                        new FieldDefinition("PRoute", ConstantType.DEFAULT,
//                                "proute", null, 1, 1));
//                prouteField.setValue(_o.proute());
//                interfaceForm.add(prouteField);
//            }
            // @formatter:on
        }

        if (_o.hasTags() && _mode == FormEditMode.READ_ONLY) {
            List<String> tags = new ArrayList<String>(_o.tags().size());
            for (Tag tag : _o.tags()) {
                tags.add(tag.name());
            }
            Field<List<String>> tagsField = new Field<List<String>>(
                    new FieldDefinition("tags",
                            new ListOfType(ConstantType.DEFAULT), null, null, 0,
                            1));
            tagsField.setValue(tags);
            interfaceForm.add(tagsField);
        }

        int min = 0;
        if (_o instanceof Project)
            min = 1;
        String name = "Name of object";
        if (_o instanceof Subject) {
            name = "Subject name; generally do NOT use this for human subject details which  should be located in specialised and protected meta-data specified by the Method";
        }

        Field<String> nameField = new Field<String>(new FieldDefinition("name",
                StringType.DEFAULT, name, null, min, 1));
        nameField.setValue(_o.name());
        nameField.addListener(new FormItemListener<String>() {
            @Override
            public void itemValueChanged(FormItem<String> f) {

                _o.setName(f.value());
            }

            @Override
            public void itemPropertyChanged(FormItem<String> f,
                    FormItem.Property p) {

            }
        });
        interfaceForm.add(nameField);

        String ddesc = _o.type() + " description";
        if (_o instanceof Project) {
            ddesc = "Description of project; this may be harvested into meta-data registries (with your permission) so make as meaningful as possible.";
        }
        Field<String> descField = new Field<String>(new FieldDefinition(
                "description", TextType.DEFAULT, ddesc, null, min, 1));
        descField.setValue(_o.description());
        descField.addListener(new FormItemListener<String>() {

            @Override
            public void itemValueChanged(FormItem<String> f) {

                _o.setDescription(f.value());
            }

            @Override
            public void itemPropertyChanged(FormItem<String> f,
                    FormItem.Property p) {

            }
        });
        FieldRenderOptions fro = new FieldRenderOptions();
        fro.setWidth(1.0);
        descField.setRenderOptions(fro);
        interfaceForm.add(descField);

        // Original filename. Only implemented for DataSets at this time, but is
        // generic and any PSSD object could have it.
        if (_mode == FormEditMode.READ_ONLY && _o.fileName() != null) {
            Field<String> fnField = new Field<String>(new FieldDefinition(
                    "filename", StringType.DEFAULT,
                    "Original name of file when uploaded.", null, min, 1));
            fnField.setValue(_o.fileName());
            fnField.addListener(new FormItemListener<String>() {
                @Override
                public void itemValueChanged(FormItem<String> f) {

                    _o.setFileName(f.value());
                }

                @Override
                public void itemPropertyChanged(FormItem<String> f,
                        FormItem.Property p) {

                }
            });
            interfaceForm.add(fnField);
        }
    }

    private static Widget headerFor(DObject o) {

        String text = StringUtil.upperCaseFirst(o.type().toString());
        if (o.id() != null) {
            text += " - " + o.id();
        }
        Label label = new Label(text);
        label.setFontSize(12);
        label.setFontWeight(FontWeight.BOLD);

        CenteringPanel cp = new CenteringPanel(Axis.BOTH);
        cp.setWidth100();
        cp.setHeight(20);
        cp.setMarginTop(1);
        cp.setBorderTop(1, BorderStyle.SOLID, new RGB(221, 221, 221));
        cp.setBorderLeft(1, BorderStyle.SOLID, new RGB(221, 221, 221));
        cp.setBorderRight(1, BorderStyle.SOLID, new RGB(221, 221, 221));
        cp.setBorderRadiusTopLeft(5);
        cp.setBorderRadiusTopRight(5);
        cp.setBackgroundImage(
                new LinearGradient(LinearGradient.Orientation.TOP_TO_BOTTOM,
                        new RGB(221, 221, 221), new RGB(204, 204, 204)));
        cp.add(label);
        return cp;
    }

    public static DObjectDetails detailsFor(DObject o, FormEditMode mode) {

        // No parent object provided, must be viewing or editing.
        assert !mode.equals(FormEditMode.CREATE);
        return detailsFor(new DObjectRef(IDUtil.getParentId(o.id()), o.proute(),
                false, true, -1), o, mode);
    }

    public static DObjectDetails detailsFor(DObjectRef po, DObject o,
            FormEditMode mode) {

        if (o instanceof Repository) {
            return new RepositoryDetails((Repository) o, mode);
        } else if (o instanceof Project) {
            return new ProjectDetails((Project) o, mode);
        } else if (o instanceof Subject) {
            return new SubjectDetails(po, (Subject) o, mode);
        } else if (o instanceof ExMethod) {
            return new ExMethodDetails(po, (ExMethod) o, mode);
        } else if (o instanceof Study) {
            return new StudyDetails(po, (Study) o, mode);
        } else if (o instanceof DataSet) {
            return new DataSetDetails(po, (DataSet) o, mode);
        } else if (o instanceof DataObject) {
            return new DataObjectDetails(po, (DataObject) o, mode);
        } else {
            throw new AssertionError(
                    "Failed to instantiate details(GUI) for object " + o.id());
        }
    }

    private void updateAttachmentTab() {

        if (_o instanceof Repository) {
            return;
        }

        if (_mode.equals(FormEditMode.CREATE)) {
            return;
        }
        _attachmentPanel = new AttachmentPanel(_o);
        _attachmentPanel.fitToParent();
        setTab(TAB_NAME_ATTACHMENT, TAB_DESC_ATTACHMENT, _attachmentPanel);
    }

    private void updateThumbnailTab() {
        if (_o instanceof Repository) {
            return;
        }

        if (_mode.equals(FormEditMode.CREATE)) {
            return;
        }

        new ObjectThumbnailGet(_o.id(), false)
                .send(new ObjectMessageResponse<Thumbnail>() {

                    @Override
                    public void responded(Thumbnail t) {
                        if (t == null) {
                            removeTab(TAB_NAME_THUMBNAIL);
                            return;
                        }
                        ThumbnailPanel thumbnailPanel = new ThumbnailPanel(t);
                        thumbnailPanel.fitToParent();
                        setTab(TAB_NAME_THUMBNAIL, TAB_DESC_THUMBNAIL,
                                thumbnailPanel);
                    }
                });
    }

    private void updateMetaTab() {

        if ((_mode == FormEditMode.READ_ONLY && _o.meta() == null)
                || (_mode != FormEditMode.READ_ONLY
                        && _o.metaForEdit() == null)) {
            removeTab(TAB_NAME_METADATA);
            return;
        }
        if (_mode != FormEditMode.READ_ONLY) {
            if (_metaForm != null) {
                removeMustBeValid(_metaForm);
            }
            _metaForm = XmlMetaForm.formFor(_o.metaForEdit(), _mode);
            _metaForm.setAllowMissingMandatory(_o.allowIncompleteMeta());
            _metaForm.addListener(new FormListener() {

                @Override
                public void rendering(Form f) {

                }

                @Override
                public void rendered(Form f) {
                    BaseWidget.resized(f);
                }

                @Override
                public void formValuesUpdated(Form f) {

                    XmlStringWriter w = new XmlStringWriter();
                    if (mode().equals(FormEditMode.CREATE)) {
                        w.push("meta");
                    } else {
                        w.push("meta", new String[] { "action", "replace" });
                    }
                    f.save(w);
                    w.pop();
                    _o.setMeta(w);
                }

                @Override
                public void formStateUpdated(Form f, Property p) {

                }
            });
            addMustBeValid(_metaForm);
        } else {
            _metaForm = XmlMetaForm.formFor(_o.meta(), _mode);
        }
        _metaForm.render();
        setTab(TAB_NAME_METADATA, TAB_DESC_METADATA,
                new ScrollPanel(_metaForm, ScrollPolicy.AUTO));
    }

}
