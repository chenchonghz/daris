package daris.client.model.object.tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import arc.gui.gwt.colour.RGB;
import arc.gui.gwt.widget.BaseWidget;
import arc.gui.gwt.widget.label.Label;
import arc.gui.gwt.widget.tree.TreeNodeGUI;
import arc.gui.image.Image;
import arc.mf.client.util.DynamicBoolean;
import arc.mf.client.util.Fuzzy;
import arc.mf.client.util.ObjectUtil;
import arc.mf.event.Filter;
import arc.mf.event.SystemEvent;
import arc.mf.object.CollectionResolveHandler;
import arc.mf.object.ObjectMessageResponse;
import arc.mf.object.ObjectResolveHandler;
import arc.mf.object.tree.Container;
import arc.mf.object.tree.Node;
import arc.mf.object.tree.NodeEventMonitor;
import arc.mf.object.tree.NodeListener;
import arc.mf.object.tree.RemoteNode;
import arc.mf.object.tree.TreeNodeAddHandler;
import arc.mf.object.tree.TreeNodeContentsHandler;
import arc.mf.object.tree.TreeNodeDescriptionHandler;
import arc.mf.object.tree.TreeNodeRemoveHandler;

import com.google.gwt.dom.client.Style.BorderStyle;

import daris.client.Resource;
import daris.client.model.IDUtil;
import daris.client.model.dataset.DerivedDataSet;
import daris.client.model.object.DObject;
import daris.client.model.object.DObjectRef;
import daris.client.model.object.events.PSSDObjectEvent;
import daris.client.model.object.events.PSSDObjectEvent.Action;
import daris.client.model.object.messages.CanAccess;
import daris.client.model.object.messages.DObjectExists;
import daris.client.model.repository.Repository;
import daris.client.model.repository.RepositoryRef;

public class DObjectTreeNode implements Container, RemoteNode {

    public static final arc.gui.image.Image FOLDER_ICON = new arc.gui.image.Image(Resource.INSTANCE.folderGrey16()
            .getSafeUri().asString(), 16, 16);
    public static final arc.gui.image.Image FOLDER_OPEN_ICON = new arc.gui.image.Image(Resource.INSTANCE
            .folderGreyOpen16().getSafeUri().asString(), 16, 16);
    public static final arc.gui.image.Image PRIMARY_DATASET_ICON = new arc.gui.image.Image(Resource.INSTANCE
            .datasetPrimary16().getSafeUri().asString(), 16, 16);
    public static final arc.gui.image.Image DERIVED_DATASET_ICON = new arc.gui.image.Image(Resource.INSTANCE
            .datasetDerived16().getSafeUri().asString(), 16, 16);
    public static final arc.gui.image.Image PROCESSED_DATASET_ICON = new arc.gui.image.Image(Resource.INSTANCE
            .datasetProcessed16().getSafeUri().asString(), 16, 16);
    public static final arc.gui.image.Image DATA_OBJECT_ICON = new arc.gui.image.Image(Resource.INSTANCE
            .datasetObject16().getSafeUri().asString(), 16, 16);

    private DObjectTreeNode _pn;
    private DObjectRef _o;
    private Fuzzy _hasChildren;
    private long _stateId;
    private Map<Object, NodeListener> _listeners;

    public DObjectTreeNode(DObjectTreeNode pn, DObjectRef o) {

        _o = o;
        _hasChildren = hasChildren(o);
        _pn = pn;
        _stateId = 0;
    }

    public DObjectTreeNode parent() {
        return _pn;
    }

    @Override
    public String type() {

        return _o.referentTypeName();
    }

