package daris.client.gui.dataset;

import java.net.URI;
import java.util.Map.Entry;

import arc.mf.client.util.UnhandledException;
import daris.client.gui.object.DObjectView;
import daris.client.gui.xml.KVTreeTableView;
import daris.client.model.dataset.DataSet;
import daris.client.model.dataset.DerivedDataSet;
import daris.client.model.dataset.PrimaryDataSet;
import daris.client.model.mime.MimeTypes;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TreeItem;
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
            dicomViewerTab = new Tab("DICOM", dicomViewerPane);
            ContextMenu menu = new ContextMenu();
            MenuItem item = new MenuItem("Open with default web browser...");
            item.setOnAction(event -> {
                try {
                    java.awt.Desktop.getDesktop()
                            .browse(new URI(dataset.dicomViewerUrl()));
                } catch (Throwable e) {
                    UnhandledException.report(
                            "Opening DICOM Viewer in default web browser", e);
                }
            });
            menu.getItems().add(item);
            dicomViewerTab.setContextMenu(menu);
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
            niftiViewerTab = new Tab("NIFTI", niftiViewerPane);
            ContextMenu menu = new ContextMenu();
            MenuItem item = new MenuItem("Open with default web browser...");
            item.setOnAction(event -> {
                try {
                    java.awt.Desktop.getDesktop()
                            .browse(new URI(dataset.niftiViewerUrl()));
                } catch (Throwable e) {
                    UnhandledException.report(
                            "Opening NIFTI Viewer in default web browser", e);
                }
            });
            menu.getItems().add(item);
            niftiViewerTab.setContextMenu(menu);
            getTabs().add(niftiViewerTab);
        }

    }

    @Override
    protected void addInterfaceMetadata(KVTreeTableView<String, Object> table,
            DataSet dataset) {
        super.addInterfaceMetadata(table, dataset);

        table.addEntry("VID", dataset.dataSetVid());
        table.addEntry("Source Type", dataset.sourceType());
        if (dataset.mimeType() != null
                && !MimeTypes.CONTENT_UNKNOWN.equals(dataset.mimeType())) {
            table.addEntry("MIME Type", dataset.mimeType());
        }

        if (dataset instanceof PrimaryDataSet) {
            PrimaryDataSet pds = ((PrimaryDataSet) dataset);
            if (pds.subjectId() != null) {
                TreeItem<Entry<String, Object>> item = table.addEntry("Subject",
                        pds.subjectId());
                if (pds.subjectState() != null) {
                    table.addEntry(item, "State", pds.subjectState());
                }
            }
        } else {
            DerivedDataSet dds = ((DerivedDataSet) dataset);
            if (dds.inputs() != null && !dds.inputs().isEmpty()) {
                TreeItem<Entry<String, Object>> item = table
                        .addEntry("Derivation");
                for (DerivedDataSet.Input input : dds.inputs()) {
                    TreeItem<Entry<String, Object>> inputItem = table
                            .addEntry(item, "Input", input.cid);
                    table.addEntry(inputItem, "VID", input.vid);
                }
            }
            if (dds.processed()) {
                table.addEntry("Processed", dds.processed());
            }
        }

        if (dataset.method() != null) {
            TreeItem<Entry<String, Object>> item = table.addEntry("method",
                    dataset.method().citeableId());
            if (dataset.step() != null) {
                table.addEntry(item, "step", dataset.step());
            }
        }

        if (dataset.fileName() != null) {
            table.addEntry("Original File Name", dataset.fileName());
        }

    }

}
