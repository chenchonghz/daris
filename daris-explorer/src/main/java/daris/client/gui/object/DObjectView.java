package daris.client.gui.object;

import daris.client.gui.dataset.DataSetView;
import daris.client.gui.exmethod.ExMethodView;
import daris.client.gui.project.ProjectView;
import daris.client.gui.repository.RepositoryView;
import daris.client.gui.study.StudyView;
import daris.client.gui.subject.SubjectView;
import daris.client.gui.xml.KVTreeTableView;
import daris.client.gui.xml.XmlTreeTableView;
import daris.client.model.DObject;
import daris.client.model.DataSet;
import daris.client.model.ExMethod;
import daris.client.model.Project;
import daris.client.model.Repository;
import daris.client.model.Study;
import daris.client.model.Subject;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.StackPane;

public abstract class DObjectView<T extends DObject> extends TabPane {

    private T _object;

    protected final int interfaceTabIndex = 0;
    private KVTreeTableView<String, Object> _interfaceTreeTableView;

    protected int metadataTabIndex = -1;

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
        if (object.hasMetadataForView()) {
            StackPane metadataPane = new StackPane();
            XmlTreeTableView metadataTreeTableView = new XmlTreeTableView(
                    object.metadataForView(), false);
            metadataPane.getChildren().add(metadataTreeTableView);
            Tab metadataTab = new Tab("Metadata", metadataPane);
            getTabs().add(metadataTab);
            metadataTabIndex = getTabs().indexOf(metadataTab);
        }
    }

    protected T object() {
        return _object;
    }

    protected void addInterfaceMetadata(
            KVTreeTableView<String, Object> treeTableView, T object) {
        treeTableView.addEntry("id", object.citeableId());
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
