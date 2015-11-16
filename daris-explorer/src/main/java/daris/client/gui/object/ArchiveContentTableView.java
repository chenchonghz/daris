package daris.client.gui.object;

import daris.client.gui.control.PagedTableView;
import daris.client.model.archive.ArchiveEntry;
import daris.client.model.archive.ArchiveEntryCollectionRef;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class ArchiveContentTableView extends PagedTableView<ArchiveEntry> {

    public ArchiveContentTableView(ArchiveEntryCollectionRef collection) {
        super(collection);
    }

    @Override
    protected void addTableColumns(TableView<ArchiveEntry> table) {
        TableColumn<ArchiveEntry, String> ordinalColumn = new TableColumn<ArchiveEntry, String>(
                "Ordinal");
        ordinalColumn.setCellValueFactory(cellData -> {
            return new ReadOnlyStringWrapper(
                    Integer.toString(cellData.getValue().ordinal()));
        });
        table.getColumns().add(ordinalColumn);
        TableColumn<ArchiveEntry, String> sizeColumn = new TableColumn<ArchiveEntry, String>(
                "Size(Bytes)");
        sizeColumn.setCellValueFactory(cellData -> {
            long size = cellData.getValue().size();
            return new ReadOnlyStringWrapper(
                    size < 0 ? null : Long.toString(size));
        });
        table.getColumns().add(sizeColumn);
        TableColumn<ArchiveEntry, String> nameColumn = new TableColumn<ArchiveEntry, String>(
                "Name");
        nameColumn.setCellValueFactory(cellData -> {
            return new ReadOnlyStringWrapper(cellData.getValue().name());
        });
        table.getColumns().add(nameColumn);
    }

}
