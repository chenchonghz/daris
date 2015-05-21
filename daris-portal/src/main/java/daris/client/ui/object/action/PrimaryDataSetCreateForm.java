package daris.client.ui.object.action;

import java.util.List;

import arc.gui.form.Field;
import arc.gui.form.FieldDefinition;
import arc.gui.form.FieldGroup;
import arc.gui.form.Form;
import arc.gui.form.FormItem.XmlType;
import arc.mf.client.file.LocalFile;
import arc.mf.dtype.ConstantType;
import arc.mf.dtype.DocType;
import arc.mf.dtype.LongType;
import daris.client.model.IDUtil;
import daris.client.model.object.DObjectRef;
import daris.client.model.task.PrimaryDataSetCreateTask;

public class PrimaryDataSetCreateForm extends DataSetCreateForm {

	public PrimaryDataSetCreateForm(DObjectRef po, List<LocalFile> files) {
		super(new PrimaryDataSetCreateTask(po, files), po);
	}

	@Override
	protected void addInterfaceFormItems(Form interfaceForm) {

		FieldGroup subjectFieldGroup = new FieldGroup(new FieldDefinition("subject", DocType.DEFAULT,
				"Details about the subject for which this acquisition was made.", null, 0, 1));
		FieldGroup subjectIdFieldGroup = new FieldGroup(new FieldDefinition("id", DocType.DEFAULT,
				"The citeable id of the subject.", null, 1, 1));
		Field<String> subjectProuteField = new Field<String>(new FieldDefinition("proute", ConstantType.DEFAULT,
				"Proute of the subject.", null, 0, 1));
		subjectProuteField.setValue(parentObject().proute());
		subjectProuteField.setXmlType(XmlType.ATTRIBUTE);
		subjectIdFieldGroup.add(subjectProuteField);
		// TODO: CiteableIdType
		Field<String> subjectIdField = new Field<String>(new FieldDefinition(null, ConstantType.DEFAULT, "Citeable id of the subject.", null, 1,1));
		subjectIdField.setValue(IDUtil.getParentId(parentObject().id(), 2));
		subjectIdFieldGroup.add(subjectIdField);
		subjectFieldGroup.add(subjectIdFieldGroup);
		Field<Long> subjectStateField = new Field<Long>(new FieldDefinition("state", LongType.POSITIVE,
				"The state of the subject at the time of acquisition.", null, 0, 1));
		subjectFieldGroup.add(subjectStateField);
		interfaceForm.add(subjectFieldGroup);
		
	}

}