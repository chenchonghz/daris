package daris.client.ui.sc.sink;

import arc.gui.form.Field;
import arc.gui.form.FieldDefinition;
import arc.gui.form.Form;
import arc.gui.form.FormEditMode;
import arc.gui.form.FormItem;
import arc.gui.form.FormItem.Property;
import arc.gui.form.FormItemListener;
import arc.gui.gwt.widget.panel.SimplePanel;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.mf.dtype.BooleanType;
import arc.mf.dtype.ConstantType;
import arc.mf.dtype.StringType;
import arc.mf.session.Session;
import daris.client.model.sc.DeliveryArg;
import daris.client.model.sc.DeliveryDestination;
import daris.client.model.sink.WebDavSink;
import daris.client.util.URL;

public class WebDavSinkForm extends SinkForm<WebDavSink> {

    WebDavSinkForm(DeliveryDestination destination, WebDavSink sink, FormEditMode mode) {
        super(destination, sink, mode);
    }

    @Override
    void fillInArgs(SimplePanel sp) {

        VerticalPanel vp = new VerticalPanel();
        vp.fitToParent();

        Form form = new Form(mode());

        /*
         * url
         */
        DeliveryArg arg = destination().arg(WebDavSink.Param.URL.paramName());
        Field<String> url = new Field<String>(new FieldDefinition(WebDavSink.Param.URL.paramName(),
                sink().url() != null ? ConstantType.DEFAULT : StringType.DEFAULT, "The URL of the WebDAV server.",
                null, 1, 1));
        if (sink().url() != null) {
            url.setInitialValue(sink().url(), false);
        } else {
            if (arg != null) {
                url.setInitialValue(arg.value(), false);
            }
            url.addListener(new FormItemListener<String>() {

                @Override
                public void itemValueChanged(FormItem<String> f) {
                    destination().setArg(WebDavSink.Param.URL.paramName(), DeliveryArg.Type.delivery_arg, f.value());
                }

                @Override
                public void itemPropertyChanged(FormItem<String> f, Property property) {

                }
            });
        }
        form.add(url);

        /*
         * directory
         */
        arg = destination().arg(WebDavSink.Param.DIRECTORY.paramName());
        Field<String> directory = new Field<String>(
                new FieldDefinition(
                        WebDavSink.Param.DIRECTORY.paramName(),
                        StringType.DEFAULT,
                        "The base directory into which the data is placed. Defaults to the user's root directory on the WebDAV server.",
                        null, 0, 1));
        if (sink().directory() != null) {
            directory.setInitialValue(sink().directory(), false);
        }
        if (arg != null) {
            directory.setInitialValue(arg.value(), false);
        }
        directory.addListener(new FormItemListener<String>() {

            @Override
            public void itemValueChanged(FormItem<String> f) {
                destination().setArg(WebDavSink.Param.DIRECTORY.paramName(), DeliveryArg.Type.delivery_arg, f.value());
            }

            @Override
            public void itemPropertyChanged(FormItem<String> f, Property property) {

            }
        });
        form.add(directory);

        /*
         * decompress
         */
        arg = destination().arg(WebDavSink.Param.DECOMPRESS.paramName());
        Field<Boolean> decompress = new Field<Boolean>(new FieldDefinition(WebDavSink.Param.DECOMPRESS.paramName(),
                BooleanType.DEFAULT_TRUE_FALSE, "Decompress the data if compressed. Defaults to false.", null, 0, 1));
        if (sink().decompress()) {
            decompress.setInitialValue(sink().decompress(), false);
        }
        if (arg != null && arg.value() != null) {
            decompress.setInitialValue(Boolean.parseBoolean(arg.value()), false);
        }
        decompress.addListener(new FormItemListener<Boolean>() {

            @Override
            public void itemValueChanged(FormItem<Boolean> f) {
                destination().setArg(WebDavSink.Param.DECOMPRESS.paramName(), DeliveryArg.Type.delivery_arg, f.value());
            }

            @Override
            public void itemPropertyChanged(FormItem<Boolean> f, Property property) {

            }
        });
        form.add(decompress);

        /*
         * user
         */
        arg = destination().arg(WebDavSink.Param.USER.paramName());
        Field<String> user = new Field<String>(new FieldDefinition(WebDavSink.Param.USER.paramName(),
                StringType.DEFAULT, "The WebDAV user name.", null, 1, 1));
        if (arg != null && arg.value() != null) {
            user.setInitialValue(arg.value(), false);
        }
        user.addListener(new FormItemListener<String>() {

            @Override
            public void itemValueChanged(FormItem<String> f) {
                destination().setArg(WebDavSink.Param.USER.paramName(), DeliveryArg.Type.delivery_arg, f.value());
            }

            @Override
            public void itemPropertyChanged(FormItem<String> f, Property property) {

            }
        });
        form.add(user);

        form.render();
        addMustBeValid(form);
        vp.add(form);

        /*
         * password
         */
        SecureArgForm passwordForm = new SecureArgForm(destination(), mode(), WebDavSink.Param.PASSWORD.paramName(),
                true, WebDavSink.Param.PASSWORD.paramName(), "The WebDAV user's password.", true) {
            @Override
            protected String generateSecureWalletKey() {
                DeliveryArg arg = destination().arg(WebDavSink.Param.URL.paramName());
                String host = arg != null ? URL.getHost(arg.value()) : "webdav-server";
                arg = destination().arg(WebDavSink.Param.USER.paramName());
                String user = arg != null ? arg.value() : Session.userName();
                StringBuilder sb = new StringBuilder();
                sb.append("password_webdav://");
                if (user != null) {
                    sb.append(user);
                    sb.append("@");
                }
                sb.append(host);
                return sb.toString();
            }
        };
        addMustBeValid(passwordForm);
        vp.add(passwordForm.gui());

        sp.setContent(vp);
    }
}
