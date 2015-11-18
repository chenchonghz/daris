package daris.client.gui.subject;

import java.util.Map.Entry;

import daris.client.gui.object.DObjectView;
import daris.client.gui.xml.KVTreeTableView;
import daris.client.gui.xml.XmlTreeTableView;
import daris.client.model.method.MethodRef;
import daris.client.model.subject.Subject;
import javafx.scene.control.Tab;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.StackPane;

public class SubjectView extends DObjectView<Subject> {

    protected Tab privateMetadataTab;

    protected Tab publicMetadataTab;

    public SubjectView(Subject subject) {
        super(subject);

        /*
         * public metadata tab
         */
        if (subject.hasPublicMetadata()) {
            StackPane publicMetadataPane = new StackPane();
            XmlTreeTableView publicMetadataTreeTableView = new XmlTreeTableView(
                    subject.publicMetadata(), false);
            publicMetadataPane.getChildren().add(publicMetadataTreeTableView);
            publicMetadataTab = new Tab("Metadata(Public)", publicMetadataPane);
            getTabs().add(1, publicMetadataTab);
        }

        /*
         * private metadata tab
         */
        if (subject.hasPrivateMetadata()) {
            StackPane privateMetadataPane = new StackPane();
            XmlTreeTableView privateMetadataTreeTableView = new XmlTreeTableView(
                    subject.privateMetadata(), false);
            privateMetadataPane.getChildren().add(privateMetadataTreeTableView);
            privateMetadataTab = new Tab("Metadata(Private)",
                    privateMetadataPane);
            getTabs().add(publicMetadataTab == null ? 1 : 2,
                    privateMetadataTab);
        }
    }

    @Override
    protected void addInterfaceMetadata(KVTreeTableView<String, Object> table,
            Subject subject) {

        super.addInterfaceMetadata(table, subject);

        MethodRef method = subject.method();
        if (method != null) {
            TreeItem<Entry<String, Object>> item = table.addEntry("Method",
                    method.citeableId());
            table.addEntry(item, "Name", method.name());
            if (method.description() != null) {
                table.addEntry(item, "Description", method.description());
            }
        }
        if (subject.dataUse() != null) {
            table.addEntry("Data Use", subject.dataUse());
        }
        if (subject.isVirtual()) {
            table.addEntry("Virtual", subject.isVirtual());
        }
    }

}
