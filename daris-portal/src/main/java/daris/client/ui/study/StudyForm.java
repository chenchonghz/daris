package daris.client.ui.study;

import arc.gui.form.FormEditMode;
import daris.client.model.study.Study;
import daris.client.model.study.StudyBuilder;
import daris.client.ui.object.DObjectForm;

public class StudyForm extends DObjectForm<Study> {

    protected StudyForm(StudyBuilder builder, Study study, FormEditMode mode) {
        super(builder, study, mode);
    }

    public StudyForm(StudyBuilder builder) {
        this(builder, null, FormEditMode.CREATE);
    }

    public StudyForm(Study study, FormEditMode mode) {
        this(null, study, mode);
    }

}