    @Override
    public Image icon() {

        String id = _o.id();
        if (id == null) {
            return FOLDER_ICON;
        } else if (IDUtil.isProjectId(id)) {
            return FOLDER_ICON;
        } else if (IDUtil.isSubjectId(id)) {
            return FOLDER_ICON;
        } else if (IDUtil.isExMethodId(id)) {
            return FOLDER_ICON;
        } else if (IDUtil.isStudyId(id)) {
            return FOLDER_ICON;
        } else if (IDUtil.isDataSetId(id)) {
            if (_o.referent() != null) {
                DObject oo = _o.referent();
                if (oo instanceof DerivedDataSet) {
                    if (((DerivedDataSet) oo).processed()) {
                        return PROCESSED_DATASET_ICON;
                    }
                    return DERIVED_DATASET_ICON;
                }
                return PRIMARY_DATASET_ICON;
            } else {
                return PRIMARY_DATASET_ICON;
            }
        } else if (IDUtil.isDataObjectId(id)) {
            return DATA_OBJECT_ICON;
        }
        return null;
    }

    @Override
    public String name() {
        if (_o.isRepository()) {
            if (_o.referent() != null) {
                return ((Repository) _o.referent()).name();
            } else {
                return "DaRIS Repository";
            }
        } else if (_o.isProject()) {
            return _o.id() + (_o.name() == null ? "" : ": " + _o.name());
        } else {
            return IDUtil.getLastSection(_o.id()) + (_o.name() == null ? "" : ": " + _o.name());
        }
    }

    @Override
    public String path() {

        return _o.id();
    }

    @Override
    public void description(TreeNodeDescriptionHandler dh) {

        String description = _o.resolved() ? _o.referent().description() : (_o.referentTypeName() + " " + _o.id());
        if (dh != null) {
            dh.description(description);
        }
    }

    @Override
    public Object object() {

        return _o;
    }

