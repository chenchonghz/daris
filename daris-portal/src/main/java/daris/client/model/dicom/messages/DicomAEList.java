package daris.client.model.dicom.messages;

import java.util.List;
import java.util.Vector;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;
import daris.client.model.dicom.DicomAE;

public class DicomAEList extends ObjectMessage<List<DicomAE>> {

	public static enum Type {
		LOCAL, REMOTE, ALL;
		public String toString() {
			return super.toString().toLowerCase();
		}

		public static String[] strings(){
			Type[] vs = values();
			String[] ss = new String[vs.length];
			for(int i=0;i<vs.length;i++){
				ss[i] = vs[i].toString();
			}
			return ss;
		}
	}

	public static enum Access {
		PUBLIC, PRIVATE, ALL;
		public String toString() {
			return super.toString().toLowerCase();
		}

		public static String[] strings(){
			Access[] vs = values();
			String[] ss = new String[vs.length];
			for(int i=0;i<vs.length;i++){
				ss[i] = vs[i].toString();
			}
			return ss;
		}
	}

	private Type _type;
	private Access _access;

	public DicomAEList(Type type, Access access) {
		_type = type;
		_access = access;
	}

	public void setType(Type type) {
		_type = type;
	}

	public void setAccess(Access access) {
		_access = access;
	}

	@Override
	protected void messageServiceArgs(XmlWriter w) {
		w.add("access", _access.toString());
		w.add("type", _type.toString());
	}

	@Override
	protected String messageServiceName() {
		return "om.pssd.dicom.ae.list";
	}

	@Override
	protected List<DicomAE> instantiate(XmlElement xe) throws Throwable {
		if (xe != null) {
			List<XmlElement> aes = xe.elements("ae");
			if (aes != null) {
				if (!aes.isEmpty()) {
					List<DicomAE> as = new Vector<DicomAE>();
					for (XmlElement ae : aes) {
						as.add(new DicomAE(ae));
					}
					return as;
				}
			}
		}
		return null;
	}

	@Override
	protected String objectTypeName() {
		return DicomAE.class.getName();
	}

	@Override
	protected String idToString() {
		return null;
	}

}
