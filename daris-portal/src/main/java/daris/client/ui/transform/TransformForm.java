package daris.client.ui.transform;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import arc.gui.ValidatedInterfaceComponent;
import arc.gui.form.Field;
import arc.gui.form.FieldDefinition;
import arc.gui.form.FieldEditMode;
import arc.gui.form.FieldGroup;
import arc.gui.form.FieldRenderOptions;
import arc.gui.form.Form;
import arc.gui.form.FormEditMode;
import arc.gui.form.FormItem;
import arc.gui.form.FormItem.Property;
import arc.gui.form.FormItem.XmlType;
import arc.gui.form.FormItemListener;
import arc.gui.gwt.colour.RGB;
import arc.gui.gwt.dnd.DropCheck;
import arc.gui.gwt.dnd.DropHandler;
import arc.gui.gwt.dnd.DropListener;
import arc.gui.gwt.widget.BaseWidget;
import arc.gui.gwt.widget.HTML;
import arc.gui.gwt.widget.panel.SimplePanel;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.gui.gwt.widget.scroll.ScrollPanel;
import arc.gui.gwt.widget.scroll.ScrollPolicy;
import arc.mf.client.util.DateTime;
import arc.mf.client.util.StateChangeListener;
import arc.mf.client.util.Validity;
import arc.mf.dtype.BooleanType;
import arc.mf.dtype.ConstantType;
import arc.mf.dtype.DocType;
import arc.mf.dtype.DoubleType;
import arc.mf.dtype.FloatType;
import arc.mf.dtype.IntegerType;
import arc.mf.dtype.ListOfType;
import arc.mf.dtype.LongType;
import arc.mf.dtype.StringType;
import arc.mf.dtype.TextType;

import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.user.client.ui.Widget;

import daris.client.model.transform.Transform;
import daris.client.model.transform.TransformBuilder;
import daris.client.model.transform.TransformDefinition;
import daris.client.model.transform.TransformDefinition.ParameterDefinition;

public class TransformForm extends ValidatedInterfaceComponent {

