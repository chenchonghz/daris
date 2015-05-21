package daris.client.ui.dictionary;

import arc.gui.ValidatedInterfaceComponent;
import arc.gui.dialog.DialogProperties;
import arc.gui.form.Field;
import arc.gui.form.FieldDefinition;
import arc.gui.form.Form;
import arc.gui.form.FormEditMode;
import arc.gui.form.FormItem;
import arc.gui.form.FormItem.Property;
import arc.gui.form.FormItemListener;
import arc.gui.gwt.widget.dialog.Dialog;
import arc.mf.client.util.ActionListener;
import arc.mf.client.util.AsynchronousAction;
import arc.mf.dtype.StringType;
import arc.mf.model.dictionary.DictionaryRef;
import arc.mf.model.dictionary.Term;
import arc.mf.model.dictionary.TermRef;
import arc.mf.model.dictionary.messages.AddEntry;
import arc.mf.object.ObjectMessageResponse;

import com.google.gwt.user.client.ui.Widget;

public class TermAddForm extends ValidatedInterfaceComponent implements AsynchronousAction {

    private String _otype;
    private DictionaryRef _dict;
    private Form _form;
    private String _term;
    private String _defn;

    public TermAddForm(String otype, DictionaryRef dict) {

        _otype = otype;
        _dict = dict;
        _form = new Form(FormEditMode.CREATE);
        Field<String> termField = new Field<String>(new FieldDefinition(_otype, StringType.DEFAULT, null, null, 1, 1));
        termField.addListener(new FormItemListener<String>() {

            @Override
            public void itemValueChanged(FormItem<String> f) {
                _term = f.value();
            }

            @Override
            public void itemPropertyChanged(FormItem<String> f, Property property) {

            }
        });
        _form.add(termField);
        Field<String> defnField = new Field<String>(new FieldDefinition(_otype.equalsIgnoreCase("term") ? "definition"
                : "description", StringType.DEFAULT, null, null, 0, 1));
        defnField.addListener(new FormItemListener<String>() {

            @Override
            public void itemValueChanged(FormItem<String> f) {
                _defn = f.value();
            }

            @Override
            public void itemPropertyChanged(FormItem<String> f, Property property) {

            }
        });
        _form.add(defnField);
        _form.render();
    }

    @Override
    public Widget gui() {
        return _form;
    }

    @Override
    public void execute(final ActionListener l) {
        Term term = new Term(_dict, _term);
        term.addDefinition(_defn);
        new AddEntry(term).send(new ObjectMessageResponse<TermRef>() {

            @Override
            public void responded(TermRef r) {
                if (l != null) {
                    l.executed(r != null);
                }
            }
        });
    }

    public void showDialog(arc.gui.window.Window owner, ActionListener al) {
        DialogProperties dp = new DialogProperties(DialogProperties.Type.ACTION, "Create new " + _otype, this);
        dp.setButtonAction(this);
        dp.setActionEnabled(false);
        dp.setButtonLabel("Create");
        dp.setModal(true);
        dp.setOwner(owner);
        dp.setSize(320, 200);
        Dialog.postDialog(dp, al).show();
    }
}
