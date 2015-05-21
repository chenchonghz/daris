package daris.client.ui.nifti.papaya;

import arc.gui.gwt.widget.ContainerWidget;
import arc.gui.gwt.widget.ResizeListener;
import arc.gui.gwt.widget.panel.AbsolutePanel;
import arc.gui.gwt.widget.panel.SimplePanel;

import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.user.client.ui.Widget;

public class RatioPanel extends ContainerWidget {

    private AbsolutePanel _ap;
    private SimplePanel _sp;
    private double _ratio;

    public RatioPanel() {
        this(1.0);
    }

    public RatioPanel(double ratio) {
        assert ratio > 0;
        _ratio = ratio;
        _ap = new AbsolutePanel();
        _sp = new SimplePanel();
        _sp.setPosition(Position.ABSOLUTE);
        _ap.add(_sp);
        initWidget(_ap);
    }

    public void setRatio(double ratio) {
        assert ratio > 0;
        if (_ratio != ratio) {
            _ratio = ratio;
            resizeByRatio();
        }
    }

    private void resizeByRatio() {
        assert _ratio > 0;
        int pw = width();
        int ph = height();
        if (pw == 0 || ph == 0) {
            return;
        }
        _sp.setPosition(Position.ABSOLUTE);
        double pratio = (double) pw / (double) ph;
        if (pratio > _ratio) {
            int w = (int) (ph * _ratio);
            _sp.setWidth(w);
            _sp.setHeight(ph);
            _sp.setLeft((pw - w) / 2);
            _sp.setTop(0);
        } else {
            int h = (int) (pw / _ratio);
            _sp.setWidth(pw);
            _sp.setHeight(h);
            _sp.setLeft(0);
            _sp.setTop((ph - h) / 2);
        }
    }

    public void setContent(Widget w) {
        _sp.setContent(w);
    }

    public void clear() {
        _sp.clear();
    }

    @Override
    protected void doAdd(Widget w, boolean doLayout) {
        _sp.add(w);
    }

    @Override
    protected boolean doRemove(Widget w, boolean doLayout) {
        return _sp.remove(w);
    }

    @Override
    protected void doLayoutChildren() {

        super.doLayoutChildren();
        resizeByRatio();
    }

    @SuppressWarnings("deprecation")
    public com.google.gwt.user.client.Element getElement() {
        return _sp.getElement();
    }

    @Override
    public void addResizeListener(final ResizeListener rl) {
        if (rl == null) {
            return;
        }
        _sp.addResizeListener(new ResizeListener() {

            @Override
            public Widget widget() {
                if (rl.widget() == null) {
                    return _sp;
                } else {
                    return rl.widget();
                }
            }

            @Override
            public void resized(long w, long h) {
                rl.resized(w, h);
            }
        });
    }

}
