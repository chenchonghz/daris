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
import arc.gui.gwt.widget.panel.SimplePanel;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.mf.client.util.ActionListener;
import arc.mf.client.util.AsynchronousAction;
import arc.mf.dtype.ConstantType;
import arc.mf.dtype.DocType;
import arc.mf.dtype.EnumerationType;
import arc.mf.dtype.EnumerationType.Value;
import arc.mf.dtype.StringType;
import arc.mf.model.dictionary.Dictionary;
import arc.mf.model.dictionary.DictionaryRef;
import arc.mf.model.dictionary.VariantDefinition;
import arc.mf.object.ObjectResolveHandler;

import com.google.gwt.user.client.ui.Widget;

import daris.client.model.query.options.QueryOptions;
import daris.client.model.query.options.XPath;

public class XPathEditForm extends ValidatedInterfaceComponent implements AsynchronousAction {

    private VerticalPanel _vp;
    private SimplePanel _formSP;
    private Form _form;
    private QueryOptions _opts;
    private XPath _xpath;

    public XPathEditForm(QueryOptions opts, XPath xpath) {
        _opts = opts;
        _xpath = xpath;
        _vp = new VerticalPanel();
        _vp.fitToParent();
        _vp.setPadding(10);

        _formSP = new SimplePanel();
        _formSP.fitToParent();
        _vp.add(_formSP);

        _form = new Form();
        _form.setShowDescriptions(false);
        _form.setShowHelp(false);

        Field<String> xpathField = new Field<String>(new FieldDefinition("xpath", ConstantType.DEFAULT,
                "The xpath of the metadata node.", null, 1, 1));
        xpathField.setValue(_xpath.value(), false);
        _form.add(xpathField);

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

        if (_xpath.dictionary() == null) {
            _form.render();
            _formSP.setContent(_form);
            addMustBeValid(_form);
        } else {
            new DictionaryRef(_xpath.dictionary()).resolve(new ObjectResolveHandler<Dictionary>() {

                @Override
                public void resolved(Dictionary o) {
                    if (o != null) {
                        List<VariantDefinition> vds = o.variants();
                        if (vds != null && !vds.isEmpty()) {
                            List<Value<String>> variants = new ArrayList<Value<String>>();
                            for (VariantDefinition vd : vds) {
                                variants.add(new Value<String>(vd.language()));
                            }

                            FieldGroup dictFG = new FieldGroup(new FieldDefinition("dictionary", DocType.DEFAULT,
                                    "The dictionary variant settings.", null, 0, 1));
                            Field<String> dictNameField = new Field<String>(new FieldDefinition("name",
                                    ConstantType.DEFAULT, "The name of the dictionary.", null, 1, 1));
                            dictNameField.setValue(_xpath.dictionary(), false);
                            dictFG.add(dictNameField);

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
                        addMustBeValid(_form);
                    }
                }
            });
        }
        _vp.add(_formSP);

    }

    @Override
    public Widget gui() {
        return _vp;
    }

    @Override
    public void execute(final ActionListener l) {
        boolean valid = valid().valid();
        if (valid) {
            _opts.addXPath(_xpath);
        }
        l.executed(valid);
    }

}
