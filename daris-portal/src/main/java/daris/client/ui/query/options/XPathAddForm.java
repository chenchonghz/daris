package daris.client.ui.query.options;

import java.util.ArrayList;
import java.util.List;

import arc.gui.ValidatedInterfaceComponent;
import arc.gui.form.Field;
import arc.gui.form.FieldDefinition;
import arc.gui.form.FieldGroup;
import arc.gui.form.FieldRenderOptions;
import arc.gui.form.Form;
import arc.gui.form.FormItem;
import arc.gui.form.FormItem.Property;
import arc.gui.form.FormItemListener;
import arc.gui.gwt.colour.RGB;
import arc.gui.gwt.widget.label.Label;
import arc.gui.gwt.widget.panel.HorizontalPanel;
import arc.gui.gwt.widget.panel.SimplePanel;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.mf.client.util.ActionListener;
import arc.mf.client.util.AsynchronousAction;
import arc.mf.client.util.IsNotValid;
import arc.mf.client.util.StateChangeListener;
import arc.mf.client.util.Validity;
import arc.mf.dtype.ConstantType;
import arc.mf.dtype.DataType;
import arc.mf.dtype.DocType;
import arc.mf.dtype.EnumerationType;
import arc.mf.dtype.EnumerationType.Value;
import arc.mf.dtype.StringType;
import arc.mf.model.asset.document.MetadataDocument;
import arc.mf.model.asset.document.MetadataDocumentRef;
import arc.mf.model.dictionary.Dictionary;
import arc.mf.model.dictionary.DictionaryRef;
import arc.mf.model.dictionary.VariantDefinition;
import arc.mf.object.ObjectResolveHandler;
import arc.mf.xml.defn.Attribute;
import arc.mf.xml.defn.Element;
import arc.mf.xml.defn.Node;

import com.google.gwt.dom.client.Style.BorderStyle;
import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.user.client.ui.Widget;

import daris.client.model.query.filter.mf.MetadataPath;
import daris.client.model.query.options.QueryOptions;
import daris.client.model.query.options.XPath;
import daris.client.ui.query.MetadataPathSelectComboBox;

public class XPathAddForm extends ValidatedInterfaceComponent implements AsynchronousAction {

    private VerticalPanel _vp;
    private MetadataPathSelectComboBox _mps;
    private SimplePanel _formSP;
    private Form _form;

    private QueryOptions _opts;
    private XPath _xpath;
    private List<VariantDefinition> _variants;

    public XPathAddForm(QueryOptions opts) {
        _opts = opts;
        _vp = new VerticalPanel();
        _vp.fitToParent();
        _vp.setPadding(10);

        HorizontalPanel hp = new HorizontalPanel();
        hp.setSpacing(5);
        hp.setWidth100();
        Label xpathLabel = new Label("xpath:");
        xpathLabel.setFontWeight(FontWeight.BOLD);
        xpathLabel.setFontSize(11);
        xpathLabel.setMarginTop(10);
        hp.add(xpathLabel);
        hp.setSpacing(5);
        _mps = new MetadataPathSelectComboBox(null, _opts.metadataTree(), true);
        _mps.setWidth100();
        _mps.addChangeListener(new StateChangeListener() {

            @Override
            public void notifyOfChangeInState() {
                MetadataPath mp = _mps.value();
                if (mp == null) {
                    _xpath = null;
                    _variants = null;
                    updateForm();
                } else {
                    _xpath = new XPath(mp.path().replace('/', '_').replace('@', '_'), "meta/" + mp.path());
                    _variants = null;
                    mp.resolveNode(new ObjectResolveHandler<Node>() {
                        @Override
                        public void resolved(Node o) {
                            if (o != null) {
                                DataType dataType = o.type();
                                if (dataType instanceof EnumerationType) {
                                    _xpath.setDictionary(((EnumerationType<?>) dataType).dictionary());
                                } else if (dataType instanceof StringType) {
                                    StringType.Dictionary dict = ((StringType) dataType).dictionaryRestriction();
                                    if (dict != null) {
                                        _xpath.setDictionary(dict.name());
                                    }
                                }
                                if (_xpath.dictionary() != null) {
                                    new DictionaryRef(_xpath.dictionary())
                                            .resolve(new ObjectResolveHandler<Dictionary>() {
                                                @Override
                                                public void resolved(Dictionary o) {
                                                    if (o != null && o.variants() != null && !o.variants().isEmpty()) {
                                                        _variants = o.variants();
                                                    }
                                                    updateForm();
                                                }
                                            });
                                }
                            }
                            updateForm();
                        }
                    });
                }
            }
        });
        _mps.setValue(
                (_xpath == null || _xpath.value() == null) ? null : new MetadataPath(
                        _xpath.value().startsWith("meta/") ? _xpath.value().substring(5) : _xpath.value()), false);
        hp.add(_mps);
        _vp.add(hp);

        _formSP = new SimplePanel();
        _formSP.fitToParent();
        _formSP.setPadding(10);
        _formSP.setMarginTop(5);
        _formSP.setBorder(1, BorderStyle.DOTTED, new RGB(0xcc, 0xcc, 0xcc));
        _vp.add(_formSP);

        updateForm();
    }

