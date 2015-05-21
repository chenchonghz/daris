package daris.client.ui.announcement;

import java.util.ArrayList;
import java.util.List;

import arc.gui.gwt.colour.RGB;
import arc.gui.gwt.widget.event.SelectionHandler;
import arc.gui.gwt.widget.panel.SimplePanel;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.gui.gwt.widget.panel.VerticalSplitPanel;
import arc.gui.gwt.widget.window.Window;
import arc.gui.gwt.widget.window.WindowCloseListener;
import arc.gui.window.WindowProperties;
import arc.mf.event.Filter;
import arc.mf.event.SystemEvent;
import arc.mf.event.SystemEventChannel;
import arc.mf.object.ObjectResolveHandler;

import com.google.gwt.resources.client.ImageResource;

import daris.client.Resource;
import daris.client.model.announcement.Announcement;
import daris.client.model.announcement.AnnouncementRef;
import daris.client.model.announcement.events.AnnouncementEvent;
import daris.client.ui.widget.LoadingBar;

public class AnnouncementNavigator implements arc.mf.event.Subscriber {

    public static final ImageResource ICON_FIRST = Resource.INSTANCE.first24();

    public static final ImageResource ICON_LAST = Resource.INSTANCE.last24();

    public static final ImageResource ICON_NEXT = Resource.INSTANCE.forward24();

    public static final ImageResource ICON_PREV = Resource.INSTANCE.rewind24();

    public static AnnouncementNavigator _instance;

    public static AnnouncementNavigator get() {
        if (_instance == null) {
            _instance = new AnnouncementNavigator();
        }
        return _instance;
    }

    private VerticalSplitPanel _vp;

    private AnnouncementGrid _grid;

    private SimplePanel _detailSP;
    private boolean _showing = false;

    private AnnouncementNavigator() {

        _vp = new VerticalSplitPanel(10);
        _vp.fitToParent();

        VerticalPanel gridVP = new VerticalPanel();
        gridVP.setWidth100();
        gridVP.setPreferredHeight(.4);

        _grid = new AnnouncementGrid(0, 100);
        _grid.fitToParent();
        gridVP.add(_grid);

        // ButtonBar gridBB = ButtonUtil.createButtonBar(Position.BOTTOM,
        // Alignment.CENTER, 32);
        //
        // Button firstPageButton = ButtonUtil.createImageButton(ICON_FIRST, 24,
        // 24);
        // firstPageButton.addClickHandler(new ClickHandler() {
        //
        // @Override
        // public void onClick(com.google.gwt.event.dom.client.ClickEvent event)
        // {
        // _grid.firstPage();
        // }
        // });
        // gridBB.add(firstPageButton);
        //
        // Button prevPageButton = ButtonUtil.createImageButton(ICON_PREV, 24,
        // 24);
        // prevPageButton.addClickHandler(new ClickHandler() {
        //
        // @Override
        // public void onClick(com.google.gwt.event.dom.client.ClickEvent event)
        // {
        // _grid.prevPage();
        // }
        // });
        // gridBB.add(prevPageButton);
        //
        // Button nextPageButton = ButtonUtil.createImageButton(ICON_NEXT, 24,
        // 24);
        // nextPageButton.addClickHandler(new ClickHandler() {
        //
        // @Override
        // public void onClick(com.google.gwt.event.dom.client.ClickEvent event)
        // {
        // _grid.nextPage();
        // }
        // });
        // gridBB.add(nextPageButton);
        //
        // Button lastPageButton = ButtonUtil.createImageButton(ICON_LAST, 24,
        // 24);
        // lastPageButton.addClickHandler(new ClickHandler() {
        //
        // @Override
        // public void onClick(com.google.gwt.event.dom.client.ClickEvent event)
        // {
        // _grid.lastPage();
        // }
        // });
        // gridBB.add(lastPageButton);
        //
        // gridVP.add(gridBB);

        _vp.add(gridVP);

        _detailSP = new SimplePanel();
        _detailSP.fitToParent();
        _detailSP.setBorder(2, new RGB(0x99, 0x99, 0x99));
        _vp.add(_detailSP);

        _grid.setSelectionHandler(new SelectionHandler<AnnouncementRef>() {

            @Override
            public void selected(AnnouncementRef o) {
                _detailSP.setContent(new LoadingBar("Loading announcement " + o.uid() + ": " + o.title()));
                o.resolve(new ObjectResolveHandler<Announcement>() {

                    @Override
                    public void resolved(Announcement o) {
                        _detailSP.setContent(new AnnouncementPanel(o));
                    }
                });
            }

            @Override
            public void deselected(AnnouncementRef o) {
                _detailSP.setContent(null);
            }
        });

    }

    public void show(Window owner, double width, double height) {
        if (!_showing) {

            _grid.reset();
            _grid.refresh(true);

            // SystemEventChannel.subscribe();
            SystemEventChannel.add(this);

            WindowProperties wp = new WindowProperties();
            wp.setModal(false);
            wp.setCanBeResized(true);
            wp.setCanBeClosed(true);
            wp.setCanBeMoved(true);
            wp.setSize(width, height);
            wp.setOwnerWindow(owner);
            wp.setTitle("System Announcements");
            Window win = Window.create(wp);
            win.setContent(_vp);
            win.addCloseListener(new WindowCloseListener() {
                @Override
                public void closed(Window w) {
                    _showing = false;
                    SystemEventChannel.remove(AnnouncementNavigator.this);
                }
            });
            win.centerInPage();
            win.show();
            _showing = true;
        }
    }

    private List<Filter> _filters;

    @Override
    public List<Filter> systemEventFilters() {
        if (_filters == null) {
            _filters = new ArrayList<Filter>(1);
            _filters.add(new Filter(AnnouncementEvent.SYSTEM_EVENT_NAME));
        }
        return _filters;
    }

    @Override
    public void process(SystemEvent se) {
        if (!(se instanceof AnnouncementEvent)) {
            return;
        }
        _grid.reset();
        _grid.refresh(true);
    }

}
