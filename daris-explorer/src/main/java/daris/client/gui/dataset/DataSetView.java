package daris.client.gui.dataset;

import daris.client.gui.object.DObjectView;
import daris.client.gui.xml.KVTreeTableView;
import daris.client.model.dataset.DataSet;
import daris.client.model.mime.MimeTypes;
import javafx.scene.control.Tab;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;

public class DataSetView extends DObjectView<DataSet> {

    protected Tab dicomViewerTab;
    protected Tab niftiViewerTab;

    public DataSetView(DataSet dataset) {
        super(dataset);
        /*
         * dicom viewer tab
         */
        if (dataset.isDicomSeries()) {
            StackPane dicomViewerPane = new StackPane();
            WebView web = new WebView();
            web.getEngine().load(dataset.dicomViewerUrl());
            dicomViewerPane.getChildren().add(web);
            dicomViewerTab = new Tab("DCIOM", dicomViewerPane);
            getTabs().add(dicomViewerTab);
        }
        /*
         * nifti viewer tab
         */
        if (dataset.isNiftiSeries()) {
            StackPane niftiViewerPane = new StackPane();
            WebView web = new WebView();
            web.getEngine().load(dataset.niftiViewerUrl());
            niftiViewerPane.getChildren().add(web);
            niftiViewerTab = new Tab("DCIOM", niftiViewerPane);
            getTabs().add(niftiViewerTab);
        }
    }

    @Override
    protected void addInterfaceMetadata(KVTreeTableView<String, Object> table,
            DataSet dataset) {
        table.addEntry("Source Type", dataset.sourceType());
        if (dataset.mimeType() != null
                && !MimeTypes.CONTENT_UNKNOWN.equals(dataset.mimeType())) {
            table.addEntry("MIME Type", dataset.mimeType());
        }

        if (dataset.fileName() != null) {
            table.addEntry("Original File Name", dataset.fileName());
        }
    }

}
