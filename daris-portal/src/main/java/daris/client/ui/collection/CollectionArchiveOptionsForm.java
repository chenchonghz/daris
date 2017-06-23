package daris.client.ui.collection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.user.client.ui.Widget;

import arc.gui.ValidatedInterfaceComponent;
import arc.gui.form.Field;
import arc.gui.form.FieldDefinition;
import arc.gui.form.Form;
import arc.gui.form.Form.BooleanAs;
import arc.gui.form.FormItem;
import arc.gui.form.FormItem.Property;
import arc.gui.form.FormItemListener;
import arc.gui.gwt.widget.panel.SimplePanel;
import arc.mf.client.Output;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlStringWriter;
import arc.mf.dtype.BooleanType;
import arc.mf.dtype.EnumerationType;
import arc.mf.object.ObjectResolveHandler;
import arc.mf.session.ServiceResponseHandler;
import arc.mf.session.Session;
import daris.client.model.collection.archive.ArchiveFormat;
import daris.client.model.collection.archive.ArchiveOptions;
import daris.client.model.collection.messages.CollectionTranscodeList;
import daris.client.model.dataset.DataSet;
import daris.client.model.object.DObject;
import daris.client.model.object.DObjectRef;
import daris.client.model.object.Parts;
import daris.client.ui.widget.LoadingMessage;

public class CollectionArchiveOptionsForm extends ValidatedInterfaceComponent {

    private SimplePanel _sp;
    private Form _form;
    private DObjectRef _obj;
    private ArchiveOptions _options;

    public CollectionArchiveOptionsForm(final DObjectRef obj) {
        _obj = obj;
        _options = new ArchiveOptions();
        _sp = new SimplePanel();
        _sp.fitToParent();
        _sp.setContent(new LoadingMessage("loading..."));
        updateForm();
    }

    protected DObjectRef object() {
        return _obj;
    }

    protected ArchiveOptions archiveOptions() {
        return _options;
    }

    private void updateForm() {
        XmlStringWriter w = new XmlStringWriter();
        w.push("service",
                new String[] { "name", "daris.collection.content.size.sum" });
        w.add("cid", _obj.id());
        w.add("include-attachments", _options.includeAttachments());
        w.pop();
        w.push("service",
                new String[] { "name", "daris.collection.transcode.list" });
        w.add("cid", _obj.id());
        w.pop();
        Session.execute("service.execute", w.document(),
                new ServiceResponseHandler() {

                    @Override
                    public void processResponse(XmlElement xe,
                            List<Output> outputs) throws Throwable {
                        long size = xe.longValue(
                                "reply[@service='daris.collection.content.size.sum']/response/size");
                        Map<String, List<String>> transcodes = null;
                        XmlElement re = xe.element(
                                "reply[@service='daris.collection.transcode.list']/response");
                        if (re.element("transcode") != null) {
                            transcodes = CollectionTranscodeList
                                    .instantiateAvailableTranscodes(re);
                        }
                        updateForm(ArchiveFormat.availableFormatsForSize(size),
                                transcodes);
                    }
                });
    }

