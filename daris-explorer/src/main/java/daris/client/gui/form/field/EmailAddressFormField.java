package daris.client.gui.form.field;

import daris.client.gui.form.FormItem;

public class EmailAddressFormField extends StringFormField {

    public static final String REGEX = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    public EmailAddressFormField(FormItem<String> formItem) {
        super(formItem, REGEX);
    }

}
