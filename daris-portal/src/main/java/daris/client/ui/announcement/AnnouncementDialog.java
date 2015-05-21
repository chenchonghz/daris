package daris.client.ui.announcement;

import arc.gui.gwt.widget.button.Button;
import arc.gui.gwt.widget.button.ButtonBar;
import arc.gui.gwt.widget.button.ButtonBar.Alignment;
import arc.gui.gwt.widget.button.ButtonBar.Position;
import arc.gui.gwt.widget.panel.SimplePanel;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.gui.gwt.widget.window.Window;
import arc.gui.gwt.widget.window.WindowCloseListener;
import arc.gui.window.WindowProperties;
import arc.mf.object.ObjectResolveHandler;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import daris.client.Application;
import daris.client.model.announcement.Announcement;
import daris.client.model.announcement.AnnouncementRef;
import daris.client.ui.DObjectBrowser;
import daris.client.ui.util.ButtonUtil;
import daris.client.ui.widget.LoadingBar;

public class AnnouncementDialog {

    private AnnouncementRef _a;

    private Window _win;

    private VerticalPanel _vp;

    private SimplePanel _sp;

    private boolean _showing = false;

    public AnnouncementDialog(AnnouncementRef a) {

        _a = a;

        _vp = new VerticalPanel();
        _vp.fitToParent();

        _sp = new SimplePanel();
        _sp.fitToParent();

        _vp.add(_sp);

        ButtonBar bb = ButtonUtil.createButtonBar(Position.BOTTOM, Alignment.RIGHT, 28);
        Button akButton = bb.addButton("Acknowledge");
        akButton.setMarginRight(20);
        akButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                Announcement.Cookie.setLatest(_a.uid());
                _win.close();
            }
        });
        _vp.add(bb);

        _sp.setContent(new LoadingBar("Loading system announcement "));
        _a.resolve(new ObjectResolveHandler<Announcement>() {

            @Override
            public void resolved(Announcement o) {

                _sp.setContent(new AnnouncementPanel(o));
            }
        });

    }

    public void show(Window owner, double width, double height) {
        if (!_showing) {
            WindowProperties wp = new WindowProperties();
            wp.setModal(true);
            wp.setCanBeResized(true);
            wp.setCanBeClosed(false);
            wp.setCanBeMoved(true);
            wp.setSize(width, height);
            wp.setOwnerWindow(owner);
            wp.setTitle("System announcement " + _a.uid());
            _win = Window.create(wp);
            _win.setContent(_vp);
            _win.addCloseListener(new WindowCloseListener() {
                @Override
                public void closed(Window w) {
                    _showing = false;
                }
            });
            _win.centerInPage();
            _win.show();
            _showing = true;
        }
    }

    public void show(double width, double height) {
        show(Application.window(), width, height);
    }

}
