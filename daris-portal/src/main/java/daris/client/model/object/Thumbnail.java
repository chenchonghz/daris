package daris.client.model.object;

import java.util.ArrayList;
import java.util.List;

import arc.mf.client.xml.XmlElement;

public class Thumbnail {

	public static String MIME_TYPE = "application/zip";
	public static String EXTENSION = "zip";

	public static class Image {
		private String _name;
		private String _url;

		public Image(String name) {
			_name = name;
		}

		public String name() {
			return _name;
		}

		public String url() {
			return _url;
		}

		public void setUrl(String url) {
			_url = url;
		}
	}

	private String _assetId;
	private String _mimeType;
	private String _ext;
	private String _url;
	private List<Image> _images;

	public Thumbnail(XmlElement te) {
		_assetId = te.value("@id");
		_mimeType = MIME_TYPE;
		_ext = EXTENSION;
		List<XmlElement> ies = te.elements("image");
		if (ies != null) {
			_images = new ArrayList<Image>(ies.size());
			for (XmlElement ie : ies) {
				_images.add(new Image(ie.value()));
			}
		}
	}

	public String assetId() {

		return _assetId;
	}

	public List<Image> images() {
		return _images;
	}

	public Image image(int index) {
		if (_images != null) {
			if (index >= 0 && index < _images.size()) {
				return _images.get(index);
			}
		}
		return null;
	}

	public boolean hasImages() {
		return _images != null && !_images.isEmpty();
	}

	public String extension() {

		return _ext;
	}

	public String mimeType() {

		return _mimeType;
	}

	public void setUrl(String url) {
		_url = url;
	}

	public String url() {
		return _url;
	}

}
