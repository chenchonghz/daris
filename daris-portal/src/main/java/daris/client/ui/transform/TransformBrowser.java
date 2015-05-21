package daris.client.ui.transform;

import java.util.List;

import arc.gui.gwt.object.ObjectDetailedView;
import arc.gui.gwt.widget.button.Button;
import arc.gui.gwt.widget.button.ButtonBar;
import arc.gui.gwt.widget.dialog.Dialog;
import arc.gui.gwt.widget.event.SelectionHandler;
import arc.gui.gwt.widget.panel.HorizontalSplitPanel;
import arc.gui.gwt.widget.panel.SimplePanel;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.gui.gwt.widget.scroll.ScrollPolicy;
import arc.gui.gwt.widget.window.WindowCloseListener;
import arc.gui.window.Window;
import arc.gui.window.WindowProperties;
import arc.mf.client.util.ActionListener;
import arc.mf.object.Null;
import arc.mf.object.ObjectMessage;
import arc.mf.object.ObjectMessageResponse;
import arc.mf.object.ObjectResolveHandler;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Timer;

import daris.client.model.transform.Transform;
import daris.client.model.transform.Transform.Status.State;
import daris.client.model.transform.TransformRef;
import daris.client.model.transform.messages.TransformDestroy;
import daris.client.model.transform.messages.TransformResume;
import daris.client.model.transform.messages.TransformStatusGet;
import daris.client.model.transform.messages.TransformSuspend;
import daris.client.model.transform.messages.TransformTerminate;
import daris.client.ui.DObjectGUIRegistry;
import daris.client.ui.util.ButtonUtil;

public class TransformBrowser {

    private arc.gui.gwt.widget.window.Window _win;

    private VerticalPanel _vp;

    private HorizontalSplitPanel _hsp;
    private TransformNavigator _nav;
    private ObjectDetailedView _dv;

    private SimplePanel _bbSP;
    private Button _suspendButton;
    private Button _resumeButton;
    private Button _terminateButton;
    private Button _deleteButton;
    private Button _refreshButton;
    private Button _refreshAllButton;

    private ObjectMessage<Null> _checkStatusMessage;
    private Timer _checkStatusTimer;

    private boolean _showing = false;

    public static int CHECK_STATUS_INTERVAL_MILLISECS = 30000;

    private TransformBrowser() {

        _vp = new VerticalPanel();
        _vp.fitToParent();

        _hsp = new HorizontalSplitPanel();
        _vp.add(_hsp);
        _hsp.fitToParent();
        _nav = new TransformNavigator();
        _nav.setHeight100();
        _nav.setPreferredWidth(0.6);
        _hsp.add(_nav);

        _dv = new ObjectDetailedView(ScrollPolicy.AUTO);
        _dv.setObjectRegistry(DObjectGUIRegistry.get());
        _dv.setPreferredWidth(0.4);
        _dv.setHeight100();
        _dv.setReadOnly(true);
        _hsp.add(_dv);

        _nav.addSelectionHandler(new SelectionHandler<TransformRef>() {

            @Override
            public void selected(TransformRef t) {
                _dv.loadAndDisplayObject(t, false);
                updateButtons(t);
            }

            @Override
            public void deselected(TransformRef o) {
                _dv.clear();
            }
        });
        _nav.addPageResolveHandler(new ObjectResolveHandler<List<TransformRef>>() {

            @Override
            public void resolved(List<TransformRef> data) {
                if (data == null || data.isEmpty()) {
                    updateButtons(null);
                }
            }
        });

        _bbSP = new SimplePanel();
        _bbSP.setHeight(30);
        _bbSP.setWidth100();
        _vp.add(_bbSP);

        _nav.gotoOffset(0);

        _checkStatusMessage = new TransformStatusGet();
        _checkStatusTimer = new Timer() {
            @Override
            public void run() {
                _checkStatusMessage.send();
            }
        };
    }

    private void startMonitor() {
        _checkStatusMessage.send();
        if (!_checkStatusTimer.isRunning()) {
            _checkStatusTimer.scheduleRepeating(CHECK_STATUS_INTERVAL_MILLISECS);
        }
    }

    private void stopMonitor() {
        if (_checkStatusTimer.isRunning()) {
            _checkStatusTimer.cancel();
        }
    }

