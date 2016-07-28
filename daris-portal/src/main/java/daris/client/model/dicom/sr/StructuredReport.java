package daris.client.model.dicom.sr;

import arc.mf.client.xml.XmlElement;

public class StructuredReport {

	private ContentItem _root;
	private String _assetId;
	private String _cid;
	private String _name;

	public StructuredReport(XmlElement xe) {
		_assetId = xe.value("@id");
		_cid = xe.value("@cid");
		_name = xe.value("@name");
		_root = new ContentItem(xe.element("item"));
	}

	public ContentItem root() {
		return _root;
	}

	public String assetId() {
		return _assetId;
	}

	public String cid() {
		return _cid;
	}

	public String assetName() {
		return _name;
	}

}
