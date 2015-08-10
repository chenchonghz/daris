package nig.mf.plugin.pssd.object;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nig.compress.ZipUtil;
import nig.mf.plugin.pssd.Application;
import nig.mf.plugin.pssd.Asset;
import arc.archive.ArchiveInput;
import arc.archive.ArchiveRegistry;
import arc.archive.TableOfContentsEntry;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mime.MimeType;
import arc.mime.NamedMimeType;
import arc.streams.LongInputStream;
import arc.streams.SizedInputStream;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class Thumbnail {

	public static final String RELATIONSHIP = "thumbnail";
	public static final String INVERSE_RELATIONSHIP = "thumbnail-of";
	public static final String ZIP_MIME_TYPE = "application/zip";

	public static class ThumbnailImage {

		private String _name;
		private long _ordinal;
		private long _size;

		public ThumbnailImage(String name, long ordinal, long size) {
			this._name = name;
			this._ordinal = ordinal;
			this._size = size;
		}

		public String name() {
			return _name;
		}

		public long ordinal() {
			return _ordinal;
		}

		public long size() {
			return _size;
		}

	}

	public static MimeType zipMimeType() {
		MimeType zipMimeType = null;
		List<MimeType> types = ArchiveRegistry.supportedMimeTypes();
		if (types != null) {
			for (MimeType type : types) {
				if (type.name().equals(ZIP_MIME_TYPE)) {
					zipMimeType = type;
					break;
				}
			}
		}
		if (zipMimeType == null) {
			zipMimeType = new NamedMimeType(ZIP_MIME_TYPE);
		}
		return zipMimeType;
	}

	public static final String[] IMG_FILE_EXTS = new String[] { "jpg", "jpeg", "png", "gif", "bmp" };

	public static void clear(ServiceExecutor executor, String cid) throws Throwable {
		String assetId = find(executor, cid);
		if (assetId != null) {
			Asset.destroyById(executor, assetId);
		}
	}

	private static String find(ServiceExecutor executor, String cid) throws Throwable {
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("where", "related to{" + INVERSE_RELATIONSHIP + "} (cid='" + cid + "')");
		dm.add("pdist", 0);
		XmlDoc.Element r = executor.execute("asset.query", dm.root());
		return r.value("id");
	}

	public static String set(ServiceExecutor executor, String cid, InputStream is) throws Throwable {
		if (!is.markSupported()) {
			is = new BufferedInputStream(is);
		}
		if (!ZipUtil.isZipStream(is)) {
			throw new Exception("The input file is not a valid zip archive. Only zip archive is supported.");
		}
		String assetId = find(executor, cid);
		if (assetId != null) {
			XmlDocMaker dm = new XmlDocMaker("args");
			dm.add("id", assetId);
			PluginService.Input input = new PluginService.Input(is, -1, ZIP_MIME_TYPE, null);
			executor.execute("asset.set", dm.root(), new PluginService.Inputs(input), null);
		} else {
			XmlDocMaker dm = new XmlDocMaker("args");
			// save to the same namespace as the object.
			dm.add("namespace", Asset.getNamespaceByCid(executor, null, cid));
			dm.push("related");
			dm.add("from", new String[] { "relationship", RELATIONSHIP }, Asset.getIdByCid(executor, cid));
			dm.pop();
			PluginService.Input input = new PluginService.Input(is, -1, ZIP_MIME_TYPE, null);
			XmlDoc.Element r = executor.execute("asset.create", dm.root(), new PluginService.Inputs(input), null);
			return r.value("id");
		}
		return assetId;
	}

	public static void get(ServiceExecutor executor, String cid, PluginService.Outputs outputs, XmlWriter w)
			throws Throwable {
		String assetId = find(executor, cid);
		if (assetId == null) {
			return;
		}
		System.out.println(assetId);
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("id", assetId);
		if (outputs != null) {
			// content output
			executor.execute("asset.get", dm.root(), null, outputs);
		}

		// meta data output
		List<ThumbnailImage> images = getImages(executor, assetId);
		w.push("thumbnail",
				new String[] { "id", assetId, INVERSE_RELATIONSHIP, cid, "number-of-images",
						Integer.toString(images == null ? 0 : images.size()) });
		if (images != null) {
			for (ThumbnailImage image : images) {
				w.add("image", new String[] { "ordinal", Long.toString(image.ordinal()) }, image.name());
			}
		}
		w.pop();
	}

	private static List<ThumbnailImage> getImages(ServiceExecutor executor, String assetId) throws Throwable {

		return getImages(getArchiveInput(executor, assetId));
	}

	private static ArchiveInput getArchiveInput(ServiceExecutor executor, String assetId) throws Throwable {

		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("id", assetId);
		PluginService.Outputs outputs = new PluginService.Outputs(1);
		XmlDoc.Element r = executor.execute("asset.get", dm.root(), null, outputs);
		if (r.element("asset/content") == null || outputs.output(0) == null) {
			throw new Exception("Failed to retrieve the content of asset " + assetId + ".");
		}
		long size = r.longValue("asset/content/size");
		LongInputStream in = new SizedInputStream(outputs.output(0).stream(), size);
		return ArchiveRegistry.createInput(in, zipMimeType(), ArchiveInput.ACCESS_RANDOM);
	}

	private static List<ThumbnailImage> getImages(ArchiveInput ai) throws Throwable {

		List<ThumbnailImage> images = new ArrayList<ThumbnailImage>();
		if (ai != null) {
			try {
				TableOfContentsEntry[] es = ai.tableOfContents();
				if (es != null) {
					List<String> exts = Arrays.asList(IMG_FILE_EXTS);
					for (int i = 0; i < es.length; i++) {
						TableOfContentsEntry e = es[i];
						for (String ext : exts) {
							if (e.name().endsWith("." + ext)) {
								images.add(new ThumbnailImage(e.name(), e.ordinal(), e.size()));
							}
						}
					}
				}
			} finally {
				ai.close();
			}
		}
		if (images.isEmpty()) {
			return null;
		}
		return images;
	}

	public static void getImage(ServiceExecutor executor, String assetId, int index, PluginService.Outputs outputs)
			throws Throwable {

		List<ThumbnailImage> images = getImages(executor, assetId);
		if (images != null) {
			if (index < 0 || index >= images.size()) {
				throw new IndexOutOfBoundsException("Index: " + index + " Size: " + images.size());
			}
			ThumbnailImage image = images.get(index);
			ArchiveInput ai = getArchiveInput(executor, assetId);
			if (ai != null) {
				try {
					ArchiveInput.Entry ae = ai.get(image.ordinal());
					if (ae == null) {
						throw new Exception("Failed to retrieve image " + index + " from the thumbnail asset "
								+ assetId + ".");
					}
					File tf = PluginService.createTemporaryFile();
					arc.streams.StreamCopy.copy(ae.stream(), new FileOutputStream(tf));
					outputs.output(0).setData(PluginService.deleteOnCloseInputStream(tf), ae.size(), null);
				} finally {
					ai.close();
				}
			}
		}
	}

}
