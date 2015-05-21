package daris.client.ui.object;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import arc.gui.ValidatedInterfaceComponent;
import arc.gui.form.Field;
import arc.gui.form.FieldDefinition;
import arc.gui.form.FieldRenderOptions;
import arc.gui.form.Form;
import arc.gui.form.FormEditMode;
import arc.gui.form.FormItem.XmlType;
import arc.gui.gwt.colour.RGB;
import arc.gui.gwt.widget.BaseWidget;
import arc.gui.gwt.widget.HTML;
import arc.gui.gwt.widget.panel.TabPanel;
import arc.gui.gwt.widget.panel.TitlePanel;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.gui.gwt.widget.scroll.ScrollPanel;
import arc.gui.gwt.widget.scroll.ScrollPolicy;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.dtype.ConstantType;
import arc.mf.dtype.ListOfType;
import arc.mf.dtype.StringType;
import arc.mf.dtype.TextType;
import arc.mf.object.ObjectMessageResponse;

import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.user.client.ui.Widget;

import daris.client.model.object.DObject;
import daris.client.model.object.DObjectBuilder;
import daris.client.model.object.Tag;
import daris.client.model.object.Thumbnail;
import daris.client.model.object.messages.ObjectThumbnailGet;
import daris.client.model.repository.Repository;
import daris.client.ui.form.XmlMetaForm;
import daris.client.util.StringUtil;
import daris.client.xml.CanSaveToXml;

public class DObjectForm<T extends DObject> extends ValidatedInterfaceComponent implements CanSaveToXml {

    private DObjectBuilder<T> _builder;
    private T _object;

    private FormEditMode _mode;

    private List<CanSaveToXml> _xcs;

    private VerticalPanel _vp;
    private TabPanel _tp;
    private HTML _statusBar;

    private Map<String, Integer> _tabIds;

    private VerticalPanel _interfaceVP;
    private Form _interfaceForm;

    private Form _metadataForm;

    protected DObjectForm(DObjectBuilder<T> builder, T object, FormEditMode mode) {

        _builder = builder;
        _object = object;
        _mode = mode;

        if (_mode != FormEditMode.READ_ONLY) {
            _xcs = new ArrayList<CanSaveToXml>();
        }

        _vp = new VerticalPanel();
        _vp.fitToParent();

        if (_mode == FormEditMode.READ_ONLY) {
            _vp.add(new TitlePanel(title()));
        }

        _tp = new TabPanel();
        _tp.fitToParent();

        _tabIds = new HashMap<String, Integer>();

        _vp.add(_tp);

        _statusBar = new HTML();
        _statusBar.setHeight(20);
        _statusBar.setWidth100();
        _statusBar.setPaddingLeft(20);
        _statusBar.setFontSize(12);
        _statusBar.setFontWeight(FontWeight.BOLD);
        _statusBar.setColour(RGB.RED);
        if (_mode != FormEditMode.READ_ONLY) {
            _vp.add(_statusBar);
        }

        updateInterfaceTab();

        updateMetadataTab();

        if (_mode == FormEditMode.READ_ONLY) {
            updateAttachmentTab();
            if (!(_object instanceof Repository)) {
                updateThumbnailTab();
            }
        }

    }

    private void updateInterfaceTab() {

        if (_interfaceVP == null) {
            _interfaceVP = new VerticalPanel();
            _interfaceVP.fitToParent();
            if (_mode != FormEditMode.READ_ONLY) {
                addXmlComponent(new CanSaveToXml() {

                    @Override
                    public void save(XmlWriter w) {
                        if (_interfaceForm != null) {
                            _interfaceForm.save(w);
                        }
                    }
                });
            }
        } else {
            _interfaceVP.removeAll();
        }

        // for sub-classes to insert components to interface tab
        insertToInterfaceTab(_interfaceVP);

        /*
         * interface form
         */
        if (_interfaceForm != null && !_mode.equals(FormEditMode.READ_ONLY)) {
            removeMustBeValid(_interfaceForm);
        }
        _interfaceForm = new Form(_mode);

        // for sub-classes to append form items to interface form
        addToInterfaceForm(_interfaceForm);

        _interfaceForm.render();
        _interfaceVP.add(_interfaceForm);

        if (!_mode.equals(FormEditMode.READ_ONLY)) {
            addMustBeValid(_interfaceForm);
        }

        // for sub-classes to append components to interface tab
        appendToInterfaceTab(_interfaceVP);

        putTab("interface", new ScrollPanel(_interfaceVP, ScrollPolicy.AUTO));
    }

    protected void insertToInterfaceTab(VerticalPanel vp) {

    }

    protected void appendToInterfaceTab(VerticalPanel vp) {

    }

