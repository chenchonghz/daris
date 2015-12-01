package daris.client.model.dicom.task;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import arc.file.matching.Profile;
import arc.xml.XmlStringWriter;
import daris.client.model.object.DObjectRef;
import daris.client.model.task.UploadFCP;
import daris.client.model.task.UploadTask;

public class DicomIngestTask extends UploadTask {

    public static final String ENGINE_NIG_DICOM = "nig.dicom";
    public static final String NIG_DICOM_ID_CITABLE = "nig.dicom.id.citable";

    private Boolean _anonymize = false;
    private String _anonymizeElements = null;
    private Map<String, String> _args = null;
    private String _engine = null;
    private String _service = null;
    private String _type = null;
    private boolean _wait = true;

    protected DicomIngestTask(String parentCid, List<File> files) {
        super("dicom.ingest", files);
        _engine = ENGINE_NIG_DICOM;
        _args = new LinkedHashMap<String, String>();
        _args.put(NIG_DICOM_ID_CITABLE, parentCid);
    }

    public DicomIngestTask(DObjectRef parent, File dir) {
        this(parent.citeableId(), Arrays.asList(dir));
    }

    @Override
    protected final Profile profile() throws Throwable {
        return UploadFCP.getDicomIngestFCP();
    }

    protected void setArg(String name, String value) {
        _args.put(name, value);
    }

    protected void setService(String service) {
        _service = service;
    }

    protected void setType(String type) {
        _type = type;
    }

    protected void setAnonymize(Boolean anonymize) {
        _anonymize = anonymize;
    }

    protected void setAnonymizeElements(String anonymizeElements) {
        _anonymizeElements = anonymizeElements;
    }

    protected void setWait(boolean wait) {
        _wait = wait;
    }

    @Override
    protected final void doExecute() throws Throwable {
        XmlStringWriter w = new XmlStringWriter();
        w.push("args");
        if (_anonymize != null) {
            w.add("anonymize", _anonymize);
        }
        if (_anonymizeElements != null) {
            w.add("anonymize-elements", _anonymizeElements);
        }
        if (_args != null) {
            for (String name : _args.keySet()) {
                w.add("arg", new String[] { "name", name }, _args.get(name));
            }
        }
        w.add("engine", _engine);
        if (_service != null) {
            w.add("service", _service);
        }
        if (_type != null) {
            w.add("type", _type);
        }
        w.add("wait", _wait);
        w.pop();
        super.doExecute();
    }

}
