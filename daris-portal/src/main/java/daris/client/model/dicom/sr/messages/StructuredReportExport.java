package daris.client.model.dicom.sr.messages;

import java.util.List;

import arc.mf.client.Output;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.Null;
import arc.mf.object.ObjectMessage;
import daris.client.model.dicom.sr.StructuredReportRef;

public class StructuredReportExport extends ObjectMessage<Null> {

	public static enum Format {
		html, csv, xml
	}

	public static final String SERVICE_NAME = "daris.dicom.sr.export";

	private String _cid;
	private Format _format;
	private String _outputFileName;

	public StructuredReportExport(String cid) {
		_cid = cid;
		_format = Format.html;
		_outputFileName = "structured_report-" + cid;
	}

	public StructuredReportExport(StructuredReportRef sr) {
		this(sr.cid());
	}

	public Format format() {
		return _format;
	}

	public void setFormat(Format format) {
		_format = format;
	}

	public String outputFileName() {
		return _outputFileName;
	}

	public void setOutputFileName(String outputFileName) {
		_outputFileName = outputFileName;
	}

	@Override
	protected void messageServiceArgs(XmlWriter w) {
		w.add("cid", _cid);
		w.add("format", _format.name());
	}

	@Override
	protected String messageServiceName() {
		return SERVICE_NAME;
	}

	@Override
	protected Null instantiate(XmlElement xe) throws Throwable {
		return new Null();
	}

	@Override
	protected String objectTypeName() {
		return "DICOM Structured Report";
	}

	@Override
	protected String idToString() {
		return _cid;
	}

	@Override
	protected void process(Null o, List<Output> outputs) {
		String filename = _outputFileName;
		if (!_outputFileName.endsWith("." + _format.name()) && !_outputFileName.endsWith("." + _format.name().toUpperCase())) {
			filename = _outputFileName + "." + _format.name();
		}
		outputs.get(0).download(filename);
	}

	@Override
	protected int numberOfOutputs() {
		return 1;
	}

}
