package daris.client.gui.dataset.action;

import arc.gui.ValidatedInterfaceComponent;
import daris.client.model.object.DObjectRef;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;

public class DataSetCreateForm extends ValidatedInterfaceComponent {

    private DObjectRef _study;
    private TabPane _tabPane;
    private Tab _interfaceTab;

    public DataSetCreateForm(DObjectRef study) {
        _study = study;

        _tabPane = new TabPane();
        _interfaceTab = new Tab("Interface");
        _tabPane.getTabs().add(_interfaceTab);
        initInterfaceTab();

    }

    private void initInterfaceTab() {
        GridPane grid = new GridPane();
        grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(5);
        grid.setVgap(5);
        grid.setPadding(new Insets(5, 5, 5, 5));
        grid.setBorder(new Border(new BorderStroke[] {
                new BorderStroke(Color.BEIGE, BorderStrokeStyle.SOLID,
                        new CornerRadii(5), BorderWidths.DEFAULT) }));
        ColumnConstraints cc = new ColumnConstraints();
        cc.setHalignment(HPos.RIGHT);
        grid.getColumnConstraints().add(cc);
        cc = new ColumnConstraints();
        cc.setHalignment(HPos.LEFT);
        grid.getColumnConstraints().add(cc);
        int rowIndex = 0;
        
        
    }

    @Override
    public Node gui() {
        return _tabPane;
    }

}
