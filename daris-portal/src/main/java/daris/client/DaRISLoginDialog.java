package daris.client;

import java.util.List;

import arc.gui.form.Field;
import arc.gui.form.FieldDefinition;
import arc.gui.form.Form;
import arc.gui.form.Form.BooleanAs;
import arc.gui.form.FormEditMode;
import arc.gui.form.FormItem;
import arc.gui.form.FormItem.Property;
import arc.gui.form.FormItemListener;
import arc.gui.form.FormSubmitListener;
import arc.gui.gwt.colour.Colour;
import arc.gui.gwt.colour.RGB;
import arc.gui.gwt.widget.BaseWidget;
import arc.gui.gwt.widget.HTML;
import arc.gui.gwt.widget.Spacer;
import arc.gui.gwt.widget.button.Button;
import arc.gui.gwt.widget.button.ButtonBar;
import arc.gui.gwt.widget.label.Label;
import arc.gui.gwt.widget.panel.HorizontalPanel;
import arc.gui.gwt.widget.panel.RootPanel;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.gui.gwt.widget.window.Window;
import arc.gui.image.Image;
import arc.gui.window.WindowProperties;
import arc.mf.client.Output;
import arc.mf.client.ResponseHandler;
import arc.mf.client.util.Action;
import arc.mf.client.util.ActionListener;
import arc.mf.client.util.StateChangeListener;
import arc.mf.client.xml.XmlElement;
import arc.mf.dtype.BooleanType;
import arc.mf.dtype.ConstantType;
import arc.mf.dtype.DataType;
import arc.mf.dtype.EmailAddressType;
import arc.mf.dtype.PasswordType;
import arc.mf.dtype.StringType;
import arc.mf.session.AccountCreationHandler;
import arc.mf.session.LoginDialog;
import arc.mf.session.PasswordChangeDialog;
import arc.mf.session.PasswordRecoveryHandler;
import arc.mf.session.PasswordResetHandler;
import arc.mf.session.Session;
import arc.mf.session.SessionHandler;

import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;

import daris.client.cookies.UserCookies;

/**
 * Modal login dialog. Will execute login to the server.
 * 
 * @author jason
 * 
 */
public class DaRISLoginDialog implements LoginDialog, SessionHandler {

	public static final String TITLE = "Log on to DaRIS";
	public static final String VERSION = Version.VERSION;

	public static final Colour LINK_COLOUR = new RGB(0xaa, 0xaa, 0xaa);
	public static final Colour LINK_HIGHLIGHT_COLOUR = new RGB(241, 93, 34);

	public static final int LINK_FONT_SIZE = 9;

	private String _title;
	private String _version;
	private String _domain;
	private Object _user;
	private boolean _rememberDomainAndUser = true;
	private Colour _backgroundColour;
	private Colour _formBackgroundColour;
	private Colour _formLabelColour;
	private Colour _versionColour;
	private Colour _infoColour;
	private Image _icon;
	private IconPosition _ipos;

	private Window _winModal;
	private Button _loginButton;

	private String _userLabel;
	private DataType _userType;
	private String _userDesc;

	private Field<String> _domainField;
	private Field<Object> _userField;
	private Field<String> _passwordField;
	private Field<Boolean> _rememberDomainAndUserField;
	private HTML _info;
	private AccountCreationHandler _ach;
	private PasswordRecoveryHandler _prh;
	private PasswordResetHandler _prsh;

	private DaRISLoginDialog() {
		_title = TITLE;
		_version = VERSION;
		_domain = UserCookies.domain();
		_user = UserCookies.user();
		_userLabel = "User";
		_userType = StringType.DEFAULT;
		_userDesc = "The authentication user name";

		_backgroundColour = RGB.WHITE;
		_formBackgroundColour = new RGB(0xee, 0xee, 0xee);
		_versionColour = RGB.BLACK;
		_infoColour = null;
		_formLabelColour = null;
		_icon = null;
		_ipos = null;
		_ach = null;
		_prh = null;
		_prsh = null;
		Session.addSessionHandler(this);
	}

