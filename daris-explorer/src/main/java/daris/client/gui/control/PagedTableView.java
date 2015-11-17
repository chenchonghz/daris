package daris.client.gui.control;

import java.util.List;

import arc.mf.desktop.ui.util.ApplicationThread;
import arc.mf.object.CollectionResolveHandler;
import arc.mf.object.OrderedCollectionRef;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.Pagination;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableView;

public abstract class PagedTableView<T> extends Pagination {

    private TableView<T> _table;
    private OrderedCollectionRef<T> _collection;

    public PagedTableView(OrderedCollectionRef<T> collection) {
        _collection = collection;
        _table = new TableView<T>();
        addTableColumns(_table);
        setPageFactory(index -> {
            goToPage(index);
            return _table;
        });
        setCurrentPageIndex(0);
    }

    protected abstract void addTableColumns(TableView<T> table);

    public void addTableSelectionListener(ChangeListener<T> l) {
        _table.getSelectionModel().selectedItemProperty().addListener(l);
    }

    public void setTableSelectionMode(SelectionMode mode) {
        _table.getSelectionModel().setSelectionMode(mode);
    }

    private void goToPage(int index) {
        int start = index * _collection.pagingSize();
        int end = start + _collection.pagingSize();
        _collection.resolve(start, end, new CollectionResolveHandler<T>() {
            @Override
            public void resolved(List<T> os) throws Throwable {
                ApplicationThread.execute(() -> {
                    _table.getItems().setAll(os);
                    long total = _collection.totalNumberOfMembers();
                    int pageSize = _collection.pagingSize();
                    int nbPages = (int) (total / pageSize);
                    if (total % pageSize != 0) {
                        nbPages++;
                    }
                    setPageCount(nbPages);
                });
            }
        });
    }

}
