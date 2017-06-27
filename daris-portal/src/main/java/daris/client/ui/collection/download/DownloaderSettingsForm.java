package daris.client.ui.collection.download;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.user.client.ui.Widget;

import arc.gui.ValidatedInterfaceComponent;
import arc.gui.form.Field;
import arc.gui.form.FieldDefinition;
import arc.gui.form.FieldGroup;
import arc.gui.form.Form;
import arc.gui.form.Form.BooleanAs;
import arc.gui.form.FormEditMode;
import arc.gui.form.FormItem;
import arc.gui.form.FormItem.Property;
import arc.gui.form.FormItemListener;
import arc.gui.gwt.widget.panel.SimplePanel;
import arc.mf.client.Output;
import arc.mf.client.util.ActionListener;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlStringWriter;
import arc.mf.dtype.BooleanType;
import arc.mf.dtype.ConstantType;
import arc.mf.dtype.DateType;
import arc.mf.dtype.DocType;
import arc.mf.dtype.EnumerationType;
import arc.mf.dtype.IntegerType;
import arc.mf.session.ServiceResponseHandler;
import arc.mf.session.Session;
import daris.client.model.collection.download.DownloaderSettings;
import daris.client.model.collection.download.OutputPattern;
import daris.client.model.collection.download.TargetPlatform;
import daris.client.model.collection.messages.CollectionTranscodeList;
import daris.client.model.object.DObjectRef;
import daris.client.model.object.Parts;
import daris.client.ui.widget.LoadingMessage;

public class DownloaderSettingsForm extends ValidatedInterfaceComponent {

    private DObjectRef _obj;

    private DownloaderSettings _settings;

    private SimplePanel _sp;

    private Form _form;

    public DownloaderSettingsForm(DObjectRef obj, DownloaderSettings settings) {
        _obj = obj;
        _settings = settings;
        _settings.addObject(_obj);

        _sp = new SimplePanel();
        _sp.fitToParent();

        _sp.setContent(new LoadingMessage("loading"));
        updateForm();
    }

    private void updateForm() {
        XmlStringWriter w = new XmlStringWriter();
        w.push("service", new String[] { "name", "daris.collection.content.size.sum" });
        w.add("cid", _obj.id());
        w.add("include-attachments", false);
        w.pop();
        w.push("service", new String[] { "name", "daris.collection.transcode.list" });
        w.add("cid", _obj.id());
        w.pop();
        w.add("service", new String[] { "name", "om.pssd.shoppingcart.layout-pattern.list" });
        Session.execute("service.execute", w.document(), new ServiceResponseHandler() {

            @Override
            public void processResponse(XmlElement xe, List<Output> outputs) throws Throwable {
                XmlElement se = xe.element("reply[@service='daris.collection.content.size.sum']/response/size");
                long totalSize = se.longValue();
                int nbAssets = se.intValue("@nbe", -1);
                Map<String, List<String>> availableTranscodes = CollectionTranscodeList.instantiateAvailableTranscodes(
                        xe.elements("reply[@service='daris.collection.transcode.list']/response/transcode"));
                Map<String, OutputPattern> availableOutputPatterns = OutputPattern
                        .instantiateShoppingCartLayoutPatterns(xe.elements(
                                "reply[@service='om.pssd.shoppingcart.layout-pattern.list']/response/layout-pattern"));
                updateForm(nbAssets, totalSize, availableTranscodes, availableOutputPatterns);
            }
        });
    }