    protected void addToInterfaceForm(Form form) {

        /*
         * id
         */
        if (_mode != FormEditMode.CREATE) {
            Field<String> idField = new Field<String>(new FieldDefinition("id", ConstantType.DEFAULT, "object id",
                    null, 1, 1));
            idField.setXmlType(XmlType.ELEMENT);
            if (_mode != FormEditMode.CREATE) {
                idField.setValue(_object.id(), false);
            }
            form.add(idField);
        }

        /*
         * tags
         */
        if (_mode == FormEditMode.READ_ONLY) {
            if (_object.hasTags()) {
                List<String> tags = new ArrayList<String>(_object.tags().size());
                for (Tag tag : _object.tags()) {
                    tags.add(tag.name());
                }
                Field<List<String>> tagsField = new Field<List<String>>(new FieldDefinition("tags", new ListOfType(
                        ConstantType.DEFAULT), null, null, 0, 1));
                tagsField.setValue(tags, false);
                form.add(tagsField);
            }
        }

        /*
         * name
         */
        String objectType = objectType() == null ? "object" : objectType().toString();
        StringBuilder sb = new StringBuilder();
        sb.append("Name of the ");
        sb.append(objectType);
        sb.append(". ");
        if (objectType() == DObject.Type.subject) {
            sb.append("Generally do NOT use this for human subject details which should be located in specialised and protected meta-data specified by the method.");
        }
        int minOccurs = objectType() == DObject.Type.project ? 1 : 0;
        Field<String> nameField = new Field<String>(new FieldDefinition("name", StringType.DEFAULT, sb.toString(),
                null, minOccurs, 1));
        nameField.setXmlType(XmlType.ELEMENT);
        if (_mode != FormEditMode.CREATE) {
            nameField.setValue(_object.name(), false);
        }
        form.add(nameField);

        /*
         * description
         */
        sb = new StringBuilder();
        sb.append("Description of the ");
        sb.append(objectType);
        sb.append(". ");
        if (objectType() == DObject.Type.project) {
            sb.append("This may be harvested into meta-data registries (with your permission) so make as meaningful as possible.");
        }
        Field<String> descriptionField = new Field<String>(new FieldDefinition("description", TextType.DEFAULT,
                sb.toString(), null, 0, 1));
        descriptionField.setXmlType(XmlType.ELEMENT);
        if (_mode != FormEditMode.CREATE) {
            descriptionField.setValue(_object.description(), false);
        }
        FieldRenderOptions fro = new FieldRenderOptions();
        fro.setWidth(1.0);
        descriptionField.setRenderOptions(fro);
        form.add(descriptionField);

    }

    private void updateMetadataTab() {

        if (_metadataForm == null) {
            // initial
            if (_mode != FormEditMode.READ_ONLY) {
                addXmlComponent(new CanSaveToXml() {

                    @Override
                    public void save(XmlWriter w) {
                        if (_metadataForm == null) {
                            return;
                        }
                        if (_mode == FormEditMode.CREATE) {
                            w.push("meta");
                        } else {
                            w.push("meta", new String[] { "action", "replace" });
                        }
                        _metadataForm.save(w);
                        w.pop();
                    }
                });
            }
        } else {
            removeMustBeValid(_metadataForm);
        }

        if (_mode == FormEditMode.READ_ONLY) {
            // view
            if (_object.meta() == null) {
                removeTab("metadata");
                return;
            }
            _metadataForm = XmlMetaForm.formFor(_object.meta(), mode());
        } else {
            // create or edit
            XmlElement metadata = null;
            if (_mode == FormEditMode.CREATE) {
                metadata = _builder.metadata();
            } else {
                metadata = _object.metaForEdit();
            }
            if (metadata == null) {
                removeTab("metadata");
                return;
            }
            _metadataForm = XmlMetaForm.formFor(metadata, mode());
            addMustBeValid(_metadataForm);
        }
        _metadataForm.render();
        putTab("metadata", new ScrollPanel(_metadataForm, ScrollPolicy.AUTO));

    }

    private void updateAttachmentTab() {

        assert _mode == FormEditMode.READ_ONLY;

        if (DObject.Type.repository == objectType()) {
            return;
        }
        AttachmentPanel attachmentPanel = new AttachmentPanel(_object);
        attachmentPanel.fitToParent();
        putTab("attachments", attachmentPanel);
    }

    private void updateThumbnailTab() {

        assert _mode == FormEditMode.READ_ONLY;

        if (objectType() == DObject.Type.repository) {
            return;
        }

        new ObjectThumbnailGet(_object.id(), false).send(new ObjectMessageResponse<Thumbnail>() {

            @Override
            public void responded(Thumbnail t) {
                if (t == null) {
                    removeTab("thumbnails");
                    return;
                }
                ThumbnailPanel thumbnailPanel = new ThumbnailPanel(t);
                thumbnailPanel.fitToParent();
                putTab("thumbnails", thumbnailPanel);
            }
        });
    }

    protected void putTab(String name, BaseWidget widget) {

        Integer tabId = _tabIds.get(name);
        if (tabId == null) {
            tabId = _tp.addTab(name, null, widget);
            _tabIds.put(name, tabId);
        } else {
            _tp.setTabContent(tabId, widget);
        }
    }

    protected void removeTab(String name) {
        Integer tabId = _tabIds.get(name);
        if (tabId != null) {
            _tp.removeTabById(tabId);
        }
    }

    private String title() {
        if (_object == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(StringUtil.upperCaseFirst(_object.type().toString()));
        if (_object.id() != null) {
            sb.append(" - ");
            sb.append(_object.id());
        }
        return sb.toString();
    }

    protected T object() {
        return _object;
    }

    protected DObjectBuilder<T> builder() {
        return _builder;
    }

    protected DObject.Type objectType() {
        if (_mode == FormEditMode.CREATE) {
            return _builder.type();
        } else {
            return _object.type();
        }
    }

    protected FormEditMode mode() {
        return _mode;
    }

    @Override
    public Widget gui() {
        return _vp;
    }

    public BaseWidget widget() {
        return _vp;
    }

    public arc.gui.window.Window Window() {
        return _vp.window();
    }

    @Override
    public void save(XmlWriter w) {
        if (_xcs != null) {
            for (CanSaveToXml xc : _xcs) {
                xc.save(w);
            }
        }
    }

    protected void addXmlComponent(CanSaveToXml xc) {
        if (_xcs == null) {
            _xcs = new ArrayList<CanSaveToXml>();
        }
        _xcs.add(xc);
    }

}
