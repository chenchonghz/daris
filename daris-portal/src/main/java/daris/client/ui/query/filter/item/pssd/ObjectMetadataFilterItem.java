package daris.client.ui.query.filter.item.pssd;

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
import arc.gui.gwt.widget.panel.HorizontalPanel;
import arc.gui.gwt.widget.panel.SimplePanel;
import arc.mf.client.util.StateChangeListener;
import arc.mf.dtype.BooleanType;
import arc.mf.dtype.EnumerationType;
import arc.mf.dtype.StringType;
import arc.mf.expr.Operator;
import arc.mf.object.ObjectResolveHandler;
import arc.mf.xml.defn.Node;

import com.google.gwt.user.client.ui.Widget;

import daris.client.model.object.DObject;
import daris.client.model.object.DObject.Type;
import daris.client.model.object.metadata.tree.ObjectMetadataTree;
import daris.client.model.query.filter.mf.MetadataFilter;
import daris.client.model.query.filter.pssd.ObjectMetadataFilter;
import daris.client.ui.query.MetadataPathSelectComboBox;
import daris.client.ui.query.filter.form.CompositeFilterForm;
import daris.client.ui.query.filter.item.FilterItem;
import daris.client.ui.query.filter.item.mf.MetadataFilterItem;

public class ObjectMetadataFilterItem extends FilterItem<ObjectMetadataFilter> {

    private HorizontalPanel _hp;

    private MetadataPathSelectComboBox _mp;
    private SimplePanel _mpSP;
    private HorizontalPanel _formsHP;
    private Form _operatorForm;
    private Form _ignoreCaseForm;
    private Form _valueForm;

    public ObjectMetadataFilterItem(CompositeFilterForm cform, ObjectMetadataFilter filter, boolean editable) {
        super(cform, filter, editable);

        _hp = new HorizontalPanel();
        _hp.setHeight(22);

        Form typeForm = new Form(editable ? FormEditMode.UPDATE : FormEditMode.READ_ONLY);
        typeForm.setShowDescriptions(false);
        typeForm.setShowHelp(false);
        typeForm.setShowLabels(false);

        Field<DObject.Type> typeField = new Field<DObject.Type>(new FieldDefinition("type",
                new EnumerationType<DObject.Type>(new DObject.Type[] { DObject.Type.subject, DObject.Type.ex_method,
                        DObject.Type.study, DObject.Type.dataset }), null, null, 1, 1));
        typeField.setInitialValue(filter.objectType(), false);
        typeField.addListener(new FormItemListener<DObject.Type>() {

            @Override
            public void itemValueChanged(FormItem<Type> f) {
                filter().setObjectType(f.value());
                updateGUI();
            }

            @Override
            public void itemPropertyChanged(FormItem<Type> f, Property property) {

            }
        });
        typeForm.add(typeField);
        typeForm.render();

        _hp.setSpacing(3);
        addMustBeValid(typeForm);
        _hp.add(typeForm);

        HTML label = new HTML("metadata:");
        label.setFontSize(11);
        label.setMarginTop(7);
        _hp.setSpacing(3);
        _hp.add(label);

        _mpSP = new SimplePanel();
        _mpSP.setHeight100();
        _mpSP.setWidth(150);
        _hp.add(_mpSP);

        _formsHP = new HorizontalPanel();
        _formsHP.setHeight100();
        _hp.add(_formsHP);

        updateGUI();

    }

    private void updateGUI() {

        if (filter().objectType() == null) {
            return;
        }

        _mp = new MetadataPathSelectComboBox(filter().path(), new ObjectMetadataTree(filter().project(), filter()
                .objectType()), true);
        _mp.setReadOnly(!editable());
        _mp.addChangeListener(new StateChangeListener() {

            @Override
            public void notifyOfChangeInState() {
                filter().setPath(_mp.value());
                updateForms();
            }
        });
        _mp.setHeight100();
        _mp.setWidth(150);

        _mpSP.setContent(_mp);

        updateForms();

    }

