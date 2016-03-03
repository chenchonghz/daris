package daris.client.gui.form;

import java.io.File;

import arc.gui.ValidatedInterfaceComponent;
import arc.mf.dtype.DataType;
import arc.mf.dtype.DocType;
import daris.client.gui.form.FormItem.XmlNodeType;
import daris.client.gui.form.field.BooleanFormField;
import daris.client.gui.form.field.DoubleFormField;
import daris.client.gui.form.field.EmailAddressFormField;
import daris.client.gui.form.field.EnumerationFormField;
import daris.client.gui.form.field.FileFormField;
import daris.client.gui.form.field.FloatFormField;
import daris.client.gui.form.field.IntegerFormField;
import daris.client.gui.form.field.LongFormField;
import daris.client.gui.form.field.PasswordFormField;
import daris.client.gui.form.field.StringFormField;
import daris.client.gui.form.field.TextFormField;
import daris.client.gui.form.field.UneditableStringFormField;
import daris.client.gui.form.field.UrlFormField;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.layout.StackPane;
import javafx.stage.Window;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class Form extends ValidatedInterfaceComponent {

    private FormItem<?> _rootItem;
    private StackPane _stackPane;
    private TreeTableView<FormItem<?>> _ttv;

    public Form() {
        _stackPane = new StackPane();
        _rootItem = new FormItem(this, null, DocType.DEFAULT, "root", null,
                null, 1, 1, XmlNodeType.ELEMENT, null);
    }

    public void render() {
        if (_ttv == null) {
            _ttv = new TreeTableView<FormItem<?>>(
                    new FormTreeItem(this, null, _rootItem));
            _ttv.getRoot().setExpanded(true);
            _ttv.setShowRoot(false);

            TreeTableColumn<FormItem<?>, String> nameColumn = new TreeTableColumn<FormItem<?>, String>(
                    "Name");
            nameColumn.setPrefWidth(250);
            nameColumn.setCellValueFactory(param -> {
                String displayName = param.getValue().getValue().displayName();
                return new ReadOnlyStringWrapper(displayName);
            });
            nameColumn.setStyle("-fx-font-weight: bold;");

            TreeTableColumn<FormItem<?>, FormItem<?>> valueColumn = new TreeTableColumn<FormItem<?>, FormItem<?>>(
                    "Value");
            valueColumn.setPrefWidth(500);
            valueColumn.setCellValueFactory(param -> {
                return param.getValue().valueProperty();
            });
            valueColumn.setCellFactory(column -> {
                return new FormTreeTableCell();
            });
            _ttv.getColumns().add(nameColumn);
            _ttv.getColumns().add(valueColumn);
            _stackPane.getChildren().setAll(_ttv);

            FormTreeItem rootTreeItem = (FormTreeItem) _ttv.getRoot();
            ObservableList<FormItem<?>> items = _rootItem.getItems();
            if (items != null) {
                for (FormItem<?> item : items) {
                    addTreeItem(rootTreeItem, item);
                }
            }
        }
    }

    @Override
    public Node gui() {
        return _stackPane;
    }

    public Window window() {
        Scene scene = _stackPane.getScene();
        if (scene != null) {
            return scene.getWindow();
        }
        return null;
    }

    private FormTreeItem addTreeItem(FormTreeItem parentTreeItem,
            FormItem<?> formItem) {
        FormTreeItem treeItem = new FormTreeItem(this, parentTreeItem,
                formItem);
        parentTreeItem.getChildren().add(treeItem);
        ObservableList<FormItem<?>> items = formItem.getItems();
        if (items != null) {
            for (FormItem<?> item : items) {
                addTreeItem(treeItem, item);
            }
        }
        return treeItem;
    }

    public void add(FormItem<?> formItem) {
        _rootItem.add(formItem);
    }

    private static class FormTreeTableCell
            extends TreeTableCell<FormItem<?>, FormItem<?>> {

        private Node _graphic;

        public FormTreeTableCell() {

        }

        @Override
        protected void updateItem(FormItem<?> formItem, boolean empty) {
            super.updateItem(formItem, empty);
            if (empty) {
                setGraphic(null);
            } else {
                setGraphic(graphicFor(formItem));
            }
        }

        private Node graphicFor(FormItem<?> item) {
            DataType type = item.dataType();
            if (_graphic == null) {
                if (type instanceof DocType) {
                    _graphic = null;
                } else if (type instanceof arc.mf.dtype.IntegerType) {
                    _graphic = new IntegerFormField((FormItem<Integer>) item);
                } else if (type instanceof arc.mf.dtype.LongType) {
                    _graphic = new LongFormField((FormItem<Long>) item);
                } else if (type instanceof arc.mf.dtype.FloatType) {
                    _graphic = new FloatFormField((FormItem<Float>) item);
                } else if (type instanceof arc.mf.dtype.DoubleType) {
                    _graphic = new DoubleFormField((FormItem<Double>) item);
                } else if (type instanceof arc.mf.dtype.BooleanType) {
                    _graphic = new BooleanFormField((FormItem<Boolean>) item);
                } else if (type instanceof arc.mf.dtype.EnumerationType) {
                    _graphic = new EnumerationFormField(item);
                } else if ((type instanceof arc.mf.dtype.AssetType)
                        || (type instanceof arc.mf.dtype.AssetIdType)
                        || (type instanceof arc.mf.dtype.ConstantType)
                        || (type instanceof arc.mf.dtype.CiteableIdType)
                        || (type instanceof arc.mf.dtype.IdentifierType)
                        || (type instanceof arc.mf.dtype.ReplicaIdType)) {
                    _graphic = new UneditableStringFormField(item, null);
                } else if (type instanceof arc.mf.dtype.DateType) {

                } else if (type instanceof arc.mf.dtype.StringType) {
                    _graphic = new StringFormField((FormItem<String>) item);
                } else if (type instanceof arc.mf.dtype.TextType) {
                    _graphic = new TextFormField((FormItem<String>) item);
                } else if (type instanceof arc.mf.dtype.PasswordType) {
                    _graphic = new PasswordFormField((FormItem<String>) item);
                } else if (type instanceof arc.mf.dtype.UrlType) {
                    _graphic = new UrlFormField((FormItem<String>) item);
                } else if (type instanceof arc.mf.dtype.FileType) {
                    _graphic = new FileFormField((FormItem<File>) item);
                } else if (type instanceof arc.mf.dtype.EmailAddressType) {
                    _graphic = new EmailAddressFormField(
                            (FormItem<String>) item);
                }
            }
            return _graphic;
        }
    }

    public static class FormTreeItem extends TreeItem<FormItem<?>> {

        private FormTreeItem(Form form, FormTreeItem parent,
                FormItem<?> formItem) {
            super(formItem);
            setExpanded(true);
            // FormItem<?> parentItem = parent == null ? null :
            // parent.getValue();
            // if (formItem != null) {
            // formItem.setForm(form);
            // formItem.setParent(parentItem);
            // if (parentItem != null) {
            // parentItem.add(formItem);
            // }
            // }
        }
    }

}