    @Override
    public boolean readOnly() {
        if (_o instanceof RepositoryRef) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void discard() {
        _listeners = null;
    }

    @Override
    public boolean sorted() {
        return _o.childrenRef().sort();
    }

    @Override
    public Image openIcon() {

        String id = _o.id();
        if (id == null) {
            return FOLDER_OPEN_ICON;
        } else if (IDUtil.isProjectId(id)) {
            return FOLDER_OPEN_ICON;
        } else if (IDUtil.isSubjectId(id)) {
            return FOLDER_OPEN_ICON;
        } else if (IDUtil.isExMethodId(id)) {
            return FOLDER_OPEN_ICON;
        } else if (IDUtil.isStudyId(id)) {
            return FOLDER_OPEN_ICON;
        } else if (IDUtil.isDataSetId(id)) {
            if (_o.referent() != null) {
                DObject oo = _o.referent();
                if (oo instanceof DerivedDataSet) {
                    if (((DerivedDataSet) oo).processed()) {
                        return PROCESSED_DATASET_ICON;
                    }
                    return DERIVED_DATASET_ICON;
                }
                return PRIMARY_DATASET_ICON;
            } else {
                return PRIMARY_DATASET_ICON;
            }
        } else if (IDUtil.isDataObjectId(id)) {
            return DATA_OBJECT_ICON;
        }
        return null;
    }

    @Override
    public void contents(final long start, final long end, final TreeNodeContentsHandler ch) {

        assert start >= 0 && end >= 0 && start <= end;
        _o.childrenRef().resolve(new CollectionResolveHandler<DObjectRef>() {

            @Override
            public void resolved(List<DObjectRef> cos) {

                if (cos == null) {
                    ch.loaded(start, end, 0, null);
                    return;
                }
                if (cos.isEmpty()) {
                    ch.loaded(start, end, 0, null);
                    return;
                }
                int start0 = (int) start;
                if (start0 >= cos.size()) {
                    ch.loaded(start, end, cos.size(), null);
                    return;
                }
                int end0 = end < cos.size() ? (int) end : cos.size();
                List<DObjectRef> ros = cos.subList(start0, end0);
                if (ros.isEmpty()) {
                    ch.loaded(start, end, cos.size(), null);
                    return;
                }
                List<Node> cns = new Vector<Node>();
                for (DObjectRef ro : ros) {
                    cns.add(new DObjectTreeNode(DObjectTreeNode.this, ro));
                }
                ch.loaded(start0, end0, cos.size(), cns);
            }
        });

    }

    @Override
    public void add(Node cn, TreeNodeAddHandler ah) {

        if (_listeners != null && !_listeners.isEmpty()) {
            List<NodeListener> ls = new ArrayList<NodeListener>(_listeners.values());
            for (NodeListener l : ls) {
                l.added(this, cn, -1);
            }
        }
    }

    @Override
    public void remove(Node cn, TreeNodeRemoveHandler rh) {

        if (_listeners != null && !_listeners.isEmpty()) {
            List<NodeListener> ls = new ArrayList<NodeListener>(_listeners.values());
            for (NodeListener l : ls) {
                l.removed(this, cn);
            }
        }

        new DObjectExists((DObjectRef) object()).send(new ObjectMessageResponse<Boolean>() {

            @Override
            public void responded(Boolean exists) {
                if (!exists) {
                    parent().remove(DObjectTreeNode.this, null);
                }
            }
        });
    }

    private void doRefresh(DObjectRef o, final TreeNodeGUI gui) {
        o.reset();
        o.resolve(new ObjectResolveHandler<DObject>() {
            @Override
            public void resolved(DObject obj) {
                _o = new DObjectRef(obj, false, true);
                if (_listeners != null && !_listeners.isEmpty()) {
                    List<NodeListener> ls = new ArrayList<NodeListener>(_listeners.values());
                    for (NodeListener l : ls) {
                        l.modified(DObjectTreeNode.this);
                    }
                }
                final boolean isOpen = gui.isOpen();
                if (isOpen) {
                    gui.close();
                }
                _o.childrenRef().reset();
                _o.childrenRef().resolve(new CollectionResolveHandler<DObjectRef>() {

                    @Override
                    public void resolved(List<DObjectRef> os) throws Throwable {
                        if (isOpen) {
                            gui.reopen();
                        }
                    }
                });
            }
        });
    }

    public void refresh(final TreeNodeGUI gui) {
        final DObjectRef o = ((DObjectRef) object());
        if (o.isRepository()) {
            doRefresh(o, gui);
        } else {
            new DObjectExists(o).send(new ObjectMessageResponse<Boolean>() {

                @Override
                public void responded(Boolean exists) {
                    if (exists) {
                        doRefresh(o, gui);
                    } else {
                        parent().remove(DObjectTreeNode.this, null);
                    }
                }
            });
        }
    }

    @Override
    public Fuzzy hasChildren() {

        return _hasChildren;

    }

    private static Fuzzy hasChildren(DObjectRef o) {
        if (o.nbChildren() < 0) {
            return Fuzzy.MAYBE;
        } else if (o.nbChildren() == 0) {
            return Fuzzy.NO;
        } else {
            return Fuzzy.YES;
        }
    }

    @Override
    public boolean equals(Object o) {

        if (o == null) {
            return false;
        }
        if (!(o instanceof DObjectTreeNode)) {
            return false;
        }
        return ObjectUtil.equals(path(), ((DObjectTreeNode) o).path());
    }

    @Override
    public List<BaseWidget> adornments() {
        if (_o.referent() != null) {

            int nbChildren = _o.referent().nbChildren();
            if (nbChildren > 0) {
                DObject.Type ct = IDUtil.childTypeFromId(_o.id());
                String cts = ct.toString();
                if (DObject.Type.study == ct && nbChildren > 1) {
                    cts = "studie";
                }
                List<BaseWidget> adornments = new ArrayList<BaseWidget>(1);
                Label l = new Label(Integer.toString(nbChildren));
                l.setFontSize(9);
                l.setColour(RGB.BLUE);
                l.setBackgroundColour(new RGB(0xe0, 0xe0, 0xe0));
                l.setPaddingLeft(5);
                l.setPaddingRight(5);
                l.setBorderRadius(3);
                l.setBorder(1, BorderStyle.DOTTED, new RGB(0x80, 0x80, 0x80));
                l.setToolTip("contains " + nbChildren + " " + cts + (nbChildren > 1 ? "s." : "."));
                adornments.add(l);
                return adornments;
            }
        }
        return null;
    }

    @Override
    public Object subscribe(DynamicBoolean descend, NodeListener l) {
        Object key = NodeEventMonitor.subscribe(this, descend, l);
        if (_listeners == null) {
            _listeners = new HashMap<Object, NodeListener>();
        }
        _listeners.put(key, l);
        return key;
    }

    @Override
    public void unsubscribe(Object key) {
        NodeEventMonitor.unsubscribe(key);
        if (_listeners != null) {
            _listeners.remove(key);
        }
    }

    public String toString() {
        return object().toString();
    }

    @Override
    public Filter systemEventFilter(DynamicBoolean descend) {
        return new Filter("pssd-object", _o.id(), descend);
    }

    private void created(final long stateId, final DObjectRef co, final NodeListener nl) {
        co.resolve(new ObjectResolveHandler<DObject>() {
            @Override
            public void resolved(DObject coo) {
                _o.reset();
                _o.resolve(new ObjectResolveHandler<DObject>() {

                    @Override
                    public void resolved(DObject ooo) {
                        nl.modified(DObjectTreeNode.this);
                        if (_stateId != stateId) {
                            _hasChildren = hasChildren(_o);
                            _stateId = stateId;
                        }
                        nl.added(DObjectTreeNode.this, new DObjectTreeNode(DObjectTreeNode.this, co), -1);
                        _o.childrenRef().reset();
                        _o.childrenRef().resolve(new CollectionResolveHandler<DObjectRef>() {
                            @Override
                            public void resolved(List<DObjectRef> os) {
                                nl.changeInMembers(DObjectTreeNode.this);
                            }
                        });
                    }
                });
            }
        });
    }

    private void destroyed(final long stateId, final DObjectRef co, final NodeListener nl) {

        _o.reset();
        _o.resolve(new ObjectResolveHandler<DObject>() {

            @Override
            public void resolved(DObject ooo) {
                nl.modified(DObjectTreeNode.this);
                if (_stateId != stateId) {
                    _hasChildren = hasChildren(_o);
                    _stateId = stateId;
                }
                nl.removed(DObjectTreeNode.this, new DObjectTreeNode(DObjectTreeNode.this, co));
                _o.childrenRef().reset();
                _o.childrenRef().resolve(new CollectionResolveHandler<DObjectRef>() {
                    @Override
                    public void resolved(List<DObjectRef> os) {
                        nl.changeInMembers(DObjectTreeNode.this);
                    }
                });
            }
        });
    }

    private void modified(final long stateId, final NodeListener nl) {
        _o.reset();
        _o.resolve(new ObjectResolveHandler<DObject>() {

            @Override
            public void resolved(DObject ooo) {
                if (_stateId != stateId) {
                    _stateId = stateId;
                    _hasChildren = hasChildren(_o);
                }
                nl.modified(DObjectTreeNode.this);
            }
        });
    }

    private void membersChanged(final long stateId, final NodeListener nl) {
        _o.childrenRef().reset();
        _o.childrenRef().resolve(new CollectionResolveHandler<DObjectRef>() {
            @Override
            public void resolved(List<DObjectRef> os) {
                if (_stateId != stateId) {
                    _stateId = stateId;
                }
                nl.changeInMembers(DObjectTreeNode.this);
            }
        });
    }

    @Override
    public void process(final SystemEvent se, final NodeListener nl) {
        final PSSDObjectEvent de = (PSSDObjectEvent) se;
        String id = de.object();
        if (de.action() == Action.CREATE && IDUtil.isProjectId(id)) {
            new CanAccess(se.object()).send(new ObjectMessageResponse<Boolean>() {

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
}
