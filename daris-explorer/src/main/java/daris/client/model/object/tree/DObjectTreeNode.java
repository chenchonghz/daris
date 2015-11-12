package daris.client.model.object.tree;

import java.util.ArrayList;
import java.util.List;

import arc.gui.image.Image;
import arc.gui.image.ResourceImage;
import arc.mf.client.util.DynamicBoolean;
import arc.mf.client.util.Fuzzy;
import arc.mf.client.util.UnhandledException;
import arc.mf.event.Filter;
import arc.mf.event.SystemEvent;
import arc.mf.object.CollectionResolveHandler;
import arc.mf.object.ObjectMessageResponse;
import arc.mf.object.ObjectResolveHandler;
import arc.mf.object.tree.Container;
import arc.mf.object.tree.NodeEventMonitor;
import arc.mf.object.tree.NodeListener;
import arc.mf.object.tree.RemoteNode;
import arc.mf.object.tree.TreeNodeAddHandler;
import arc.mf.object.tree.TreeNodeContentsHandler;
import arc.mf.object.tree.TreeNodeDescriptionHandler;
import arc.mf.object.tree.TreeNodeRemoveHandler;
import daris.client.model.CiteableIdUtils;
import daris.client.model.DObject;
import daris.client.model.object.DObjectCollectionRef;
import daris.client.model.object.DObjectRef;
import daris.client.model.object.events.PSSDObjectEvent;
import daris.client.model.object.messages.CanAccess;
import javafx.scene.Node;
import javafx.scene.control.Label;

public class DObjectTreeNode implements Container, RemoteNode {

    private static Image _icon;
    private static Image _openIcon;

    private DObjectRef _object;
    private DObjectCollectionRef _children;
    private long _stateId;

    public DObjectTreeNode(DObjectRef object) {
        _object = object;
        _children = new DObjectCollectionRef(_object);
    }

    @Override
    public List<Node> adornments() {
        List<Node> adornments = null;
        int nChildren = _object.numberOfChildren();
        if (nChildren > 0) {
            adornments = new ArrayList<Node>();
            adornments.add(new Label(Integer.toString(nChildren)));
        }
        return adornments;
    }

    @Override
    public void description(TreeNodeDescriptionHandler h) throws Throwable {
        _object.resolve(new ObjectResolveHandler<DObject>() {
            @Override
            public void resolved(DObject o) {
                StringBuilder sb = new StringBuilder();
                sb.append(o.type());
                sb.append(" ");
                sb.append(o.citeableId());
                h.description(sb.toString());
            }
        });
    }

    @Override
    public void discard() {

    }

    @Override
    public Image icon() {
        if (_icon == null) {
            _icon = new ResourceImage("images/16folder_blue.png", 16, 16);
        }
        return _icon;
    }

    @Override
    public String name() {
        return _object.name();
    }

    @Override
    public Object object() {
        return _object;
    }

    @Override
    public String path() {
        return _object.citeableId();
    }

    @Override
    public boolean readOnly() {
        return true;
    }

    @Override
    public Object subscribe(DynamicBoolean descend, NodeListener l) {
        Object key = NodeEventMonitor.subscribe(this, descend, l);
        return key;
    }

    @Override
    public String type() {
        return _object.referentTypeName();
    }

    @Override
    public void unsubscribe(Object key) {
        NodeEventMonitor.unsubscribe(key);
    }

    @Override
    public void add(arc.mf.object.tree.Node cn, TreeNodeAddHandler ah) {

    }

    @Override
    public void contents(long start, long end, TreeNodeContentsHandler arg2)
            throws Throwable {
        _children.reset();
        _children.resolve(new CollectionResolveHandler<DObjectRef>() {

            @Override
            public void resolved(List<DObjectRef> os) throws Throwable {
                
            }
        });
    }

    @Override
    public Fuzzy hasChildren() {
        return _object.hasChildren();
    }

    @Override
    public Image openIcon() {
        if (_openIcon == null) {
            _openIcon = new ResourceImage("images/16folder_blue_open.png", 16,
                    16);
        }
        return _openIcon;
    }

    @Override
    public void remove(arc.mf.object.tree.Node cn, TreeNodeRemoveHandler rh) {

    }

