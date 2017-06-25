package daris.client.ui.collection.share;

import arc.gui.form.Field;
import arc.gui.form.FieldDefinition;
import arc.gui.form.FieldRenderOptions;
import arc.gui.form.Form;
import arc.gui.form.TextFieldRenderOptions;
import arc.mf.dtype.TextType;
import daris.client.model.collection.download.DownloaderSettings;
import daris.client.model.object.DObjectRef;
import daris.client.ui.collection.download.DownloaderSettingsForm;

public class DownloaderShareForm extends DownloaderSettingsForm {

    private Field<String> _urlField;

    public DownloaderShareForm(DObjectRef obj, DownloaderSettings settings) {
        super(obj, settings);
    }

    protected void appendToForm(Form form) {
        if (_urlField == null) {
            _urlField = new Field<String>(
                    new FieldDefinition("URL", "url", TextType.DEFAULT, "Downloader link", null, 0, 1));
            FieldRenderOptions options = new FieldRenderOptions();
            options.addOption(TextFieldRenderOptions.AUTO_RESIZE, true);
            _urlField.setRenderOptions(options);
        }
        form.add(_urlField);
    }

    public void setUrl(String url) {
        _urlField.setValue(url);
    }

}
