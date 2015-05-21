package daris.client.ui.query.action;

import arc.gui.ValidatedInterfaceComponent;
import arc.gui.form.Field;
import arc.gui.form.FieldDefinition;
import arc.gui.form.Form;
import arc.gui.form.FormItem;
import arc.gui.form.FormItem.Property;
import arc.gui.form.FormItemListener;
import arc.mf.client.util.ActionListener;
import arc.mf.client.util.AsynchronousAction;
import arc.mf.dtype.StringType;
import arc.mf.object.Null;
import arc.mf.object.ObjectMessageResponse;

import com.google.gwt.user.client.ui.Widget;

import daris.client.model.query.messages.QueryResultExport;

public class ResultExportForm extends ValidatedInterfaceComponent implements AsynchronousAction {

    private QueryResultExport _qre;
    private Form _form;

    public ResultExportForm(QueryResultExport qre) {
        _qre = qre;

        _form = new Form();
        _form.setMargin(20);

        Field<String> fileNameField = new Field<String>(new FieldDefinition("output file name", StringType.DEFAULT,
                null, null, 1, 1));
        fileNameField.setInitialValue(_qre.outputFileName(), false);
        fileNameField.addListener(new FormItemListener<String>() {

            @Override
            public void itemValueChanged(FormItem<String> f) {
                _qre.setOutputFileName(f.value());
            }

            @Override
            public void itemPropertyChanged(FormItem<String> f, Property property) {

            }
        });
        _form.add(fileNameField);

        addMustBeValid(_form);

        _form.render();

    }

    @Override
    public Widget gui() {
        return _form;
    }

    @Override
    public void execute(final ActionListener l) {
        if (valid().valid()) {
            _qre.send(new ObjectMessageResponse<Null>() {

                @Override
                public void responded(Null r) {
                    l.executed(r != null);
                }
            });
        } else {
            l.executed(false);
        }
    }

}
