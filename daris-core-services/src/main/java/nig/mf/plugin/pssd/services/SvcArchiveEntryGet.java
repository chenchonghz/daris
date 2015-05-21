package nig.mf.plugin.pssd.services;

import java.io.File;
import java.io.FileOutputStream;

import arc.archive.ArchiveInput;
import arc.archive.ArchiveRegistry;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.AssetType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.LongType;
import arc.mime.MimeType;
import arc.streams.SizedInputStream;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcArchiveEntryGet extends PluginService {

	private Interface _defn;

	public SvcArchiveEntryGet() {
		_defn = new Interface();
		_defn.add(new Interface.Element("id", AssetType.DEFAULT, "The id of the asset that has the archive as content",
				0, 1));
		_defn.add(new Interface.Element("cid", CiteableIdType.DEFAULT,
				"The citeable id of the asset that has the archive as content", 0, 1));
		_defn.add(new Interface.Element("index", new LongType(0, Long.MAX_VALUE),
				"The ordinal index of the entry with in the archive.", 1, 1));
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
		return "Retrieve a entry (file) from the specified asset with archive as its content.";
	}

	@Override
	public void execute(Element args, Inputs inputs, Outputs outputs, XmlWriter w) throws Throwable {
		String id = args.value("id");
		String cid = args.value("cid");
		long index = args.longValue("index");
		if (id == null && cid == null) {
			throw new Exception("id or cid is expected. Found none.");
		}
		if (id != null && cid != null) {
			throw new Exception("id or cid is expected. Found both.");
		}
		if (outputs == null) {
			throw new Exception("Expect 1 out. Found none.");
		}
		if (outputs.size() != 1) {
			throw new Exception("Expect 1 out. Found " + outputs.size() + ".");
		}

		getArchiveEntry(executor(), id, cid != null, index, outputs.output(0));

	}

	@Override
	public String name() {
		return "om.pssd.archive.entry.get";
	}

	@Override
	public int minNumberOfOutputs() {
		return 1;
	}

	@Override
	public int maxNumberOfOutputs() {
		return 1;
	}

	public static void getArchiveEntry(ServiceExecutor executor, String id, boolean cid, long index,
			PluginService.Output out) throws Throwable {
		XmlDocMaker dm = new XmlDocMaker("args");
		if (cid) {
			dm.add("cid", id);
		} else {
			dm.add("id", id);
		}
		PluginService.Outputs outputs = new PluginService.Outputs(1);
		XmlDoc.Element r = executor.execute("asset.get", dm.root(), null, outputs);
		long size = r.longValue("asset/content/size");
		String type = r.stringValue("asset/content/type");
		if (r.element("asset/content") == null || outputs == null || size < 0) {
			throw new Exception("Failed to retrieve asset content. (id=" + id + ")");
		}
		MimeType mimeType = SvcArchiveEntryList.getArchiveMimeType(type);
		if (mimeType == null) {
			throw new Exception("mime type " + type + " is not supported archive mime type.");
		}
		ArchiveInput in = ArchiveRegistry.createInput(new SizedInputStream(outputs.output(0).stream(), size), mimeType,
				ArchiveInput.ACCESS_RANDOM);
		if (in != null) {
			try {
				ArchiveInput.Entry ae = in.get((int) index);
				if (ae == null) {
					throw new Exception("Failed to retrieve entry " + index + " from asset content.");
				}
				File tf = PluginService.createTemporaryFile();
				arc.streams.StreamCopy.copy(ae.stream(), new FileOutputStream(tf));
				out.setData(PluginService.deleteOnCloseInputStream(tf), ae.size(), null);
			} finally {
				in.close();
			}
		}
	}
}
