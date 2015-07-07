package daris.client.ui.query.filter.item.mf;

import arc.gui.form.Field;
import arc.gui.form.FieldDefinition;
import arc.gui.form.Form;
import arc.gui.form.FormItem;
import arc.gui.form.FormItem.Property;
import arc.gui.form.FormItemListener;
import arc.gui.gwt.widget.HTML;
import arc.gui.gwt.widget.panel.HorizontalPanel;
import arc.gui.gwt.widget.panel.SimplePanel;
import arc.mf.dtype.EnumerationType;
import arc.mf.dtype.LongType;
import arc.mf.expr.Operator;

import com.google.gwt.user.client.ui.Widget;

import daris.client.model.query.filter.mf.CSizeFilter;
import daris.client.model.query.filter.mf.CSizeFilter.Unit;
import daris.client.model.query.filter.operators.CompareOperator;
import daris.client.ui.query.filter.form.CompositeFilterForm;
import daris.client.ui.query.filter.item.FilterItem;

public class CSizeFilterItem extends FilterItem<CSizeFilter> {

	private HorizontalPanel _hp;
	private SimplePanel _formSP;
	private Form _form;
	private Field<CompareOperator> _opField;
	private Field<Long> _csizeField;
	private Field<Unit> _unitField;

	public CSizeFilterItem(CompositeFilterForm form, CSizeFilter filter,
			boolean editable) {
		super(form, filter, editable);

		_hp = new HorizontalPanel();
		_hp.setHeight(22);

		HTML label = new HTML("csize");
		label.setFontSize(11);
		label.setMarginTop(8);
		_hp.add(label);
		_hp.setSpacing(3);

		_formSP = new SimplePanel();
		_formSP.setHeight100();
		_formSP.setWidth(200);
		_hp.add(_formSP);

		_form = new Form();
		_form.setHeight100();
		_form.setMarginTop(8);
		_form.setShowLabels(false);
		_form.setShowDescriptions(false);
		_form.setShowHelp(false);

		_form.setNumberOfColumns(3);

		_opField = new Field<CompareOperator>(new FieldDefinition("operator",
				new EnumerationType<Operator>(CompareOperator.VALUES), null,
				null, 1, 1));
		_opField.setInitialValue(filter().operator());
		_opField.addListener(new FormItemListener<CompareOperator>() {

			@Override
			public void itemValueChanged(FormItem<CompareOperator> f) {
				filter().setOperator(f.value());
			}

			@Override
			public void itemPropertyChanged(FormItem<CompareOperator> f,
					Property property) {

			}
		});
		_form.add(_opField);

		_csizeField = new Field<Long>(new FieldDefinition("csize",
				LongType.POSITIVE, "The asset content size.", null, 1, 1));
		_csizeField.setInitialValue(filter().csize(), false);
		_csizeField.addListener(new FormItemListener<Long>() {

			@Override
			public void itemValueChanged(FormItem<Long> f) {
				filter().setCSize(f.value(), _unitField.value());
			}

			@Override
			public void itemPropertyChanged(FormItem<Long> f, Property property) {

			}
		});
		_form.add(_csizeField);

		_unitField = new Field<Unit>(new FieldDefinition("unit",
				new EnumerationType<Unit>(Unit.values()),
				"The unit of the content size.", null, 1, 1));
		_unitField.setInitialValue(Unit.MB, false);
		_unitField.addListener(new FormItemListener<Unit>() {

			@Override
			public void itemValueChanged(FormItem<Unit> f) {
				filter().setCSize(_csizeField.value(), f.value());
			}

			@Override
			public void itemPropertyChanged(FormItem<Unit> f, Property property) {

			}
		});
		_form.add(_unitField);

		_form.render();
		addMustBeValid(_form);
		_formSP.setContent(_form);
		_hp.setSpacing(3);

	}

	@Override
	public Widget gui() {
		return _hp;
	}

}