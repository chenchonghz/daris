package daris.client.ui.theme;

import arc.gui.gwt.colour.Colour;
import arc.gui.gwt.colour.RGB;
import arc.gui.gwt.style.Style;
import arc.gui.gwt.style.StyleSet;
import arc.gui.gwt.theme.StandardTheme;
import arc.gui.gwt.theme.ThemeRegistry;
import arc.gui.gwt.widget.image.LinearGradient;

public class Theme extends StandardTheme {

    // private static final Style LIST_SELECT = new StyleSet(new BackgroundColour("#D6E8FF"), new ForegroundColour(
    // "#333"));
    
    // TODO: check if needed any more.

    private static Style LIST_SELECT;

    public Style listSelect() {
        if (LIST_SELECT == null) {
            LIST_SELECT = StyleSet.INSTANCE.createNewStyle("action_highlight");

            Colour base = new RGB(0x32, 0x77, 0xdf);

            LIST_SELECT.setBackgroundImage(new LinearGradient(LinearGradient.Orientation.TOP_TO_BOTTOM, new Colour[] {
                    base, base.lighter(0.1), base }));

            LIST_SELECT.setForegroundColour(RGB.WHITE);
        }
        return LIST_SELECT;
    }

    private static Theme _theme = new Theme();
    private static boolean _initialized = false;

    public static void initialize() {
        if (!_initialized) {
            new ThemeRegistry().setCurrentTheme(_theme);
        }
    }

}
