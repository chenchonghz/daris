package daris.client.ui.project;

import arc.gui.form.Form;
import arc.gui.form.FormEditMode;
import arc.gui.gwt.widget.panel.VerticalPanel;
import daris.client.model.project.Project;
import daris.client.model.project.ProjectBuilder;
import daris.client.ui.object.DObjectForm;

public class ProjectForm extends DObjectForm<Project> {

    protected ProjectForm(ProjectBuilder builder, Project project, FormEditMode mode) {
        super(builder, project, mode);

        updateMembersTab();

        updateRoleMembersTab();
    }

    public ProjectForm(ProjectBuilder builder) {
        super(builder, null, FormEditMode.CREATE);
    }

    public ProjectForm(Project project, FormEditMode mode) {
        super(null, project, mode);
    }

    @Override
    protected void insertToInterfaceTab(VerticalPanel vp) {
        // TODO
        // namespace tree selector

    }

    @Override
    protected void addToInterfaceForm(Form form) {
        // TODO:
    }

    private void updateMembersTab() {
        // TODO:
    }

    private void updateRoleMembersTab() {
        // TODO:
    }
}