	public static Form formForView(Transform t) {

		Form form = new Form(FormEditMode.READ_ONLY);

		Field<Long> uid = new Field<Long>(new FieldDefinition("uid",
				LongType.DEFAULT, "Unique transform identifier", null, 1, 1));
		uid.setInitialValue(t.uid(), false);
		form.add(uid);

		Field<String> assetId = new Field<String>(new FieldDefinition("asset",
				StringType.DEFAULT, "The asset identifier of the transform.",
				null, 1, 1));
		assetId.setInitialValue(t.assetId(), false);
		form.add(assetId);

		if (t.name() != null) {
			Field<String> name = new Field<String>(
					new FieldDefinition("name", StringType.DEFAULT,
							"The name of the transform", null, 0, 1));
			name.setInitialValue(t.name(), false);
			form.add(name);
		}

		if (t.description() != null) {
			Field<String> description = new Field<String>(new FieldDefinition(
					"description", StringType.DEFAULT,
					"The description about the transform.", null, 0, 1));
			description.setInitialValue("description", false);
			form.add(description);
		}

		Field<Transform.Type> type = new Field<Transform.Type>(
				new FieldDefinition("type", ConstantType.DEFAULT,
						"The type of the transform (provider)", null, 1, 1));
		type.setInitialValue(t.type(), false);
		form.add(type);

		FieldGroup statusFG = new FieldGroup(new FieldDefinition("status",
				DocType.DEFAULT, "The status of the transform.", null, 1, 1));
		form.add(statusFG);

		Field<Transform.Status.State> statusState = new Field<Transform.Status.State>(
				new FieldDefinition("state", ConstantType.DEFAULT, null, null,
						1, 1));
		statusState.setInitialValue(t.status().state(), false);
		statusFG.add(statusState);

		Field<Date> statusTime = new Field<Date>(new FieldDefinition("time",
				ConstantType.DEFAULT, null, null, 0, 1));
		statusTime.setInitialValue(t.status().time(), false);
		statusFG.add(statusTime);

		FieldGroup defnFG = new FieldGroup(new FieldDefinition("definition",
				DocType.DEFAULT, "The transform definition.", null, 1, 1));
		form.add(defnFG);

		Field<Long> defnUid = new Field<Long>(new FieldDefinition("uid",
				LongType.DEFAULT,
				"Unique identifier of the transform definition.", null, 1, 1));
		defnUid.setInitialValue(t.definition().uid(), false);
		defnFG.add(defnUid);

		Field<Integer> defnVersion = new Field<Integer>(new FieldDefinition(
				"version", IntegerType.DEFAULT,
				"The version of the transform definition.", null, 0, 1));
		defnVersion.setInitialValue(t.definition().version(), false);
		defnFG.add(defnVersion);

		if (t.hasParameters()) {
			FieldGroup paramsFG = new FieldGroup(new FieldDefinition(
					"parameters", DocType.DEFAULT, "Transform parameters",
					null, 0, 1));
			form.add(paramsFG);
			Collection<Transform.Parameter> params = t.parameters().values();
			for (Transform.Parameter param : params) {
				Field<String> f = new Field<String>(new FieldDefinition(
						param.name(), StringType.DEFAULT, null, null, 1, 1));
				f.setInitialValue(param.value(), false);
				paramsFG.add(f);
			}
		}

		if (t.hasRuntimeProperties()) {
			FieldGroup runtimePropsFG = new FieldGroup(new FieldDefinition(
					"runtime properties", DocType.DEFAULT,
					"Runtime properties", null, 0, 1));
			form.add(runtimePropsFG);
			Set<String> props = t.runtimeProperties().keySet();
			for (String prop : props) {
				Field<String> f = new Field<String>(new FieldDefinition(prop,
						StringType.DEFAULT, null, null, 1, 1));
				f.setInitialValue(t.runtimeProperties().get(prop), false);
				runtimePropsFG.add(f);
			}
		}

		if (t.progress() != null) {
			Field<String> progress = new Field<String>(new FieldDefinition(
					"progress", ConstantType.DEFAULT, "Progress", null, 0, 1));
			progress.setInitialValue(t.progress().toString(), false);
			form.add(progress);
		}

		if (t.hasLogs()) {
			FieldGroup logsFG = new FieldGroup(new FieldDefinition("logs",
					DocType.DEFAULT, "Transform logs.", null, 0, 1));
			form.add(logsFG);
			List<Transform.Log> logs = t.logs();
			for (Transform.Log log : logs) {
				String label = DateTime.SERVER_DATE_TIME_FORMAT.format(log
						.time()) + " " + log.type();
				Field<String> f = new Field<String>(new FieldDefinition(label,
						ConstantType.DEFAULT, null, null, 0, 1));
				f.setInitialValue(log.message(), false);
				logsFG.add(f);
			}
		}
		form.render();
		return form;

	}

	private TransformBuilder _tb;

	private VerticalPanel _vp;

	private SimplePanel _formSP;

	private Form _form;

	private HTML _sb;

