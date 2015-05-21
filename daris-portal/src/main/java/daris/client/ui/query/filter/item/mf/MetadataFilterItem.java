package daris.client.ui.query.filter.item.mf;

import java.util.Date;

import arc.gui.form.Field;
import arc.gui.form.FieldDefinition;
import arc.gui.form.FieldRenderOptions;
import arc.gui.form.Form;
import arc.gui.form.Form.BooleanAs;
import arc.gui.form.FormEditMode;
import arc.gui.form.FormItem;
import arc.gui.form.FormItem.Property;
import arc.gui.form.FormItemListener;
import arc.gui.gwt.widget.HTML;
import arc.gui.gwt.widget.dialog.Dialog;
import arc.gui.gwt.widget.panel.HorizontalPanel;
import arc.gui.gwt.widget.panel.SimplePanel;
import arc.mf.client.util.StateChangeListener;
import arc.mf.dtype.AssetIdType;
import arc.mf.dtype.AssetType;
import arc.mf.dtype.BooleanType;
import arc.mf.dtype.CiteableIdType;
import arc.mf.dtype.DataType;
import arc.mf.dtype.DateType;
import arc.mf.dtype.DoubleType;
import arc.mf.dtype.EnumerationType;
import arc.mf.dtype.FloatType;
import arc.mf.dtype.IntegerType;
import arc.mf.dtype.LongType;
import arc.mf.dtype.StringType;
import arc.mf.model.asset.document.tree.MetadataTree;
import arc.mf.model.asset.document.tree.MetadataTree.DisplayTo;
import arc.mf.object.ObjectResolveHandler;
import arc.mf.xml.defn.Node;

import com.google.gwt.user.client.ui.Widget;

import daris.client.model.query.filter.mf.MetadataFilter;
import daris.client.ui.query.MetadataPathSelectComboBox;
import daris.client.ui.query.filter.form.CompositeFilterForm;
import daris.client.ui.query.filter.item.FilterItem;

public class MetadataFilterItem extends FilterItem<MetadataFilter> {

    private HorizontalPanel _hp;
    private MetadataPathSelectComboBox _mp;
    private SimplePanel _formSP;
    private Form _form;
    private Field<MetadataFilter.MetadataOperator> _opField;

    public MetadataFilterItem(CompositeFilterForm cform, MetadataFilter filter, boolean editable) {
        super(cform, filter, editable);

        _hp = new HorizontalPanel();
        _hp.setHeight(22);

        HTML label = new HTML("metadata:");
        label.setFontSize(11);
        label.setMarginTop(7);
        _hp.add(label);
        _hp.setSpacing(5);

        _mp = new MetadataPathSelectComboBox(filter.path(), new MetadataTree(DisplayTo.DOCUMENT_NODES, true), true);
        _mp.setReadOnly(!editable);
        _mp.addChangeListener(new StateChangeListener() {

            @Override
            public void notifyOfChangeInState() {
                filter().setPath(_mp.value());
                updateForm();
            }
        });
        _mp.setHeight100();
        _mp.setWidth(150);

        _hp.add(_mp);

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

        if (filter().path() == null) {
            // path not selected yet. no form required.
            return;
        }

        _form = new Form(editable() ? FormEditMode.UPDATE : FormEditMode.READ_ONLY);
        _form.setBooleanAs(BooleanAs.TRUE_FALSE);
        _form.setNumberOfColumns(filter().requiresValue() ? 2 : 1);
        _form.setShowLabels(false);
        _form.setShowHelp(false);
        _form.setShowDescriptions(false);

        MetadataFilter.MetadataOperator op = filter().operator();
        _opField = new Field<MetadataFilter.MetadataOperator>(new FieldDefinition("operator",
                new EnumerationType<MetadataFilter.MetadataOperator>(filter().availableOperators()), null, null, 1, 1));
        _opField.setInitialValue(op);
        FieldRenderOptions fro = new FieldRenderOptions();
        // Operator field width = 100px
        fro.setWidth(100);

        _opField.setRenderOptions(fro);
        _opField.addListener(new FormItemListener<MetadataFilter.MetadataOperator>() {

            @Override
            public void itemValueChanged(FormItem<MetadataFilter.MetadataOperator> f) {
                filter().setOperator(f.value());
                updateForm();
            }

            @Override
            public void itemPropertyChanged(FormItem<MetadataFilter.MetadataOperator> f, Property property) {

            }
        });
        _form.add(_opField);

        if (filter().requiresValue()) {
            addValueField();
        } else {
            _form.render();
        }
        addMustBeValid(_form);
        _formSP.setContent(_form);
    }

