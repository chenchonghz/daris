package daris.client.ui.archive;

import java.util.List;
import java.util.Vector;

import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.user.client.ui.Widget;

import arc.gui.gwt.widget.BaseWidget;
import arc.gui.gwt.widget.ContainerWidget;
import arc.gui.gwt.widget.HTML;
import arc.gui.gwt.widget.image.Image;
import arc.gui.gwt.widget.image.ImageLoadHandler;
import arc.gui.gwt.widget.image.ImageLoader;
import arc.gui.gwt.widget.panel.AbsolutePanel;
import arc.gui.image.ImageWidget;
import arc.mf.object.ObjectMessageResponse;
import daris.client.model.archive.ArchiveEntry;
import daris.client.model.archive.ArchiveEntryCollectionRef;
import daris.client.model.archive.ImageEntry;
import daris.client.model.archive.messages.ArchiveContentImageGet;

public class ArchiveEntryImagePanel extends ContainerWidget {

    private ArchiveEntryCollectionRef _arc;
    private ArchiveEntry _entry;

    private AbsolutePanel _ap;

    public ArchiveEntryImagePanel(ArchiveEntryCollectionRef arc, ArchiveEntry entry) {

        _arc = arc;
        _entry = entry;

        _ap = new AbsolutePanel();
        _ap.fitToParent();
        _ap.setOverflow(Overflow.HIDDEN);
        initWidget(_ap);

        loadImage();
    }

    private void loadImage() {
        /*
         * show loading message
         */
        HTML loadingMsg = new HTML("loading image: " + _entry.name());
        loadingMsg.setFontSize(10);
        loadingMsg.setPosition(Position.ABSOLUTE);
        add(loadingMsg);

        /*
         * load image
         */
        new ArchiveContentImageGet(_arc, _entry)
                .send(new ObjectMessageResponse<ImageEntry>() {

                    @Override
                    public void responded(ImageEntry ie) {
                        Image img = new Image(ie.outputUrl());
                        new ImageLoader(img, new ImageLoadHandler() {
                            @Override
                            public void loaded(ImageWidget i) {
                                i.setPosition(Position.ABSOLUTE);
                                add(i);
                            }
                        }).load();
                    }
                });

    }

    @Override
    protected void doAdd(Widget w, boolean doLayout) {

        if (children() != null) {
            List<Widget> rws = new Vector<Widget>();
            for (Widget cw : children()) {
                if (cw != w) {
                    rws.add(cw);
                }
            }
            for (Widget rw : rws) {
                remove(rw);
            }
        }
        _ap.add(w);
    }

    @Override
    protected boolean doRemove(Widget w, boolean doLayout) {

        return _ap.remove(w);
    }

    @Override
    protected void doLayoutChildren() {

        super.doLayoutChildren();
        for (BaseWidget w : children()) {
            if (w instanceof Image) {
                resizeAndPositionImage((Image) w);
            } else {
                // must be the loading message
                w.setLeft((width() - w.width()) / 2);
                w.setTop((height() - w.height()) / 2);
            }
        }
    }

    private void resizeAndPositionImage(Image i) {

        int w = width();
        int h = height();
        double ar = ((double) i.width()) / ((double) i.height());

        int nw = w;
        int nh = (int) (nw / ar);
        if (nh > h) {
            nh = h;
            nw = (int) (nh * ar);
        }
        i.resizeTo(nw, nh);
        if (nw == w) {
            i.setLeft(0);
            i.setTop((h - nh) / 2);
        } else {
            i.setLeft((w - nw) / 2);
            i.setTop(0);
        }
    }
}