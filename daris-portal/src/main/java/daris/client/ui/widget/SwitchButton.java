package daris.client.ui.widget;

import arc.gui.gwt.colour.Colour;
import arc.gui.gwt.colour.RGB;
import arc.gui.gwt.widget.HTML;
import arc.gui.gwt.widget.image.LinearGradient;
import arc.gui.gwt.widget.panel.AbsolutePanel;
import arc.gui.menu.ActionEntry;

import com.google.gwt.dom.client.Style.BorderStyle;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

public class SwitchButton extends AbsolutePanel {

    public static final Colour BG_COLOR_ENABLED = new RGB(0xa0, 0xa0, 0xa0);
    public static final Colour BG_COLOR_ENABLED_LIGHT = new RGB(0xb1, 0xb1, 0xb1);
    public static final Colour BORDER_COLOR_ENABLED = new RGB(0x23, 0x23, 0x23);
    public static final Colour FONT_COLOR_ENABLED = RGB.WHITE;

    public static final Colour BG_COLOR_DISABLED = new RGB(0xe1, 0xe1, 0xe1);
    public static final Colour BG_COLOR_DISABLED_LIGHT = new RGB(0xf2, 0xf2, 0xf2);
    public static final Colour BORDER_COLOR_DISABLED = new RGB(0xa2, 0xa2, 0xa2);
    public static final Colour FONT_COLOR_DISABLED = new RGB(0x66, 0x66, 0x66);

    public static final int BORDER_RADIUS = 5;
    public static final int DEFAULT_WIDTH = 120;
    public static final int DEFAULT_HEIGHT = 20;
    public static final int DEFAULT_FONT_SIZE = 12;

    private AbsolutePanel _onAP;
    private HTML _onText;
    private AbsolutePanel _offAP;
    private HTML _offText;
    private boolean _on;
    private ActionEntry _onAction;
    private ActionEntry _offAction;

    public SwitchButton(ActionEntry onAction, ActionEntry offAction, boolean on) {

        _onAction = onAction;
        _offAction = offAction;

        setOverflow(Overflow.VISIBLE);
        setHeight(DEFAULT_HEIGHT);
        setWidth(DEFAULT_WIDTH);
        setBackgroundImage(new LinearGradient(LinearGradient.Orientation.TOP_TO_BOTTOM, BG_COLOR_DISABLED,
                BG_COLOR_DISABLED_LIGHT));
        // setBorder(1, BorderStyle.SOLID, BORDER_COLOR_DISABLED);
        setBorderRadius(BORDER_RADIUS);

        _onText = new HTML(_onAction.label());
        _onText.setFontSize(DEFAULT_FONT_SIZE);
        _onText.setFontFamily("Helvetica");
        _onText.setPosition(Position.ABSOLUTE);
        _onText.setToolTip(_onAction.description());
        _onAP = new AbsolutePanel() {
            protected void doLayoutChildren() {
                super.doLayoutChildren();
                int w = width();
                int h = height();
                _onText.setLeft(w / 2 - _onText.width() / 2);
                _onText.setTop(h / 2 - _onText.height() / 2);
            }
        };
        _onAP.setBorderRadiusTopLeft(BORDER_RADIUS);
        _onAP.setBorderRadiusBottomLeft(BORDER_RADIUS);
        _onAP.add(_onText);
        _onAP.setPosition(Position.ABSOLUTE);
        _onAP.setLeft(0);
        _onAP.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                if (!_on) {
                    if (_onAction.action() != null) {
                        _onAction.action().execute();
                    }
                    _on = true;
                    updateState();
                }
            }
        });

        add(_onAP);

        _offText = new HTML(_offAction.label());
        _offText.setFontSize(DEFAULT_FONT_SIZE);
        _offText.setFontFamily("Helvetica");
        _offText.setPosition(Position.ABSOLUTE);
        _offText.setToolTip(_offAction.description());
        _offAP = new AbsolutePanel() {
            protected void doLayoutChildren() {
                super.doLayoutChildren();
                int w = width();
                int h = height();
                _offText.setLeft(w / 2 - _offText.width() / 2);
                _offText.setTop(h / 2 - _offText.height() / 2);
            }
        };
        _offAP.setBorderRadiusTopRight(BORDER_RADIUS);
        _offAP.setBorderRadiusBottomRight(BORDER_RADIUS);
        _offAP.add(_offText);
        _offAP.setPosition(Position.ABSOLUTE);
        _offAP.setRight(0);
        _offAP.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                if (_on) {
                    if (_offAction.action() != null) {
                        _offAction.action().execute();
                    }
                    _on = false;
                    updateState();
                }
            }
        });

        add(_offAP);

        _on = on;
        updateState();

    }

    private void updateState() {

        if (_on) {
            _offAP.style().removeBorder();
            _offAP.style().setBackgroundImage(null);

            _offAP.setCursor(Cursor.POINTER);
            _offText.setColour(FONT_COLOR_DISABLED);
            _offText.setCursor(Cursor.POINTER);
            _onAP.setBackgroundImage(new LinearGradient(LinearGradient.Orientation.TOP_TO_BOTTOM, BG_COLOR_ENABLED,
                    BG_COLOR_ENABLED_LIGHT));
            _onAP.setBorder(1, BorderStyle.SOLID, BORDER_COLOR_ENABLED);
            _onAP.setCursor(Cursor.DEFAULT);
            _onText.setColour(FONT_COLOR_ENABLED);
            _onText.setCursor(Cursor.DEFAULT);
        } else {
            _onAP.style().setBackgroundImage(null);
            _onAP.style().removeBorder();
            _onAP.setCursor(Cursor.POINTER);
            _onText.setColour(FONT_COLOR_DISABLED);
            _onText.setCursor(Cursor.POINTER);
            _offAP.setBackgroundImage(new LinearGradient(LinearGradient.Orientation.TOP_TO_BOTTOM, BG_COLOR_ENABLED,
                    BG_COLOR_ENABLED_LIGHT));
            _offAP.setBorder(1, BorderStyle.SOLID, BORDER_COLOR_ENABLED);
            _offAP.setCursor(Cursor.DEFAULT);
            _offText.setColour(FONT_COLOR_ENABLED);
            _offText.setCursor(Cursor.DEFAULT);
        }
    }

    @Override
    protected void doLayoutChildren() {
        super.doLayoutChildren();

        int w = widthWithMargins() / 2;
        int h = heightWithMargins() - 2;
        _onAP.setWidth(w);
        _onAP.setHeight(h);
        _onAP.setLeft(0);
        _offAP.setWidth(w);
        _offAP.setHeight(h);
        _offAP.setRight(0);
    }

}
