package daris.client.ui.sc;

import arc.gui.ValidatedInterfaceComponent;
import arc.gui.form.Field;
import arc.gui.form.FieldDefinition;
import arc.gui.form.FieldGroup;
import arc.gui.form.Form;
import arc.gui.form.FormEditMode;
import arc.gui.form.FormItem;
import arc.gui.form.FormItem.Property;
import arc.gui.form.FormItemListener;
import arc.gui.gwt.widget.panel.SimplePanel;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.gui.gwt.widget.scroll.ScrollPanel;
import arc.gui.gwt.widget.scroll.ScrollPolicy;
import arc.mf.client.dti.DTI;
import arc.mf.client.util.ActionListener;
import arc.mf.client.util.ObjectUtil;
import arc.mf.dtype.ConstantType;
import arc.mf.dtype.EnumerationType;
import arc.mf.object.ObjectResolveHandler;

import com.google.gwt.user.client.ui.Widget;

import daris.client.model.sc.Archive;
import daris.client.model.sc.DeliveryDestination;
import daris.client.model.sc.DeliveryDestinationEnum;
import daris.client.model.sc.DeliveryMethod;
import daris.client.model.sc.ShoppingCart;
import daris.client.model.sink.Sink;
import daris.client.ui.sc.sink.SinkForm;

public class DestinationForm extends ValidatedInterfaceComponent {

    private ShoppingCart _cart;
    private FormEditMode _mode;
    private SimplePanel _sp;

    @SuppressWarnings("rawtypes")
    private SinkForm _sinkForm;

    public DestinationForm(ShoppingCart cart, FormEditMode mode) {
        _cart = cart;
        _mode = mode;
        _sp = new SimplePanel();
        _sp.fitToParent();

        updateForms();

    }

    private void addDestinationForm(VerticalPanel vp) {

        Form form = new Form(_mode);
        Field<DeliveryDestination> destinationField = new Field<DeliveryDestination>(new FieldDefinition("destination",
                new EnumerationType<DeliveryDestination>(new DeliveryDestinationEnum()), "destination", null, 1, 1));
        destinationField.setInitialValue(_cart.destination(), false);
        destinationField.addListener(new FormItemListener<DeliveryDestination>() {

            @Override
            public void itemValueChanged(FormItem<DeliveryDestination> f) {
                if (!ObjectUtil.equals(_cart.destination(), f.value())) {
                    if (_cart.destination() != null && _cart.destination().method() == DeliveryMethod.deposit) {
                        _cart.destination().saveSinkSettings(null);
                    }
                    _cart.setDestination(f.value());
                    if (_cart.destination().method() == DeliveryMethod.deposit) {
                        _cart.destination().loadAndApplySinkSettings(new ActionListener() {
                            @Override
                            public void executed(boolean succeeded) {
                                updateForms();
                            }
                        });
                    } else {
                        updateForms();
                    }
                }
            }

            @Override
            public void itemPropertyChanged(FormItem<DeliveryDestination> f, Property property) {

            }
        });
        form.add(destinationField);
        addMustBeValid(form);
        form.render();
        vp.add(form);
    }

    private void addArchiveForm(VerticalPanel vp) {

        Form form = new Form(_mode);
        FieldGroup arcFieldGroup = new FieldGroup(new FieldDefinition("archive", ConstantType.DEFAULT,
                "the archive specification.", null, 1, 1));
        Field<Archive.Type> arcTypeField = new Field<Archive.Type>(new FieldDefinition("type",
                new EnumerationType<Archive.Type>(Archive.Type.values()), "the archive type.", null, 1, 1));
        arcTypeField.addListener(new FormItemListener<Archive.Type>() {

            @Override
            public void itemValueChanged(FormItem<Archive.Type> f) {
                _cart.setArchive(new Archive(f.value()));
            }

            @Override
            public void itemPropertyChanged(FormItem<Archive.Type> f, Property property) {

            }
        });
        arcTypeField.setInitialValue(_cart.archive().type(), false);
        arcFieldGroup.add(arcTypeField);
        form.add(arcFieldGroup);
        addMustBeValid(form);
        form.render();
        vp.add(form);
    }

    private void addSinkForm(final VerticalPanel vp) {

        if (_cart.destination().method() != DeliveryMethod.deposit || _cart.destination().sink() == null) {
            return;
        }
        _cart.destination().sink().resolve(new ObjectResolveHandler<Sink>() {

            @Override
            public void resolved(final Sink sink) {
                _sinkForm = SinkForm.create(_cart.destination(), sink, _mode);
                addMustBeValid(_sinkForm);
                vp.add(_sinkForm.gui());
            }
        });
    }
    


    private void updateForms() {

        removeAllMustBeValid();
        _sp.clear();

        VerticalPanel vp = new VerticalPanel();
        vp.setWidth100();
        addDestinationForm(vp);

        DeliveryDestination dst = _cart.destination();
        if (dst.method() == DeliveryMethod.download) {
            addArchiveForm(vp);
            if(DTI.enabled()){
                addDTIOptionsForm(vp);
            }
        } else {
            addSinkForm(vp);
        }
        _sp.setContent(new ScrollPanel(vp, ScrollPolicy.AUTO));
    }
    
    private void addDTIOptionsForm( VerticalPanel vp){
        DTIDownloadOptionsForm form = new DTIDownloadOptionsForm();
        vp.add(form.gui());
        addMustBeValid(form);
    }

    @Override
    public Widget gui() {
        return _sp;
    }

}
