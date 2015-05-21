package daris.client.ui.widget;

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

import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.user.client.ui.Widget;

public class ImagePanel extends ContainerWidget {

    private AbsolutePanel _ap;
    private String _loadingMessage;

    public ImagePanel() {
        this(null);
    }

    public ImagePanel(String url) {

        _ap = new AbsolutePanel();
        _ap.fitToParent();
        _ap.setOverflow(Overflow.HIDDEN);
        initWidget(_ap);
        if (url != null) {
            setImage(url);
        }
    }

    public void showLoadingMessage(String loadingMessage) {
        _loadingMessage = loadingMessage;
        showLoadingMessage();
    }

    private void showLoadingMessage() {
        if (_loadingMessage != null) {
            HTML msg = new HTML(_loadingMessage);
            msg.setFontSize(10);
            msg.setPosition(Position.ABSOLUTE);
            add(msg);
        }
    }

    public void setImage(String url) {

        if (url == null) {
            clear();
        }
        showLoadingMessage();
        preLoad();
        Image i = new Image(url);
        new ImageLoader(i, new ImageLoadHandler() {

            @Override
            public void loaded(ImageWidget i) {
                i.setPosition(Position.ABSOLUTE);
                add(i);
                postLoad();

            }
        }).load();
    }

    public void clear() {
        for (Widget cw : children()) {
            remove(cw);
        }
    }

    protected void preLoad() {

    }

    protected void postLoad() {

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
