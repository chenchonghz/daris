package daris.client.ui.nifti.papaya;

import arc.gui.window.Window;
import arc.gui.window.WindowProperties;
import arc.mf.object.ObjectResolveHandler;
import daris.client.model.dataset.DataSet;
import daris.client.model.object.DObject;
import daris.client.model.object.DObjectRef;
import daris.client.model.object.MimeTypes;
import daris.client.ui.nifti.papaya.params.Params;

public class PapayaViewerDialog {

    public static void show(Window owner, double width, double height, String imageUrl) {
        WindowProperties wp = new WindowProperties();
        wp.setOwnerWindow(owner);
        wp.setSize(width, height);
        wp.setCanBeClosed(true);
        wp.setCanBeMaximised(true);
        wp.setCanBeMoved(true);
        wp.setCanBeResized(true);
        wp.setCenterInPage(true);
        arc.gui.gwt.widget.window.Window win = arc.gui.gwt.widget.window.Window.create(wp);
        PapayaViewer pv = new PapayaViewer(new Params().addImage(imageUrl));
        win.setContent(pv.widget());
        win.show();
        pv.resizeComponents();
    }

    public static void showImage(Window owner, String imageUrl) {
        show(owner, 0.7, 0.7, imageUrl);
    }

    public static void show(Window owner, double width, double height, DataSet dataset) {
        if (dataset != null) {
            if (dataset.data() != null && MimeTypes.NIFTI_SERIES.equals(dataset.mimeType())
                    && dataset.contentDownloadUrl() != null) {
                show(owner, width, height, dataset.contentDownloadUrl());
            }
        }
    }

    public static void show(Window owner, DataSet dataset) {
        show(owner, 0.7, 0.7, dataset);
    }

    public static void show(final Window owner, final double width, final double height, DObjectRef dataset) {
        if (dataset != null) {
            dataset.resolve(new ObjectResolveHandler<DObject>() {

                @Override
                public void resolved(DObject o) {
                    if (o != null && o instanceof DataSet) {
                        DataSet d = (DataSet) o;
                        show(owner, width, height, d);
                    }
                }
            });
        }
    }

    public static void show(Window owner, DObjectRef dataset) {
        show(owner, 0.7, 0.7, dataset);
    }

}
