package daris.client.ui;

import arc.gui.gwt.colour.RGB;
import arc.gui.gwt.widget.HTML;
import arc.gui.gwt.widget.button.Button;
import arc.gui.gwt.widget.button.ButtonBar;
import arc.gui.gwt.widget.button.ButtonBar.Alignment;
import arc.gui.gwt.widget.button.ButtonBar.Position;
import arc.gui.gwt.widget.panel.CenteringPanel;
import arc.gui.gwt.widget.panel.CenteringPanel.Axis;
import arc.gui.gwt.widget.panel.TabPanel;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.gui.gwt.widget.window.Window;
import arc.gui.window.WindowProperties;
import arc.mf.client.util.DateTime;
import arc.mf.object.ObjectMessageResponse;
import arc.mf.object.ObjectResolveHandler;

import com.google.gwt.dom.client.Style.BorderStyle;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import daris.client.DaRIS;
import daris.client.mf.ServerVersion;
import daris.client.mf.pkg.Package;
import daris.client.mf.pkg.PackageRef;
import daris.client.ui.mf.pkg.PackageGrid;
import daris.client.ui.util.ButtonUtil;

public class AboutDialog {

    private Window _win;
    private PackageRef _pkg;

    public AboutDialog() {
        _pkg = new PackageRef(DaRIS.PACKAGE);
    }

    private static String generateHTML(Package pkg, String serverVersion) {
        StringBuilder sb = new StringBuilder();
        sb.append("<table width=\"100%\">");
        sb.append("<thead><th colspan=\"2\" align=\"center\"><h2>DaRIS Portal</h2></th></thead>");
        sb.append("<tbody>");
        sb.append("<tr><td align=\"right\"><b>Version:</b></td><td align=\"left\">" + pkg.version() + "</td></tr>");
        if (pkg.buildTime() != null) {
            sb.append("<tr><td align=\"right\"><b>Build Time:</b></td><td align=\"left\">"
                    + DateTime.SERVER_DATE_TIME_FORMAT.format(pkg.buildTime()) + "</td></tr>");
        }
        sb.append("<tr><td align=\"right\"><b>Mediaflux Version:</b></td><td align=\"left\">" + serverVersion
                + "</td></tr>");
        sb.append("</tbody>");
        sb.append("</table>");
        return sb.toString();
    }

    public void show(Window owner) {

        final HTML html = new HTML();
        html.fitToParent();
        html.setFontSize(12);

        new ServerVersion().send(new ObjectMessageResponse<String>() {
            @Override
            public void responded(final String serverVersion) {
                _pkg.resolve(new ObjectResolveHandler<Package>() {

                    @Override
                    public void resolved(Package o) {
                        html.setHTML(generateHTML(o, serverVersion));
                    }
                });
            }
        });
        TabPanel tp = new TabPanel();
        tp.fitToParent();

        CenteringPanel cp = new CenteringPanel(Axis.BOTH);
        cp.fitToParent();
        cp.setContent(html);
        cp.setBorderTop(3, BorderStyle.SOLID, new RGB(0x88, 0x88, 0x88));
        tp.setActiveTabById(tp.addTab("General", "General information about DaRIS portal", cp));

        tp.addTab("Package", "Installed DaRIS packages.", new PackageGrid());

        ButtonBar bb = ButtonUtil.createButtonBar(Position.BOTTOM, Alignment.RIGHT, 28);
        Button okButton = bb.addButton("OK");
        okButton.setMarginRight(20);
        okButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                _win.close();
            }
        });

        VerticalPanel vp = new VerticalPanel();
        vp.fitToParent();

        vp.add(tp);
        vp.add(bb);

        WindowProperties wp = new WindowProperties();
        wp.setModal(true);
        wp.setCanBeResized(false);
        wp.setCanBeClosed(false);
        wp.setCanBeMoved(true);
        wp.setSize(0.4, 0.4);
        wp.setOwnerWindow(owner);
        wp.setTitle("About DaRIS Portal");
        _win = Window.create(wp);
        _win.setContent(vp);
        _win.centerInPage();
        _win.show();
    }

}
