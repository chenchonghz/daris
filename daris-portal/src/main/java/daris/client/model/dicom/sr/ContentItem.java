package daris.client.model.dicom.sr;

import java.util.ArrayList;
import java.util.List;

import arc.mf.client.xml.XmlElement;

public class ContentItem {

	private String _name;
	private String _type;
	private String _value;
	private String _code;
	private String _relationship;

	private List<ContentItem> _items;

	public ContentItem(XmlElement ie) {
		_name = ie.value("@name");
		_type = ie.value("@type");
		_value = ie.value();
		_code = ie.value("@code");
		_relationship = ie.value("@relationship");
		List<XmlElement> cies = ie.elements("item");
		if (cies != null && !cies.isEmpty()) {
			_items = new ArrayList<ContentItem>(cies.size());
			for (XmlElement cie : cies) {
				_items.add(new ContentItem(cie));
			}
		}
	}

	public String name() {
		return _name;
	}

	public String code() {
		return _code;
	}

	public String type() {
		return _type;
	}

	public String value() {
		return _value;
	}

	public String relationship() {
		return _relationship;
	}

	public List<ContentItem> items() {
		return _items;
	}

	public boolean hasItems() {
		return _items != null && !_items.isEmpty();
	}
}
