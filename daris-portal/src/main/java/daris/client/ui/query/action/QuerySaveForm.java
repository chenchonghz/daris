package daris.client.ui.query.action;

import arc.gui.ValidatedInterfaceComponent;
import arc.gui.form.Field;
import arc.gui.form.FieldDefinition;
import arc.gui.form.Form;
import arc.gui.form.FormItem;
import arc.gui.form.FormItem.Property;
import arc.gui.form.FormItemListener;
import arc.gui.gwt.widget.dialog.Dialog;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.mf.client.util.ActionListener;
import arc.mf.client.util.AsynchronousAction;
import arc.mf.dtype.ConstantType;
import arc.mf.dtype.EnumerationType;
import arc.mf.dtype.StringType;
import arc.mf.dtype.TextType;
import arc.mf.model.asset.AssetRef;
import arc.mf.object.Null;
import arc.mf.object.ObjectMessageResponse;

import com.google.gwt.user.client.ui.Widget;

import daris.client.model.query.Query;
import daris.client.model.query.QueryAssetRef;
import daris.client.model.query.messages.QueryAssetCreate;
import daris.client.model.query.messages.QueryAssetCreate.Access;
import daris.client.model.query.messages.QueryAssetSet;
import daris.client.ui.widget.MessageBox;

public class QuerySaveForm extends ValidatedInterfaceComponent implements AsynchronousAction {

    private QueryAssetCreate _qac;

    private VerticalPanel _vp;

    private Field<String> _nameField;
    private Field<String> _namespaceField;

    public QuerySaveForm(Query query) {
        _qac = new QueryAssetCreate(query.filter(), query.options());

        _vp = new VerticalPanel();
        _vp.fitToParent();
        _vp.setPadding(20);
/*
        HorizontalPanel nsHP = new HorizontalPanel();
        nsHP.setWidth100();
        nsHP.setPaddingRight(20);
        nsHP.setPaddingLeft(20);
        nsHP.setHeight(22);
        Label nsLabel = new Label("namespace:");
        nsLabel.setFontSize(11);
        nsLabel.setPaddingTop(8);
        nsLabel.setHeight100();
        nsHP.add(nsLabel);
        nsHP.setSpacing(8);

        TreeSelectComboBox<NamespaceRef> nsCombo = new TreeSelectComboBox<NamespaceRef>(_qac.namespace(),
                new NamespaceTree(), true) {

            @Override
            protected String toString(NamespaceRef ns) {
                return ns.path();
            }

            @Override
            protected boolean canSelect(Node n) {

                return n.object() != null && (n.object() instanceof NamespaceRef);
            }

            @Override
            protected NamespaceRef transform(Node n) {
                return (NamespaceRef) n.object();
            }
        };
        nsCombo.setWidth100();
        nsCombo.addSelectionHandler(new SelectionHandler<NamespaceRef>() {

            @Override
            public void selected(NamespaceRef o) {
                _qac.setNamespace(o);
            }
        });
        nsHP.add(nsCombo);

        _vp.add(nsHP);
*/
        Form form = new Form();
        form.fitToParent();
        
        _namespaceField = new Field<String>(new FieldDefinition("namespace",ConstantType.DEFAULT, "The asset namespace that the query is saved to.", null, 1,1));
        _namespaceField.setInitialValue(_qac.namespace(), false);
        form.add(_namespaceField);
        
        _nameField = new Field<String>(new FieldDefinition("name", StringType.DEFAULT, "name", null, 1, 1));
        _nameField.setInitialValue(_qac.name(), false);
        _nameField.addListener(new FormItemListener<String>() {

            @Override
            public void itemValueChanged(FormItem<String> f) {
                _qac.setName(f.value());
            }

            @Override
            public void itemPropertyChanged(FormItem<String> f, Property property) {

            }
        });
        form.add(_nameField);

        Field<String> descriptionField = new Field<String>(new FieldDefinition("description", TextType.DEFAULT,
                "description", null, 0, 1));
        descriptionField.setInitialValue(_qac.description(), false);
        descriptionField.addListener(new FormItemListener<String>() {

            @Override
            public void itemValueChanged(FormItem<String> f) {
                _qac.setDescription(f.value());
            }

            @Override
            public void itemPropertyChanged(FormItem<String> f, Property property) {

            }
        });
        form.add(descriptionField);

//        Field<QueryAssetCreate.Access> accessField = new Field<QueryAssetCreate.Access>(new FieldDefinition("access",
//                new EnumerationType<QueryAssetCreate.Access>(QueryAssetCreate.Access.values()), null, null, 1, 1));
//        accessField.setInitialValue(_qac.access(), false);
//        accessField.addListener(new FormItemListener<QueryAssetCreate.Access>() {
//
//            @Override
//            public void itemValueChanged(FormItem<Access> f) {
//                _qac.setAccess(f.value());
//                _namespaceField.setValue(_qac.namespace(),false);
//            }
//
//            @Override
//            public void itemPropertyChanged(FormItem<Access> f, Property property) {
//
//            }
//        });
//        form.add(accessField);

        form.render();

        addMustBeValid(form);
        _vp.add(form);

    }

    @Override
    public void execute(final ActionListener l) {
        AssetRef.exists(_qac.path(), new ObjectMessageResponse<Boolean>() {

            @Override
            public void responded(Boolean exists) {
                if (exists) {
                    _nameField.markInvalid("Asset path=" + _qac.path() + " already exists.");
                    Dialog.confirm(_vp.window(), "Asset exists", "Asset path=" + _qac.path()
                            + " already exists. Overwrite?", new ActionListener() {

                        @Override
                        public void executed(boolean succeeded) {
                            if (succeeded) {
                                l.executed(true);
                                setQueryAsset(_qac, l);
                            } else {
                                l.executed(false);
                            }
                        }
                    });
                } else {
                    createQueryAsset(_qac, l);
                }
            }
        });

    }

    private void createQueryAsset(QueryAssetCreate qac, final ActionListener l) {
        qac.send(new ObjectMessageResponse<QueryAssetRef>() {

            @Override
            public void responded(QueryAssetRef r) {
                if (r != null) {
                    MessageBox.info("Query", "Query " + r.name() + "(id=" + r.idToString() + ") has been created.", 2);
                }
                l.executed(r != null);
            }
        });
    }

    private void setQueryAsset(final QueryAssetCreate qac, final ActionListener l) {
        new QueryAssetSet(qac).send(new ObjectMessageResponse<Null>() {

            @Override
            public void responded(Null r) {
                MessageBox.info("Query", "Query (path=" + qac.path() + ") has been updated.", 2);
            }
        });
    }

    @Override
    public Widget gui() {
        return _vp;
    }

}
