package daris.client.ui.query;

import java.util.List;

import arc.gui.gwt.widget.button.Button;
import arc.gui.gwt.widget.button.ButtonBar;
import arc.gui.gwt.widget.event.SelectionHandler;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.gui.gwt.widget.window.Window;
import arc.gui.gwt.widget.window.WindowCloseListener;
import arc.gui.window.WindowProperties;
import arc.mf.client.Output;
import arc.mf.client.xml.XmlElement;
import arc.mf.object.ObjectResolveHandler;
import arc.mf.session.ServiceResponseHandler;
import arc.mf.session.Session;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import daris.client.model.query.Query;
import daris.client.model.query.QueryAsset;
import daris.client.model.query.QueryAssetRef;

public class SavedQueryBrowser {

    private Window _win;
    private VerticalPanel _vp;
    private SavedQueryNavigator _nav;
    private Button _loadButton;
    private Button _deleteButton;

    private QueryAssetRef _selected;
    private ObjectResolveHandler<Query> _loadHandler;

    private SavedQueryBrowser() {
        _vp = new VerticalPanel();
        _vp.fitToParent();

        _nav = new SavedQueryNavigator();
        _nav.fitToParent();
        _nav.setSelectionHandler(new SelectionHandler<QueryAssetRef>() {

            @Override
            public void selected(QueryAssetRef o) {
                _selected = o;
                _loadButton.setEnabled(o != null);
                if (o != null) {
                    _deleteButton.disable();
                    Session.execute("asset.acl.have", "<id>" + o.id()
                            + "</id><metadata>read-write</metadata><content>read-write</content>",
                            new ServiceResponseHandler() {

                                @Override
                                public void processResponse(XmlElement xe, List<Output> outputs)
                                        throws Throwable {
                                    boolean canDelete = xe.booleanValue("asset/metadata")
                                            && xe.booleanValue("asset/content");
                                    if (canDelete) {
                                        _deleteButton.enable();
                                    }
                                }
                            });
                }
            }

            @Override
            public void deselected(QueryAssetRef o) {
                _selected = null;
                _loadButton.disable();
                _deleteButton.disable();
            }
        });
        _vp.add(_nav);

        ButtonBar bb = new ButtonBar(ButtonBar.Position.BOTTOM, ButtonBar.Alignment.CENTER);
        bb.setButtonMarginFirst(15);
        bb.setButtonSpacing(15);
        _vp.add(bb);
        _loadButton = bb.addButton("Load");
        _loadButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                _loadButton.disable();
                _deleteButton.disable();
                if (_selected != null) {
                    _selected.resolve(new ObjectResolveHandler<QueryAsset>() {

                        @Override
                        public void resolved(QueryAsset qa) {
                            if (_loadHandler != null) {
                                _loadHandler.resolved(qa == null ? null : Query.create(qa));
                                if (_win != null) {
                                    _win.close();
                                }
                            }
                        }
                    });
                }
            }
        });
        _loadButton.disable();

        _deleteButton = bb.addButton("Delete");
        _deleteButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                _loadButton.disable();
                _deleteButton.disable();
                if (_selected != null) {
                    Session.execute("asset.destroy", "<id>" + _selected.id() + "</id>",
                            new ServiceResponseHandler() {

                                @Override
                                public void processResponse(XmlElement xe, List<Output> outputs)
                                        throws Throwable {
                                    _nav.refresh();
                                }
                            });
                }
            }
        });
        _deleteButton.disable();

    }

    public void setQueryLoadHandler(ObjectResolveHandler<Query> loadHandler) {
        _loadHandler = loadHandler;
    }

    public void show(arc.gui.window.Window owner, ObjectResolveHandler<Query> loadHandler) {
        _loadHandler = loadHandler;
        if (_win == null) {
            WindowProperties wp = new WindowProperties();
            wp.setOwnerWindow(owner);
            wp.setModal(false);
            wp.setCanBeClosed(true);
            wp.setCanBeMoved(true);
            wp.setCanBeResized(true);
            wp.setCanBeMaximised(false);
            wp.setSize(0.5, 0.5);
            wp.setTitle("Saved queries");
            _win = Window.create(wp);
            _win.setContent(_vp);
            _win.addCloseListener(new WindowCloseListener() {

                @Override
                public void closed(Window w) {
                    _win = null;
                }
            });
        }
        _win.centerOnOwner();
        _win.centerInPage();
        _win.show();
    }

    private void close() {
        if (_win != null) {
            _win.close();
        }
    }

    private static SavedQueryBrowser _instance;

    public static SavedQueryBrowser get() {
        if (_instance != null) {
            _instance.close();
        }
        _instance = new SavedQueryBrowser();
        return _instance;
    }

}
