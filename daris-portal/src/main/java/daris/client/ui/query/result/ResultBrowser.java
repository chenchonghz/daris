package daris.client.ui.query.result;

import java.util.List;

import arc.gui.InterfaceComponent;
import arc.gui.gwt.object.ObjectDetailedView;
import arc.gui.gwt.widget.ContainerWidget;
import arc.gui.gwt.widget.dialog.Dialog;
import arc.gui.gwt.widget.event.SelectionHandler;
import arc.gui.gwt.widget.panel.HorizontalSplitPanel;
import arc.gui.gwt.widget.panel.SimplePanel;
import arc.mf.client.util.Action;
import arc.mf.object.ObjectRef;

import com.google.gwt.user.client.ui.Widget;

import daris.client.model.query.ResultCollectionRef;
import daris.client.model.query.messages.QueryResult;
import daris.client.model.query.messages.QueryResultExport;
import daris.client.model.query.messages.QueryResultExport.Format;
import daris.client.ui.DObjectGUIRegistry;
import daris.client.ui.object.action.DicomSendAction;
import daris.client.ui.query.action.ResultExportAction;
import daris.client.ui.widget.MessageBox;

public class ResultBrowser<T extends ObjectRef<?>> extends ContainerWidget
        implements InterfaceComponent {

    private ResultCollectionRef<T> _rc;

    private SimplePanel _sp;
    private ResultNavigator<T> _nav;
    private ObjectDetailedView _dv;
    private boolean _showDV;

    public ResultBrowser(ResultCollectionRef<T> rc, boolean showDetailedView) {
        _rc = rc;
        _showDV = showDetailedView;

        _sp = new SimplePanel();
        _sp.fitToParent();

        updateGUI(true);

        initWidget(_sp);

        _nav.gotoOffset(0);
    }

    public ResultCollectionRef<T> collection() {
        return _rc;
    }

    public boolean showDetailedView() {
        return _showDV;
    }

    public void setShowDetailedView(boolean showDetailedView) {
        if (_showDV != showDetailedView) {
            _showDV = showDetailedView;
            updateGUI(false);
        }
    }

    private void updateGUI(boolean refreshPage) {
        _sp.clear();
        if (_nav == null) {
            _nav = ResultNavigator.create(_rc);
            _nav.addSelectionHandler(new SelectionHandler<T>() {

                @Override
                public void selected(T o) {
                    if (_dv != null && _showDV) {
                        _dv.loadAndDisplayObject(o, false);
                    }
                }

                @Override
                public void deselected(T o) {
                    if (_dv != null && _showDV) {
                        _dv.clear();
                    }
                }
            });
        }
        if (_showDV) {
            HorizontalSplitPanel hsp = new HorizontalSplitPanel();
            hsp.fitToParent();
            _nav.setPreferredWidth(0.35);
            hsp.add(_nav);

            _dv = new ObjectDetailedView();
            _dv.setObjectRegistry(DObjectGUIRegistry.get());
            _dv.fitToParent();
            hsp.add(_dv);

            _sp.setContent(hsp);
            if (refreshPage) {
                _nav.refreshPage();
            } else {
                // Re-fire event to refresh the detailed view
                _nav.notifyOfSelectionInPage(_nav.selected());
            }
        } else {
            _dv = null;
            _nav.fitToParent();
            _sp.setContent(_nav);
        }
    }

    @Override
    public Widget gui() {
        return this;
    }

    public void downloadSelected() {
        if (_nav.selected() == null) {
            return;
        }
        QueryResult.addToShoppingCart(_nav.selected(), new Action() {

            @Override
            public void execute() {
                MessageBox.info("Add to shopping-cart", _nav.selected()
                        .referentTypeName()
                        + " "
                        + _nav.selected().idToString()
                        + " has been added to shopping-cart", 2);
            }
        });
    }

    public void downloadCurrentPage() {
        List<T> ros = _nav.dataInCurrentPage();
        if (ros == null || ros.isEmpty()) {
            return;
        }
        QueryResult.addToShoppingCart(ros, new Action() {

            @Override
            public void execute() {
                MessageBox
                        .info("Add to shopping-cart",
                                "Results in the current page have been added to shopping-cart",
                                2);
            }
        });
    }

    public void downloadAll(boolean includeContainingDataSets) {
        if (_rc.totalNumberOfMembers() <= 0) {
            return;
        }
        try {
            QueryResult.addToShoppingCart(_rc, includeContainingDataSets,
                    new Action() {

                        @Override
                        public void execute() {
                            MessageBox
                                    .info("Add to shopping-cart",
                                            "All results have been added to shopping-cart",
                                            2);
                        }
                    });
        } catch (Throwable e) {
            Dialog.warn("Error", e.getMessage());
            e.printStackTrace();
        }
    }

    public void exportCurrentPageToXML() {
        new ResultExportAction(new QueryResultExport(_rc, _nav.offset(),
                _nav.pageSize(), Format.xml, null), window()).execute();
    }

    public void exportAllToXML() {
        new ResultExportAction(new QueryResultExport(_rc, Format.xml, null),
                window()).execute();
    }

    public void exportCurrentPageToCSV() {
        new ResultExportAction(new QueryResultExport(_rc, _nav.offset(),
                _nav.pageSize(), Format.csv, null), window()).execute();
    }

    public void exportAllToCSV() {
        new ResultExportAction(new QueryResultExport(_rc, Format.csv, null),
                window()).execute();
    }

    public void dicomSendAll() {
        new DicomSendAction(_rc.query(), window()).execute();
    }

}
