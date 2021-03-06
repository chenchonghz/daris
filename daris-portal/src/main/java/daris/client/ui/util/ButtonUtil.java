package daris.client.ui.util;

import arc.gui.gwt.widget.button.Button;
import arc.gui.gwt.widget.button.ButtonBar;
import arc.gui.gwt.widget.image.LinearGradient;
import arc.gui.gwt.widget.list.ListGridHeader;
import arc.gui.gwt.widget.menu.MenuButton;
import arc.gui.image.Image;
import arc.gui.menu.Menu;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;

public class ButtonUtil {

    public static Button createButton(String icon, int iconWidth, int iconHeight, String label, String description,
            boolean gradient, ClickHandler ch) {
        Button b = new Button("<div><img src=\"" + icon + "\" style=\"width:" + iconWidth + "px;height:" + iconHeight
                + "px;vertical-align:middle\"><span style=\"\">&nbsp;" + label + "</span></div>", gradient);
        b.setBorderRadius(3);
        b.setFontSize(10);
        b.addClickHandler(ch);
        if (description != null) {
            b.setToolTip(description);
        }
        return b;
    }

    public static Button createButton(Image icon, String label, String description, boolean gradient, ClickHandler ch) {
        return createButton(icon.path(), icon.width(), icon.height(), label, description, gradient, ch);
    }

    public static Button createButton(ImageResource icon, String label, String description, boolean gradient,
            ClickHandler ch) {
        return createButton(icon.getSafeUri().asString(), icon.getWidth(), icon.getHeight(), label, description,
                gradient, ch);
    }

    public static Button createImageButton(String imgURL, int imgWidth, int imgHeight) {
        Button button = new Button("<img width=\"" + imgWidth + "px\" height=\"" + imgHeight + "px\" src=\"" + imgURL
                + "\" style=\"vertical-align: middle; padding-top: 0px;\"></img>", false);
        button.setWidth(imgWidth + 10);
        return button;
    }

    public static Button createImageButton(ImageResource ir, int imgWidth, int imgHeight) {
        return createImageButton(ir.getSafeUri().asString(), imgWidth, imgHeight);
    }

    public static Button createImageButton(ImageResource ir) {
        return createImageButton(ir, ir.getWidth(), ir.getHeight());
    }

    public static MenuButton createMenuButton(String icon, int iconWidth, int iconHeight, String label, Menu menu) {
        MenuButton mb = menu != null ? new MenuButton(menu) : new MenuButton(new Menu());
        String mbText = label != null ? label : menu.label();
        mb.setHTML("<div><img src=\"" + icon + "\" style=\"width:" + iconWidth + "px;height:" + iconHeight
                + "px;vertical-align:middle\"><span style=\"font-size:1em;\">&nbsp;" + mbText + "</span></div>");
        return mb;
    }

    public static MenuButton createMenuButton(arc.gui.image.Image icon, String label, Menu menu) {
        return createMenuButton(icon.path(), icon.width(), icon.height(), label, menu);
    }

    public static void setButtonLabel(Button button, String icon, int iconWidth, int iconHeight, String label) {
        button.setHTML("<div><img src=\"" + icon + "\" style=\"width:" + iconWidth + "px;height:" + iconHeight
                + "px;vertical-align:middle\"><span style=\"\">&nbsp;" + label + "</span></div>");
    }

    public static void setButtonLabel(Button button, arc.gui.image.Image icon, String label) {
        button.setHTML("<div><img src=\"" + icon.path() + "\" style=\"width:" + icon.width() + "px;height:" + icon.height()
                + "px;vertical-align:middle\"><span style=\"\">&nbsp;" + label + "</span></div>");

   }

    public static ButtonBar createButtonBar(ButtonBar.Position position, ButtonBar.Alignment align, int height) {
        ButtonBar bb = new ButtonBar(position, align);
        bb.setHeight(height);
        bb.setWidth100();
        bb.setColourEnabled(false);
        bb.setBackgroundImage(new LinearGradient(LinearGradient.Orientation.TOP_TO_BOTTOM,
                ListGridHeader.HEADER_COLOUR_LIGHT, ListGridHeader.HEADER_COLOUR_DARK));
        return bb;
    }

}
