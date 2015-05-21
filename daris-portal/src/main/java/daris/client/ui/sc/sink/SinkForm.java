package daris.client.ui.sc.sink;

import arc.gui.ValidatedInterfaceComponent;
import arc.gui.form.Field;
import arc.gui.form.FieldDefinition;
import arc.gui.form.Form;
import arc.gui.form.FormEditMode;
import arc.gui.gwt.widget.panel.SimplePanel;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.mf.dtype.ConstantType;

import com.google.gwt.user.client.ui.Widget;

import daris.client.model.sc.DeliveryDestination;
import daris.client.model.sink.FileSystemSink;
import daris.client.model.sink.OwnCloudSink;
import daris.client.model.sink.ScpSink;
import daris.client.model.sink.Sink;
import daris.client.model.sink.WebDavSink;

public abstract class SinkForm<T extends Sink> extends ValidatedInterfaceComponent {

    private DeliveryDestination _destination;
    private T _sink;
    private FormEditMode _mode;

    private VerticalPanel _vp;
    private SimplePanel _argFormSP;

    SinkForm(DeliveryDestination destination, T sink, FormEditMode mode) {
        _destination = destination;
        _sink = sink;
        _mode = mode;

        _vp = new VerticalPanel();
        _vp.fitToParent();

        /*
         * sink type
         */
        Form typeForm = new Form();
        Field<Sink.Type> typeField = new Field<Sink.Type>(new FieldDefinition("sink type", ConstantType.DEFAULT,
                "Sink type", null, 1, 1));
        typeField.setInitialValue(_sink.type(), false);
        typeForm.add(typeField);
        typeForm.setHeight(22);
        typeForm.render();
        _vp.add(typeForm);

        _argFormSP = new SimplePanel();
        _argFormSP.fitToParent();
        _vp.add(_argFormSP);

        fillInArgs(_argFormSP);
    }

    DeliveryDestination destination() {
        return _destination;
    }

    T sink() {
        return _sink;
    }

    FormEditMode mode() {
        return _mode;
    }

    abstract void fillInArgs(SimplePanel sp);

    @Override
    public Widget gui() {
        return _vp;
    }

    @SuppressWarnings({ "rawtypes" })
    public static <T extends Sink> SinkForm create(DeliveryDestination destination, T sink, FormEditMode mode) {
        switch (sink.type()) {
        case scp:
            return new ScpSinkForm(destination, (ScpSink) sink, mode);
        case webdav:
            return new WebDavSinkForm(destination, (WebDavSink) sink, mode);
        case owncloud:
            return new OwnCloudSinkForm(destination, (OwnCloudSink) sink, mode);
        case file_system:
            return new FileSystemSinkForm(destination, (FileSystemSink) sink, mode);
        default:
            throw new AssertionError("Unsupported sink type: " + sink.type());
        }
    }

}
