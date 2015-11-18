package daris.client.gui.project;

import java.util.List;
import java.util.Map.Entry;

import daris.client.gui.object.DObjectView;
import daris.client.gui.xml.KVTreeTableView;
import daris.client.model.method.MethodRef;
import daris.client.model.project.Project;
import javafx.scene.control.TreeItem;

public class ProjectView extends DObjectView<Project> {

    public ProjectView(Project project) {
        super(project);
        // TODO
    }

    @Override
    protected void addInterfaceMetadata(KVTreeTableView<String, Object> table,
            Project project) {

        super.addInterfaceMetadata(table, project);

        List<MethodRef> methods = project.methods();
        if (methods != null) {
            for (MethodRef method : methods) {
                TreeItem<Entry<String, Object>> item = table.addEntry("Method",
                        method.citeableId());
                table.addEntry(item, "Name", method.name());
                if (method.description() != null) {
                    table.addEntry(item, "Description", method.description());
                }
            }
        }
        if (project.dataUse() != null) {
            table.addEntry("Data Use", project.dataUse());
        }
    }

}
