package daris.client.ui.query.filter.item.mf;

import arc.gui.form.Field;
import arc.gui.form.FieldDefinition;
import arc.gui.form.FieldRenderOptions;
import arc.gui.form.Form;
import arc.gui.form.FormEditMode;
import arc.gui.form.FormItem;
import arc.gui.form.FormItem.Property;
import arc.gui.form.FormItemListener;
import arc.gui.gwt.widget.HTML;
import arc.gui.gwt.widget.panel.HorizontalPanel;
import arc.gui.gwt.widget.panel.SimplePanel;
import arc.mf.dtype.EnumerationType;
import arc.mf.dtype.StringType;

import com.google.gwt.user.client.ui.Widget;

import daris.client.mf.aclass.AssetClassEnumDataSource;
import daris.client.mf.aclass.AssetClassRef;
import daris.client.mf.aclass.AssetClassSchemeEnumDataSource;
import daris.client.mf.aclass.AssetClassSchemeRef;
import daris.client.model.query.filter.mf.ClassFilter;
import daris.client.model.query.filter.mf.ClassFilter.ClassOperator;
import daris.client.ui.query.filter.form.CompositeFilterForm;
import daris.client.ui.query.filter.item.FilterItem;

public class ClassFilterItem extends FilterItem<ClassFilter> {
    private HorizontalPanel _hp;
    private SimplePanel _formSP;
    private Form _form;

    public ClassFilterItem(CompositeFilterForm cform, ClassFilter filter, boolean editable) {
        super(cform, filter, editable);
        _hp = new HorizontalPanel();
        _hp.setHeight(22);

        HTML label = new HTML("class");
        label.setFontSize(11);
        label.setMarginTop(8);
        _hp.add(label);
        _hp.setSpacing(3);

        _formSP = new SimplePanel();
        _formSP.setHeight100();
        _hp.add(_formSP);

        updateForm();
    }

    private void updateForm() {
        if (_form != null) {
            removeMustBeValid(_form);
        }

        _formSP.clear();

        _form = new Form(editable() ? FormEditMode.UPDATE : FormEditMode.READ_ONLY);
        ClassOperator op = filter().operator();
        _form.setNumberOfColumns((op != null && op.isCompareOperator()) ? 3 : 2);
        _form.setShowLabels(false);
        _form.setShowDescriptions(false);
        _form.setShowHelp(false);

        Field<ClassOperator> opField = new Field<ClassOperator>(new FieldDefinition("operator",
                new EnumerationType<ClassOperator>(ClassOperator.VALUES), null, null, 1, 1));
        opField.setInitialValue(filter().operator(), false);
        opField.addListener(new FormItemListener<ClassOperator>() {

            @Override
            public void itemValueChanged(FormItem<ClassOperator> f) {
                filter().setOperator(f.value());
                updateForm();
            }

            @Override
            public void itemPropertyChanged(FormItem<ClassOperator> f, Property property) {

            }
        });
        FieldRenderOptions fro = new FieldRenderOptions();
        fro.setWidth(90);
        opField.setRenderOptions(fro);
        _form.add(opField);

        if (op != null) {
            if (op.isTextOperator()) {
                Field<String> textField = new Field<String>(new FieldDefinition("text", StringType.DEFAULT, null, null,
                        1, 1));
                textField.setInitialValue(filter().value(), false);
                textField.addListener(new FormItemListener<String>() {

                    @Override
                    public void itemValueChanged(FormItem<String> f) {
                        filter().setText(f.value());
                    }

                    @Override
                    public void itemPropertyChanged(FormItem<String> f, Property property) {

                    }
                });
                fro = new FieldRenderOptions();
                fro.setWidth(120);
                textField.setRenderOptions(fro);
                _form.add(textField);
            } else {
                AssetClassSchemeRef scheme = filter().assetClassScheme();
                Field<AssetClassSchemeRef> schemeField = new Field<AssetClassSchemeRef>(new FieldDefinition("scheme",
                        new EnumerationType<AssetClassSchemeRef>(new AssetClassSchemeEnumDataSource()), null, null, 1,
                        1));
                schemeField.setInitialValue(scheme, false);
                final AssetClassEnumDataSource classEnumDataSource = new AssetClassEnumDataSource(scheme);
                fro = new FieldRenderOptions();
                fro.setWidth(120);
                schemeField.setRenderOptions(fro);
                _form.add(schemeField);
                final Field<AssetClassRef> classField = new Field<AssetClassRef>(new FieldDefinition("class",
                        new EnumerationType<AssetClassRef>(classEnumDataSource), null, null, 1, 1));
                classField.setInitialValue(filter().assetClass(), false);
                classField.addListener(new FormItemListener<AssetClassRef>() {

                    @Override
                    public void itemValueChanged(FormItem<AssetClassRef> f) {
                        filter().setAssetClass(f.value());
                    }

                    @Override
                    public void itemPropertyChanged(FormItem<AssetClassRef> f, Property property) {

                    }
                });
                fro = new FieldRenderOptions();
                fro.setWidth(120);
                classField.setRenderOptions(fro);
                schemeField.addListener(new FormItemListener<AssetClassSchemeRef>() {

                    @Override
                    public void itemValueChanged(FormItem<AssetClassSchemeRef> f) {
                        filter().setAssetClassScheme(f.value());
                        classEnumDataSource.setScheme(f.value());
                        classField.clear();
                    }

                    @Override
                    public void itemPropertyChanged(FormItem<AssetClassSchemeRef> f, Property property) {

                    }
                });
                _form.add(classField);
            }
        }
        addMustBeValid(_form);
        _form.render();
        _formSP.setContent(_form);
    }

    @Override
    public Widget gui() {
        return _hp;
    }

}