package daris.client.temp;

import java.io.BufferedReader;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import arc.mf.client.RemoteServer;
import arc.mf.client.ServerClient;
import arc.utils.DateTime;
import daris.client.pssd.CiteableIdUtils;

public class AnalyzeDicomSendLog {
	public static class Entry {
		public Date date;
		public String domain;
		public String user;
		public String callingAET;
		public String calledAET;
		public String assetId;
		public String cid;
	}

	public static class StudySent {
		public Date date;
		public String cid;
		public String to;

		StudySent(Entry e) {
			this.date = e.date;
			this.cid = CiteableIdUtils.getStudyCID(e.cid);
			this.to = e.calledAET;
		}

		@Override
		public boolean equals(Object o) {
			if (o != null && (o instanceof StudySent)) {
				return cid.equals(((StudySent) o).cid) && to.equals(((StudySent) o).to);
			}
			return false;
		}

		@Override
		public int hashCode() {
			return (cid + to).hashCode();
		}
	}

	public static class SubjectSent {
		public Date date;
		public String cid;
		public String to;

		SubjectSent(Entry e) {
			this.date = e.date;
			this.cid = CiteableIdUtils.getSubjectCID(e.cid);
			this.to = e.calledAET;
		}

		@Override
		public boolean equals(Object o) {
			if (o != null && (o instanceof StudySent)) {
				return cid.equals(((SubjectSent) o).cid) && to.equals(((SubjectSent) o).to);
			}
			return false;
		}

		@Override
		public int hashCode() {
			return (cid + to).hashCode();
		}
	}

	public static final String HOST = "daris-1.cloud.unimelb.edu.au";
	public static final int PORT = 443;
	public static final String DOMAIN = "CHANGE_ME";
	public static final String USER = "CHANGE_ME";
	public static final String PASSWORD = "CHANGE_ME";

	public static final String LOG_FILE_PATH = "/tmp/dcmsend.log";

	public static void main(String[] args) throws Throwable {

		Map<String, StudySent> studies = new LinkedHashMap<String, StudySent>();
		Map<String, SubjectSent> subjects = new LinkedHashMap<String, SubjectSent>();
		List<Entry> logEntries = new ArrayList<Entry>();
		RemoteServer rs = new RemoteServer(HOST, PORT, true, true);

		ServerClient.Connection cxn = rs.open();
		try (BufferedReader br = new BufferedReader(new FileReader(LOG_FILE_PATH))) {
			cxn.connect(DOMAIN, USER, PASSWORD);
			String line;
			while ((line = br.readLine()) != null) {
				Entry e = parseEntry(line.trim());
				if (e != null) {
					e.cid = getCid(cxn, e.assetId);
					logEntries.add(e);
					StudySent study = new StudySent(e);
					SubjectSent subject = new SubjectSent(e);
					studies.put(study.cid + ":" + study.to, study);
					subjects.put(subject.cid + ":" + subject.to, subject);
				}
			}
		} finally {
			cxn.close();
		}

		for (SubjectSent subject : subjects.values()) {
			System.out.println(subject.cid + "," + new SimpleDateFormat(DateTime.DATE_FORMAT).format(subject.date) + ","
					+ subject.to + ",");
		}

	}

	static Entry parseEntry(String line) throws Throwable {
		Entry e = new Entry();
		int last = 0;
		int idx = line.indexOf("]:dicom-send:");
		if (idx > 0) {
			String dateTime = line.substring(idx + 13, idx + 37);
			e.date = new SimpleDateFormat(DateTime.DATE_TIME_MS_FORMAT).parse(dateTime);
			last = idx + 37;
		} else {
			throw new Exception("Failed to parse date & time.");
		}
		idx = line.indexOf(":WARN:[", last);
		if (idx > 0) {
			last = line.indexOf(':', idx + 7);
			e.domain = line.substring(idx + 7, last);
			last++;
		} else {
			throw new Exception("Failed to parse domain.");
		}
		idx = line.indexOf("][", last);
		if (idx > 0) {
			e.user = line.substring(last, idx);
			last = idx + 2;
		} else {
			throw new Exception("Failed to parse user.");
		}
		idx = line.indexOf("calling AE:", last);
		if (idx > 0) {
			last = line.indexOf(", ", idx + 11);
			e.callingAET = line.substring(idx + 11, last);
			last += 2;
		} else {
			throw new Exception("Failed to parse calling AE.");
		}
		idx = line.indexOf("called AE:", last);
		if (idx > 0) {
			last = line.indexOf("] ", idx + 10);
			e.calledAET = line.substring(idx + 10, last);
			last += 2;
		} else {
			throw new Exception("Failed to parse called AE.");
		}
		idx = line.indexOf("dataset ", last);
		if (idx > 0) {
			e.assetId = line.substring(idx + 8);
		} else {
			throw new Exception("Failed to parse asset id.");
		}
		return e;
	}

	static String getCid(ServerClient.Connection cxn, String assetId) throws Throwable {
		return cxn.execute("asset.get", "<id>" + assetId + "</id>").value("asset/cid");
	}
}
