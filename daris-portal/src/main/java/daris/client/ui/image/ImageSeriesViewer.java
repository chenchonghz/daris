package daris.client.ui.image;

import arc.gui.InterfaceComponent;
import arc.gui.gwt.widget.BaseWidget;

import com.google.gwt.user.client.ui.Widget;

public abstract class ImageSeriesViewer implements InterfaceComponent {

    public static enum InitialPosition {
        FIRST, MIDDLE, LAST
    }

    private InitialPosition _initialPosition;

    protected ImageSeriesViewer(InitialPosition initialPosition) {
        _initialPosition = initialPosition;
    }

    protected InitialPosition initialPosition() {
        return _initialPosition;
    }
    
    @Override
    public Widget gui(){
        return widget();
    }
    
    public abstract BaseWidget widget();
}
