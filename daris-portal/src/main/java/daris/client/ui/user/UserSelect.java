package daris.client.ui.user;

import arc.gui.form.Field;
import arc.gui.form.FieldDefinition;
import arc.gui.form.FieldRenderOptions;
import arc.gui.form.Form;
import arc.gui.form.FormItem;
import arc.gui.form.FormItem.Property;
import arc.gui.form.FormItemListener;
import arc.gui.gwt.widget.ContainerWidget;
import arc.gui.gwt.widget.list.ListGridRowContextMenuHandler;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.mf.dtype.EnumerationType;
import arc.mf.dtype.StringType;
import arc.mf.model.authentication.DomainEnumerationDataSource;
import arc.mf.model.authentication.DomainRef;
import arc.mf.model.authentication.UserRef;

public class UserSelect extends ContainerWidget {

    private UserListGrid _userList;
    private DomainRef _domain;
    private String _filterString;

    public UserSelect() {
        _domain = null;
        _filterString = null;

        VerticalPanel vp = new VerticalPanel();
        vp.fitToParent();

        Form form = new Form();
        form.setHeight(22);
        form.setShowDescriptions(false);
        form.setShowHelp(false);
        form.setNumberOfColumns(2);
        /*
         * domain filter
         */
        Field<DomainRef> domainField = new Field<DomainRef>(
                new FieldDefinition("domain",
                        new EnumerationType<DomainRef>(
                                new DomainEnumerationDataSource()),
                        null, null, 0, 1));
        domainField.setRenderOptions(new FieldRenderOptions().setWidth(200));
        domainField.addListener(new FormItemListener<DomainRef>() {

            @Override
            public void itemValueChanged(FormItem<DomainRef> f) {
                _domain = f.value();
                _userList.setFilters(_domain, _filterString);
            }

            @Override
            public void itemPropertyChanged(FormItem<DomainRef> f,
                    Property property) {

            }
        });
        form.add(domainField);
        /*
         * name/email filter
         */
        Field<String> filterField = new Field<String>(new FieldDefinition(
                "filter", StringType.DEFAULT, null, null, 0, 1));
        filterField.setRenderOptions(new FieldRenderOptions().setWidth(200));
        filterField.addListener(new FormItemListener<String>() {

            @Override
            public void itemValueChanged(FormItem<String> f) {
                _filterString = f.value();
                _userList.setFilters(_domain, _filterString);
            }

            @Override
            public void itemPropertyChanged(FormItem<String> f,
                    Property property) {

            }
        });
        form.add(filterField);

        form.render();
        vp.add(form);

        _userList = new UserListGrid(_domain);
        _userList.fitToParent();
        vp.add(_userList);

        initWidget(vp);
    }

    public void setRowContextMenuHandler(
            ListGridRowContextMenuHandler<UserRef> listGridRowContextMenuHandler) {
        _userList.setRowContextMenuHandler(listGridRowContextMenuHandler);
    }

}
