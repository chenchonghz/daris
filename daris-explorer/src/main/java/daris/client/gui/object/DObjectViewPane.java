package daris.client.gui.object;

import org.apache.commons.lang.ObjectUtils;

import arc.mf.desktop.ui.util.ApplicationThread;
import daris.client.model.object.DObjectRef;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class DObjectViewPane extends StackPane {

    private DObjectRef _o;

    public DObjectViewPane() {

    }

    public void displayObject(DObjectRef o) {
        if (!ObjectUtils.equals(_o, o)) {
            _o = o;
            display(false);
        }
    }

    protected void display(boolean refresh) {
        ApplicationThread.execute(() -> {
            getChildren().clear();
        });
        if (_o == null) {
            return;
        }
        if (refresh) {
            _o.reset();
        }
        // show loading message
        StringBuilder loadingMessage = new StringBuilder("Loading ");
        loadingMessage.append(_o.referentTypeName());
        if (_o.citeableId() != null) {
            loadingMessage.append(" ");
            loadingMessage.append(_o.citeableId());
        }
        loadingMessage.append("...");
        ApplicationThread.execute(() -> {
            getChildren().add(new Label(loadingMessage.toString()));
        });
        _o.resolve(oo -> {
            ApplicationThread.execute(() -> {
                getChildren().clear();
                getChildren().add(DObjectView.create(oo));
            });
        });
    }

    public void refresh() {
        display(true);
    }

}
