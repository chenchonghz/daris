package daris.client.gui.form.field;

import daris.client.gui.form.FormItem;
import javafx.scene.control.TextField;

public class StringFormField extends TextField {

    private FormItem<String> _formItem;
    private String _regex;

    public StringFormField(FormItem<String> formItem) {
        this(formItem, null);
    }

    public StringFormField(FormItem<String> formItem, String regex) {
        super(formItem.value());
        _formItem = formItem;
        _regex = regex;
        textProperty().addListener((obs, ov, nv) -> {
            _formItem.setValue(nv);
        });
        _formItem.addResetListener((item) -> {
            textProperty().setValue(item.value());
        });
    }

    @Override
    public void replaceText(int start, int end, String text) {
        if (_regex == null || text.matches(_regex)) {
            super.replaceText(start, end, text);
        }
    }

    @Override
    public void replaceSelection(String text) {
        if (_regex == null || text.matches(_regex)) {
            super.replaceSelection(text);
        }
    }
}
