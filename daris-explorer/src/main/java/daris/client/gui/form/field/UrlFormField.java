package daris.client.gui.form.field;

import daris.client.gui.form.FormItem;

public class UrlFormField extends StringFormField {

    public static final String REGEX = "^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";

    public UrlFormField(FormItem<String> formItem) {
        super(formItem, REGEX);
    }

}