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
import arc.mf.client.util.IsNotValid;
import arc.mf.client.util.Validity;
import arc.mf.dtype.BooleanType;
import arc.mf.dtype.ConstantType;
import arc.mf.dtype.IntegerType;
import arc.mf.dtype.StringType;
import arc.mf.dtype.TextType;
import arc.mf.session.Session;
import daris.client.model.sc.DeliveryArg;
import daris.client.model.sc.DeliveryDestination;
import daris.client.model.sink.ScpSink;

public class ScpSinkForm extends SinkForm<ScpSink> {

	ScpSinkForm(DeliveryDestination destination, ScpSink sink, FormEditMode mode) {
		super(destination, sink, mode);
	}

	@Override
	public Validity valid() {
		Validity v = super.valid();
		if (v.valid()) {
			if (!destination().argExists(ScpSink.Param.PASSWORD.paramName())
					&& !destination().argExists(ScpSink.Param.PRIVATE_KEY.paramName())) {
				return new IsNotValid("The password or private key must be specified.");
			}
		}
		return v;
	}

	@Override
	void fillInArgs(SimplePanel sp) {

		VerticalPanel vp = new VerticalPanel();
		vp.fitToParent();

		Form form = new Form(mode());

		/*
		 * host
		 */
		DeliveryArg arg = destination().arg(ScpSink.Param.HOST.paramName());
		Field<String> serverHost = new Field<String>(new FieldDefinition(ScpSink.Param.HOST.paramName(), sink()
				.serverHost() != null ? ConstantType.DEFAULT : StringType.DEFAULT, "The SSH server host.", null, 1, 1));
		if (sink().serverHost() != null) {
			serverHost.setInitialValue(sink().serverHost(), false);
		} else {
			if (arg != null && arg.value() != null) {
				serverHost.setInitialValue(arg.value(), false);
			}
			serverHost.addListener(new FormItemListener<String>() {

				@Override
				public void itemValueChanged(FormItem<String> f) {
					destination().setArg(ScpSink.Param.HOST.paramName(), DeliveryArg.Type.delivery_arg, f.value());
				}

				@Override
				public void itemPropertyChanged(FormItem<String> f, Property property) {

				}
			});
		}
		form.add(serverHost);

		/*
		 * port
		 */
		 arg = destination().arg(ScpSink.Param.PORT.paramName());
		if (sink().serverHost() == null || sink().serverPort() != ScpSink.DEFAULT_SSH_PORT) {
			Field<Integer> serverPort = new Field<Integer>(new FieldDefinition(ScpSink.Param.PORT.paramName(), sink()
					.serverHost() != null ? ConstantType.DEFAULT : IntegerType.POSITIVE_ONE,
							"The SSH server port. Defaults to 22.", null, 0, 1));
			if (sink().serverPort() > 0) {
				serverPort.setInitialValue(sink().serverPort(), false);
			}
			if (arg != null && arg.value() != null) {
				serverPort.setInitialValue(Integer.parseInt(arg.value()), false);
			}
			if (sink().serverHost() == null) {
				serverPort.addListener(new FormItemListener<Integer>() {

					@Override
					public void itemValueChanged(FormItem<Integer> f) {
						destination().setArg(ScpSink.Param.PORT.paramName(), DeliveryArg.Type.delivery_arg, f.value());
					}

					@Override
					public void itemPropertyChanged(FormItem<Integer> f, Property property) {

					}
				});
			}
			form.add(serverPort);
		}

		/*
		 * host-key
		 */
		arg = destination().arg(ScpSink.Param.HOST_KEY.paramName());
		if (sink().serverHost() == null) {
			Field<String> serverHostKey = new Field<String>(new FieldDefinition(ScpSink.Param.HOST_KEY.paramName(),
					TextType.DEFAULT, "SSH server host key", null, 0, 1));
			if (sink().serverHostKey() != null) {
				serverHostKey.setInitialValue(sink().serverHostKey(), false);
			}
			if (arg != null && arg.value() != null) {
				serverHostKey.setInitialValue(arg.value(), false);
			}
			serverHostKey.addListener(new FormItemListener<String>() {

				@Override
				public void itemValueChanged(FormItem<String> f) {
					destination().setArg(ScpSink.Param.HOST_KEY.paramName(), DeliveryArg.Type.delivery_arg, f.value());
				}

				@Override
				public void itemPropertyChanged(FormItem<String> f, Property property) {

				}
			});
			form.add(serverHostKey);
		}


		/*
		 * file-mode
		 */
		// TDDO:

		/*
		 * user
		 */
		arg = destination().arg(ScpSink.Param.USER.paramName());
		Field<String> user = new Field<String>(new FieldDefinition(ScpSink.Param.USER.paramName(), StringType.DEFAULT,
				"The SSH user name.", null, 1, 1));
		if (arg != null && arg.value() != null) {
			user.setInitialValue(arg.value(), false);
		}
		user.addListener(new FormItemListener<String>() {

			@Override
			public void itemValueChanged(FormItem<String> f) {
				destination().setArg(ScpSink.Param.USER.paramName(), DeliveryArg.Type.delivery_arg, f.value());
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
		 SecureArgForm passwordForm = new SecureArgForm(destination(), mode(), ScpSink.Param.PASSWORD.paramName(),
				 false, ScpSink.Param.PASSWORD.paramName(), "The SSH user's password.", true) {
			 @Override
			 protected String generateSecureWalletKey() {
				 DeliveryArg arg = destination().arg(ScpSink.Param.HOST.paramName());
				 String host = arg != null ? arg.value() : "ssh-server";
				 arg = destination().arg(ScpSink.Param.USER.paramName());
				 String user = arg != null ? arg.value() : Session.userName();
				 StringBuilder sb = new StringBuilder();
				 sb.append("password_ssh://");
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

		 /*
		  * private-key
		  */
		 SecureArgForm privateKeyForm = new SecureArgForm(
				 destination(),
				 mode(),
				 ScpSink.Param.PRIVATE_KEY.paramName(),
				 false,
				 ScpSink.Param.PRIVATE_KEY.paramName(),
				 "The SSH user's private key. To be used for public key authentication. The public key must be pre-installed to ~/.ssh/authorized_keys file on the SSH Server.",
				 false) {
			 @Override
			 protected String generateSecureWalletKey() {
				 DeliveryArg arg = destination().arg(ScpSink.Param.HOST.paramName());
				 String host = arg != null ? arg.value() : "ssh-server";
				 arg = destination().arg(ScpSink.Param.USER.paramName());
				 String user = arg != null ? arg.value() : Session.userName();
				 StringBuilder sb = new StringBuilder();
				 sb.append("private-key_ssh://");
				 if (user != null) {
					 sb.append(user);
					 sb.append("@");
				 }
				 sb.append(host);
				 return sb.toString();
			 }
		 };
		 addMustBeValid(privateKeyForm);
		 vp.add(privateKeyForm.gui());

		 /*
		  * base directory
		  */
		 Form form2 = new Form(mode());
		 //
		 arg = destination().arg(ScpSink.Param.DIRECTORY.paramName());
		 Field<String> directory = new Field<String>(
				 new FieldDefinition(
						 ScpSink.Param.DIRECTORY.paramName(),
						 StringType.DEFAULT,
						 "The base directory into which the data should be placed. Defaults to the user's home dirctory on the SSH server.",
						 null, 0, 1));
		 if (sink().directory() != null) {
			 directory.setInitialValue(sink().directory(), false);
		 }
		 if (arg != null && arg.value() != null) {
			 directory.setInitialValue(arg.value(), false);
		 }
		 directory.addListener(new FormItemListener<String>() {

			 @Override
			 public void itemValueChanged(FormItem<String> f) {
				 destination().setArg(ScpSink.Param.DIRECTORY.paramName(), DeliveryArg.Type.delivery_arg, f.value());
			 }

			 @Override
			 public void itemPropertyChanged(FormItem<String> f, Property property) {

			 }
		 });
		 form2.add(directory);

		 /*
		  * decompress
		  */
		  arg = destination().arg(ScpSink.Param.DECOMPRESS.paramName());
		 Field<Boolean> decompress = new Field<Boolean>(new FieldDefinition(ScpSink.Param.DECOMPRESS.paramName(),
				 BooleanType.DEFAULT_TRUE_FALSE, "Whether decompress the data if compressed. Defaults to false.", null,
				 0, 1));
		 if (sink().decompress()) {
			 decompress.setInitialValue(sink().decompress(), false);
		 }
		 if (arg != null && arg.value() != null) {
			 decompress.setInitialValue(Boolean.parseBoolean(arg.value()), false);
		 }
		 decompress.addListener(new FormItemListener<Boolean>() {

			 @Override
			 public void itemValueChanged(FormItem<Boolean> f) {
				 destination().setArg(ScpSink.Param.DECOMPRESS.paramName(), DeliveryArg.Type.delivery_arg, f.value());
			 }

			 @Override
			 public void itemPropertyChanged(FormItem<Boolean> f, Property property) {

			 }
		 });
		 form2.add(decompress);       
		 form2.render();
		 addMustBeValid(form2);
		 vp.add(form2);

		 //
		 sp.setContent(vp);
	}
}
