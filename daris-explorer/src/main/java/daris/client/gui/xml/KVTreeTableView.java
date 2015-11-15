package daris.client.gui.xml;

import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;

public class KVTreeTableView<K, V> extends TreeTableView<Entry<K, V>> {

    public KVTreeTableView() {
        super(new TreeItem<Entry<K, V>>(null));
        getRoot().setExpanded(true);
        setShowRoot(false);

        TreeTableColumn<Entry<K, V>, String> nameColumn = new TreeTableColumn<>(
                "Name");
        nameColumn.setPrefWidth(250);
        nameColumn.setCellValueFactory(param -> {
            Entry<K, V> o = param.getValue().getValue();
            return new ReadOnlyStringWrapper(
                    keyToString(o.getKey(), o.getValue()));
        });

        TreeTableColumn<Entry<K, V>, String> valueColumn = new TreeTableColumn<>(
                "Value");
        valueColumn.setPrefWidth(200);
        valueColumn.setCellValueFactory(param -> {
            Entry<K, V> o = param.getValue().getValue();
            V v = o.getValue();
            return new ReadOnlyStringWrapper(valueToString(v));
        });

        getColumns().add(nameColumn);
        getColumns().add(valueColumn);
    }

    protected String valueToString(V value) {
        return value == null ? null : value.toString();
    }

    protected String keyToString(K key, V value) {
        return key == null ? null : key.toString();
    }

    public TreeItem<Entry<K, V>> addEntry(TreeItem<Entry<K, V>> parent, K key,
            V value) {
        TreeItem<Entry<K, V>> item = new TreeItem<Entry<K, V>>(
                new SimpleEntry<K, V>(key, value));
        parent.getChildren().add(item);
        item.setExpanded(true);
        return item;
    }

    public TreeItem<Entry<K, V>> addEntry(TreeItem<Entry<K, V>> parent, K key) {
        return addEntry(parent, key, null);
    }

    public TreeItem<Entry<K, V>> addEntry(K key, V value) {
        return addEntry(getRoot(), key, value);
    }

    public TreeItem<Entry<K, V>> addEntry(K key) {
        return addEntry(getRoot(), key, null);
    }

}
