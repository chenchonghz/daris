package daris.client.model.dicom.messages;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import arc.mf.client.xml.XmlWriter;
import arc.mf.object.BackgroundObjectMessage;
import daris.client.model.dicom.AttributeTag;
import daris.client.model.object.DObjectRef;

public class DicomSend extends BackgroundObjectMessage {
	public static enum AssetType {
		primary, replica, all;
	}

	public static enum ElementAction {
		unchanged("unchanged"), set("set"), anonymize("anonymize"), use_subject_cid(
				"use-subject-cid"), use_mf_dicom_patient_name(
				"use-mf-dicom-patient-name"), use_mf_dicom_patient_id(
				"use-mf-dicom-patient-id");
		private String _stringValue;

		ElementAction(String stringValue) {
			_stringValue = stringValue;
		}

		public final String stringValue() {
			return _stringValue;
		}

		public final String toString() {
			return stringValue();
		}

		public static final String[] stringValues() {
			ElementAction[] vs = values();
			String[] svs = new String[vs.length];
			for (int i = 0; i < vs.length; i++) {
				svs[i] = vs[i].stringValue();
			}
			return svs;
		}

		public static final ElementAction fromString(String action,
				ElementAction defaultValue) {
			if (action != null) {
				ElementAction[] vs = values();
				for (ElementAction v : vs) {
					if (v.stringValue().equalsIgnoreCase(action)) {
						return v;
					}
				}
			}
			return defaultValue;
		}
	}

	public static class NamedElement {
		private ElementAction _action;
		private String _value;

		public NamedElement(ElementAction action, String value) {
			_action = action;
			_value = value;
		}

		public ElementAction action() {
			return _action;
		}

		public String value() {
			return _value;
		}

		public void setAction(ElementAction action) {
			_action = action;
		}

		public void setValue(String value) {
			_value = value;
		}

		public void set(ElementAction action, String value) {
			_action = action;
			if (action == ElementAction.set) {
				_value = value;
			}
		}
	}

	public static class GenericElement extends NamedElement {
		private int _group;
		private int _element;

		public GenericElement(int group, int element, ElementAction action,
				String value) {
			super(action, value);
			_group = group;
			_element = element;
		}

		public int group() {
			return _group;
		}

		public int element() {
			return _element;
		}

		public AttributeTag tag() {
			return new AttributeTag(_group, _element);
		}
	}

	private String _pid;
	private String _where;
	private AssetType _assetType;
	private String _localAET;
	private String _remoteHost;
	private int _remotePort;
	private String _remoteAET;
	private NamedElement _patientId;
	private NamedElement _patientName;
	private NamedElement _studyId;
	private NamedElement _performingPhysicianName;
	private NamedElement _referringPhysicianName;
	private NamedElement _referringPhysicianPhone;
	private Map<AttributeTag, GenericElement> _genericElements;

	public DicomSend(DObjectRef parent) {
		this(parent.id(), AssetType.all, null);
	}

	public DicomSend(String where) {
		this(null, AssetType.all, where);
	}

	public DicomSend(String pid, AssetType assetType, String where) {
		_pid = pid;
		_assetType = assetType;
		_where = where;
		_localAET = null;
		_remoteHost = null;
		_remotePort = 104;
		_remoteAET = null;
		_patientId = new NamedElement(ElementAction.unchanged, null);
		_patientName = new NamedElement(ElementAction.unchanged, null);
		_studyId = new NamedElement(ElementAction.unchanged, null);
		_performingPhysicianName = new NamedElement(ElementAction.unchanged,
				null);
		_referringPhysicianName = new NamedElement(ElementAction.unchanged,
				null);
		_referringPhysicianPhone = new NamedElement(ElementAction.unchanged,
				null);
		_genericElements = new HashMap<AttributeTag, GenericElement>();
	}

	public void setLocalAET(String aet) {
		_localAET = aet;
	}

	public void setRemoteHost(String host) {
		_remoteHost = host;
	}

	public void setRemotePort(int port) {
		_remotePort = port;
	}

	public void setRemoteAET(String aet) {
		_remoteAET = aet;
	}

	public void setPatientId(ElementAction action, String value) {
		_patientId.set(action, value);
	}

	public void setPatientName(ElementAction action, String value) {
		_patientName.set(action, value);
	}

	public void setStudyId(ElementAction action, String value) {
		_studyId.set(action, value);
	}

	public void setPerformingPhysicianName(ElementAction action, String value) {
		_performingPhysicianName.set(action, value);
	}

	public void setReferringPhysicianName(ElementAction action, String value) {
		_referringPhysicianName.set(action, value);
	}

	public void setReferringPhysicianPhone(ElementAction action, String value) {
		_referringPhysicianPhone.set(action, value);
	}

