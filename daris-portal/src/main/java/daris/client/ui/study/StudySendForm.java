package daris.client.ui.study;

import java.util.ArrayList;
import java.util.List;

import arc.gui.ValidatedInterfaceComponent;
import arc.gui.dialog.DialogProperties;
import arc.gui.gwt.widget.BaseWidget;
import arc.gui.gwt.widget.dialog.Dialog;
import arc.gui.gwt.widget.scroll.ScrollPolicy;
import arc.gui.gwt.widget.tree.TreeGUI;
import arc.gui.gwt.widget.tree.TreeGUIEventHandler;
import arc.gui.image.Image;
import arc.gui.window.Window;
import arc.mf.client.Output;
import arc.mf.client.util.ActionListener;
import arc.mf.client.util.AsynchronousAction;
import arc.mf.client.util.DynamicBoolean;
import arc.mf.client.util.Fuzzy;
import arc.mf.client.util.IsNotValid;
import arc.mf.client.util.IsValid;
import arc.mf.client.util.ObjectUtil;
import arc.mf.client.util.Validity;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlStringWriter;
import arc.mf.object.tree.Container;
import arc.mf.object.tree.Node;
import arc.mf.object.tree.NodeListener;
import arc.mf.object.tree.Tree;
import arc.mf.object.tree.TreeNodeAddHandler;
import arc.mf.object.tree.TreeNodeContentsHandler;
import arc.mf.object.tree.TreeNodeDescriptionHandler;
import arc.mf.object.tree.TreeNodeRemoveHandler;
import arc.mf.session.ServiceResponseHandler;
import arc.mf.session.Session;

import com.google.gwt.user.client.ui.Widget;

import daris.client.Resource;
import daris.client.model.object.DObject;
import daris.client.model.object.DObjectRef;
import daris.client.ui.widget.MessageBox;

