package nig.mf.plugin.pssd.user;

import nig.util.ObjectUtil;
import arc.xml.XmlDoc;

public class Authority {

	private String _name;
	private String _protocol;

	public static Authority instantiate(XmlDoc.Element ae) throws Throwable {

		if (ae == null) {
			return null;
		}
		if (!ae.nameEquals("authority")) {
			throw new Exception("Invalid XML element name: " + ae.name() + ". Should be 'authority'.");
		}
		String name = ae.value();
		if (name == null) {
			throw new Exception("The value of 'authority' element cannot be null.");
		}
		return new Authority(name, ae.value("@protocol"));
	}

	private Authority(String name, String protocol) {

		_name = name;
		assert _name != null;
		_protocol = protocol;
	}

	public String name() {

		return _name;
	}

	public String protocol() {

		return _protocol;
	}

	@Override
	public String toString() {

		return _name;
	}

	@Override
	public boolean equals(Object o) {

		if (o != null) {
			if (o instanceof Authority) {
				Authority ao = (Authority) o;
				return ObjectUtil.equals(ao.name(), name()) && ObjectUtil.equals(ao.protocol(), protocol());
			}
		}
		return false;
	}

	public XmlDoc.Element toXmlElement() throws Throwable {

		return toXmlElement(this);
	}

	public static XmlDoc.Element toXmlElement(Authority authority) throws Throwable {

		return toXmlElement(authority.name(), authority.protocol());
	}

	public static XmlDoc.Element toXmlElement(String authority, String protocol) throws Throwable {

		if (authority == null) {
			throw new Exception("Failed to create XML element: authority. Value is null.");
		}
		XmlDoc.Element ae = new XmlDoc.Element("authority", authority);
		if (protocol != null) {
			ae.add(new XmlDoc.Attribute("protocol", protocol));
		}
		return ae;
	}

	public static Authority instantiate(String authority, String protocol) {

		if (authority == null) {
			return null;
		}
		return new Authority(authority, protocol);
	}
}
