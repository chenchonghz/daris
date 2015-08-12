package nig.mf.plugin.pssd.services;


import java.util.Collection;

import nig.mf.pssd.Role;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.BooleanType;
import arc.mf.plugin.dtype.EnumType;
import arc.mf.plugin.dtype.IntegerType;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcUserCreate extends PluginService {

	private Interface _defn;

	public SvcUserCreate()  {
		_defn = new Interface();
		Interface.Element ie = new Interface.Element("authority",StringType.DEFAULT,"The authority of interest for users. Defaults to local.",0,1);
		ie.add(new Interface.Attribute("protocol", StringType.DEFAULT,
				"The protocol of the identity authority. If unspecified, defaults to federated user within the same type of repository.", 0));
		_defn.add(ie);
		_defn.add(new Interface.Element("domain", StringType.DEFAULT, "The name of the domain that the users will be created in. Defaults to 'nig'. ", 0, 1));
		_defn.add(new Interface.Element("user", StringType.DEFAULT, "The username.", 1, 1));
		//
		Interface.Element me = new Interface.Element("name", StringType.DEFAULT, "User's name", 0, Integer.MAX_VALUE);
		me.add(new Interface.Attribute("type", new EnumType(new String[] {"first", "middle", "last"}),
				"Type of name", 1));
		_defn.add(me);
		//
		Interface.Element me2 = new Interface.Element("password", StringType.DEFAULT, "The user's password.", 0, 1);
		me2.add(new Interface.Attribute("notify", BooleanType.DEFAULT,
				"If notification has been requested for account creation, and this attribute it true, then the password will be included in the notification. Defaults to false.", 0));
		_defn.add(me2);
		//
		_defn.add(new Interface.Element("email", StringType.DEFAULT, "The user's email address", 0, 1));
		_defn.add(new Interface.Element("project-creator", BooleanType.DEFAULT, "Should this user be allowed to create projects ? Defaults to false.", 0, 1));
		_defn.add(new Interface.Element("role", StringType.DEFAULT, "Any additional role you wish to grant to the user.", 0, Integer.MAX_VALUE));
		//
		Interface.Element me3  = new Interface.Element("generate-password", BooleanType.DEFAULT, "Auto-generate the password and send to the user via the given email. Defaults to false.", 0, 1);
		me3.add(new Interface.Attribute("length", IntegerType.DEFAULT,
				"The password length. A length less than the minimum length for the domain will be ignored. If not specified, defaults to the minimum length for the domain.", 0));
		_defn.add(me3);
		//
		_defn.add(new Interface.Element("notify", BooleanType.DEFAULT, "If true, and the user has an e-mail address then notify them of account creation. If generate-password is true, then e-mail the generated password to the user.", 0, 1));
		_defn.add(new Interface.Element("notification", StringType.DEFAULT, "Some extra text sent with the notification to the user.", 0, 1));
	}

	public Access access() {
		return ACCESS_ADMINISTER;
	}

	public Interface definition() {
		return _defn;
	}

	public String description() {
		return "Creates a standard PSSD user and assigns the basic roles: model-user, subject-creator, project-creator (optional).";
	}

	public String name() {
		return "om.pssd.user.create";
	}

	public void execute(Element args, Inputs inputs, Outputs outputs, XmlWriter w) throws Throwable {
		
		// Inputs
		XmlDoc.Element authority = args.element("authority");
		String domain = args.stringValue("domain", "nig");
		if (domain.equalsIgnoreCase("dicom"))  {
			throw new Exception ("Use service om.pssd.dicom.user.create for DICOM domain users");
		}
		String user = args.value("user");
		Collection<XmlDoc.Element> names = args.elements("name");
		String email = args.value("email");
		XmlDoc.Element pw = args.element("password");
		Boolean projectCreator = args.booleanValue("project-creator", false);
		Collection<String> roles = args.values("role");
		XmlDoc.Element notify = args.element("notify");
		XmlDoc.Element generate = args.element("generate-password");
		if (pw==null && generate==null) {
			throw new Exception("You must provide 'password' or set 'generate-password=true'");
		}
		if (pw==null && generate!=null && !generate.booleanValue()) {
			throw new Exception("You must provide 'password' or set 'generate-password=true'");
		}
		String notification = args.value("notification");

		// Create user
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("domain", domain);
		dm.add("user", user);
		if (pw!=null) dm.add(pw);
		if (email!=null) dm.add("email", email);
		if (generate!=null) dm.add(generate);
		if (notify!=null) {
			dm.add(notify);
			// Broken in 3.9.011; fixed in 4.0.001
			dm.add("notify-subject", "DaRIS account created");
			String t ="A DaRIS account has been created for you as follows \n" +
                    "Domain : " + domain + "\n" +
                    "Username : " + user + "\n " +
                    "Password : $password$ \n \n" +
                    " You can change the password in the DaRIS portal";
			if (notification!=null) {
				t += "\n" + notification;
			}
			dm.add("notify-body", t);
			 
		}

		// Create the user
		
		if (authority==null) {
			if (names!=null) {
				// user.create does not take authority because it is intended for
				// local authority accounts only. 
				dm.push("meta");
				dm.push("mf-user");
				for (XmlDoc.Element name : names) {
					dm.add(name);
				}
				dm.pop();
				dm.pop();
			}
			executor().execute("user.create", dm.root());
		} else {
			// Use the 'name' element of authentication.user.create
			// We will have to pull this apart again in om.pssd.user.desctribe
			String fullName = makeName (names);	
			if (fullName!=null) dm.add("name", fullName);

			// athentication.user.create does not take element "meta" (it does
			// not make an asset); it is intended for local representations of
			// remote authorities or accounts like DICOM
			if (authority!=null) dm.add(authority);
			executor().execute("authentication.user.create", dm.root());
		}

		// Grant roles. Special case for DICOM
		dm = new XmlDocMaker("args");
		if (authority!=null) dm.add(authority);
		dm.add("domain", domain);
		dm.add("user", user);
		dm.add("role", Role.modelUserRoleName());
		dm.add("role", Role.subjectCreatorRoleName());
		if (projectCreator) dm.add("role", Role.projectCreatorRoleName());
		if (roles!=null) {
			for (String role : roles) {
				dm.add("role", role);
			}
		}
		executor().execute("om.pssd.user.role.grant", dm.root());
	}


	private String makeName (Collection<XmlDoc.Element> names) throws Throwable {
		String first = null;
		String middle = null;
		String last = null;
		if (names!=null) {
			for (XmlDoc.Element name : names) { 
				if (name.value("@type").equals("first")) {
					first = name.value();
				} else 	if (name.value("@type").equals("middle")) {
					if (middle==null) {
						middle  = name.value();
					} else {
						middle += " " + name.value();
					}
				} else if (name.value("@type").equals("last")) {
					last = name.value();
				}
			}
		}
		String fullName = null;
		if (first!=null) fullName = first;
		if (middle!=null) fullName = fullName + " " +  middle;
		if (last!=null) fullName = fullName + " " + last;
		return fullName;
	}
}