    private void updateForm(final int nbAssets, final long totalSize,
            final Map<String, List<String>> availableTranscodes,
            final Map<String, OutputPattern> availableOutputPatterns) {
        if (_form != null) {
            removeMustBeValid(_form);
        }
        _form = new Form(FormEditMode.UPDATE);
        _form.setShowHelp(false);
        _form.setShowDescriptions(true);
        _form.setMargin(30);
        _form.setBooleanAs(BooleanAs.CHECKBOX);
        _form.fitToParent();

        Field<String> totalSizeField = new Field<String>(new FieldDefinition("Total Size", "size", ConstantType.DEFAULT,
                "Total size of the object contents.", null, 0, 1));
        totalSizeField.setValue(Long.toString(totalSize) + " bytes");

        /*
         * parts
         */
        Field<Parts> partsField = new Field<Parts>(
                new FieldDefinition("Parts", "parts", new EnumerationType<Parts>(Parts.values()),
                        "Specifies which parts of the assets to download.", null, 0, 1));
        partsField.setInitialValue(_settings.parts());
        partsField.addListener(new FormItemListener<Parts>() {

            @Override
            public void itemValueChanged(FormItem<Parts> f) {
                _settings.setParts(f.value());
            }

            @Override
            public void itemPropertyChanged(FormItem<Parts> f, Property property) {

            }
        });
        _form.add(partsField);

        /*
         * unarchive
         */
        Field<Boolean> unarchiveField = new Field<Boolean>(
                new FieldDefinition("Unpack", "unarchive", BooleanType.DEFAULT_TRUE_FALSE,
                        "Unpack data contents if they are archive containers, e.g. zip, aar(Arcitecta archive format).",
                        null, 0, 1));
        unarchiveField.setInitialValue(_settings.unarchive());
        unarchiveField.addListener(new FormItemListener<Boolean>() {

            @Override
            public void itemValueChanged(FormItem<Boolean> f) {
                _settings.setUnarchive(f.value());
            }

            @Override
            public void itemPropertyChanged(FormItem<Boolean> f, Property property) {

            }
        });
        _form.add(unarchiveField);

        /*
         * transcodes.
         */
        if (availableTranscodes != null && !availableTranscodes.isEmpty()) {
            Set<String> froms = availableTranscodes.keySet();
            for (String from : froms) {
                List<String> tos = new ArrayList<String>();
                tos.add("none");
                tos.addAll(availableTranscodes.get(from));
                Field<String> tf = new Field<String>(
                        new FieldDefinition("Transcode from: " + from + " to", from, new EnumerationType<String>(tos),
                                "Convert " + from + " data to specified mime type.", null, 0, 1));
                tf.setInitialValue(_settings.transcodeFor(from));
                tf.addListener(new FormItemListener<String>() {

                    @Override
                    public void itemValueChanged(FormItem<String> f) {
                        if ("none".equals(f.value())) {
                            _settings.removeTranscode(f.name());
                        } else {
                            _settings.addTranscode(f.name(), f.value());
                        }
                    }

                    @Override
                    public void itemPropertyChanged(FormItem<String> f, Property property) {

                    }
                });
                _form.add(tf);
            }
        }

        /*
         * output patterns;
         */
        if (availableOutputPatterns != null && !availableOutputPatterns.isEmpty()) {
            OutputPattern firstAvailable = availableOutputPatterns.values().iterator().next();
            _settings.setOutputPattern(firstAvailable.pattern());
            if (availableOutputPatterns.size() > 1) {
                List<EnumerationType.Value<OutputPattern>> values = new ArrayList<EnumerationType.Value<OutputPattern>>();
                Set<String> names = availableOutputPatterns.keySet();
                for (String name : names) {
                    OutputPattern pattern = availableOutputPatterns.get(name);
                    EnumerationType.Value<OutputPattern> value = new EnumerationType.Value<OutputPattern>(
                            pattern.name(), pattern.description(), pattern);
                    values.add(value);
                }
                Field<OutputPattern> outputPatternField = new Field<OutputPattern>(new FieldDefinition("Output Pattern",
                        "output-pattern", new EnumerationType<OutputPattern>(values), "The output file path pattern.",
                        null, 0, 1));
                outputPatternField.setInitialValue(firstAvailable, false);
                outputPatternField.addListener(new FormItemListener<OutputPattern>() {

                    @Override
                    public void itemValueChanged(FormItem<OutputPattern> f) {
                        _settings.setOutputPattern(f.value() == null ? null : f.value().pattern());
                    }

                    @Override
                    public void itemPropertyChanged(FormItem<OutputPattern> f, Property property) {

                    }
                });
                _form.add(outputPatternField);
            }
        }

        Field<Boolean> generateTokenField = new Field<Boolean>(
                new FieldDefinition("Generate Identity Token", "generate-token", BooleanType.DEFAULT_TRUE_FALSE,
                        "Check to generate secure identity token.", null, 0, 1));
        generateTokenField.setInitialValue(_settings.generateToken(), false);
        generateTokenField.addListener(new FormItemListener<Boolean>() {

            @Override
            public void itemValueChanged(FormItem<Boolean> f) {
                _settings.setGenerateToken(f.value());
                updateForm(nbAssets, totalSize, availableTranscodes, availableOutputPatterns);
            }

            @Override
            public void itemPropertyChanged(FormItem<Boolean> f, Property property) {

            }
        });
        _form.add(generateTokenField);

        if (_settings.generateToken()) {
            FieldGroup fg = new FieldGroup(
                    new FieldDefinition("Token", "token", DocType.DEFAULT, null, "Identity token.", 0, 1));

            final Field<Boolean> encrypt = new Field<Boolean>(new FieldDefinition("Encrypt Token", "encrypt",
                    BooleanType.DEFAULT_TRUE_FALSE, "Encrypt token with password.", null, 0, 1));
            encrypt.setValue(_settings.tokenPassword() != null, false);
            encrypt.addListener(new FormItemListener<Boolean>() {

                @Override
                public void itemValueChanged(FormItem<Boolean> f) {
                    if (f.value()) {
                        final TokenPasswordSetForm form = new TokenPasswordSetForm();
                        form.showDialog(_sp.window(), new ActionListener() {

                            @Override
                            public void executed(boolean succeeded) {
                                if (succeeded) {
                                    _settings.setTokenPassword(form.password());
                                } else {
                                    encrypt.setValue(false);
                                }
                            }
                        });
                    } else {
                        _settings.setTokenPassword(null);
                    }
                }

                @Override
                public void itemPropertyChanged(FormItem<Boolean> f, Property property) {

                }
            });
            fg.add(encrypt);

            Field<Date> expiry = new Field<Date>(new FieldDefinition("Token Expiry", "expiry", DateType.DATE_ONLY,
                    "Set when token should expire.", null, 0, 1));
            expiry.setValue(_settings.tokenExpiry(), false);
            expiry.addListener(new FormItemListener<Date>() {

                @Override
                public void itemValueChanged(FormItem<Date> f) {
                    _settings.setTokenExpiry(f.value());
                }

                @Override
                public void itemPropertyChanged(FormItem<Date> f, Property property) {

                }
            });
            fg.add(expiry);

            Field<Integer> useCount = new Field<Integer>(new FieldDefinition("Use Count", "use-count",
                    IntegerType.POSITIVE, "The number of times the token may be used.", null, 0, 1));
            if (_settings.tokenUseCount() > 0) {
                useCount.setValue(_settings.tokenUseCount(), false);
            }
            useCount.addListener(new FormItemListener<Integer>() {

                @Override
                public void itemValueChanged(FormItem<Integer> f) {
                    if (f.value() == null || f.value() < 0) {
                        _settings.setTokenUseCount(0);
                    } else {
                        _settings.setTokenUseCount(f.value());
                    }
                }

                @Override
                public void itemPropertyChanged(FormItem<Integer> f, Property property) {

                }
            });
            fg.add(useCount);

            _form.add(fg);

        }

        Field<TargetPlatform> platformField = new Field<TargetPlatform>(
                new FieldDefinition("Target Platform", "platform", TargetPlatform.asEnumerationType(),
                        "The target platform to run the downloader application.", null, 0, 1));
        platformField.setValue(_settings.targetPlatform(), false);
        platformField.addListener(new FormItemListener<TargetPlatform>() {

            @Override
            public void itemValueChanged(FormItem<TargetPlatform> f) {
                _settings.setTargetPlatform(f.value());
            }

            @Override
            public void itemPropertyChanged(FormItem<TargetPlatform> f, Property property) {

            }
        });
        _form.add(platformField);

        appendToForm(_form);

        _form.render();

        _sp.setContent(_form);
        addMustBeValid(_form);
    }

    protected void appendToForm(Form form) {

    }

    @Override
    public Widget gui() {
        return _sp;
    }

}
