package nig.mf.plugin.pssd.services;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.Vector;

import nig.mf.dicom.plugin.util.DICOMPatient;
import nig.mf.plugin.pssd.PSSDObject;
import nig.mf.plugin.util.AssetUtil;
import nig.mf.pssd.CiteableIdUtil;
import nig.mf.pssd.plugin.util.DistributedQuery;
import nig.mf.pssd.plugin.util.DistributedQuery.ResultAssetType;
import nig.util.DateUtil;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.PluginService.Interface.Element;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.BooleanType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.DictionaryEnumType;
import arc.mf.plugin.dtype.EnumType;
import arc.mf.plugin.dtype.IntegerType;
import arc.mf.plugin.dtype.LongType;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcStudyFind extends PluginService {
	private Interface _defn;

	public SvcStudyFind() throws Throwable {

		_defn = new Interface();
		_defn.add(new Interface.Element("type", new DictionaryEnumType("pssd.study.types"), "The type of the study.",
				0, 1));
		_defn.add(new Interface.Element("pid", CiteableIdType.DEFAULT, "Restrict the search to this parent citeable ID.",
				0, 1));
		_defn.add(new Element("age", IntegerType.DEFAULT, "Find Studies younger (created/modified) than this in days (defaults to infinity).", 0, 1));
		_defn.add(new Interface.Element("text", StringType.DEFAULT, "Arbitrary search text for free text query.", 0, 1));
		_defn.add(new Interface.Element("method", CiteableIdType.DEFAULT, "Method utilized.", 0, 1));
		_defn.add(new Interface.Element("idx", LongType.POSITIVE_ONE, "Cursor position. Defaults to 1", 0, 1));
		_defn.add(new Interface.Element("size", IntegerType.POSITIVE_ONE, "Cursor size. Defaults to 100", 0, 1));
		_defn.add(new Interface.Element("action", new EnumType(new String[] { "list", "describe", "email" }),
				"The type of display. Defaults to 'list'. If 'email', you can supply an email address also to send the record to.", 0, 1));
		_defn.add(new Interface.Element(
				"foredit",
				BooleanType.DEFAULT,
				"Indicates whether the object may be edited. If true, then a description of the structure of the data is returned. Defaults to 'false'.",
				0, 1));
		_defn.add(new Interface.Element("asset-type", new EnumType(new String[] { "primary", "replica", "all" }),
				"Specify type of asset to find. Defaults to all.", 0, 1));
		_defn.add(new Interface.Element(
				"pdist",
				IntegerType.DEFAULT,
				"Specifies the peer distance for a distributed query. Defaults to all servers in a federated session.  Set to 0 for local only or infinity for all peers (regardless of whether session is federated or not).",
				0, 1));
		_defn.add(new Element("email", StringType.DEFAULT, "An email address to send the report to if action='email'.", 0, Integer.MAX_VALUE));

	}

	public String name() {

		return "om.pssd.study.find";
	}

	public String description() {

		return "Returns Studies that match the given search criteria. It does a distributed query in a federation.";
	}

	public Interface definition() {

		return _defn;
	}

	public Access access() {

		return ACCESS_ACCESS;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {

		String type = args.value("type");
		String text = args.value("text");
		String method = args.value("method");
		boolean forEdit = args.booleanValue("foredit", false);
		String pdist = args.value("pdist");
		String assetType = args.stringValue("asset-type", "all");
		String pid = args.value("pid");
		String age = args.value("age");
		Collection<String> emails = args.values("email");


		// Set up query.
		StringBuilder query = new StringBuilder();
		query.append("model='om.pssd.study'");


		if (type != null) {
			query.append("xpath(daris:pssd-study/type)='" + type + "'");
		}

		if (method != null) {
			String sq = "xpath(daris:pssd-study/method)='" + method + "'";
			query.append(" and " + sq);
		}
		if (pid!=null) {
			String sq = "(cid starts with '" + pid + "')";
			query.append(" and " + sq);
		}

		if (text != null) {
			String sq = "mtext contains '" + text + "'";
			query.append(" and " + sq);
		}

		if (age!=null) {
			query.append (" and (mtime>='TODAY-" + age + "DAY')");
		}


		// Type of asset to find; primary/replica/both (default)
		DistributedQuery.appendResultAssetTypePredicate(query, ResultAssetType.instantiate(assetType));

		// Set up service call.
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("where", query);
		dm.add("action", "get-meta");
		dm.add("idx", args.longValue("idx", 1));
		dm.add("size", args.intValue("size", 100));

		dm.add("get-related-meta", "true");
		dm.add("related-type", "attachment");
		if (pdist != null)
			dm.add("pdist", pdist);

		// Query is distributed in federation
		XmlDoc.Element r = executor().execute("asset.query", dm.root());
		if (r==null) return;

		String action = args.stringValue("action", "list");

		// String si = ( callerIsAPeer() ) ? serverIdentityAsString() : null;

		if (action.equalsIgnoreCase("list")) {
			list(w, r);
		} else if (action.equalsIgnoreCase("describe")) {
			describe(executor(), w, r, forEdit);
		} else if (action.equals("email")) {

			// Make a map with Subject CID the key and a list of Studies the value
			HashMap<String, Vector<String>> map = new HashMap<String, Vector<String>>();
			Collection<String> ids = r.values("asset/cid");
			String msg = "";
			if (ids!=null) {
				for (String id : ids) {
					String subjectCID = CiteableIdUtil.getSubjectId(id);
					if (map.containsKey(subjectCID)) {
						Vector<String> values = map.get(subjectCID);
						values.add(id);            // Reference
					} else {
						Vector<String> values = new Vector<String>();
						values.add(id);
						map.put(subjectCID, values);
					}
				}

				// Iterate through the subjects and format
				Set<String> keySet = map.keySet();
				if (keySet==null || keySet.size()==0) {
					msg = "No new studies were found";
				} else {
					msg = formatForEmail (executor(), map, keySet, w);
				} 
			} else {
				msg = "No new studies were found";
			}

			// Today's date
			String today = DateUtil.todaysDate(2);
			String msgSubject = today + " : data uploaded in the PSSD data model in the last " + age + " days";

			if (emails!=null) {
				for (String email : emails) {
					XmlDocMaker doc = new XmlDocMaker("args");
					doc.add("subject", msgSubject);
					doc.add ("to", email);
					doc.add("async", true);
					doc.add("body", msg);
					executor().execute("mail.send", doc.root());
				}
			}
		}
	}


	private String formatForEmail (ServiceExecutor executo, HashMap<String, Vector<String>> map, Set<String> keySet, XmlWriter w) throws Throwable {
		// If there is a way to convert XML into aterm' representation, I don't know what it is !
		String msg = "";

		// Iterate over subjects
		for (String key : keySet) {
			XmlDoc.Element meta = AssetUtil.getAsset(executor(), key, null);
			DICOMPatient dp = new DICOMPatient(meta.element("asset/meta/mf-dicom-patient"));
			//
			w.push("subject", new String[]{"name", dp.getFullName(), "id", key});
			msg += ":subject " + dp.getFullName() + "\n";
			// Iterate over studies (there must be some, but they might be empty)
			Vector<String> studies = map.get(key);
			for (String study : studies) {
				XmlDoc.Element sm = AssetUtil.getAsset(executor(),  study,  null);
				w.push("study", new String[]{"id", study});
				msg += "   :study -id " + study + "\n";
				//
				w.add("study-type", sm.value("asset/meta/daris:pssd-study/type"));
				msg += "      :study-type    " + sm.value("asset/meta/daris:pssd-study/type") + "\n";
				//
				XmlDoc.Element dicomMeta = sm.element("asset/meta/mf-dicom-study");
				XmlDoc.Element siemensMeta = sm.element("asset/meta/daris:siemens-raw-petct-study");
				if (dicomMeta!=null) {
					w.add("type", "dicom/study");
					msg += "      :type          dicom/study \n";
					//
					w.add("description", dicomMeta.value("description"));
					msg += "      :description   " + dicomMeta.value("description") + "\n";
					//
					w.add("date", dicomMeta.dateValue("sdate"));
					w.add("ingest", dicomMeta.dateValue("ingest/date"));
					msg += "      :date          " + dicomMeta.value("sdate") + "\n";
					msg += "      :ingest        " + dicomMeta.dateValue("ingest/date") + "\n";
				}  else if (siemensMeta!=null) {
					w.add("type", "raw-siemens-petct/study");
					w.add("date", siemensMeta.dateValue("sdate"));
					w.add("ingest", siemensMeta.dateValue("ingest/date"));
				}			
				//
				String size = studySize (executor(), study);
				if (size==null) {
					msg += "      :size            0";
				} else {				String size2 = formatSize(w, size);
				msg += "      :size          " + size + " bytes \n";
				msg += "      :size          " + size2 + " \n";
				}
				//
				int nDataSets = PSSDObject.countDataSets(executor(), null, study);
				w.add("number-datasets", nDataSets);
				w.pop();
			}
			w.pop();
		}

		return msg;

	}

	private String studySize (ServiceExecutor executor, String pid) throws Throwable {
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("where", "cid starts with '" + pid + "'");
		dm.add("size", "infinity");
		dm.add("pdist", 0);           // local query
		dm.add("action", "sum");
		dm.add("xpath", "content/size");
		XmlDoc.Element r = executor.execute("asset.query", dm.root());
		return r.value("value");    //bytes
	}

	private String formatSize (XmlWriter w, String sizeInBytes) throws Throwable {
		Long s =Long.parseLong(sizeInBytes);
		w.add("size", new String[]{"units", "bytes"}, s);
		if (s > 10000L && s <= 10000000L) {
			s = s / 1000L;
			w.add("size", new String[]{"units", "KBytes"}, s);
			return s + " KBytes";
		} else if (s > 10000000L && s <= 10000000000L) {
			s = s / 1000000L;
			w.add("size", new String[]{"units", "MBytes"}, s);
			return s + " MBytes";
		} else {
			s = s / 1000000000L;
			w.add("size", new String[]{"units", "GBytes"}, s);
			return s + " GBytes";
		}
	}

	public static void list(XmlWriter w, XmlDoc.Element r) throws Throwable {

		Collection<XmlDoc.Element> assets = r.elements("asset");
		if (assets == null)
			return;
		for (XmlDoc.Element el : assets) {
			String cid = el.value("cid");
			String proute = el.value("@proute");
			w.add("id", new String[] { "proute", proute }, cid);
		}
	}

	public static void describe(ServiceExecutor executor, XmlWriter w, XmlDoc.Element r, boolean forEdit)
			throws Throwable {

		SvcObjectFind.addPssdObjects(executor, w, r, false, forEdit);
	}

}
