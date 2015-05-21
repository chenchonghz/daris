package nig.mf.plugin.pssd.services;

import java.util.List;

import arc.archive.ArchiveInput;
import arc.archive.ArchiveRegistry;
import arc.archive.TableOfContentsEntry;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.AssetType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mime.MimeType;
import arc.streams.LongInputStream;
import arc.streams.SizedInputStream;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcArchiveEntryList extends PluginService {

	private Interface _defn;

	public SvcArchiveEntryList() {
		_defn = new Interface();
		_defn.add(new Interface.Element("id", AssetType.DEFAULT, "The id of the asset with an archive as its content.",
				0, 1));
		_defn.add(new Interface.Element("cid", CiteableIdType.DEFAULT,
				"The citeable id of the asset with an archive as its content.", 0, 1));
	}

	@Override
	public Access access() {
		return ACCESS_ACCESS;
	}

	@Override
	public Interface definition() {
		return _defn;
	}

	@Override
	public String description() {
		return "list the entries of the specified asset's content archive.";
	}

	@Override
	public void execute(Element args, Inputs inputs, Outputs outputs, XmlWriter w) throws Throwable {
		String id = args.value("id");
		String cid = args.value("cid");
		if (id == null && cid == null) {
			throw new Exception("id or cid is expected. Found none.");
		}
		if (id != null && cid != null) {
			throw new Exception("id or cid is expected. Found both.");
		}
		ArchiveInput in = getAssetContentAsArchiveInput(executor(), id, cid != null, ArchiveInput.ACCESS_RANDOM);
		if (in != null) {
			try {
				TableOfContentsEntry[] es = in.tableOfContents();
				if (es != null) {
					for (int i = 0; i < es.length; i++) {
						TableOfContentsEntry e = es[i];
						w.add("entry", new String[] { "size", e.size() >= 0 ? Long.toString(e.size()) : null, "idx",
								Long.toString(e.ordinal()) }, e.name());
					}
				}
			} finally {
				in.close();
			}
		}
	}

	@Override
	public String name() {
		return "om.pssd.archive.entry.list";
	}

	public static ArchiveInput getAssetContentAsArchiveInput(ServiceExecutor executor, String id, boolean cid,
			int access) throws Throwable {
		XmlDocMaker dm = new XmlDocMaker("args");
		if (cid) {
			dm.add("cid", id);
		} else {
			dm.add("id", id);
		}
		PluginService.Outputs outputs = new PluginService.Outputs(1);
		XmlDoc.Element r = executor.execute("asset.get", dm.root(), null, outputs);
		if (outputs.output(0) == null) {
			return null;
		}
		if (r.element("asset/content") == null) {
			return null;
		}
		long size = r.longValue("asset/content/size");
		MimeType mimeType = getArchiveMimeType(r.stringValue("asset/content/type"));
		if (size <= 0 || mimeType == null) {
			return null;
		}
		LongInputStream in = new SizedInputStream(outputs.output(0).stream(), size);
		return ArchiveRegistry.createInput(in, mimeType, access);
	}

	public static MimeType getArchiveMimeType(String mimeType) {
		if (mimeType != null) {
			List<MimeType> mimeTypes = ArchiveRegistry.supportedMimeTypes();
			if (mimeTypes != null) {
				for (MimeType type : mimeTypes) {
					if (type.name().equals(mimeType)) {
						return type;
					}
				}
			}
		}
		return null;
	}
}
