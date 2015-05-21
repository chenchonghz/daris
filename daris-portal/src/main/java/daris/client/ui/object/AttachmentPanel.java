package daris.client.ui.object;

import java.util.List;
import java.util.Vector;

import arc.gui.InterfaceComponent;
import arc.gui.gwt.widget.ContainerWidget;
import arc.gui.gwt.widget.menu.MenuButton;
import arc.gui.gwt.widget.menu.MenuToolBar;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.mf.client.util.IsValid;
import arc.mf.client.util.MustBeValid;
import arc.mf.client.util.StateChangeListener;
import arc.mf.client.util.Validity;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;

import daris.client.Resource;
import daris.client.model.object.DObject;
import daris.client.model.object.DObjectRef;
import daris.client.ui.dti.file.LocalFileBrowser;
import daris.client.ui.util.ButtonUtil;

public class AttachmentPanel extends ContainerWidget implements MustBeValid, InterfaceComponent {

    private VerticalPanel _vp;
    private AttachmentGrid _aGrid;

    private List<StateChangeListener> _ls;

    public AttachmentPanel(DObjectRef o) {
        this(o.id());
    }

    public AttachmentPanel(DObject o) {
        this(o.id());
    }

    protected AttachmentPanel(String cid) {

        _vp = new VerticalPanel();

        _aGrid = new AttachmentGrid(cid);
        _aGrid.fitToParent();

        MenuToolBar actionMenuToolBar = new MenuToolBar();
        actionMenuToolBar.setHeight(28);
        actionMenuToolBar.setWidth100();
        MenuButton actionMenuButton = new MenuButton("Action", null);
        actionMenuToolBar.add(actionMenuButton);
        actionMenuButton.setMenu(_aGrid.actionMenu());
        actionMenuToolBar.add(ButtonUtil.createButton(Resource.INSTANCE.hardDrive16().getSafeUri().asString(), 16, 16,
                "Show Local Files", "Show Local File System", false, new ClickHandler() {

                    @Override
                    public void onClick(ClickEvent event) {
                        LocalFileBrowser.get().show(_vp.window());
                    }
                }));

        _vp.add(actionMenuToolBar);
        _vp.add(_aGrid);

        initWidget(_vp);
    }

    public void refresh() {
        _aGrid.refresh();
    }

    @Override
    public boolean changed() {
        return false;
    }

    @Override
    public void addChangeListener(StateChangeListener listener) {
        if (_ls == null) {
            _ls = new Vector<StateChangeListener>();
        }
        _ls.add(listener);
    }

    @Override
    public void removeChangeListener(StateChangeListener listener) {
        if (_ls != null) {
            _ls.remove(listener);
        }
    }

    @Override
    public Validity valid() {
        return IsValid.INSTANCE;
    }

    @Override
    public Widget gui() {
        return _vp;
    }

}