public class StudySendForm extends ValidatedInterfaceComponent implements
        AsynchronousAction {

    private static class DestinationTree implements Tree {

        private static class DestinationNode implements Container {

            public static final arc.gui.image.Image FOLDER_ICON = new arc.gui.image.Image(
                    Resource.INSTANCE.folderGrey16().getSafeUri().asString(),
                    16, 16);

            private DObjectRef _o;

            public DestinationNode(DObjectRef o) {
                _o = o;
            }

            @Override
            public String type() {
                return _o == null ? null : _o.referentTypeName();
            }

            @Override
            public Image icon() {
                return FOLDER_ICON;
            }

            @Override
            public String name() {
                if (_o == null) {
                    return null;
                }
                StringBuilder sb = new StringBuilder();
                sb.append(_o.referentTypeName());
                sb.append(" ");
                sb.append(_o.id());
                if (_o.name() != null) {
                    sb.append(": ");
                    sb.append(_o.name());
                }
                return sb.toString();
            }

            @Override
            public String path() {
                return _o == null ? null : _o.id();
            }

            @Override
            public void description(TreeNodeDescriptionHandler dh) {

            }

            @Override
            public List<BaseWidget> adornments() {
                return null;
            }

            @Override
            public Object object() {
                return _o;
            }

            @Override
            public boolean readOnly() {
                return true;
            }

            @Override
            public Object subscribe(DynamicBoolean descend, NodeListener l) {
                return null;
            }

            @Override
            public void unsubscribe(Object key) {

            }

            @Override
            public void discard() {

            }

            @Override
            public boolean sorted() {
                return false;
            }

            @Override
            public Image openIcon() {
                return null;
            }

            @Override
            public Fuzzy hasChildren() {
                if (_o == null || _o.isProject() || _o.isSubject()) {
                    return Fuzzy.MAYBE;
                } else {
                    return Fuzzy.NO;
                }
            }

            @Override
            public void contents(final long start, final long end,
                    final TreeNodeContentsHandler ch) {
                XmlStringWriter w = new XmlStringWriter();
                w.add("size", "infinity");
                w.add("sort", true);
                w.add("isleaf", true);
                if (_o != null) {
                    w.add("id", _o.id());
                }
                Session.execute("om.pssd.collection.member.list", w.document(),
                        new ServiceResponseHandler() {

                            @Override
                            public void processResponse(XmlElement xe,
                                    List<Output> outputs) throws Throwable {
                                List<XmlElement> oes = xe.elements("object");
                                if (oes != null && !oes.isEmpty()) {
                                    List<Node> os = new ArrayList<Node>(oes
                                            .size());
                                    for (XmlElement oe : oes) {
                                        os.add(new DestinationNode(
                                                new DObjectRef(DObject
                                                        .create(oe), false,
                                                        true)));
                                    }
                                    ch.loaded(start, end, os.size(), os);
                                    return;
                                }
                                ch.loaded(0, 0, 0, null);
                            }
                        });
            }

            @Override
            public void add(Node n, TreeNodeAddHandler ah) {

            }

            @Override
            public void remove(Node n, TreeNodeRemoveHandler rh) {

            }

        }

        private DestinationNode _root;

        public DestinationTree() {
            _root = new DestinationNode(null);
        }

        @Override
        public Image icon() {
            return null;
        }

        @Override
        public Container root() {
            return _root;
        }

        @Override
        public boolean readOnly() {
            return true;
        }

        @Override
        public void setReadOnly(boolean readOnly) {

        }

        @Override
        public void discard() {

        }

    }

    private DObjectRef _srcStudy;
    private TreeGUI _treeGUI;
    private boolean _changed;
    private DObjectRef _selected;

    public StudySendForm(DObjectRef srcStudy) {
        _srcStudy = srcStudy;
        _treeGUI = new TreeGUI(new DestinationTree(), ScrollPolicy.AUTO,
                new TreeGUIEventHandler() {

                    @Override
                    public void clicked(Node n) {

                    }

                    @Override
                    public void selected(Node n) {
                        DObjectRef oldSelected = _selected;
                        _selected = (DObjectRef) n.object();
                        _changed = ObjectUtil.equals(oldSelected, _selected);
                        StudySendForm.this.notifyOfChangeInState();
                    }

                    @Override
                    public void deselected(Node n) {
                        DObjectRef oldSelected = _selected;
                        _selected = null;
                        _changed = ObjectUtil.equals(oldSelected, _selected);
                        StudySendForm.this.notifyOfChangeInState();
                    }

                    @Override
                    public void opened(Node n) {

                    }

                    @Override
                    public void closed(Node n) {

                    }

                    @Override
                    public void added(Node n) {

                    }

                    @Override
                    public void removed(Node n) {

                    }

                    @Override
                    public void changeInMembers(Node n) {

                    }
                });
        _treeGUI.setShowRoot(false);
        _treeGUI.fitToParent();

    }

    @Override
    public boolean changed() {
        return _changed;
    }

    @Override
    public Validity valid() {
        if (_selected == null) {
            return new IsNotValid(
                    "No destination subject or ex-method is selected.");
        }
        if (!(_selected.isSubject() || _selected.isExMethod())) {
            return new IsNotValid("You must select a subject or ex-method.");
        }
        String srcStudyCid = _srcStudy.id();
        String dstParentCid = _selected.id();
        if (srcStudyCid.startsWith(dstParentCid + ".")) {
            return new IsNotValid("You cannot send to the same "
                    + _selected.referentTypeName() + ".");
        }
        return IsValid.INSTANCE;
    }

    @Override
    public Widget gui() {
        return _treeGUI;
    }

    @Override
    public void execute(ActionListener l) {
        XmlStringWriter w = new XmlStringWriter();
        w.add("cid", _srcStudy.id());
        w.add("to", _selected.id());
        Session.execute("daris.study.copy", w.document(),
                new ServiceResponseHandler() {
                    @Override
                    public void processResponse(XmlElement xe,
                            List<Output> outputs) throws Throwable {
                        MessageBox.info(
                                "Study sent",
                                "Study " + _srcStudy.id()
                                        + " has been sent to "
                                        + _selected.referentTypeName() + " "
                                        + _selected.id() + ".", 3);
                    }
                });
        if (l != null) {
            l.executed(true);
        }
    }

    public void show(Window owner, ActionListener al) {
        DialogProperties dp = new DialogProperties(
                DialogProperties.Type.ACTION, "Send study " + _srcStudy.id() + " to...", this);
        dp.setActionEnabled(false);
        dp.setButtonAction(this);
        dp.setCancelLabel("Cancel");
        dp.setButtonLabel("Send");
        dp.setOwner(owner);
        dp.setModal(true);
        dp.setScrollPolicy(ScrollPolicy.AUTO);
        dp.setSize(700, 500);
        Dialog dlg = Dialog.postDialog(dp, al);
        dlg.show();
    }
}
