package daris.client.ui.object.action;

import java.util.ArrayList;
import java.util.List;

import arc.gui.ValidatedInterfaceComponent;
import arc.gui.form.Field;
import arc.gui.form.FieldDefinition;
import arc.gui.form.FieldGroup;
import arc.gui.form.FieldSet;
import arc.gui.form.FieldSetListener;
import arc.gui.form.FieldValidHandler;
import arc.gui.form.FieldValueValidator;
import arc.gui.form.Form;
import arc.gui.form.FormEditMode;
import arc.gui.form.FormItem;
import arc.gui.form.FormItem.Property;
import arc.gui.form.FormItem.XmlType;
import arc.gui.form.FormItemListener;
import arc.gui.gwt.colour.RGB;
import arc.gui.gwt.widget.dialog.Dialog;
import arc.gui.gwt.widget.label.Label;
import arc.gui.gwt.widget.panel.SimplePanel;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.gui.gwt.widget.scroll.ScrollPanel;
import arc.gui.gwt.widget.scroll.ScrollPolicy;
import arc.mf.client.util.ActionListener;
import arc.mf.client.util.AsynchronousAction;
import arc.mf.client.util.ObjectUtil;
import arc.mf.client.util.Validity;
import arc.mf.dtype.BooleanType;
import arc.mf.dtype.ConstantType;
import arc.mf.dtype.EnumerationType;
import arc.mf.object.ObjectMessageResponse;

import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.user.client.ui.Widget;

import daris.client.model.exmethod.StepEnum;
import daris.client.model.exmethod.StepItem;
import daris.client.model.object.DObjectRef;
import daris.client.model.study.messages.StudyPreCreate;

public class StudyPreCreateForm extends ValidatedInterfaceComponent implements AsynchronousAction {

	private StudyPreCreate _svc;

	private VerticalPanel _vp;

	private Form _form;

	private Label _statusArea;

