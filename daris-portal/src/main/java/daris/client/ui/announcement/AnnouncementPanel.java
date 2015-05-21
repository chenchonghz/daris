package daris.client.ui.announcement;

import arc.gui.gwt.widget.HTML;
import arc.gui.gwt.widget.label.Label;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.gui.gwt.widget.scroll.ScrollPanel;
import arc.gui.gwt.widget.scroll.ScrollPolicy;

import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.i18n.client.DateTimeFormat;

import daris.client.model.announcement.Announcement;

public class AnnouncementPanel extends VerticalPanel {

    public AnnouncementPanel(Announcement o) {
        
        fitToParent();
        setPaddingTop(10);
        setPaddingLeft(30);
        setPaddingRight(30);
        setPaddingBottom(10);
        Label title = new Label("Announcement " + o.uid() + ": "
                + o.title());
        title.setFontWeight(FontWeight.BOLD);
        title.setFontSize(12);
        title.setHeight(20);
        add(title);
        
        Label date = new Label("Date: "
                + DateTimeFormat.getFormat("yyyy-MM-dd HH:mm:ss")
                        .format(o.created()));
        date.setFontSize(11);
        date.setHeight(20);
        add(date);
        
        HTML text = new HTML(
                "<div style=\"line-height:150%;text-align:justify;\">"
                        + o.text() + "</div>");
        text.setFontFamily("sans-serif");
        text.setFontSize(12);
        text.fitToParent();
        ScrollPanel sp = new ScrollPanel(text, ScrollPolicy.AUTO);
        sp.fitToParent();
        add(sp);
    }
}
