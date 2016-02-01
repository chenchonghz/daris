package daris.client.gui.form;

import java.io.File;
import java.util.Iterator;

import arc.gui.ValidatedInterfaceComponent;
import arc.mf.dtype.DataType;
import arc.mf.dtype.DocType;
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
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.stage.Window;

public class Form extends ValidatedInterfaceComponent {

    private TreeTableView<FormItem<?>> _ttv;

    public Form() {
        _ttv = new TreeTableView<FormItem<?>>(
                new FormTreeItem(this, null, null));
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
    }

    @Override
    public Node gui() {
        return _ttv;
    }

    public Window window() {
        Scene scene = _ttv.getScene();
        if (scene != null) {
            return scene.getWindow();
        }
        return null;
    }

    public FormTreeItem add(FormTreeItem parent, FormItem<?> formItem) {
        FormTreeItem formTreeItem = new FormTreeItem(this, parent, formItem);
        parent.getChildren().add(formTreeItem);
        return formTreeItem;
    }

    public FormTreeItem add(FormItem<?> formItem) {
        FormTreeItem formTreeItem = add((FormTreeItem) _ttv.getRoot(),
                formItem);
        addMustBeValid(formItem);
        return formTreeItem;
    }

    public void remove(FormTreeItem parent, FormItem<?> formItem) {
        for (Iterator<TreeItem<FormItem<?>>> it = parent.getChildren()
                .iterator(); it.hasNext();) {
            TreeItem<FormItem<?>> treeItem = it.next();
            if (formItem.equals(treeItem.getValue())) {
                it.remove();
                formItem.setForm(null);
                formItem.setParent(null);
                FormItem<?> parentItem = parent.getValue();
                if (parentItem == null) {
                    // It is a top level item as parent is root.
                    removeMustBeValid(formItem);
                } else {
                    parentItem.remove(formItem);
                }
            }
        }
    }

    public void remove(FormItem<?> formItem) {
        remove((FormTreeItem) _ttv.getRoot(), formItem);
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

        @SuppressWarnings({ "unchecked", "rawtypes" })
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
            formItem.setForm(form);
            FormItem<?> parentItem = parent.getValue();
            formItem.setParent(parentItem);
            if (parentItem != null) {
                parentItem.add(formItem);
            }
        }
    }

}
