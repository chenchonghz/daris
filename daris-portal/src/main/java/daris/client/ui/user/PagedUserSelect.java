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
import arc.mf.model.authentication.DomainEnumerationDataSource;
import arc.mf.model.authentication.DomainRef;
import arc.mf.model.authentication.UserRef;

public class PagedUserSelect extends ContainerWidget {

    private PagedUserList _userList;

    public PagedUserSelect() {
        VerticalPanel vp = new VerticalPanel();
        vp.fitToParent();

        Form domainForm = new Form();
        domainForm.setHeight(22);
        domainForm.setShowDescriptions(false);
        domainForm.setShowHelp(false);
        Field<DomainRef> domainField = new Field<DomainRef>(
                new FieldDefinition("domain", new EnumerationType<DomainRef>(
                        new DomainEnumerationDataSource()), null, null, 0, 1));
        domainField.setRenderOptions(new FieldRenderOptions().setWidth(200));
        domainField.addListener(new FormItemListener<DomainRef>() {

            @Override
            public void itemValueChanged(FormItem<DomainRef> f) {
                _userList.setDomain(f.value());
            }

            @Override
            public void itemPropertyChanged(FormItem<DomainRef> f,
                    Property property) {

            }
        });
        domainForm.add(domainField);
        domainForm.render();
        vp.add(domainForm);

        _userList = new PagedUserList(null);
        _userList.fitToParent();
        vp.add(_userList);

        initWidget(vp);
    }

    public void setRowContextMenuHandler(
            ListGridRowContextMenuHandler<UserRef> listGridRowContextMenuHandler) {
        _userList.setRowContextMenuHandler(listGridRowContextMenuHandler);

    }

}