	public TransformForm(TransformBuilder tb) {

		_tb = tb;

		_vp = new VerticalPanel();
		_vp.fitToParent();

		_formSP = new SimplePanel();
		_formSP.fitToParent();
		_vp.add(_formSP);

		_sb = new HTML();
		_sb.setColour(RGB.RED);
		_sb.setFontFamily("Helvetica");
		_sb.setFontWeight(FontWeight.BOLD);
		_sb.setFontSize(12);
		_sb.setHeight(20);
		_sb.setMarginLeft(20);
		_vp.add(_sb);

		addChangeListener(new StateChangeListener() {

			@Override
			public void notifyOfChangeInState() {
				Validity v = valid();
				if (!v.valid()) {
					_sb.setHTML(v.reasonForIssue());
				} else {
					_sb.clear();
				}
			}
		});

		updateForm();

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void addParameterField(Form f, final ParameterDefinition pd) {
		arc.mf.dtype.DataType dataType = StringType.DEFAULT;
		String pv = _tb.parameterValue(pd.name());
		String stringValue = pv != null ? pv : pd.value();
		Field<?> field;
		FieldRenderOptions ro = new FieldRenderOptions();
		ro.setWidth(300);
		switch (pd.type()) {
		case BOOLEAN:
			dataType = BooleanType.DEFAULT_TRUE_FALSE;
			field = new Field<Boolean>(FieldEditMode.EDITABLE_IF_CREATE,
					new FieldDefinition(pd.name(), dataType, pd.description(),
							null, pd.minOccurs(), pd.maxOccurs()));
			if (stringValue != null) {
				((Field<Boolean>) field).setInitialValue(
						Boolean.parseBoolean(stringValue), false);
			}
			break;
		case INTEGER:
			dataType = IntegerType.DEFAULT;
			field = new Field<Integer>(FieldEditMode.EDITABLE_IF_CREATE,
					new FieldDefinition(pd.name(), dataType, pd.description(),
							null, pd.minOccurs(), pd.maxOccurs()));
			if (stringValue != null) {
				((Field<Integer>) field).setInitialValue(
						Integer.parseInt(stringValue), false);
			}
			break;
		case LONG:
			dataType = LongType.DEFAULT;
			field = new Field<Long>(FieldEditMode.EDITABLE_IF_CREATE,
					new FieldDefinition(pd.name(), dataType, pd.description(),
							null, pd.minOccurs(), pd.maxOccurs()));
			if (stringValue != null) {
				((Field<Long>) field).setInitialValue(
						Long.parseLong(stringValue), false);
			}
			break;
		case FLOAT:
			dataType = FloatType.DEFAULT;
			field = new Field<Float>(FieldEditMode.EDITABLE_IF_CREATE,
					new FieldDefinition(pd.name(), dataType, pd.description(),
							null, pd.minOccurs(), pd.maxOccurs()));
			if (stringValue != null) {
				((Field<Float>) field).setInitialValue(
						Float.parseFloat(stringValue), false);
			}
			break;
		case DOUBLE:
			dataType = DoubleType.DEFAULT;
			field = new Field<Double>(FieldEditMode.EDITABLE_IF_CREATE,
					new FieldDefinition(pd.name(), dataType, pd.description(),
							null, pd.minOccurs(), pd.maxOccurs()));
			if (stringValue != null) {
				((Field<Double>) field).setInitialValue(
						Double.parseDouble(stringValue), false);
			}
			break;
		default:
			dataType = StringType.DEFAULT;
			field = new Field<String>(FieldEditMode.EDITABLE_IF_CREATE,
					new FieldDefinition(pd.name(), dataType, pd.description(),
							null, pd.minOccurs(), pd.maxOccurs()));
			if (stringValue != null) {
				((Field<String>) field).setInitialValue(stringValue, false);
			}
			break;
		}
		field.setRenderOptions(ro);
		field.addListener(new FormItemListener() {

			@Override
			public void itemValueChanged(FormItem f) {
				_tb.setParameter(pd.name(), String.valueOf(f.value()));
			}

			@Override
			public void itemPropertyChanged(FormItem f, Property property) {

			}
		});
		f.add(field);
	}

	private void updateForm() {

		if (_form != null) {
			removeMustBeValid(_form);
			_formSP.clear();
		}

		_form = new Form(FormEditMode.CREATE);
		_form.setWidth100();

		/*
		 * definition
		 */
		FieldGroup definition = new FieldGroup(new FieldDefinition(
				"definition", DocType.DEFAULT,
				"The uid of the transform definition (template).", null, 1, 1));

		Field<Integer> definitionVersion = new Field<Integer>(
				new FieldDefinition("version", ConstantType.DEFAULT,
						"The version of the transform definition (template).",
						null, 1, 1));
		definitionVersion.setXmlType(XmlType.ATTRIBUTE);
		definitionVersion.setValue(_tb.definition().version(), false);
		definition.add(definitionVersion);

		Field<Transform.Type> definitionType = new Field<Transform.Type>(
				new FieldDefinition("type", ConstantType.DEFAULT,
						"The type of the transform definition.", null, 1, 1));
		definitionType.setXmlType(XmlType.ATTRIBUTE);
		definitionType.setValue(_tb.definition().type(), false);
		definition.add(definitionType);

		Field<Long> definitionUid = new Field<Long>(new FieldDefinition(null,
				ConstantType.DEFAULT, null, null, 1, 1));
		definitionUid.setValue(_tb.definition().uid(), false);
		definition.add(definitionUid);

		_form.add(definition);
		
		FieldRenderOptions ro = new FieldRenderOptions();
		ro.setWidth100();

		/*
		 * name
		 */
		Field<String> name = new Field<String>(new FieldDefinition("name",
				StringType.DEFAULT, "The transform name.", null, 0, 1));
		name.setValue(_tb.name(), false);
		name.setRenderOptions(ro);
		name.addListener(new FormItemListener<String>() {

			@Override
			public void itemValueChanged(FormItem<String> f) {
				_tb.setName(f.value());
			}

			@Override
			public void itemPropertyChanged(FormItem<String> f,
					Property property) {

			}
		});
		_form.add(name);

		/*
		 * description
		 */
		Field<String> description = new Field<String>(new FieldDefinition(
				"description", TextType.DEFAULT,
				"The transform description.", null, 0, 1));
		description.setRenderOptions(ro);
		description.setValue(_tb.description(), false);
		description.addListener(new FormItemListener<String>() {

			@Override
			public void itemValueChanged(FormItem<String> f) {
				_tb.setDescription(f.value());
			}

			@Override
			public void itemPropertyChanged(FormItem<String> f,
					Property property) {

			}
		});
		_form.add(description);

		/*
		 * iterator parameter
		 */
		if (_tb.hasIterator()) {
			Field<List<String>> itValuesField = new Field<List<String>>(
					new FieldDefinition(_tb.iteratorParameter(),
							new ListOfType(StringType.DEFAULT), null, null, 1,
							1));
			itValuesField.setInitialValue(
					new ArrayList<String>(_tb.iteratorValues()), false);
			itValuesField.setRenderOptions(ro);
			itValuesField.addListener(new FormItemListener<List<String>>() {

				@Override
				public void itemValueChanged(FormItem<List<String>> f) {
					_tb.setIteratorValues(_tb.iteratorParameter(), f.value());
				}

				@Override
				public void itemPropertyChanged(FormItem<List<String>> f,
						Property property) {

				}
			});
			_form.add(itValuesField);
		}

		/*
		 * parameters
		 */
		Collection<TransformDefinition.ParameterDefinition> paramDefns = _tb
				.definition().paramDefinitions().values();
		if (paramDefns != null && !paramDefns.isEmpty()) {
			for (ParameterDefinition paramDefn : paramDefns) {
				if (!_tb.isIterator(paramDefn.name())) {
					addParameterField(_form, paramDefn);
				}
			}
		}

		/*
		 * runtime properties
		 */
		// TODO:

		/*
		 * make the form a drop target to accept iterator values dragged from
		 * the navigator.
		 */
		_form.makeDropTarget(new DropHandler() {

			@Override
			public DropCheck checkCanDrop(Object data) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public void drop(BaseWidget target, List<Object> data,
					DropListener dl) {
				// TODO Auto-generated method stub

			}
		});

		_form.render();
		_formSP.setContent(new ScrollPanel(_form, ScrollPolicy.AUTO));
		addMustBeValid(_form);
	}

	@Override
	public Widget gui() {
		return _vp;
	}

	public BaseWidget widget() {
		return _vp;
	}
}