	public String title() {
		return _title;
	}

	public void setTitle(String title) {
		_title = title;
	}

	public String version() {
		return _version;
	}

	public void setVersion(String version) {
		_version = version;
	}

	public void setVersionColour(Colour colour) {
		_versionColour = colour;
	}

	public String domain() {
		return _domain;
	}

	public void setDomain(String domain) {
		_domain = domain;
	}

	public String userName() {
		return _user.toString();
	}

	public Object user() {
		return _user;
	}

	public void setUser(Object user) {
		_user = user;
	}

	public void setIcon(Image icon, IconPosition position) {
		_icon = icon;
		_ipos = position;
	}

	public void setBackgroundColour(Colour colour) {
		_backgroundColour = colour;
	}

	public void setUserLabel(String label) {
		_userLabel = label;
	}

	public void setUserType(DataType type) {
		_userType = type;
	}

	public void setUserDescription(String desc) {
		_userDesc = desc;
	}

	@Override
	public void setAllowAccountCreation(AccountCreationHandler ach) {
		_ach = ach;
	}

	@Override
	public void setAllowPasswordRecovery(PasswordRecoveryHandler prh) {
		_prh = prh;
	}

	@Override
	public void setAllowPasswordReset(PasswordResetHandler prh) {
		_prsh = prh;
	}

	public void setFormLabelColour(Colour colour) {
		_formLabelColour = colour;
	}

	public void setFormBackgroundColour(Colour colour) {
		_formBackgroundColour = colour;
	}

	public void setInfoColour(Colour colour) {
		_infoColour = colour;
	}

	public boolean rememberDomainAndUser() {
		return _rememberDomainAndUser;
	}

	public void setRememberDomainAndUser(boolean remember) {
		_rememberDomainAndUser = remember;
		if (!remember) {
			UserCookies.setDomain(null);
			UserCookies.setUser(null);
		}
	}

	/**
	 * Shows, displays the login dialog.
	 * 
	 * @param a
	 *            An action to be performed after login.
	 * 
	 */
	private boolean _showing = false;

