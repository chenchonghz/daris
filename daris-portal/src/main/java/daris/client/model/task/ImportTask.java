package daris.client.model.task;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import arc.mf.client.dti.task.DTITaskCreateHandler;
import arc.mf.client.file.LocalFile;
import arc.mf.client.xml.XmlDoc;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlStringWriter;
import arc.mf.client.xml.XmlWriter;
import arc.mf.model.asset.namespace.NamespaceRef;
import arc.mf.model.asset.task.AssetImportControls;
import arc.mf.model.asset.task.AssetImportTask;

public abstract class ImportTask {

	public static final String VAR_ARGS = "args";
	public static final String VAR_SERVICE = "service";

	public static interface Args {
		void save(XmlWriter w);
	}

	private FileCompilationProfile _fcp;
	private List<LocalFile> _files;
	private Map<String, String> _variables;

	protected ImportTask(FileCompilationProfile fcp) {
		this(fcp, null, null);
	}

	protected ImportTask(FileCompilationProfile fcp, List<LocalFile> files, Map<String, String> variables) {
		_fcp = fcp;
		_files = files;
		_variables = variables;
	}

	protected Args args() {
		String argsStr = variableValue(VAR_ARGS);
		if (argsStr == null) {
			return null;
		}
		XmlElement ae = null;
		try {
			ae = XmlDoc.parse(argsStr);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		if (ae == null) {
			return null;
		}
		Args args = null;
		try {
			args = parseArgs(ae);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return args;
	}

	protected abstract Args parseArgs(XmlElement ae) throws Throwable;

	protected void setArgs(Args args) {
		if (args == null) {
			setVariable(VAR_ARGS, null);
			return;
		}
		XmlStringWriter w = new XmlStringWriter();
		w.push("args");
		args.save(w);
		w.pop();
		setArgs(w.document());
	}

	public void setArgs(String args) {
		setVariable(VAR_ARGS, args);
	}

	public FileCompilationProfile profile() {
		return _fcp;
	}

	public List<LocalFile> files() {
		return _files;
	}

	public void setFiles(List<LocalFile> files) {
		_files = files;
	}

	public void setVariable(String name, String value) {
		if (!_fcp.hasVariable(name)) {
			return;
		}
		if (_variables == null) {
			_variables = new HashMap<String, String>();
		}
		if (value == null) {
			_variables.remove(name);
		} else {
			_variables.put(name, value);
		}
	}

	protected String variableValue(String name) {
		if (_variables != null) {
			return _variables.get(name);
		}
		return null;
	}

	public AssetImportTask execute(DTITaskCreateHandler<AssetImportTask> ch) {
		AssetImportControls aic = new AssetImportControls();
		aic.setProfile(profile().name());
		aic.setVariables(_variables);
		return AssetImportTask.create((NamespaceRef) null, files(), aic, ch);
	}

}
