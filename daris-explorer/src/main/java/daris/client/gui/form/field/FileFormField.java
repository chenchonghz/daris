package daris.client.gui.form.field;

import java.io.File;

import arc.mf.dtype.FileType;
import daris.client.gui.control.FileField;
import daris.client.gui.form.FormItem;

public class FileFormField extends FileField {

    private FormItem<File> _formItem;

    public FileFormField(FormItem<File> formItem) {
        super(null);
        _formItem = formItem;
        File value = _formItem.value();
        if (value != null) {
            setFile((File) value);
        }
        arc.mf.dtype.FileType dataType = (FileType) formItem.dataType();
        setSelectDirectory(dataType.mustBeDirectory());
        fileProperty().addListener((obs, ov, nv) -> {
            _formItem.setValue(nv);
        });
        _formItem.addResetListener(item -> {
            setFile(item.value());
        });
    }

}
