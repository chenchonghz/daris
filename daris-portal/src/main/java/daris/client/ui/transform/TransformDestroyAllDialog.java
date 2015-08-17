package daris.client.ui.transform;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import arc.gui.gwt.widget.dialog.Dialog;
import arc.gui.gwt.widget.scroll.ScrollPolicy;
import arc.gui.window.Window;
import arc.mf.client.util.ActionListener;
import daris.client.model.transform.Transform.Status;
import daris.client.model.transform.messages.TransformDestroyAll;

public class TransformDestroyAllDialog {

    private Set<Status.State> _states;
    private int _width;
    private int _height;

    private TransformDestroyAllDialog(Collection<Status.State> states,
            int width, int height) {
        _width = width;
        _height = height;
        _states = new HashSet<Status.State>();
        if (states != null && !states.isEmpty()) {
            _states.addAll(states);
        } else {
            _states.add(Status.State.terminated);
            _states.add(Status.State.failed);
            _states.add(Status.State.unknown);
        }
    }

    public TransformDestroyAllDialog(Collection<Status.State> states) {
        this(states, 320, 240);
    }

    public TransformDestroyAllDialog() {
        this(null);
    }

    public void show(Window owner, final ActionListener al) {
        Dialog dlg = Dialog.postDialog(owner, null, "Delete transforms",
                "Delete", "Cancel", new TransformDestroyAllForm(_states),
                _width, _height, ScrollPolicy.AUTO, new ActionListener() {

                    @Override
                    public void executed(boolean succeeded) {
                        if (succeeded) {
                            new TransformDestroyAll(_states).send();
                            if (al != null) {
                                al.executed(true);
                            }
                        } else {
                            if (al != null) {
                                al.executed(false);
                            }
                        }
                    }
                });
        dlg.show();
    }

    public void show(Window owner) {
        show(owner, null);
    }

}
