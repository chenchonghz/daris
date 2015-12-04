package daris.client.gui.object;

import daris.client.gui.dataset.DataSetView;
import daris.client.gui.exmethod.ExMethodView;
import daris.client.gui.project.ProjectView;
import daris.client.gui.repository.RepositoryView;
import daris.client.gui.study.StudyView;
import daris.client.gui.subject.SubjectView;
import daris.client.gui.xml.KVTreeTableView;
import daris.client.gui.xml.XmlTreeTableView;
import daris.client.model.dataset.DataSet;
import daris.client.model.exmethod.ExMethod;
import daris.client.model.object.DObject;
import daris.client.model.project.Project;
import daris.client.model.repository.Repository;
import daris.client.model.study.Study;
import daris.client.model.subject.Subject;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.StackPane;

public abstract class DObjectView<T extends DObject> extends TabPane {

    private T _object;

    protected final int interfaceTabIndex = 0;
    private KVTreeTableView<String, Object> _interfaceTreeTableView;

    protected Tab metadataTab;

    protected Tab contentTab;

    public DObjectView(T object) {
        super();
        setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
        _object = object;

        /*
         * interface tab
         */
        StackPane interfacePane = new StackPane();
        this._interfaceTreeTableView = new KVTreeTableView<String, Object>();
        interfacePane.getChildren().add(this._interfaceTreeTableView);
        addInterfaceMetadata(_interfaceTreeTableView, _object);
        getTabs().add(0, new Tab("Interface", interfacePane));

        /*
         * metadata tab
         */
        if (object.hasMetadata()) {
            StackPane metadataPane = new StackPane();
            XmlTreeTableView metadataTreeTableView = new XmlTreeTableView(
                    object.metadata(), false);
            metadataPane.getChildren().add(metadataTreeTableView);
            metadataTab = new Tab("Metadata", metadataPane);
            getTabs().add(metadataTab);
        }

        /*
         * content tab
         */
        if (object.hasContent()) {
            StackPane contentPane = new StackPane();
            ContentView contentView = new ContentView(object);
            contentPane.getChildren().add(contentView);
            contentTab = new Tab("Content", contentPane);
            getTabs().add(contentTab);
        }
    }

    protected T object() {
        return _object;
    }

    protected void addInterfaceMetadata(
            KVTreeTableView<String, Object> treeTableView, T object) {

        if (object instanceof Repository) {
            treeTableView.addEntry("Description Asset ID", object.assetId());
        } else {
            treeTableView.addEntry("Object Type", object.type().typeName());
            treeTableView.addEntry("Citeable ID", object.citeableId());
            treeTableView.addEntry("Asset ID", object.assetId());
        }
        treeTableView.addEntry("Name", object.name());
        treeTableView.addEntry("Description", object.description());
        if (!(object instanceof Repository)) {
            treeTableView.addEntry("Namespace", object.namespace());
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends DObject> DObjectView<T> create(T o) {
        if (o instanceof Repository) {
            return (DObjectView<T>) new RepositoryView((Repository) o);
        } else if (o instanceof Project) {
            return (DObjectView<T>) new ProjectView((Project) o);
        } else if (o instanceof Subject) {
            return (DObjectView<T>) new SubjectView((Subject) o);
        } else if (o instanceof ExMethod) {
            return (DObjectView<T>) new ExMethodView((ExMethod) o);
        } else if (o instanceof Study) {
            return (DObjectView<T>) new StudyView((Study) o);
        } else if (o instanceof DataSet) {
            return (DObjectView<T>) new DataSetView((DataSet) o);
        } else {
            throw new UnsupportedOperationException("View for object type "
                    + o.type() + " is not implemented.");
        }
    }
}
