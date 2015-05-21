package daris.client.model.dicom;

import arc.mf.client.util.ObjectUtil;
import arc.mf.client.xml.XmlElement;

public class DicomAE {

	public static enum Access {
		PUBLIC, PRIVATE;
		@Override
		public String toString() {
			return super.toString().toLowerCase();
		}

		public static Access fromString(String s) {
			if (s != null) {
				for (int i = 0; i < values().length; i++) {
					if (values()[i].toString().equalsIgnoreCase(s)) {
						return values()[i];
					}
				}
			}
			return null;
		}
	}

	public static enum Type {
		LOCAL, REMOTE;
		@Override
		public String toString() {
			return super.toString().toLowerCase();
		}

		public static Type fromString(String s) throws Throwable {
			if (s != null) {
				for (int i = 0; i < values().length; i++) {
					if (values()[i].toString().equalsIgnoreCase(s)) {
						return values()[i];
					}
				}
			}
			throw new Exception("Invalid access: " + s);
		}
	}

	public static final int DEFAULT_DICOM_PORT = 104;

	private String _name;
	private String _host;
	private int _port;
	private String _aet;
	private Type _type;
	private Access _access;

	public DicomAE(XmlElement ae) {

		_name = ae.value("@name");
		_host = ae.value("host");
		_port = 0;
		try {
			_port = ae.intValue("port", 0);
		} catch (Throwable e) {
			_port = 0;
		}
		try {
			_type = Type.fromString(ae.stringValue("@type"));
		} catch (Throwable e) {
			_type = Type.REMOTE;
		}
		try {
			_access = Access.fromString(ae.stringValue("@access", Access.PUBLIC.toString()));
		} catch (Throwable e) {
			_access = Access.PUBLIC;
		}

		_aet = ae.value("aet");
	}

	public DicomAE(String name, String aet, String host, int port, Access access) {

		_name = name;
		_host = host;
		_port = port;
		_aet = aet;
		_access = access;
		_type = Type.REMOTE;
	}

	public DicomAE(String name, String aet, String host, int port) {
		this(name, aet, host, port, Access.PRIVATE);
	}

	public String name() {
		return _name;
	}

	public String host() {

		return _host;
	}

	public int port() {

		return _port;
	}

	public String aet() {

		return _aet;
	}

	@Override
	public String toString() {

		return _name + ":" + _aet + "@" + _host + ":" + _port;
	}

	public static DicomAE fromString(String s) {
		if (s != null) {
			String[] ts1 = s.split("@");
			if (ts1.length == 2) {
				String[] ts2 = ts1[0].split(":");
				String[] ts3 = ts1[1].split(":");
				if (ts2.length == 2 && ts3.length == 2) {
					return new DicomAE(ts2[0], ts2[1], ts3[0], Integer.parseInt(ts3[1]));
				}
			}
		}
		return null;
	}

	@Override
	public boolean equals(Object o) {

		if (o == null) {
			return false;
		}
		if (o instanceof DicomAE) {
			DicomAE ae = (DicomAE) o;
			return ObjectUtil.equals(_name, ae.name()) && _host.equals(ae.host()) && _port == ae.port()
					&& _aet.equals(ae.aet());
		}
		return false;
	}

	public Type type() {
		return _type;
	}

	public Access access() {
		return _access;
	}
}