    private void updateForm(List<ArchiveFormat> formats,
            Map<String, List<String>> transcodes) {
        if (_form != null) {
            removeMustBeValid(_form);
        }
        _form = new Form();
        _form.setShowHelp(false);
        _form.setShowDescriptions(false);
        _form.setPadding(20);
        _form.setBooleanAs(BooleanAs.CHECKBOX);
        _form.fitToParent();

        /*
         * parts
         */
        Field<Parts> partsField = new Field<Parts>(new FieldDefinition("Parts",
                "parts", new EnumerationType<Parts>(Parts.values()), null, null,
                1, 1));
        partsField.setInitialValue(_options.parts());
        partsField.addListener(new FormItemListener<Parts>() {

            @Override
            public void itemValueChanged(FormItem<Parts> f) {
                _options.setParts(f.value());
            }

            @Override
            public void itemPropertyChanged(FormItem<Parts> f,
                    Property property) {

            }
        });
        _form.add(partsField);

        /*
         * include attachments?
         */
        Field<Boolean> includeAttachmentsField = new Field<Boolean>(
                new FieldDefinition("Include attchments", "include-attachments",
                        BooleanType.DEFAULT_TRUE_FALSE, null, null, 1, 1));
        includeAttachmentsField.setInitialValue(_options.includeAttachments());
        includeAttachmentsField.addListener(new FormItemListener<Boolean>() {

            @Override
            public void itemValueChanged(FormItem<Boolean> f) {
                _options.setIncludeAttachments(f.value());
            }

            @Override
            public void itemPropertyChanged(FormItem<Boolean> f,
                    Property property) {

            }
        });
        _form.add(includeAttachmentsField);

        /*
         * decompress
         */
        final Field<Boolean> decompressField = new Field<Boolean>(
                new FieldDefinition("Decompress", "decompress",
                        BooleanType.DEFAULT_TRUE_FALSE,
                        "Decompress/Unarchive the content archive before adding to the result archive.",
                        null, 1, 1));
        decompressField.setInitialValue(_options.decompress());
        decompressField.addListener(new FormItemListener<Boolean>() {

            @Override
            public void itemValueChanged(FormItem<Boolean> f) {
                _options.setDecompress(f.value());
            }

            @Override
            public void itemPropertyChanged(FormItem<Boolean> f,
                    Property property) {

            }
        });
        if (_obj.isDataSet()) {
            _obj.resolve(new ObjectResolveHandler<DObject>() {

                @Override
                public void resolved(DObject o) {
                    DataSet d = (DataSet) o;
                    if (d.data() != null) {
                        String ext = d.data().extension();
                        if (!("zip".equalsIgnoreCase(ext)
                                || "jar".equalsIgnoreCase(ext)
                                || "aar".equalsIgnoreCase(ext)
                                || "tar".equalsIgnoreCase(ext))) {
                            _options.setDecompress(false);
                            decompressField.setValue(false);
                            decompressField.setReadOnly();
                        }
                    }
                }
            });
        }
        _form.add(decompressField);

        /*
         * archive format
         */
        _options.setArchiveFormat(formats.get(0));
        if (!_obj.isDataSet() || _options.decompress()) {
            Field<ArchiveFormat> archiveFormatField = new Field<ArchiveFormat>(
                    new FieldDefinition("Archive format", "format",
                            new EnumerationType<ArchiveFormat>(formats), null,
                            null, 1, 1));
            if (_options.archiveFormat() == null
                    || !formats.contains(_options.archiveFormat())) {
                _options.setArchiveFormat(formats.get(0));
            }
            archiveFormatField.setInitialValue(_options.archiveFormat());
            archiveFormatField
                    .addListener(new FormItemListener<ArchiveFormat>() {

                        @Override
                        public void itemValueChanged(
                                FormItem<ArchiveFormat> f) {
                            _options.setArchiveFormat(f.value());
                        }

                        @Override
                        public void itemPropertyChanged(
                                FormItem<ArchiveFormat> f, Property property) {

                        }
                    });
            _form.add(archiveFormatField);
        }

        /*
         * transcodes.
         */
        if (transcodes != null && !transcodes.isEmpty()) {
            Set<String> froms = transcodes.keySet();
            for (String from : froms) {
                List<String> tos = new ArrayList<String>();
                tos.add("none");
                tos.addAll(transcodes.get(from));
                Field<String> tf = new Field<String>(new FieldDefinition(
                        "Transcode from: " + from + " to", from,
                        new EnumerationType<String>(tos), null, null, 0, 1));
                tf.setInitialValue(_options.transcodeFor(from));
                tf.addListener(new FormItemListener<String>() {

                    @Override
                    public void itemValueChanged(FormItem<String> f) {
                        if ("none".equals(f.value())) {
                            _options.removeTranscode(f.name());
                        } else {
                            _options.addTranscode(f.name(), f.value());
                        }
                    }

                    @Override
                    public void itemPropertyChanged(FormItem<String> f,
                            Property property) {

                    }
                });
                _form.add(tf);
            }
        }

        appendToForm(_form);

        _form.render();
        addMustBeValid(_form);
        _sp.setContent(_form);
    }

    protected void appendToForm(Form form) {

    }

    @Override
    public Widget gui() {
        return _sp;
    }

}