	@Override
	public void show(String domain, String user, PasswordChangeDialog pcd,
			final Action action) {
		if (_showing) {
			return;
		}
		int width = 380;

		if (_userType.equals(EmailAddressType.DEFAULT)) {
			width += 60;
		}

		int height = 230;

		if (_version != null) {
			height += 14;
		}

		// if (_domain == null) {
		// height += 40;
		// }

		if (_icon != null) {
			if (_ipos == IconPosition.LEFT || _ipos == IconPosition.RIGHT) {
				width += _icon.width() + 8;
			} else if (_ipos == IconPosition.TOP
					|| _ipos == IconPosition.BOTTOM) {
				height += _icon.height() + 8;
			}

			if (height < _icon.height() + 40) {
				height = _icon.height() + 40;
			}

			if (width < _icon.width() + 10) {
				width = _icon.width() + 10;
			}
		}

		if (_ach != null) {
			height += 15;
		}

		if (_prh != null) {
			height += 15;
		}

		if (_prsh != null) {
			height += 15;
		}

		WindowProperties wp = new WindowProperties();
		wp.setTitle(title());
		if (title() == null) {
			wp.setShowHeader(false);
		}

		wp.setShadow(true);
		wp.setSize(width, height);
		wp.setCanBeClosed(false);
		wp.setCanBeResized(false);
		wp.setModal(true);
		wp.setCentered(true);
		wp.setBackgroundColour(_backgroundColour);

		// For the iniital login, make the glass opaque.
		if (domain == null && user == null) {
			wp.setGlassOpacity(0.1);
		} else {
			wp.setGlassOpacity(0.8);
		}

		_winModal = Window.create(RootPanel.container(), wp);

		/*
		 * _winModal.addCloseClickHandler(new CloseClickHandler() { public void
		 * onCloseClick(CloseClientEvent event) { _winModal.destroy(); } });
		 */

		final Form form = new Form();
		// form.setWidth(width);
		form.setShowHelp(false);
		form.setSubmitOnEnter(true);

		if (_formLabelColour != null) {
			form.setLabelColour(_formLabelColour);
		}

		// form.setHeight(90);
		// form.setWidth100();
		form.setPadding(8);

		boolean setFocus = true;

		if (domain == null) {
			_domainField = new Field<String>(
					new FieldDefinition("Domain", StringType.DEFAULT,
							"The authentication domain", null, 1, 1));
			if (_domain != null) {
				_domainField.setInitialValue(_domain);
			} else {
				_domainField.setFocus(true);
				setFocus = false;
			}
		} else {
			_domainField = new Field<String>(new FieldDefinition("Domain",
					ConstantType.DEFAULT, "The authentication domain", null, 1,
					1));
			_domainField.setInitialValue(domain);
		}

		/*
		 * _domainItem.addKeyPressHandler(new KeyPressHandler() { public void
		 * onKeyPress(KeyPressEvent event) { if (
		 * event.getKeyName().equalsIgnoreCase("enter") ) { if (
		 * !form.validate() ) { return; }
		 * 
		 * doLogin(action); } } });
		 */

		form.add(_domainField);

		if (user == null) {
			_userField = new Field<Object>(new FieldDefinition(_userLabel,
					_userType, _userDesc, null, 1, 1));
			if (_user != null) {
				_userField.setInitialValue(_user);
			} else {
				if (setFocus) {
					_userField.setFocus(true);
					setFocus = false;
				}
			}
		} else {
			_userField = new Field<Object>(new FieldDefinition(_userLabel,
					ConstantType.DEFAULT, _userDesc, null, 1, 1));
			_userField.setInitialValue(user);
		}

		/*
		 * _userItem.addKeyPressHandler(new KeyPressHandler() { public void
		 * onKeyPress(KeyPressEvent event) { if (
		 * event.getKeyName().equalsIgnoreCase("enter") ) { if (
		 * !form.validate() ) { return; }
		 * 
		 * doLogin(action); } } });
		 */
		form.add(_userField);

		_passwordField = new Field<String>(
				new FieldDefinition("Password", PasswordType.DEFAULT,
						"The authentication password", null, 1, 1));
		if (setFocus) {
			_passwordField.setFocus(true);
		}

		/*
		 * _passwordItem.addKeyPressHandler(new KeyPressHandler() { public void
		 * onKeyPress(KeyPressEvent event) { if (
		 * event.getKeyName().equalsIgnoreCase("enter") ) { if (
		 * !form.validate() ) { return; }
		 * 
		 * doLogin(action); } } });
		 */
		form.add(_passwordField);

		HorizontalPanel sp = new HorizontalPanel();
		sp.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);

		// sp.setWidth(220);
		sp.setHeight100();
		sp.setWidth100();
		sp.setMargin(3);
		sp.setMarginLeft(10);
		sp.setMarginRight(10);
		sp.setMarginTop(10);

		sp.setBorderRadius(3);
		sp.setBorder(1, new RGB(0xaa, 0xaa, 0xaa));
		sp.setBackgroundColour(_formBackgroundColour);
		form.gui().setRight(2);
		sp.add(form.gui());

		VerticalPanel vp = new VerticalPanel();
		// vp.setMargin(10);
		vp.setWidth100();
		vp.setHeight100();

		if (_version != null) {
			HorizontalPanel lp = new HorizontalPanel();
			lp.setHeight(14);
			lp.setWidth100();
			lp.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
			lp.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
			lp.setMarginRight(12);
			lp.setSpacing(0);

			Label l = new Label(_version);
			l.setFontSize(9);
			l.setColour(_versionColour);
			lp.add(l);

			vp.add(lp);
		}

