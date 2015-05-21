package daris.client.ui.image;

import java.util.List;
import java.util.Vector;

import arc.gui.gwt.widget.BaseWidget;
import arc.gui.gwt.widget.ContainerWidget;
import arc.gui.gwt.widget.HTML;
import arc.gui.gwt.widget.image.Image;
import arc.gui.gwt.widget.image.ImageLoadHandler;
import arc.gui.gwt.widget.image.ImageLoader;
import arc.gui.gwt.widget.panel.AbsolutePanel;
import arc.gui.image.ImageWidget;
import arc.mf.client.util.ActionListener;
import arc.mf.object.ObjectMessage;
import arc.mf.object.ObjectMessageResponse;

import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.user.client.ui.Widget;

import daris.client.model.image.RemoteImage;

public abstract class ImageSeriesPanel<T extends RemoteImage> extends ContainerWidget {

    private String _assetId;
    private int _index;
    private String _loadingMessage;

    private AbsolutePanel _ap;

    public ImageSeriesPanel(String assetId, int index) {

        _assetId = assetId;
        _index = index;

        _ap = new AbsolutePanel();
        _ap.fitToParent();
        _ap.setOverflow(Overflow.HIDDEN);
        initWidget(_ap);
    }

    public String assetId() {
        return _assetId;
    }

    public int index() {
        return _index;
    }

    protected void setIndex(int index) {
        _index = index;
    }

    protected abstract ObjectMessage<T> retrieveService();

    protected void retrieveImage(final ActionListener al) {
        /*
         * Show loading message
         */
        HTML loadingMsg = new HTML(_loadingMessage);
        loadingMsg.setFontSize(10);
        loadingMsg.setPosition(Position.ABSOLUTE);
        add(loadingMsg);

        /*
         * Retrieve the remote image (url) then load it.
         */
        retrieveService().send(new ObjectMessageResponse<T>() {

            @Override
            public void responded(T ri) {
                if (ri != null) {
                    Image img = new Image(ri.url());
                    new ImageLoader(img, new ImageLoadHandler() {

                        @Override
                        public void loaded(ImageWidget i) {

                            i.setPosition(Position.ABSOLUTE);
                            add(i);
                            if (al != null) {
                                al.executed(true);
                            }
                        }
                    }).load();
                } else {
                    if (al != null) {
                        al.executed(false);
                    }
                }
            }
        });
    }

    protected String loadingMessage() {
        return _loadingMessage;
    }

    protected void setLoadingMessage(String message) {
        _loadingMessage = message;
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