	public StudyPreCreateForm(DObjectRef exm) {
		_svc = new StudyPreCreate(exm);

		_vp = new VerticalPanel();
		_vp.fitToParent();

		_form = new Form(FormEditMode.CREATE) {

			@SuppressWarnings("rawtypes")
			public Validity valid() {

				// Validates all form items in the same way
				// based on the type of the form item
				Validity v = super.valid();
				if (!v.valid()) {
					return v;
				}

				// Add additional validator for "step" field item
				// We can have multiple steps (or empty means all)
				// When adding new step, check it's not already selected
				List<FormItem> items = primarySection().fields("step");

				if (items == null || items.isEmpty()) {
					return v;
				}

				List<String> steps = new ArrayList<String>();
				for (FormItem f : items) {
					StepItem value = (StepItem) f.value();
					if (value != null) {
						final String step = value.path();
						if (!steps.contains(step)) {
							steps.add(step);
						} else {
							return new Validity() {

								@Override
								public boolean valid() {
									return false;
								}

								@Override
								public String reasonForIssue() {
									return "step " + step + " has already been selected.";
								}
							};
						}
					}
				}

				return v;
			}
		};

		/*
		 * Add "pid" field item to parent form
		 */
		FieldGroup fgPid = new FieldGroup(new FieldDefinition("pid", ConstantType.DEFAULT,
				"The id of the parent ex-method", null, 1, 1));

		Field<String> fProute = new Field<String>(new FieldDefinition("proute", ConstantType.DEFAULT,
				"The proute of the parent ex-method", null, 0, 1));
		fProute.setXmlType(XmlType.ATTRIBUTE);
		fProute.setInitialValue(exm.proute());
		fgPid.add(fProute);

		Field<String> fPidValue = new Field<String>(new FieldDefinition(null, ConstantType.DEFAULT, null, null, 1, 1));
		fPidValue.setInitialValue(exm.id());
		fgPid.add(fPidValue);
		_form.add(fgPid);

		/*
		 * Add "method-meta" field item to parent form
		 */
		Field<Boolean> fMethodMeta = new Field<Boolean>(new FieldDefinition("method-meta",
				BooleanType.DEFAULT_TRUE_FALSE, " Set the meta-data pre-specified by the Method ? Defaults to true.",
				null, 0, 1));
		fMethodMeta.setInitialValue(true);
		fMethodMeta.addListener(new FormItemListener<Boolean>() {

			@Override
			public void itemValueChanged(FormItem<Boolean> f) {
				_svc.setMethodMeta(f.value());
			}

			@Override
			public void itemPropertyChanged(FormItem<Boolean> f, Property property) {

			}
		});
		_form.add(fMethodMeta);
		
		// Validates the value of the step item as the user seelcts from
		// the menu.   The user is shown an error and must remediate
		final FieldValueValidator<StepItem> validator = new FieldValueValidator<StepItem>() {

			@SuppressWarnings("rawtypes")
			@Override
			public void validate(Field<StepItem> f, FieldValidHandler vh) {
				StepItem step = f.value();
				if (step == null) {
					vh.setValid();
					return;
				}

				List<FormItem> items = f.fieldSet().fields("step");
				if (items == null || items.isEmpty()) {
					vh.setValid();
					return;
				}

				for (FormItem item : items) {
					StepItem v = (StepItem) item.value();
					if (v != null && v != step && ObjectUtil.equals(v.path(), step.path())) {
						vh.setInvalid("step " + f.value().path() + " has already been selected.");
						return;
					}

				}
				vh.setValid();
			}
		};

		final FormItemListener<StepItem> formItemListener = new FormItemListener<StepItem>() {

			@Override
			public void itemValueChanged(FormItem<StepItem> f) {
				updateSteps();
			}

			@Override
			public void itemPropertyChanged(FormItem<StepItem> f, Property property) {
				updateSteps();
			}
		};


		/*
		 * Add "step" field item to the parent form
		 */
		Field<StepItem> fStep = new Field<StepItem>(new FieldDefinition("step", new EnumerationType<String>(
				new StepEnum(exm.id(), null)), "Step in the ExMethod to create the Study for.", null, 0,
				Integer.MAX_VALUE)) {
			
			// This is called when the 'plus' button is clicked to add a new item
			public FormItem<StepItem> cleanCopyOf() {
				Field<StepItem> cc = (Field<StepItem>) super.cleanCopyOf();   // Comes with no listeener and validator
				cc.setForm(_form);
				cc.setFieldSet(_form.primarySection());
				cc.addListener(formItemListener);
				cc.addValueValidator(validator);
				return cc;
			}
		};
		fStep.setForm(_form);
		fStep.setFieldSet(_form.primarySection());
		fStep.addListener(formItemListener);
		fStep.addValueValidator(validator);
		_form.add(fStep);

		/**
		 * Listens for all changes to the form
		 */
		_form.addListener(new FieldSetListener() {

			@Override
			public void addedField(FieldSet s, @SuppressWarnings("rawtypes") FormItem f, int idx, boolean lastUpdate) {
				if (f.title() != null && f.title().equals("step")) {
					updateSteps();
				}
			}

			@Override
			public void removedField(FieldSet s, @SuppressWarnings("rawtypes") FormItem f, int idx, boolean lastUpdate) {
				if (f.title() != null && f.title().equals("step")) {
					updateSteps();
				}
			}

			@Override
			public void updatedFields(FieldSet s) {
				updateSteps();
			}

			@Override
			public void updatedFieldValue(FieldSet s, @SuppressWarnings("rawtypes") FormItem f) {
				if (f.title() != null && f.title().equals("step")) {
					updateSteps();
				}
			}

			@Override
			public void updatedFieldState(FieldSet s, @SuppressWarnings("rawtypes") FormItem f, Property property) {
				if (f.title() != null && f.title().equals("step")) {
					updateSteps();
				}
			}
		});
		addMustBeValid(_form);

		_form.render();

		SimplePanel sp = new SimplePanel();
		sp.fitToParent();
		sp.setContent(new ScrollPanel(_form, ScrollPolicy.AUTO));
		_vp.add(sp);

		_statusArea = new Label();
		_statusArea.setHeight(30);
		_statusArea.setWidth100();
		_statusArea.setFontWeight(FontWeight.BOLD);
		_statusArea.setFontSize(14);
		_statusArea.setBorderRadius(3);
		_statusArea.setMargin(5);
		_statusArea.setPaddingTop(5);
		_statusArea.setPaddingLeft(20);
		_statusArea.setColour(RGB.RED);
		_vp.add(_statusArea);

	}

	@SuppressWarnings("rawtypes")
	private void updateSteps() {
		List<FormItem> stepItems = _form.primarySection().fields("step");
		if (stepItems != null) {
			List<String> steps = new ArrayList<String>(stepItems.size());
			for (FormItem item : stepItems) {
				StepItem v = (StepItem) item.value();
				if (v != null) {
					String step = v.path();
					if (!steps.contains(step)) {
						steps.add(step);
					}
				}
			}
			if (!steps.isEmpty()) {
				_svc.setSteps(steps);
				return;
			}
		}
		_svc.setSteps(null);
	}

	@Override
	public Validity valid() {
		Validity v = super.valid();
		if (v.valid()) {
			_statusArea.removeBorder();
			_statusArea.setText(null);
		} else {
			_statusArea.setBorder(1, RGB.RED);
			_statusArea.setText("Issue: " + v.reasonForIssue());
		}
		return v;
	}

	@Override
	public Widget gui() {
		return _vp;
	}

	@Override
	public void execute(final ActionListener l) {
		_svc.send(new ObjectMessageResponse<List<DObjectRef>>() {

			@Override
			public void responded(List<DObjectRef> objs) {
				l.executed(objs != null && !objs.isEmpty());
				if (objs != null) {
					StringBuilder sb = new StringBuilder();
					for (int i = 0; i < objs.size(); i++) {
						DObjectRef o = objs.get(i);
						sb.append(o.id());
						if (i < objs.size() - 1) {
							sb.append(", ");
						}
					}

					Dialog.inform("Results", "The following studies have been created: " + sb.toString());
				}

			}
		});
	}
}