    @Override
    public boolean sorted() {
        return true;
    }

    @Override
    public void process(SystemEvent se, NodeListener nl) throws Throwable {
        final PSSDObjectEvent de = (PSSDObjectEvent) se;
        String cid = de.object();
        if (de.action() == PSSDObjectEvent.Action.CREATE
                && CiteableIdUtils.isProjectCID(cid)) {
            new CanAccess(se.object())
                    .send(new ObjectMessageResponse<Boolean>() {
                        @Override
                        public void responded(Boolean canAccess) {
                            if (canAccess) {
                                processEvent(de, nl);
                            } else {
                                System.out.println("By-passing event: " + de);
                            }
                        }
                    });
        } else {
            processEvent(de, nl);
        }
    }

    private void processEvent(PSSDObjectEvent dse, NodeListener nl) {
        DObjectRef o = dse.objectRef();
        long stateId = dse.id();
        if (PSSDObjectEvent.Action.DESTROY.equals(dse.action())) {
            destroyed(stateId, o, nl);
        } else if (PSSDObjectEvent.Action.CREATE.equals(dse.action())) {
            created(stateId, o, nl);
        } else if (PSSDObjectEvent.Action.MODIFY.equals(dse.action())) {
            modified(stateId, nl);
        } else if (PSSDObjectEvent.Action.MEMBERS.equals(dse.action())) {
            membersChanged(stateId, nl);
        }
    }

    private void created(long stateId, final DObjectRef co,
            final NodeListener nl) {
        co.resolve(new ObjectResolveHandler<DObject>() {
            @Override
            public void resolved(DObject coo) {
                if (stateId != _stateId) {
                    _stateId = stateId;
                }
                _object.reset();
                _object.resolve(new ObjectResolveHandler<DObject>() {

                    @Override
                    public void resolved(DObject ooo) {

                        nl.modified(DObjectTreeNode.this);
                        nl.added(DObjectTreeNode.this, new DObjectTreeNode(co),
                                -1);
                        _children.reset();
                        try {
                            _children.resolve(
                                    new CollectionResolveHandler<DObjectRef>() {
                                @Override
                                public void resolved(List<DObjectRef> os) {
                                    nl.changeInMembers(DObjectTreeNode.this);
                                }
                            });
                        } catch (Throwable e) {
                            UnhandledException.report(e.getMessage(), e);
                        }
                    }
                });
            }
        });
    }

    private void destroyed(long stateId, final DObjectRef co,
            final NodeListener nl) {
        if (stateId != _stateId) {
            _stateId = stateId;
        }
        _object.reset();
        _object.resolve(new ObjectResolveHandler<DObject>() {

            @Override
            public void resolved(DObject ooo) {
                nl.modified(DObjectTreeNode.this);
                nl.removed(DObjectTreeNode.this, new DObjectTreeNode(co));
                _children.reset();
                try {
                    _children.resolve(
                            new CollectionResolveHandler<DObjectRef>() {
                        @Override
                        public void resolved(List<DObjectRef> os) {
                            nl.changeInMembers(DObjectTreeNode.this);
                        }
                    });
                } catch (Throwable e) {
                    UnhandledException.report(e.getMessage(), e);
                }
            }
        });
    }

    private void modified(long stateId, final NodeListener nl) {
        if (stateId != _stateId) {
            _stateId = stateId;
        }
        _object.reset();
        _object.resolve(new ObjectResolveHandler<DObject>() {

            @Override
            public void resolved(DObject ooo) {
                nl.modified(DObjectTreeNode.this);
            }
        });
    }

    private void membersChanged(long stateId, final NodeListener nl) {
        if (stateId != _stateId) {
            _stateId = stateId;
        }
        _children.reset();
        try {
            _children.resolve(new CollectionResolveHandler<DObjectRef>() {
                @Override
                public void resolved(List<DObjectRef> os) {
                    nl.changeInMembers(DObjectTreeNode.this);
                }
            });
        } catch (Throwable e) {
            UnhandledException.report(e.getMessage(), e);
        }
    }

    @Override
    public Filter systemEventFilter(DynamicBoolean descend) {
        return new Filter("pssd-object", _object.citeableId(), descend);
    }

}