    private void updateButtons(final TransformRef t) {
        _bbSP.clear();
        if (t == null) {
            ButtonBar bb = ButtonUtil.createButtonBar(ButtonBar.Position.BOTTOM, ButtonBar.Alignment.CENTER, 30);
            _bbSP.setContent(bb);
            return;
        }
        t.reset();
        t.resolve(new ObjectResolveHandler<Transform>() {
            @Override
            public void resolved(Transform o) {
                ButtonBar bb = ButtonUtil.createButtonBar(ButtonBar.Position.BOTTOM, ButtonBar.Alignment.CENTER, 30);
                Transform.Status.State state = o.status().state();
                if (state == State.running) {
                    _suspendButton = bb.addButton("Suspend");
                    _suspendButton.addClickHandler(new ClickHandler() {

                        @Override
                        public void onClick(ClickEvent event) {
                            _suspendButton.disable();
                            Dialog.confirm("Suspend transform " + t.uid(),
                                    "Are you sure you want to suspend transform " + t.uid() + "?",
                                    new ActionListener() {

                                        @Override
                                        public void executed(boolean succeeded) {
                                            if (succeeded) {
                                                new TransformSuspend(t).send(new ObjectMessageResponse<Null>() {

                                                    @Override
                                                    public void responded(Null r) {
                                                        _suspendButton.enable();
                                                    }
                                                });
                                            } else {
                                                _suspendButton.enable();
                                            }
                                        }
                                    });
                        }
                    });
                }
                if (state == State.suspended) {
                    _resumeButton = bb.addButton("Resume");
                    _resumeButton.addClickHandler(new ClickHandler() {

                        @Override
                        public void onClick(ClickEvent event) {
                            _resumeButton.disable();
                            new TransformResume(t).send(new ObjectMessageResponse<Null>() {

                                @Override
                                public void responded(Null r) {
                                    _resumeButton.enable();
                                }
                            });
                        }
                    });
                }
                if (state == State.pending || state == State.running || state == State.suspended) {
                    _terminateButton = bb.addButton("Terminate");
                    _terminateButton.addClickHandler(new ClickHandler() {

                        @Override
                        public void onClick(ClickEvent event) {
                            _terminateButton.disable();
                            Dialog.confirm("Terminate transform " + t.uid(),
                                    "Are you sure you want to terminate transform " + t.uid() + "?",
                                    new ActionListener() {
                                        @Override
                                        public void executed(boolean succeeded) {
                                            if (succeeded) {
                                                new TransformTerminate(t).send(new ObjectMessageResponse<Null>() {

                                                    @Override
                                                    public void responded(Null r) {
                                                        _terminateButton.enable();
                                                    }
                                                });
                                            } else {
                                                _terminateButton.enable();
                                            }
                                        }
                                    });
                        }
                    });
                } else {
                    _deleteButton = bb.addButton("Delete");
                    _deleteButton.addClickHandler(new ClickHandler() {

                        @Override
                        public void onClick(ClickEvent event) {
                            _deleteButton.disable();
                            Dialog.confirm("Delete transform " + t.uid(), "Are you sure you want to delete transform "
                                    + t.uid() + "?", new ActionListener() {

                                @Override
                                public void executed(boolean succeeded) {
                                    if (succeeded) {
                                        new TransformDestroy(t).send(new ObjectMessageResponse<Null>() {

                                            @Override
                                            public void responded(Null r) {
                                                _deleteButton.enable();
                                            }
                                        });
                                    } else {
                                        _deleteButton.enable();
                                    }
                                }
                            });
                        }
                    });
                }
                if (state != State.terminated && state != State.failed && state != State.unknown) {
                    _refreshButton = bb.addButton("Refresh");
                    _refreshButton.addClickHandler(new ClickHandler() {

                        @Override
                        public void onClick(ClickEvent event) {
                            _refreshButton.disable();
                            new TransformStatusGet(t).send(new ObjectMessageResponse<Null>() {

                                @Override
                                public void responded(Null r) {
                                    _refreshButton.enable();
                                }
                            });
                        }
                    });
                }
                if (_nav.hasTransformsInCurrentPage()) {
                    _refreshAllButton = bb.addButton("Refresh all");
                    _refreshAllButton.addClickHandler(new ClickHandler() {

                        @Override
                        public void onClick(ClickEvent event) {
                            _refreshAllButton.disable();
                            new TransformStatusGet().send(new ObjectMessageResponse<Null>() {

                                @Override
                                public void responded(Null r) {
                                    _refreshAllButton.enable();
                                }
                            });
                        }
                    });
                }
                _bbSP.setContent(bb);
            }
        });

    }

    public void show(Window owner) {
        if (_showing) {
            return;
        }

        if (_win == null) {
            WindowProperties wp = new WindowProperties();
            wp.setSize(0.7, 0.7);
            wp.setTitle("Transforms");
            wp.setCanBeClosed(true);
            wp.setCanBeMaximised(true);
            wp.setCanBeMoved(true);
            wp.setCanBeResized(true);
            wp.setCenterInPage(true);
            wp.setOwnerWindow(owner);
            _win = arc.gui.gwt.widget.window.Window.create(wp);
            _win.setContent(_vp);
            _win.addCloseListener(new WindowCloseListener() {
                @Override
                public void closed(arc.gui.gwt.widget.window.Window w) {
                    _nav.unsubscribe();
                    stopMonitor();
                    _showing = false;
                    _win = null;
                }
            });
        }
        _win.show();
        _nav.subscribe();
        startMonitor();
        _showing = true;
    }

    private void hide() {
        if (_showing && _win != null) {
            _win.close();
        }
    }

    private static TransformBrowser _instance;

    public static TransformBrowser get() {
        if (_instance == null) {
            _instance = new TransformBrowser();
        }
        return _instance;
    }

    public static void reset() {
        if (_instance != null) {
            _instance.hide();
            _instance = null;
        }
    }

}