		vp.add(sp);

		Form form2 = new Form(FormEditMode.UPDATE);
		form2.setShowHelp(false);
		form2.setHeight(20);
		form2.setBooleanAs(BooleanAs.CHECKBOX);
		form2.setFontWeight(FontWeight.NORMAL);
		form2.setFontSize(9);
		form2.setLeft(80);
		_rememberDomainAndUserField = new Field<Boolean>(
				new FieldDefinition("Remember me",
						BooleanType.DEFAULT_TRUE_FALSE, null, null, 1, 1));
		_rememberDomainAndUserField.setInitialValue(_rememberDomainAndUser);
		_rememberDomainAndUserField
				.addListener(new FormItemListener<Boolean>() {

					@Override
					public void itemValueChanged(FormItem<Boolean> f) {
						setRememberDomainAndUser(f.value());
					}

					@Override
					public void itemPropertyChanged(FormItem<Boolean> f,
							Property property) {

					}
				});
		form2.add(_rememberDomainAndUserField);
		vp.add(form2.gui());
		vp.setSpacing(2);

		if (_ach != null || _prh != null || _prsh != null) {
			VerticalPanel vap = new VerticalPanel();
			vap.setWidth100();
			vap.setMarginRight(10);

			vap.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);

			if (_ach != null) {
				HTML ca = new HTML("Create account");
				ca.setColour(LINK_COLOUR);
				ca.setMouseOverOutColour(LINK_HIGHLIGHT_COLOUR, LINK_COLOUR);
				ca.setFontSize(LINK_FONT_SIZE);
				ca.setFontFamily("helvetica");
				vap.add(ca);

				ca.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						_ach.create(new ActionListener() {
							@Override
							public void executed(boolean succeeded) {
							}
						});
					}
				});
			}

			if (_prh != null) {
				final HTML ca = new HTML("Recover password");
				ca.setColour(LINK_COLOUR);
				ca.setMouseOverOutColour(LINK_HIGHLIGHT_COLOUR, LINK_COLOUR);
				ca.setFontSize(LINK_FONT_SIZE);
				ca.setFontFamily("helvetica");
				vap.add(ca);

				ca.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						_prh.recover(new ActionListener() {
							@Override
							public void executed(boolean succeeded) {
							}
						});
					}
				});
			}

			if (_prsh != null) {
				final HTML ca = new HTML("Reset password");
				ca.setColour(LINK_COLOUR);
				ca.setMouseOverOutColour(LINK_HIGHLIGHT_COLOUR, LINK_COLOUR);
				ca.setFontSize(LINK_FONT_SIZE);
				ca.setFontFamily("helvetica");
				vap.add(ca);

				ca.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						_prsh.reset(new ActionListener() {
							@Override
							public void executed(boolean succeeded) {
							}
						});
					}
				});
			}

			vp.add(vap);
		}

		vp.setSpacing(5);

		_info = new HTML();
		_info.setMarginLeft(15);
		_info.setMarginRight(10);
		_info.setHeight(20);
		_info.setFontSize(11);
		if (_infoColour != null) {
			_info.setColour(_infoColour);
		}

		// _info.setHTML("This is a label");
		_info.setWordWrap(false);

		if (domain != null || user != null) {
			_info.setHTML("<span style=\"color: red; font-weight: bold\">Session expired</span>");
		}

		// _info.setAlign(Alignment.CENTER);

		vp.add(_info);

		ButtonBar bb = new ButtonBar(ButtonBar.Alignment.CENTER);
		bb.setWidth100();

		// bb.setHeight(20);
		bb.setColourEnabled(false);

		_loginButton = bb.addButton("Login");
		_loginButton.disable();

		vp.add(bb);
		vp.add(new Spacer(1, 10));

		// label.setShowEdges(true);
		// label.setEdgeSize(1);

		BaseWidget gui = createIconGUI(vp);

		_winModal.setContent(gui);

		form.addChangeListener(new StateChangeListener() {
			public void notifyOfChangeInState() {
				_info.setHTML(null);
				_loginButton.setEnabled(form.valid().valid());
			}

		});

		// Submit listener for "enter" (non-button submit).
		form.setSubmitListener(new FormSubmitListener() {
			public void submit(Form f) {
				if (f.valid().valid()) {
					doLogin(action);
				}
			}
		});

		_loginButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				doLogin(action);
			}

		});

		/*
		 * form.addItemChangeHandler(new ItemChangeHandler() { public void
		 * onItemChange(ItemChangeEvent ie) { if ( form.valuesAreValid(true) ) {
		 * _loginButton.enable(); } else { _loginButton.disable(); } }
		 * 
		 * });
		 * 
		 * _loginButton.addClickHandler(new ClickHandler() { public void
		 * onClick(ClickEvent event) {
		 * 
		 * if ( !form.validate() ) { return; }
		 * 
		 * doLogin(action); } });
		 */

		_winModal.show();
		_showing = true;
	}

	private BaseWidget createIconGUI(BaseWidget gui) {
		if (_icon == null) {
			return gui;
		}

		if (_ipos == LoginDialog.IconPosition.BACKGROUND) {
			gui.setBackgroundImage(new arc.gui.gwt.widget.image.Image(_icon));
			return gui;
		}

		arc.gui.gwt.widget.image.Image img = new arc.gui.gwt.widget.image.Image(
				_icon);

		if (_ipos == LoginDialog.IconPosition.TOP
				|| _ipos == LoginDialog.IconPosition.BOTTOM) {
			VerticalPanel il = new VerticalPanel();
			il.setWidth100();
			il.setHeight100();
			il.setSpacing(4);

			if (_ipos == LoginDialog.IconPosition.TOP) {
				il.add(img);
				il.add(gui);
			} else {
				il.add(gui);
				il.add(img);
			}

			return il;
		}

		if (_ipos == LoginDialog.IconPosition.LEFT
				|| _ipos == LoginDialog.IconPosition.RIGHT) {
			HorizontalPanel il = new HorizontalPanel();
			il.setWidth100();
			il.setHeight100();
			il.setSpacing(4);

			if (_ipos == LoginDialog.IconPosition.LEFT) {
				il.add(img);
				il.add(gui);
			} else {
				il.add(gui);
				il.add(img);
			}

			return il;
		}

		return gui;
	}

	private void doLogin(final Action action) {
		_loginButton.disable();
		_info.setHTML("Authorizing..");

		Session.logon(_domainField.valueAsString(), _userField.valueAsString(),
				_passwordField.valueAsString(), new ResponseHandler() {

					public void processError(Throwable se) {
						_info.setHTML("<span style=\"color: red; font-weight: bold\">Authentication failure</span>");
						_passwordField.setFocus(true);
						_passwordField.selectValue();

						// _passwordItem.selectValue();
						// _passwordItem.focusInItem();
					}

					public void processResponse(XmlElement xe,
							List<Output> outputs) {
						_winModal.close();
						_showing = false;
						// Successful logon .. let's perform the required
						// action.
						if (action != null) {
							action.execute();
						}
					}

				});

	}

	private static DaRISLoginDialog _instance;

	public static DaRISLoginDialog get() {
		if (_instance == null) {
			return new DaRISLoginDialog();
		}
		return _instance;
	}

	@Override
	public void sessionCreated(boolean initial) {
		setDomain(Session.domainName());
		setUser(Session.userName());
		UserCookies.setDomain(_rememberDomainAndUser ? Session.domainName()
				: null);
		UserCookies.setUser(_rememberDomainAndUser ? Session.userName() : null);
	}

	@Override
	public void sessionExpired() {

	}

	@Override
	public void sessionTerminated() {

	}

	public void discard() {
		Session.removeSessionHandler(this);
	}

}