package daris.client.gui.object;

import java.util.Map.Entry;

import daris.client.gui.xml.KVTreeTableView;
import daris.client.model.DObject;
import daris.client.model.archive.ArchiveEntryCollectionRef;
import daris.client.model.object.DataContent;
import javafx.scene.control.Accordion;
import javafx.scene.control.TitledPane;
import javafx.scene.control.TreeItem;

public class ContentView extends Accordion {

    private DObject _o;

    public ContentView(DObject o) {
        _o = o;
        TitledPane metadataPane = new TitledPane("Content metadata",
                createContentMetadataTreeTableView(_o.content()));
        getPanes().add(metadataPane);

        if (_o.hasBrowsableArchiveContent()) {
            ArchiveEntryCollectionRef arc = new ArchiveEntryCollectionRef(_o);
            arc.setPageSize(10);
            TitledPane archivePane = new TitledPane("Content archive",
                    new ArchiveContentTableView(arc));
            getPanes().add(archivePane);
        }
        setExpandedPane(metadataPane);
    }

    private static KVTreeTableView<String, Object> createContentMetadataTreeTableView(
            DataContent content) {

        KVTreeTableView<String, Object> table = new KVTreeTableView<String, Object>();
        table.addEntry("MIME Type", content.mimeType);
        if (content.extension != null
                && !"content/unknown".equals(content.mimeType)) {
            table.addEntry("File Extension", content.extension);
        }
        if (content.logicalMimeType != null) {
            table.addEntry("Logical MIME Type", content.logicalMimeType);
        }
        table.addEntry("Size",
                content.humanReadableSize + " (" + content.size + " bytes)");
        table.addEntry("Checksum", Long.toHexString(content.csum).toUpperCase());
        TreeItem<Entry<String, Object>> storeItem = table.addEntry("Store");
        table.addEntry(storeItem, "Name", content.storeName);
        table.addEntry(storeItem, "Type", content.storeType);
        if (content.url != null) {
            TreeItem<Entry<String, Object>> urlItem = table.addEntry("URL",
                    content.url);
            table.addEntry(urlItem, "Managed", content.urlManaged);
        }
        return table;
    }

}