    private void addValueField() {
        filter().path().resolveNode(new ObjectResolveHandler<Node>() {

            @SuppressWarnings("rawtypes")
            @Override
            public void resolved(Node n) {
                if (n == null) {
                    return;
                }
                Field<?> valueField = createValueField(filter().value(), n);
                valueField.addListener(new FormItemListener() {

                    @Override
                    public void itemValueChanged(FormItem f) {
                        filter().setValue(f.valueAsString());
                    }

                    @Override
                    public void itemPropertyChanged(FormItem f, Property property) {

                    }
                });
                _form.add(valueField);
//                if(n.type() instanceof StringType){
//                    Field<Boolean> ignoreCaseField = new Field<Boolean>(new FieldDefinition("ignore-case", BooleanType.DEFAULT_TRUE_FALSE, null,null,0,1));
//                    ignoreCaseField.addListener(new FormItemListener<Boolean>(){
//
//                        @Override
//                        public void itemValueChanged(FormItem<Boolean> f) {
//                            filter().setIgnoreCase(f.value());
//                        }
//
//                        @Override
//                        public void itemPropertyChanged(FormItem<Boolean> f, Property property) {
//                            
//                        }});
//                    ignoreCaseField.setValue(filter().ignoreCase(), false);
//                    _form.add(ignoreCaseField);
//                }
                _form.render();
            }
        });

    }

    public static Field<?> createValueField(String value, arc.mf.xml.defn.Node dn) {
        DataType dt = dn.type();
        if (dt instanceof AssetIdType || dt instanceof AssetType || dt instanceof CiteableIdType) {
            // substitute cid type with string because FormItem for certain types are not available yet.
            dt = StringType.DEFAULT;
        }
        FieldDefinition fd = new FieldDefinition(dn.name(), dt, dn.description(), null, 1, 1);
        if (dt instanceof DoubleType) {
            Field<Double> f = new Field<Double>(fd);
            f.setInitialValue(value == null ? null : Double.parseDouble(value), false);
            return f;
        } else if (dt instanceof FloatType) {
            Field<Float> f = new Field<Float>(fd);
            f.setInitialValue(value == null ? null : Float.parseFloat(value), false);
            return f;
        } else if (dt instanceof IntegerType) {
            Field<Integer> f = new Field<Integer>(fd);
            f.setInitialValue(value == null ? null : Integer.parseInt(value), false);
            return f;
        } else if (dt instanceof LongType) {
            Field<Long> f = new Field<Long>(fd);
            f.setInitialValue(value == null ? null : Long.parseLong(value), false);
            return f;
        } else if (dt instanceof BooleanType) {
            Field<Boolean> f = new Field<Boolean>(fd);
            if (value != null) {
                f.setInitialValue(Boolean.parseBoolean(value), false);
            }
            return f;
        } else if (dt instanceof DateType) {
            Field<Date> f = new Field<Date>(fd);
            try {
                f.setInitialValue(value == null ? null : DateType.parseServerDate(value), false);
            } catch (Throwable e) {
                Dialog.warn("error", e.getMessage());
                e.printStackTrace(System.out);
            }
            return f;
        } else {
            Field<String> f = new Field<String>(fd);
            f.setInitialValue(value);
            return f;
        }
    }

    @Override
    public Widget gui() {
        return _hp;
    }

}