	public void setGenericElements(Collection<GenericElement> elements) {
		_genericElements.clear();
		if (elements != null) {
			for (GenericElement ge : elements) {
				_genericElements.put(ge.tag(), ge);
			}
		}
	}

	@Override
	protected void messageServiceArgs(XmlWriter w) {
		if (_pid != null) {
			w.add("pid", _pid);
		}
		if (_where != null) {
			w.add("where", _where);
		}
		if (_assetType != null) {
			w.add("asset-type", _assetType);
		}
		w.push("local");
		w.add("aet", _localAET);
		w.pop();
		w.push("remote");
		w.add("host", _remoteHost);
		w.add("port", _remotePort);
		w.add("aet", _remoteAET);
		w.pop();
		if (override()) {
			w.push("override");
			if (_patientId.action() != ElementAction.unchanged) {
				w.push("patient-id", new String[] { "action",
						_patientId.action().stringValue() });
				if (_patientId.action() == ElementAction.set) {
					w.add("value", _patientId.value());
				}
				w.pop();
			}
			if (_patientName.action() != ElementAction.unchanged) {
				w.push("patient-name", new String[] { "action",
						_patientName.action().stringValue() });
				if (_patientName.action() == ElementAction.set) {
					w.add("value", _patientName.value());
				}
				w.pop();
			}
			if (_studyId.action() != ElementAction.unchanged) {
				w.push("study-id", new String[] { "action",
						_studyId.action().stringValue() });
				if (_studyId.action() == ElementAction.set) {
					w.add("value", _studyId.value());
				}
				w.pop();
			}
			if (_performingPhysicianName.action() != ElementAction.unchanged) {
				w.push("performing-physician-name", new String[] { "action",
						_performingPhysicianName.action().stringValue() });
				if (_performingPhysicianName.action() == ElementAction.set) {
					w.add("value", _performingPhysicianName.value());
				}
				w.pop();
			}
			if (_referringPhysicianName.action() != ElementAction.unchanged) {
				w.push("referring-physician-name", new String[] { "action",
						_referringPhysicianName.action().stringValue() });
				if (_referringPhysicianName.action() == ElementAction.set) {
					w.add("value", _referringPhysicianName.value());
				}
				w.pop();
			}
			if (_referringPhysicianPhone.action() != ElementAction.unchanged) {
				w.push("referring-physician-phone", new String[] { "action",
						_referringPhysicianPhone.action().stringValue() });
				if (_referringPhysicianPhone.action() == ElementAction.set) {	
					w.add("value", _referringPhysicianPhone.value());
				}
				w.pop();
			}
			if (!_genericElements.isEmpty()) {
				Collection<GenericElement> ges = _genericElements.values();
				for (GenericElement ge : ges) {
					if (ge.action() != ElementAction.unchanged) {
						w.push("element", new String[] {
								"group",
								Integer.toHexString(0x10000 | ge.group())
										.substring(1),
								"element",
								Integer.toHexString(0x10000 | ge.element())
										.substring(1), "action",
								ge.action().stringValue() });
						if (ge.action() == ElementAction.set) {
							w.add("value", ge.value());
						}
						w.pop();
					}
				}
			}
			w.pop();
		}
	}

	private boolean override() {
		if (_patientId.action() != ElementAction.unchanged) {
			return true;
		}
		if (_patientName.action() != ElementAction.unchanged) {
			return true;
		}
		if (_studyId.action() != ElementAction.unchanged) {
			return true;
		}
		if (_performingPhysicianName.action() != ElementAction.unchanged) {
			return true;
		}
		if (_referringPhysicianName.action() != ElementAction.unchanged) {
			return true;
		}
		if (_referringPhysicianPhone.action() != ElementAction.unchanged) {
			return true;
		}
		if (!_genericElements.isEmpty()) {
			Collection<GenericElement> ges = _genericElements.values();
			for (GenericElement ge : ges) {
				if (ge.action() != ElementAction.unchanged) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	protected String messageServiceName() {
		return "om.pssd.dicom.send";
	}

	@Override
	protected String objectTypeName() {
		return null;
	}

	@Override
	protected String idToString() {
		return _pid;
	}

	public String pid() {
		return _pid;
	}

	public String where() {
		return _where;
	}

	public String remoteAE() {
		return _remoteAET + "@" + _remoteHost + ":" + _remotePort;
	}

	public NamedElement patientName() {
		return _patientName;
	}

	public NamedElement patientId() {
		return _patientId;
	}

	public NamedElement studyId() {
		return _studyId;
	}

	public NamedElement performingPhysicianName() {
		return _performingPhysicianName;
	}

	public NamedElement referringPhysicianName() {
		return _referringPhysicianName;
	}

	public NamedElement referringPhysicianPhone() {
		return _referringPhysicianPhone;
	}
}