    private void updateForms() {

        /*
         * clear all forms in _formsHP
         */
        _formsHP.removeAll();
        if (_operatorForm != null) {
            removeMustBeValid(_operatorForm);
        }
        if (_ignoreCaseForm != null) {
            removeMustBeValid(_ignoreCaseForm);
        }

        if (filter().path() == null) {
            // path not selected yet. no form required.
            return;
        }

        _operatorForm = new Form(editable() ? FormEditMode.UPDATE : FormEditMode.READ_ONLY);
        _operatorForm.setNumberOfColumns(1);
        _operatorForm.setShowLabels(false);
        _operatorForm.setShowDescriptions(false);
        _operatorForm.setShowHelp(false);

        Operator op = filter().operator();
        Field<Operator> opField = new Field<Operator>(new FieldDefinition("operator", new EnumerationType<Operator>(
                filter().availableOperators()), null, null, 1, 1));
        opField.setInitialValue(op);
        FieldRenderOptions fro = new FieldRenderOptions();
        fro.setWidth(100);
        opField.setRenderOptions(fro);
        opField.addListener(new FormItemListener<MetadataFilter.MetadataOperator>() {

            @Override
            public void itemValueChanged(FormItem<MetadataFilter.MetadataOperator> f) {
                filter().setOperator(f.value());
                updateForms();
            }

            @Override
            public void itemPropertyChanged(FormItem<MetadataFilter.MetadataOperator> f, Property property) {

            }
        });
        _operatorForm.add(opField);
        _operatorForm.render();
        addMustBeValid(_operatorForm);
        _formsHP.add(_operatorForm);
        
        
        if (filter().requiresValue()) {
            addValueForm();
        }

    }

    private void addValueForm() {
        filter().path().resolveNode(new ObjectResolveHandler<Node>() {

            @SuppressWarnings({ "rawtypes" })
            @Override
            public void resolved(Node n) {
                if (n == null) {
                    return;
                }

                /*
                 * ignore-case
                 */
                if (_ignoreCaseForm != null) {
                    removeMustBeValid(_ignoreCaseForm);
                }
                if (n.type() instanceof StringType) {
                    _ignoreCaseForm = new Form(editable() ? FormEditMode.UPDATE : FormEditMode.READ_ONLY);
                    _ignoreCaseForm.setShowLabels(false);
                    _ignoreCaseForm.setShowDescriptions(false);
                    _ignoreCaseForm.setShowHelp(false);
                    _ignoreCaseForm.setBooleanAs(BooleanAs.CHECKBOX);
                    Field<Boolean> ignoreCaseField = new Field<Boolean>(new FieldDefinition("ignore-case",
                            BooleanType.DEFAULT_TRUE_FALSE, null, null, 0, 1));
                    ignoreCaseField.addListener(new FormItemListener<Boolean>() {

                        @Override
                        public void itemValueChanged(FormItem<Boolean> f) {
                            filter().setIgnoreCase(f.value());
                        }

                        @Override
                        public void itemPropertyChanged(FormItem<Boolean> f, Property property) {

                        }
                    });
                    ignoreCaseField.setValue(filter().ignoreCase(), false);
                    _ignoreCaseForm.add(ignoreCaseField);

                    addMustBeValid(_ignoreCaseForm);
                    _ignoreCaseForm.render();

                    _formsHP.add(_ignoreCaseForm);
                    
                    HTML label = new HTML("ignore-case");
                    label.setFontSize(11);
                    label.setMarginTop(5);
                    _formsHP.add(label);
                }

                /*
                 * value
                 */
                if (_valueForm != null) {
                    removeMustBeValid(_valueForm);
                }
                _valueForm = new Form(editable() ? FormEditMode.UPDATE : FormEditMode.READ_ONLY);
                _valueForm.setNumberOfColumns(1);
                _valueForm.setShowLabels(false);
                _valueForm.setShowDescriptions(false);
                _valueForm.setShowHelp(false);
                Field<?> valueField = MetadataFilterItem.createValueField(filter().value(), n);
                valueField.addListener(new FormItemListener() {

                    @Override
                    public void itemValueChanged(FormItem f) {
                        filter().setValue(f.valueAsString());
                    }

                    @Override
                    public void itemPropertyChanged(FormItem f, Property property) {

                    }
                });
                _valueForm.add(valueField);
                _valueForm.render();
                addMustBeValid(_valueForm);

                _formsHP.add(_valueForm);
            }
        });

    }

    @Override
    public Widget gui() {
        return _hp;
    }

}
