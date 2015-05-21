package daris.client.ui.widget;

import arc.gui.gwt.colour.RGB;
import arc.gui.gwt.widget.HTML;
import arc.gui.gwt.widget.image.Image;
import arc.gui.gwt.widget.panel.HorizontalPanel;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.gui.gwt.widget.popup.PopupPanel;

import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.PopupPanel.PositionCallback;

import daris.client.Resource;

public class MessageBox {

    public static final String ICON_INFO = Resource.INSTANCE.info48().getSafeUri().asString();
    public static final String ICON_ERROR = Resource.INSTANCE.error48().getSafeUri().asString();
    public static final String ICON_WARNING = Resource.INSTANCE.warning48().getSafeUri().asString();

    public static enum Position {
        TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT, CENTER
    }

    public static enum Type {
        info, error, warning
    }

    public static Image iconFor(Type type) {
        switch (type) {
        case info:
            return new Image(ICON_INFO, 48, 48);
        case error:
            return new Image(ICON_ERROR, 48, 48);
        case warning:
            return new Image(ICON_WARNING, 48, 48);
        default:
            return null;
        }
    }

    public static class BasePoint {

        private Position _position;
        private int _x;
        private int _y;

        public BasePoint(Position position, int x, int y) {
            _position = position;
            _x = x;
            _y = y;
        }

        public int x() {
            return _x;
        }

        public int y() {
            return _y;
        }

        public int topLeftX(int offsetWidth, int offsetHeight, int clientWidth, int clientHeight) {
            int tlx = 0;
            switch (_position) {
            case TOP_LEFT:
                tlx = _x;
                break;
            case TOP_RIGHT:
                tlx = _x - offsetWidth;
                break;
            case BOTTOM_LEFT:
                tlx = _x;
                break;
            case BOTTOM_RIGHT:
                tlx = _x - offsetWidth;
                break;
            case CENTER:
                tlx = _x - offsetWidth / 2;
                break;
            }
            if (tlx < 0) {
                tlx = 0;
            }
            if (tlx + offsetWidth > clientWidth) {
                tlx = clientWidth - offsetWidth;
            }
            return tlx;
        }

        public int topLeftY(int offsetWidth, int offsetHeight, int clientWidth, int clientHeight) {
            int tly = 0;
            switch (_position) {
            case TOP_LEFT:
                tly = _y;
                break;
            case TOP_RIGHT:
                tly = _y;
                break;
            case BOTTOM_LEFT:
                tly = _y - offsetHeight;
                break;
            case BOTTOM_RIGHT:
                tly = _y - offsetHeight;
                break;
            case CENTER:
                tly = _y - offsetHeight / 2;
                break;
            }
            if (tly < 0) {
                tly = 0;
            }
            if (tly + offsetHeight > clientHeight) {
                tly = clientHeight - offsetHeight;
            }
            return tly;
        }
    }

    private static PopupPanel _pp;

    private static void display(Type type, final BasePoint bp, String title, String message, int seconds) {

        if (_pp != null) {
            _pp.hide();
        }
        _pp = new PopupPanel();
        _pp.setAutoHideEnabled(true);
        // _pp.setWidth(MIN_WIDTH);
        // _pp.setHeight(MIN_HEIGHT);

        VerticalPanel vp = new VerticalPanel();
        vp.fitToParent();
        vp.setPadding(5);
        vp.setOpacity(0.9);
        vp.setBackgroundColour(new RGB(0x99, 0x99, 0x99));
        vp.setBorder(1, new RGB(0xee, 0xee, 0xee));
        vp.setBorderRadius(3);

        HorizontalPanel hp1 = new HorizontalPanel();
        hp1.setHeight(22);
        hp1.setPaddingLeft(5);
        vp.add(hp1);

        HTML titleHtml = new HTML(title);
        titleHtml.setHeight100();
        titleHtml.setFontSize(12);
        titleHtml.setFontWeight(FontWeight.BOLD);
        hp1.add(titleHtml);

        HorizontalPanel hp2 = new HorizontalPanel();
        hp2.fitToParent();
        vp.add(hp2);

        Image icon = iconFor(type);
        hp2.add(icon);

        HTML messageHtml = new HTML(message);
        hp2.add(messageHtml);

        _pp.setContent(vp);
        _pp.setPopupPositionAndShow(new PositionCallback() {

            @Override
            public void setPosition(int offsetWidth, int offsetHeight) {

                int clientWidth = com.google.gwt.user.client.Window.getClientWidth();
                int clientHeight = com.google.gwt.user.client.Window.getClientHeight();
                int tlx = bp.topLeftX(offsetWidth, offsetHeight, clientWidth, clientHeight);
                int tly = bp.topLeftY(offsetWidth, offsetHeight, clientWidth, clientHeight);
                _pp.setPopupPosition(tlx, tly);
            }
        });
        _pp.show();
        final PopupPanel pp = _pp;
        Timer timer = new Timer() {
            @Override
            public void run() {

                pp.hide();
                cancel();
            }
        };
        timer.schedule(seconds * 1000);
    }

    public static void display(Type type, Position position, String title, String message, int seconds) {
        int clientWidth = com.google.gwt.user.client.Window.getClientWidth();
        int clientHeight = com.google.gwt.user.client.Window.getClientHeight();
        BasePoint bp;
        switch (position) {
        case TOP_LEFT:
            bp = new BasePoint(Position.TOP_LEFT, 0, 0);
            break;
        case TOP_RIGHT:
            bp = new BasePoint(Position.TOP_RIGHT, clientWidth, 0);
            break;
        case CENTER:
            bp = new BasePoint(Position.CENTER, clientWidth / 2, clientHeight / 2);
            break;
        case BOTTOM_LEFT:
            bp = new BasePoint(Position.BOTTOM_LEFT, 0, clientHeight);
            break;
        case BOTTOM_RIGHT:
            bp = new BasePoint(Position.BOTTOM_RIGHT, clientWidth, clientHeight);
            break;
        default:
            bp = new BasePoint(Position.CENTER, clientWidth / 2, clientHeight / 2);
            break;
        }
        display(type, bp, title, message, seconds);
    }

    public static void display(Type type, String title, String message, int seconds) {
        display(type, Position.CENTER, title, message, seconds);
    }

    public static void error(String title, String message, int seconds) {
        display(Type.error, title, message, seconds);
    }

    public static void warn(String title, String message, int seconds) {
        display(Type.warning, title, message, seconds);
    }

    public static void info(String title, String message, int seconds) {
        display(Type.info, title, message, seconds);
    }
}
