package daris.client.ui.subject;

import arc.gui.form.FormEditMode;
import daris.client.model.subject.Subject;
import daris.client.model.subject.SubjectBuilder;
import daris.client.ui.object.DObjectForm;

public class SubjectForm extends DObjectForm<Subject> {

    protected SubjectForm(SubjectBuilder builder, Subject subject, FormEditMode mode) {
        super(builder, subject, mode);
    }

    public SubjectForm(SubjectBuilder builder) {
        this(builder, null, FormEditMode.CREATE);
    }

    public SubjectForm(Subject subject, FormEditMode mode) {
        this(null, subject, mode);
    }

}
