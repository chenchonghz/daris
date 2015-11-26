package daris.client.gui.form;

import java.util.ArrayList;
import java.util.List;

import arc.gui.ValidatedInterfaceComponent;
import javafx.scene.Parent;

@SuppressWarnings("rawtypes")
public class Form extends ValidatedInterfaceComponent {

    private Parent _gui;
    private List<FormItem> _items;

    public Form() {
        _items = new ArrayList<FormItem>();
    }

    public void add(FormItem item) {
        _items.add(item);
        addMustBeValid(item);
    }

    public void remove(FormItem item) {
        _items.remove(item);
        removeMustBeValid(item);
    }

    @Override
    public Parent gui() {
        if (_gui == null) {
            _gui = FormGUIFactory.createFormGUI(this);
        }
        return _gui;
    }

}