    private void updateForm() {

        _formSP.clear();
        if (_form != null) {
            removeMustBeValid(_form);
        }

        _form = new Form();
        _form.setShowDescriptions(false);
        _form.setShowHelp(false);
        Field<String> nameField = new Field<String>(new FieldDefinition("name", StringType.DEFAULT,
                "The element name for result.", null, 1, 1));
        nameField.addListener(new FormItemListener<String>() {

            @Override
            public void itemValueChanged(FormItem<String> f) {
                _xpath.setName(f.value());
            }

            @Override
            public void itemPropertyChanged(FormItem<String> f, Property property) {

            }
        });
        FieldRenderOptions fro = new FieldRenderOptions();
        fro.setWidth(100);
        nameField.setRenderOptions(fro);
        if (_xpath != null) {
            nameField.setValue(_xpath.name(), false);
        }
        _form.add(nameField);

        if (_xpath != null && _xpath.dictionary() != null && _variants != null) {
            FieldGroup dictFG = new FieldGroup(new FieldDefinition("dictionary", DocType.DEFAULT,
                    "The dictionary variant settings.", null, 0, 1));
            Field<String> dictNameField = new Field<String>(new FieldDefinition("name", ConstantType.DEFAULT,
                    "The name of the dictionary.", null, 1, 1));
            dictNameField.setValue(_xpath.dictionary(), false);
            dictFG.add(dictNameField);

            List<Value<String>> variants = new ArrayList<Value<String>>();
            for (VariantDefinition vd : _variants) {
                variants.add(new Value<String>(vd.language()));
            }
            Field<String> dictVariantField = new Field<String>(new FieldDefinition("variant",
                    new EnumerationType<String>(variants), "Dictionary variants.", null, 0, 1));
            dictVariantField.addListener(new FormItemListener<String>() {

                @Override
                public void itemValueChanged(FormItem<String> f) {
                    _xpath.setDictionaryVariant(f.value());
                }

                @Override
                public void itemPropertyChanged(FormItem<String> f, Property property) {

                }
            });
            dictVariantField.setValue(_xpath.dictionaryVariant(), false);
            dictFG.add(dictVariantField);

            _form.add(dictFG);
        }
        _form.render();
        _formSP.setContent(_form);
        notifyOfChangeInState();
    }

    @Override
    public Validity valid() {
        Validity v = super.valid();
        if (!v.valid()) {
            return v;
        }
        if (_xpath == null || _xpath.value() == null) {
            return new IsNotValid("Metadata node is not selected.");
        }
        return v;
    }

    @Override
    public Widget gui() {
        return _vp;
    }

    @Override
    public void execute(final ActionListener l) {
        boolean valid = valid().valid();
        if (valid) {
            String path = _xpath.value();
            if (path.startsWith("meta/")) {
                path = path.substring(5);
            }
            if (path.indexOf('/') == -1) {
                // document node
                new MetadataDocumentRef(path).resolve(new ObjectResolveHandler<MetadataDocument>() {

                    @Override
                    public void resolved(MetadataDocument o) {
                        if (o != null) {
                            addDocument(o.definition().root());
                            l.executed(true);
                        } else {
                            l.executed(false);
                        }

                    }
                });
            } else {
                _opts.addXPath(_xpath);
                l.executed(true);
            }
        } else {
            l.executed(false);
        }
    }

    private void addNode(Node node) {
        XPath xpath = new XPath(node.path().replace('/', '_').replace('@', '_'), "meta/" + node.path());
        if (node.type() instanceof EnumerationType) {
            xpath.setDictionary(((EnumerationType<?>) node.type()).dictionary());
        } else if (node.type() instanceof StringType) {
            StringType.Dictionary dict = ((StringType) node.type()).dictionaryRestriction();
            if (dict != null) {
                xpath.setDictionary(dict.name());
            }
        }
        _opts.addXPath(xpath);
    }

    private void addDocument(Element doc) {
        List<Attribute> as = doc.attributes();
        if (as != null) {
            for (Attribute a : as) {
                addNode(a);
            }
        }
        List<Element> es = doc.elements();
        if (es != null) {
            for (Element e : es) {
                if (e.type() instanceof DocType) {
                    addDocument(e);
                } else {
                    addNode(e);
                }
            }
        }
    }
}
