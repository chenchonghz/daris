package daris.client.ui.object;

import java.util.Date;
import java.util.List;

import com.google.gwt.user.client.ui.Widget;

import arc.gui.ValidatedInterfaceComponent;
import arc.gui.dialog.DialogProperties;
import arc.gui.form.Field;
import arc.gui.form.FieldRenderOptions;
import arc.gui.form.Form;
import arc.gui.gwt.colour.RGB;
import arc.gui.gwt.widget.HTML;
import arc.gui.gwt.widget.dialog.Dialog;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.gui.window.Window;
import arc.mf.client.Output;
import arc.mf.client.util.ActionListener;
import arc.mf.client.util.AsynchronousAction;
import arc.mf.client.util.IsNotValid;
import arc.mf.client.util.Validity;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlStringWriter;
import arc.mf.dtype.DateType;
import arc.mf.dtype.EnumerationType;
import arc.mf.dtype.IntegerType;
import arc.mf.dtype.TextType;
import arc.mf.object.ObjectResolveHandler;
import arc.mf.session.ServiceResponseHandler;
import arc.mf.session.Session;
import daris.client.model.dataset.DataSet;
import daris.client.model.object.DObject;
import daris.client.model.object.DObjectRef;

public class UrlShareForm extends ValidatedInterfaceComponent
        implements AsynchronousAction {

    private DObjectRef _obj;
    private VerticalPanel _vp;
    private Form _form;
    private Field<String> _formatField;
    private Field<Integer> _usesField;
    private Field<Date> _expiryDateField;
    private Field<String> _urlField;
    private HTML _statusHtml;

    public UrlShareForm(DObjectRef obj) {
        _obj = obj;
        _form = new Form() {
            @Override
            public Validity valid() {
                Validity validity = super.valid();
                boolean hasUses = _usesField.value() != null;
                boolean hasExpiryDate = _expiryDateField.value() != null;
                if (hasExpiryDate && _expiryDateField.value()
                        .getTime() < new Date().getTime()) {
                    validity = new IsNotValid(
                            "Expiry date is invalid. It is earlier than current date(time).");
                }
                if (!hasUses && !hasExpiryDate) {
                    validity = new IsNotValid(
                            "Either uses or expiry date must be set.");
                }
                if (validity.valid()) {
                    _statusHtml.clear();
                } else {
                    _statusHtml.setHTML(validity.reasonForIssue());
                }
                return validity;
            }
        };
        _form.fitToParent();
        _form.setPadding(20);

        /*
         * format
         */
        _formatField = new Field<String>("Format", new EnumerationType<String>(
                new String[] { "zip", "tar.gz", "aar" }), null, 0, 1);
        _formatField.setValue("zip");
        _formatField.setRenderOptions(new FieldRenderOptions().setWidth(118));
        _form.add(_formatField);

        /*
         * number of uses
         */
        _usesField = new Field<Integer>("Uses", IntegerType.POSITIVE_ONE, null,
                0, 1);
        _form.add(_usesField);

        /*
         * expiry date
         */
        _expiryDateField = new Field<Date>("Expiry Date", DateType.DATE_ONLY,
                null, 0, 1);
        _expiryDateField.setValue(new Date(
                new Date().getTime() + (1000L * 60L * 60L * 24L * 3L)));
        _form.add(_expiryDateField);

        /*
         * url
         */
        _urlField = new Field<String>("Download URL", TextType.DEFAULT, null, 0,
                1);
        _urlField.setWidth(400);
        _urlField.setHeight(150);
        _form.add(_urlField);

        _form.render();

        addMustBeValid(_form);

        _statusHtml = new HTML();
        _statusHtml.setWidth100();
        _statusHtml.setHeight(22);
        _statusHtml.setPaddingLeft(15);
        _statusHtml.setColour(RGB.RED);

        _vp = new VerticalPanel();
        _vp.fitToParent();

        _vp.add(_form);
        _vp.add(_statusHtml);
    }

    @Override
    public Widget gui() {
        return _vp;
    }

    @Override
    public void execute(final ActionListener al) {
        XmlStringWriter w = new XmlStringWriter();
        w.add("role", new String[] { "type", "user" },
                Session.domainName() + ":" + Session.userName());
        if (_obj.isDataSet()) {
            w.push("service", new String[] { "name", "asset.get" });
            w.add("cid", _obj.id());
            w.pop();
        } else {
            w.push("service", new String[] { "name", "asset.archive.create" });
            w.add("where", "cid='" + _obj.id() + "' or cid starts with '"
                    + _obj.id() + "'");
            w.add("for", "user");
            w.add("format", _formatField.value());
            w.add("remove-path-prefix", "daris/pssd");
            w.pop();
        }
        w.add("min-token-length", 20);
        w.add("max-token-length", 20);
        w.add("grant-caller-transient-roles", true);

        Date expiryDate = _expiryDateField.value();
        if (expiryDate != null && expiryDate.getTime() > new Date().getTime()) {
            w.add("to", expiryDate);
        }
        Integer uses = _usesField.value();
        if (uses != null && uses > 0) {
            w.add("use-count", uses);
        }
        w.add("tag", "daris-url-share-" + _obj.id());
        Session.execute("secure.identity.token.create", w.document(),
                new ServiceResponseHandler() {

                    @Override
                    public void processResponse(XmlElement xe,
                            List<Output> outputs) throws Throwable {
                        final String token = xe.value("token");
                        _obj.resolve(new ObjectResolveHandler<DObject>() {

                            @Override
                            public void resolved(DObject o) {
                                String ext = null;
                                if (o instanceof DataSet) {
                                    DataSet ds = ((DataSet) o);
                                    if (ds.data() != null
                                            && ds.data().extension() != null) {
                                        ext = ds.data().extension();
                                    }
                                } else {
                                    ext = _formatField.value();
                                }
                                _urlField.setValue(urlFor(o.id(), ext, token));
                                _urlField.setFocus(true);
                                _urlField.selectValue();

                                _form.render();
                                if (al != null) {
                                    al.executed(false);
                                }
                            }
                        });
                    }
                });
    }

    private static String urlFor(String cid, String ext, String token) {
        StringBuilder sb = new StringBuilder();
        sb.append(com.google.gwt.user.client.Window.Location.getProtocol());
        sb.append("//");
        sb.append(com.google.gwt.user.client.Window.Location.getHost());
        sb.append("/mflux/execute.mfjp?token=");
        sb.append(token);
        sb.append("&filename=");
        sb.append(cid);
        if (ext != null) {
            sb.append(".");
            sb.append(ext);
        }
        return sb.toString();
    }

    public void showDialog(Window owner) {
        DialogProperties dp = new DialogProperties(DialogProperties.Type.ACTION,
                "Generate Sharable Download URL", this);
        dp.setButtonAction(this);
        dp.setButtonLabel("Generate");
        dp.setActionEnabled(true);
        dp.setModal(true);
        dp.setOwner(owner);
        dp.setCancelLabel("Dismiss");
        dp.setSize(550, 320);
        Dialog.postDialog(dp).show();
    }

}
