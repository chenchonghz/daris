package daris.client.ui.transform;

import arc.gui.gwt.dnd.DragWidget;
import arc.gui.gwt.dnd.DropHandler;
import arc.gui.gwt.widget.panel.SimplePanel;
import arc.gui.gwt.widget.scroll.ScrollPanel;
import arc.gui.gwt.widget.scroll.ScrollPolicy;
import arc.gui.menu.ActionEntry;
import arc.gui.menu.Menu;
import arc.gui.object.SelectedObjectSet;
import arc.gui.object.display.ObjectDetailsDisplay;
import arc.gui.object.register.ObjectGUI;
import arc.gui.object.register.ObjectUpdateHandle;
import arc.gui.object.register.ObjectUpdateListener;
import arc.gui.window.Window;
import arc.mf.client.util.Action;
import arc.mf.object.ObjectResolveHandler;
import daris.client.model.transform.Transform;
import daris.client.model.transform.TransformRef;
import daris.client.model.transform.messages.TransformDestroy;
import daris.client.model.transform.messages.TransformResume;
import daris.client.model.transform.messages.TransformStatusGet;
import daris.client.model.transform.messages.TransformSuspend;
import daris.client.model.transform.messages.TransformTerminate;

public class TransformGUI implements ObjectGUI {

    public static final TransformGUI INSTANCE = new TransformGUI();

    private TransformGUI() {
    }

    @Override
    public String idToString(Object o) {
        if (o instanceof TransformRef) {
            return Long.toString(((TransformRef) o).uid());
        }
        return null;
    }

    @Override
    public String icon(Object o, int size) {
        return null;
    }

    @Override
    public Menu actionMenu(Window w, Object o, SelectedObjectSet selected, boolean readOnly) {
        if (readOnly) {
            return null;
        }
        final TransformRef t = (TransformRef) o;
        ActionEntry suspendAE = new ActionEntry("Suspend", new Action() {

            @Override
            public void execute() {
                new TransformSuspend(t).send();
            }
        });
        ActionEntry resumeAE = new ActionEntry("Resume", new Action() {

            @Override
            public void execute() {
                new TransformResume(t).send();
            }
        });
        ActionEntry terminateAE = new ActionEntry("Terminate", new Action() {

            @Override
            public void execute() {
                new TransformTerminate(t).send();
            }
        });
        ActionEntry deleteAE = new ActionEntry("Delete", new Action() {

            @Override
            public void execute() {
                new TransformDestroy(t).send();
            }
        });
        ActionEntry refreshAE = new ActionEntry("Refresh", new Action() {

            @Override
            public void execute() {
                new TransformStatusGet(t).send();
            }
        });
        ActionEntry refreshAllAE = new ActionEntry("Refresh All", new Action() {

            @Override
            public void execute() {
                new TransformStatusGet().send();
            }
        });

        Menu menu = new Menu();
        switch (t.state()) {
        case pending:
            menu.add(terminateAE);
            break;
        case running:
            menu.add(suspendAE);
            menu.add(terminateAE);
            break;
        case suspended:
            menu.add(resumeAE);
            menu.add(terminateAE);
            break;
        case terminated:
            menu.add(deleteAE);
            break;
        default:
            break;
        }
        menu.add(refreshAE);
        menu.add(refreshAllAE);
        return menu;
    }

    @Override
    public Menu memberActionMenu(Window w, Object o, SelectedObjectSet selected, boolean readOnly) {
        return null;
    }

    @Override
    public Object reference(Object o) {
        return null;
    }

    @Override
    public boolean needToResolve(Object o) {
        return false;
    }

    @Override
    public void displayDetails(final Object o, final ObjectDetailsDisplay dd, boolean forEdit) {
        if (forEdit) {
            throw new AssertionError("Not implemented.");
        }
        if (o instanceof TransformRef) {
            ((TransformRef) o).reset();
            ((TransformRef) o).resolve(new ObjectResolveHandler<Transform>() {

                @Override
                public void resolved(Transform t) {
                    SimplePanel sp = new SimplePanel();
                    sp.fitToParent();
                    sp.setContent(new ScrollPanel(TransformForm.formForView(t), ScrollPolicy.BOTH));
                    dd.display(o, sp);
                }
            });
        }
    }

    @Override
    public void open(Window w, Object o) {

    }

    @Override
    public DropHandler dropHandler(Object o) {
        return null;
    }

    @Override
    public DragWidget dragWidget(Object o) {
        return null;
    }

    @Override
    public ObjectUpdateHandle createUpdateMonitor(Object o, ObjectUpdateListener ul) {
        return null;
    }

}
