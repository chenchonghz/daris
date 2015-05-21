package daris.client.ui.sc;

import arc.gui.ValidatedInterfaceComponent;
import arc.gui.form.Field;
import arc.gui.form.FieldDefinition;
import arc.gui.form.FieldRenderOptions;
import arc.gui.form.Form;
import arc.gui.form.Form.BooleanAs;
import arc.gui.form.FormItem;
import arc.gui.form.FormItem.Property;
import arc.gui.form.FormItemListener;
import arc.gui.gwt.widget.button.Button;
import arc.gui.gwt.widget.panel.HorizontalPanel;
import arc.gui.gwt.widget.panel.SimplePanel;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.mf.client.dti.file.DTIDirectory;
import arc.mf.client.file.LocalFile;
import arc.mf.dtype.BooleanType;
import arc.mf.dtype.ConstantType;
import arc.mf.object.ObjectResolveHandler;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;

import daris.client.cookies.ShoppingCartCookies;
import daris.client.model.file.LocalHomeDirectory;
import daris.client.ui.dti.file.LocalFileSelectDialog;
import daris.client.ui.dti.file.LocalFileSelectTarget;

public class DTIDownloadOptionsForm extends ValidatedInterfaceComponent {

    private SimplePanel _sp;
    private Form _form;
    private Form _dstDirForm;

    DTIDownloadOptionsForm() {
        _sp = new SimplePanel();
        _sp.fitToParent();
        updateForm();
    }

    private void updateForm() {

        _sp.clear();

        VerticalPanel vp = new VerticalPanel();
        vp.fitToParent();

        if (_form != null) {
            removeMustBeValid(_form);
        }
        _form = new Form();
        _form.setBooleanAs(BooleanAs.CHECKBOX);
        Field<Boolean> useDTIField = new Field<Boolean>(new FieldDefinition(
                "download via Arcitecta Desktop Integration applet", BooleanType.DEFAULT_TRUE_FALSE, null, null, 1, 1));
        useDTIField.setInitialValue(ShoppingCartCookies.useDTI(), false);
        useDTIField.addListener(new FormItemListener<Boolean>() {

            @Override
            public void itemValueChanged(FormItem<Boolean> f) {
                boolean useDTI = f.value();
                ShoppingCartCookies.setUseDTI(useDTI);
                if (!useDTI) {
                    ShoppingCartCookies.removeDTIDstDir();
                    ShoppingCartCookies.removeDTICompress();
                    ShoppingCartCookies.removeDTIOverwrite();
                }
                updateForm();
            }

            @Override
            public void itemPropertyChanged(FormItem<Boolean> f, Property property) {

            }
        });
        _form.add(useDTIField);
        if (ShoppingCartCookies.useDTI()) {
            Field<Boolean> extractArchiveField = new Field<Boolean>(new FieldDefinition("extract archive",
                    BooleanType.DEFAULT_TRUE_FALSE, null, null, 1, 1));
            extractArchiveField.setInitialValue(ShoppingCartCookies.dtiDecompress(), false);
            extractArchiveField.addListener(new FormItemListener<Boolean>() {

                @Override
                public void itemValueChanged(FormItem<Boolean> f) {
                    ShoppingCartCookies.setDTIDecompress(f.value());
                }

                @Override
                public void itemPropertyChanged(FormItem<Boolean> f, Property property) {

                }
            });
            _form.add(extractArchiveField);

            Field<Boolean> overwriteField = new Field<Boolean>(new FieldDefinition("overwrite if exists",
                    BooleanType.DEFAULT_TRUE_FALSE, null, null, 1, 1));
            overwriteField.setInitialValue(ShoppingCartCookies.dtiOverwrite(), false);
            overwriteField.addListener(new FormItemListener<Boolean>() {

                @Override
                public void itemValueChanged(FormItem<Boolean> f) {
                    ShoppingCartCookies.setDTIOverwrite(f.value());
                }

                @Override
                public void itemPropertyChanged(FormItem<Boolean> f, Property property) {

                }
            });
            _form.add(overwriteField);
        }
        _form.render();
        addMustBeValid(_form);
        vp.add(_form);

        if (_dstDirForm != null) {
            removeMustBeValid(_dstDirForm);
        }
        if (ShoppingCartCookies.useDTI()) {
            HorizontalPanel hp = new HorizontalPanel();
            hp.setHeight(22);
            hp.setWidth100();
            _dstDirForm = new Form();
            _dstDirForm.setWidth100();
            final Field<String> dstDirField = new Field<String>(new FieldDefinition("destination directory",
                    ConstantType.DEFAULT, null, null, 1, 1));
            FieldRenderOptions opts = new FieldRenderOptions();
            opts.setWidth100();
            dstDirField.setRenderOptions(opts);
            dstDirField.setInitialValue(ShoppingCartCookies.dtiDstDir(), false);
            if (ShoppingCartCookies.dtiDstDir() == null) {
                LocalHomeDirectory.downloads(new ObjectResolveHandler<DTIDirectory>() {

                    @Override
                    public void resolved(DTIDirectory dstDir) {
                        if (dstDir != null) {
                            dstDirField.setValue(dstDir.path());
                        }
                    }
                });
            }
            dstDirField.addListener(new FormItemListener<String>() {

                @Override
                public void itemValueChanged(FormItem<String> f) {
                    ShoppingCartCookies.setDTIDstDir(f.value());
                }

                @Override
                public void itemPropertyChanged(FormItem<String> f, Property property) {

                }
            });
            _dstDirForm.add(dstDirField);
            _dstDirForm.render();
            addMustBeValid(_dstDirForm);
            hp.add(_dstDirForm);
            Button selectDstDirButton = new Button("Select");
            selectDstDirButton.setMarginTop(5);
            selectDstDirButton.addClickHandler(new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {
                    LocalFileSelectDialog dlg = new LocalFileSelectDialog(LocalFileSelectTarget.DIRECTORY, null, null,
                            new LocalFileSelectDialog.FileSelectionHandler() {

                                @Override
                                public void fileSelected(LocalFile file) {
                                    dstDirField.setValue(file.path());
                                }
                            });
                    dlg.show(_sp.window());
                }
            });
            hp.add(selectDstDirButton);
            hp.addSpacer(25);
            vp.add(hp);
        }
        SimplePanel spacerSP = new SimplePanel();
        spacerSP.fitToParent();
        vp.add(spacerSP);
        _sp.setContent(vp);
    }

    @Override
    public Widget gui() {
        return _sp;
    }

}
