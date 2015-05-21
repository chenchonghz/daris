package daris.client.ui.query.filter.item.mf;

import java.util.List;

import arc.gui.form.Field;
import arc.gui.form.FieldDefinition;
import arc.gui.form.FieldRenderOptions;
import arc.gui.form.Form;
import arc.gui.form.FormEditMode;
import arc.gui.form.FormItem;
import arc.gui.form.FormItem.Property;
import arc.gui.form.FormItemListener;
import arc.gui.gwt.widget.HTML;
import arc.gui.gwt.widget.panel.HorizontalPanel;
import arc.gui.gwt.widget.panel.SimplePanel;
import arc.mf.client.util.ObjectUtil;
import arc.mf.dtype.DynamicEnumerationDataHandler;
import arc.mf.dtype.EnumerationType;
import arc.mf.dtype.EnumerationType.Value;
import arc.mf.model.authentication.DomainEnumerationDataSource;
import arc.mf.model.authentication.DomainRef;
import arc.mf.model.authentication.UserRef;

import com.google.gwt.user.client.ui.Widget;

import daris.client.mf.user.UserEnumerationDataSource;
import daris.client.model.query.filter.mf.CreatedByFilter;
import daris.client.ui.query.filter.form.CompositeFilterForm;
import daris.client.ui.query.filter.item.FilterItem;

public class CreatedByFilterItem extends FilterItem<CreatedByFilter> {
    private HorizontalPanel _hp;
    private SimplePanel _formSP;
    private Form _form;

    public CreatedByFilterItem(CompositeFilterForm cform, CreatedByFilter filter, boolean editable) {
        super(cform, filter, editable);
        _hp = new HorizontalPanel();
        _hp.setHeight(22);

        HTML label = new HTML("created by");
        label.setFontSize(11);
        label.setMarginTop(8);
        _hp.add(label);
        _hp.setSpacing(3);

        _formSP = new SimplePanel();
        _formSP.setHeight100();
        _hp.add(_formSP);

        updateForm();
    }

    private void updateForm() {

        if (_form != null) {
            removeMustBeValid(_form);
        }

        _formSP.clear();

        _form = new Form(editable() ? FormEditMode.UPDATE : FormEditMode.READ_ONLY);
        _form.setNumberOfColumns(filter().self() ? 1 : 3);
        _form.setShowLabels(false);
        _form.setShowDescriptions(false);
        _form.setShowHelp(false);

        Field<String> selfField = new Field<String>(new FieldDefinition("self", new EnumerationType<String>(
                new String[] { "self", "user" }), null, null, 1, 1));
        selfField.setInitialValue(filter().self() ? "self" : "user", false);
        selfField.addListener(new FormItemListener<String>() {

            @Override
            public void itemValueChanged(FormItem<String> f) {
                filter().setSelf("self".equals(f.value()));
                updateForm();
            }

            @Override
            public void itemPropertyChanged(FormItem<String> f, Property property) {

            }
        });
        _form.add(selfField);

        if (!filter().self()) {
            final UserEnumerationDataSource ueds = new UserEnumerationDataSource(new DomainRef(filter().domain()));
            final Field<UserRef> userField = new Field<UserRef>(new FieldDefinition("user",
                    new EnumerationType<UserRef>(ueds) {
                        public String titleForValue(Object value) {
                            return ((UserRef) value).name();
                        }
                    }, null, null, 1, 1));
            if (filter().domain() != null && filter().user() != null) {
                ueds.retrieve(filter().user(), 0L, 1L, new DynamicEnumerationDataHandler<UserRef>() {

                    @Override
                    public void process(long start, long end, long total, List<Value<UserRef>> values) {
                        // TODO Auto-generated method stub

                    }

                });
                userField.setInitialValue(new UserRef(filter().domain() + ":" + filter().user()), false);
            }
            userField.addListener(new FormItemListener<UserRef>() {

                @Override
                public void itemValueChanged(FormItem<UserRef> f) {
                    UserRef user = f.value();
                    filter().setUser(user == null ? null : user.name());
                }

                @Override
                public void itemPropertyChanged(FormItem<UserRef> f, Property property) {

                }
            });
            FieldRenderOptions fro = new FieldRenderOptions();
            fro.setWidth(100);
            userField.setRenderOptions(fro);

            Field<DomainRef> domainField = new Field<DomainRef>(new FieldDefinition("domain",
                    new EnumerationType<DomainRef>(new DomainEnumerationDataSource()), null, null, 1, 1));
            if (filter().domain() != null) {
                domainField.setInitialValue(new DomainRef(filter().domain()), false);
            }
            domainField.addListener(new FormItemListener<DomainRef>() {

                @Override
                public void itemValueChanged(FormItem<DomainRef> f) {
                    DomainRef domain = f.value();
                    if (!ObjectUtil.equals(f.value(), filter().domain())) {
                        filter().setDomain(domain == null ? null : domain.name());
                        ueds.setDomain(domain);
                        userField.clear();
                    }
                }

                @Override
                public void itemPropertyChanged(FormItem<DomainRef> f, Property property) {

                }
            });
            fro = new FieldRenderOptions();
            fro.setWidth(100);
            domainField.setRenderOptions(fro);
            _form.add(domainField);
            _form.add(userField);
        }
        addMustBeValid(_form);
        _form.render();
        _formSP.setContent(_form);
    }

    @Override
    public Widget gui() {
        return _hp;
    }

}
